package com.redhat.microprofile.jdt.core;

import static com.redhat.quarkus.jdt.internal.core.utils.AnnotationUtils.isMatchAnnotation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

public abstract class AbstractPropertiesProvider implements IPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractPropertiesProvider.class.getName());

	protected abstract String[] getAnnotationNames();

	public SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		String[] names = getAnnotationNames();
		for (String name : names) {
			if (leftPattern == null) {
				leftPattern = createAnnotationSearchPattern(name);
			} else {
				SearchPattern rightPattern = createAnnotationSearchPattern(name);
				if (rightPattern != null) {
					leftPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
				}
			}
		}
		return leftPattern;
	}

	private static SearchPattern createAnnotationSearchPattern(String annotationName) {
		return SearchPattern.createPattern(annotationName, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IAnnotatable && element instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) element;
			processAnnotation(javaElement, context, collector, monitor);
		}
	}

	protected void processAnnotation(IJavaElement javaElement, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor) {
		try {
			String[] names = getAnnotationNames();
			IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
			for (IAnnotation annotation : annotations) {
				for (String annotationName : names) {
					if (isMatchAnnotation(annotation, annotationName)) {
						processAnnotation(javaElement, annotation, annotationName, context, collector, monitor);
					}
				}
			}
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Cannot compute MicroProfile properties for the Java element '"
						+ javaElement.getElementName() + "'.", e);
			}
		}
	}

	protected abstract void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException;

}
