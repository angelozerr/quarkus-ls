/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Abstract class for properties provider based on annotation search.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractAnnotationTypeReferencePropertiesProvider extends AbstractPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractAnnotationTypeReferencePropertiesProvider.class.getName());

	@Override
	protected String[] getPatterns() {
		return getAnnotationNames();
	}

	/**
	 * Returns the annotation names to search.
	 * 
	 * @return the annotation names to search.
	 */
	protected abstract String[] getAnnotationNames();

	@Override
	protected SearchPattern createSearchPattern(String annotationName) {
		return createAnnotationTypeReferenceSearchPattern(annotationName);
	}

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IAnnotatable && element instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) element;
			processAnnotation(javaElement, context, monitor);
		}
	}

	protected void processAnnotation(IJavaElement javaElement, SearchContext context, IProgressMonitor monitor) {
		try {
			String[] names = getAnnotationNames();
			IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
			for (IAnnotation annotation : annotations) {
				for (String annotationName : names) {
					if (isMatchAnnotation(annotation, annotationName)) {
						processAnnotation(javaElement, annotation, annotationName, context, monitor);
						break;
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
			SearchContext context, IProgressMonitor monitor) throws JavaModelException;

}
