/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template.datamodel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;

/**
 * Data model provider API.
 *
 * @author Angelo ZERR
 *
 */
public interface IDataModelProvider {

	/**
	 * Set the namespace resolver information and null otherwise.
	 * 
	 * @param namespaceResolverInfo the namespace resolver information and null
	 *                              otherwise.
	 */
	void setNamespaceResolverInfo(NamespaceResolverInfo namespaceResolverInfo);

	/**
	 * Begin the search.
	 *
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void beginSearch(SearchContext context, IProgressMonitor monitor) {

	}

	/**
	 * Create the Java search pattern.
	 *
	 * @return the Java search pattern.
	 */
	SearchPattern createSearchPattern();

	/**
	 * Collect data model from the given Java search match.
	 *
	 * @param match   the java search match.
	 * @param context the search context.
	 * @param monitor the progress monitor.
	 */
	void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor);

	/**
	 * End the search.
	 *
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void endSearch(SearchContext context, IProgressMonitor monitor) {

	}

}
