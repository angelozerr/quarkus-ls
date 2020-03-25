/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.List;

import org.eclipse.lsp4j.Position;

/**
 * MicroProfile Java completion parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaCompletionParams {

	private String uri;
	private Position position;
	private List<JavaSnippetContext> contexts;

	public MicroProfileJavaCompletionParams() {

	}

	public MicroProfileJavaCompletionParams(String uri, Position position) {
		this();
		setUri(uri);
		setPosition(position);
	}

	/**
	 * Returns the java file uri.
	 * 
	 * @return the java file uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the java file uri.
	 * 
	 * @param uri the java file uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the completion position
	 * 
	 * @return the completion position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Sets the completion position
	 * 
	 * @param position the completion position
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	public List<JavaSnippetContext> getContexts() {
		return contexts;
	}

	public void setContexts(List<JavaSnippetContext> contexts) {
		this.contexts = contexts;
	}
}
