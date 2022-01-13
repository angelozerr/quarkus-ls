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
package com.redhat.qute.settings;

import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * Qute validation type settings.
 *
 */
public class QuteValidationTypeSettings {

	private String severity;

	private List<String> excluded;

	/**
	 * Returns the severity of the validation type.
	 *
	 * @return the severity of the validation type.
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * Set the severity of the validation type.
	 *
	 * @param severity the severity of the validation type.
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * Returns the array of properties to ignore for this validation type.
	 *
	 * @return the array of properties to ignore for this validation type.
	 */
	public List<String> getExcluded() {
		return excluded;
	}

	/**
	 * Set the array of properties to ignore for this validation type.
	 *
	 * @param excluded the array of properties to ignore for this validation type.
	 */
	public void setExcluded(List<String> excluded) {
		this.excluded = excluded;
	}

	/**
	 * Returns the diagnostic severity according the given property name and null
	 * otherwise.
	 *
	 * @param propertyName the property name.
	 * @return the diagnostic severity according the given property name and null
	 *         otherwise.
	 */
	public DiagnosticSeverity getDiagnosticSeverity(String propertyName) {
		DiagnosticSeverity severity = getDiagnosticSeverity();
		if (severity == null) {
			return null;
		}
		return isExcluded(propertyName) ? null : severity;
	}

	public DiagnosticSeverity getDiagnosticSeverity() {
		DiagnosticSeverity[] severities = DiagnosticSeverity.values();
		for (DiagnosticSeverity severity : severities) {
			if (severity.name().toUpperCase().equals(this.severity.toUpperCase())) {
				return severity;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given property name must be excluded and false otherwise.
	 *
	 * @param propertyName the property name
	 * @return true if the given property name must be excluded and false otherwise.
	 */
	private boolean isExcluded(String propertyName) {
		if (excluded == null) {
			return false;
		}
		// Get compiled excluded properties
		List<String> excludedProperties = getExcluded();
		for (String excluded : excludedProperties) {
			// the property name matches an excluded pattern
			if (excluded.equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

}
