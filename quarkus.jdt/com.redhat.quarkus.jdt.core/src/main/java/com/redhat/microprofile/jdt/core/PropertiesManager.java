package com.redhat.microprofile.jdt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.jdt.internal.core.FakeJavaProject;
import com.redhat.microprofile.jdt.internal.core.PropertiesCollector;
import com.redhat.microprofile.jdt.internal.core.PropertiesProviderRegistry;
import com.redhat.quarkus.commons.ClasspathKind;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.QuarkusDeploymentJavaProject;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

public class PropertiesManager {

	private static final PropertiesManager INSTANCE = new PropertiesManager();

	private static final Logger LOGGER = Logger.getLogger(PropertiesManager.class.getName());

	public static PropertiesManager getInstance() {
		return INSTANCE;
	}

	public PropertiesManager() {

	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IFile file, QuarkusPropertiesScope[] scopes,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		ClasspathKind classpathKind = JDTQuarkusUtils.getClasspathKind(file, javaProject);
		return getMicroProfileProjectInfo(javaProject, scopes, classpathKind, progress);
	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IJavaProject javaProject, QuarkusPropertiesScope[] scopes,
			ClasspathKind classpathKind, IProgressMonitor monitor) throws JavaModelException, CoreException {
		MicroProfileProjectInfo info = createInfo(javaProject, classpathKind);
		if (classpathKind == ClasspathKind.NONE) {
			info.setProperties(Collections.emptyList());
			return info;
		}
		long startTime = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Start computing MicroProfile properties for '" + info.getProjectURI() + "' project.");
		}
		PropertiesCollector collector = new PropertiesCollector();
		SearchContext context = new SearchContext();
		try {
			beginSearch(context);
			// Create pattern
			SearchPattern pattern = createSearchPattern();
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = createSearchScope(javaProject, scopes, classpathKind == ClasspathKind.SRC);
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							collectProperties(match, context, collector, monitor);
						}
					}, monitor);
			info.setProperties(collector.getReferenceProperties());
		} finally {
			endSearch(context);
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("End computing MicroProfile properties for '" + info.getProjectURI() + "' project in "
						+ (System.currentTimeMillis() - startTime) + "ms.");
			}
		}
		return info;
	}

	private void beginSearch(SearchContext context) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.begin(context);
		}
	}

	private void endSearch(SearchContext context) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.end(context);
		}
	}

	private void collectProperties(SearchMatch match, SearchContext context, PropertiesCollector collector,
			IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.collectProperties(match, context, collector, monitor);
		}
	}

	private static MicroProfileProjectInfo createInfo(IJavaProject javaProject, ClasspathKind classpathKind) {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		info.setProjectURI(JDTQuarkusUtils.getProjectURI(javaProject));
		info.setClasspathKind(classpathKind);
		return info;
	}

	private IJavaSearchScope createSearchScope(IJavaProject project, QuarkusPropertiesScope[] scopes,
			boolean excludeTestCode) throws JavaModelException {
		int scope = scopes[0] == QuarkusPropertiesScope.sources ? IJavaSearchScope.SOURCES
				: IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES;
		List<IClasspathEntry> newClasspathEntries = new ArrayList<>();
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.contributeToClasspath(project, excludeTestCode,
					QuarkusDeploymentJavaProject.DEFAULT_ARTIFACT_RESOLVER, newClasspathEntries);
		}
		if (!newClasspathEntries.isEmpty()) {
			FakeJavaProject fakeProject = new FakeJavaProject(project,
					newClasspathEntries.toArray(new IClasspathEntry[newClasspathEntries.size()]));
			return createJavaSearchScope(fakeProject, excludeTestCode, fakeProject.getElementsToSearch(scopes[0]),
					scope);
		}
		return BasicSearchEngine.createJavaSearchScope(excludeTestCode, new IJavaElement[] { project });
	}

	private SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			if (leftPattern == null) {
				leftPattern = provider.createSearchPattern();
			} else {
				SearchPattern rightPattern = provider.createSearchPattern();
				if (rightPattern != null) {
					leftPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
				}
			}
		}
		return leftPattern;
	}

	/**
	 * This code is the same than
	 * {@link BasicSearchEngine#createJavaSearchScope(boolean, IJavaElement[], boolean)}.
	 * It overrides {@link JavaSearchScope#packageFragmentRoot(String, int, String)}
	 * to search the first the package root (JAR) from the given fake project.
	 * 
	 * @param fakeProject
	 * @param excludeTestCode
	 * @param elements
	 * @param includeMask
	 * @return
	 */
	private static IJavaSearchScope createJavaSearchScope(IJavaProject fakeProject, boolean excludeTestCode,
			IJavaElement[] elements, int includeMask) {
		HashSet projectsToBeAdded = new HashSet(2);
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaElement element = elements[i];
			if (element instanceof JavaProject) {
				projectsToBeAdded.add(element);
			}
		}
		JavaSearchScope scope = new JavaSearchScope(excludeTestCode) {

			@Override
			public IPackageFragmentRoot packageFragmentRoot(String resourcePathString, int jarSeparatorIndex,
					String jarPath) {
				// Search at first in the fake project the package root to avoid creating a non
				// existing IProject (because fake project doesn't exists)
				try {
					IPackageFragmentRoot[] roots = fakeProject.getPackageFragmentRoots();
					for (IPackageFragmentRoot root : roots) {
						if (resourcePathString.startsWith(root.getPath().toOSString())) {
							return root;
						}
					}
				} catch (JavaModelException e) {
					// ignore
				}
				// Not found...
				return super.packageFragmentRoot(resourcePathString, jarSeparatorIndex, jarPath);
			}
		};
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaElement element = elements[i];
			if (element != null) {
				try {
					if (projectsToBeAdded.contains(element)) {
						scope.add((JavaProject) element, includeMask, projectsToBeAdded);
					} else {
						scope.add(element);
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}
		return scope;
	}

	List<IPropertiesProvider> getPropertiesProviders() {
		return PropertiesProviderRegistry.getInstance().getPropertiesProviders();
	}

}
