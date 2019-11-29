package com.redhat.microprofile.jdt.core;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.quarkus.jdt.internal.core.QuarkusDeploymentJavaProject.ArtifactResolver;

public interface IPropertiesProvider {

	default void begin(SearchContext context) {

	}

	default void end(SearchContext context) {

	}

	SearchPattern createSearchPattern();

	default void contributeToClasspath(IJavaProject project, boolean excludeTestCode, ArtifactResolver artifactResolver,
			List<IClasspathEntry> newClasspathEntries) throws JavaModelException {

	}

	void collectProperties(SearchMatch match, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor);

}
