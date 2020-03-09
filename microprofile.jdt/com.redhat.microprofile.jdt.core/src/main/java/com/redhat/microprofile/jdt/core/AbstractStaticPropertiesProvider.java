package com.redhat.microprofile.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

public abstract class AbstractStaticPropertiesProvider extends AbstractPropertiesProvider {

	@Override
	public final void beginSearch(SearchContext context, IProgressMonitor monitor) {
		if (isAdaptedFor(context, monitor)) {
			collectStaticProperties(context, monitor);
		}
	}

	protected abstract boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor);

	protected abstract void collectStaticProperties(SearchContext context, IProgressMonitor monitor);

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	protected String[] getPatterns() {
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		return null;
	}

}
