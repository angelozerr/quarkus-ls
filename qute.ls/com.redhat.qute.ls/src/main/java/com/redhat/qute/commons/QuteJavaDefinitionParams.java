/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

/**
 * Qute Java definition parameters.
 *
 * @author Angelo ZERR
 *
 */
public class QuteJavaDefinitionParams {

	private String projectUri;

	private String sourceType;

	private String sourceField;

	private String sourceMethod;

	private String sourceParameter;

	private boolean dataMethodInvocation;

	public QuteJavaDefinitionParams() {

	}

	public QuteJavaDefinitionParams(String sourceType, String projectUri) {
		setSourceType(sourceType);
		setProjectUri(projectUri);
	}

	/**
	 * Returns the Qute project Uri.
	 * 
	 * @return the Qute project Uri.
	 */
	public String getProjectUri() {
		return projectUri;
	}

	/**
	 * Set the Qute project Uri.
	 * 
	 * @param projectUri the Qute project Uri.
	 */
	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	/**
	 * Returns the Java source type.
	 * 
	 * @return the Java source type.
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Set the Java source type.
	 * 
	 * @param sourceType the Java source type.
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns the Java source field and null otherwise.
	 * 
	 * @return the Java source field and null otherwise.
	 */
	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	/**
	 * Returns the Java source method and null otherwise.
	 * 
	 * @return the Java source method and null otherwise.
	 */
	public String getSourceMethod() {
		return sourceMethod;
	}

	/**
	 * Set the Java source method
	 * 
	 * @param sourceMethod the Java source method
	 */
	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	/**
	 * Returns the Java source parameter and null otherwise:
	 * 
	 * <ul>
	 * <li>method parameter when {@link #isDataMethodInvocation()} return
	 * false.</li>
	 * <li>"data" method invocation parameter when {@link #isDataMethodInvocation()}
	 * return true.</li>
	 * </ul>
	 * 
	 * @return the Java source parameter and null otherwise.
	 */
	public String getSourceParameter() {
		return sourceParameter;
	}

	/**
	 * Set the Java source parameter:
	 * 
	 * <ul>
	 * <li>method parameter when {@link #isDataMethodInvocation()} return
	 * false.</li>
	 * <li>"data" method invocation parameter when {@link #isDataMethodInvocation()}
	 * return true.</li>
	 * </ul>
	 * 
	 * @param sourceParameter the Java source method parameter
	 */
	public void setSourceParameter(String sourceParameter) {
		this.sourceParameter = sourceParameter;
	}

	/**
	 * Returns true if {@link #getSourceParameter()} is a data" method invocation
	 * parameter and method parameter otherwise.
	 * 
	 * @return true if {@link #getSourceParameter()} is a data" method invocation
	 *         parameter and method parameter otherwise.
	 */
	public boolean isDataMethodInvocation() {
		return dataMethodInvocation;
	}

	/**
	 * Set true if {@link #getSourceParameter()} is a data" method invocation
	 * parameter and method parameter otherwise.
	 * 
	 * @param dataMethodInvocation
	 */
	public void setDataMethodInvocation(boolean dataMethodInvocation) {
		this.dataMethodInvocation = dataMethodInvocation;
	}
}
