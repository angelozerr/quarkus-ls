/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

/**
 * Quarkus shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {
	private final QuteValidationSettings validationSettings;

	public SharedSettings() {
		this.validationSettings = new QuteValidationSettings();
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public QuteValidationSettings getValidationSettings() {
		return validationSettings;
	}

}
