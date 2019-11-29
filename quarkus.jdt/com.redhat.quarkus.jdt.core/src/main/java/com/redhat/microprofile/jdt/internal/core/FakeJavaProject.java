package com.redhat.microprofile.jdt.internal.core;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalJavaProject;

import com.redhat.quarkus.commons.QuarkusPropertiesScope;

public class FakeJavaProject extends ExternalJavaProject {

	private final IJavaProject rootProject;

	public FakeJavaProject(IJavaProject rootProject, IClasspathEntry[] entries) throws JavaModelException {
		super(entries);
		this.rootProject = rootProject;
	}
	
	/**
	 * Returns the java elements to search according the scope:
	 * 
	 * <ul>
	 * <li>sources scope: only Quarkus Java project</li>
	 * <li>classpath scope:
	 * <ul>
	 * <li>the Quarkus project</li>
	 * <li>all deployment JARs</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param propertiesScope
	 * 
	 * @return the java elements to search
	 * @throws JavaModelException
	 */
	public IJavaElement[] getElementsToSearch(QuarkusPropertiesScope propertiesScope) throws JavaModelException {
		if (propertiesScope == QuarkusPropertiesScope.sources) {
			return new IJavaElement[] { rootProject };
		}
		IPackageFragmentRoot[] roots = super.getPackageFragmentRoots();
		IJavaElement[] elements = new IJavaElement[1 + roots.length];
		elements[0] = rootProject;
		for (int i = 0; i < roots.length; i++) {
			elements[i + 1] = roots[i];
		}
		return elements;
	}

}
