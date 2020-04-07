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

/**
 * MicroProfile Java Project labels
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaProjectLabelsParams {

	private String uri;

	private List<String> types;

	/**
	 * Returns the Java file uri.
	 * 
	 * @return the Java file uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the Java file uri.
	 * 
	 * @param uri the Java file uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}
}
