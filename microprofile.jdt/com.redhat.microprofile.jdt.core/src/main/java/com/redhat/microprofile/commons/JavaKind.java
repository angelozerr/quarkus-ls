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

/**
 * The java element kind.
 * 
 * @author Angelo ZERR
 *
 */
public enum JavaKind {

	TYPE(1), METHOD(2), ANNOTATION(3);

	private final int value;

	JavaKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JavaKind forValue(int value) {
		JavaKind[] allValues = JavaKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

	public static JavaKind getScope(String scope) {
		if (scope != null) {
			scope = scope.toUpperCase();
			try {
				return JavaKind.valueOf(scope);
			} catch (Exception e) {
				// Do nothing
			}
		}
		return TYPE;
	}
}
