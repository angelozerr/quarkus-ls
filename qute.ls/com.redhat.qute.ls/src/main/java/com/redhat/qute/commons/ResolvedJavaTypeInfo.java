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

import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Resolved Java type information.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvedJavaTypeInfo extends JavaTypeInfo {

	private static final String ITERABLE_TYPE = "Iterable";

	private static final String JAVA_LANG_ITERABLE_TYPE = "java.lang.Iterable";

	private List<String> extendedTypes;

	private List<JavaFieldInfo> fields;

	private List<JavaMethodInfo> methods;

	private String iterableType;

	private String iterableOf;

	private Boolean isIterable;

	/**
	 * Returns list of extended types.
	 * 
	 * @return list of extended types.
	 */
	public List<String> getExtendedTypes() {
		return extendedTypes;
	}

	/**
	 * Set list of extended types.
	 * 
	 * @param extendedTypes list of extended types.
	 */
	public void setExtendedTypes(List<String> extendedTypes) {
		this.extendedTypes = extendedTypes;
	}

	/**
	 * Returns member fields.
	 * 
	 * @return member fields.
	 */
	public List<JavaFieldInfo> getFields() {
		return fields != null ? fields : Collections.emptyList();
	}

	/**
	 * Set member fields.
	 * 
	 * @param fields member fields.
	 */
	public void setFields(List<JavaFieldInfo> fields) {
		this.fields = fields;
	}

	/**
	 * Return member methods.
	 * 
	 * @return member methods.
	 */
	public List<JavaMethodInfo> getMethods() {
		return methods != null ? methods : Collections.emptyList();
	}

	/**
	 * Set member methods.
	 * 
	 * @param methods member methods.
	 */
	public void setMethods(List<JavaMethodInfo> methods) {
		this.methods = methods;
	}

	/**
	 * Returns iterable type and null otherwise.
	 * 
	 * @return iterable type and null otherwise.
	 */
	public String getIterableType() {
		return iterableType;
	}

	/**
	 * Set iterable type.
	 * 
	 * @param iterableType iterable type.
	 */
	public void setIterableType(String iterableType) {
		this.iterableType = iterableType;
	}

	/**
	 * Returns iterable of and null otherwise.
	 * 
	 * @return iterable of and null otherwise.
	 */
	public void setIterableOf(String iterableOf) {
		this.iterableOf = iterableOf;
	}

	/**
	 * Returns iterable of.
	 * 
	 * @return iterable of.
	 */
	public String getIterableOf() {
		return iterableOf;
	}

	/**
	 * Returns true if the Java type is iterable (ex :
	 * java.util.List<org.acme.item>) and false otherwise.
	 * 
	 * @return true if the Java type is iterable and false otherwise.
	 */
	public boolean isIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		isIterable = computeIsIterable();
		return isIterable.booleanValue();
	}

	private synchronized boolean computeIsIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		if (iterableOf != null) {
			return true;
		}
		boolean iterable = getName().equals(JAVA_LANG_ITERABLE_TYPE);
		if (!iterable && extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				if (ITERABLE_TYPE.equals(extendedType) || extendedType.equals(JAVA_LANG_ITERABLE_TYPE)) {
					iterable = true;
					break;
				}
			}
		}

		if (iterable) {
			this.iterableOf = "java.lang.Object";
			this.iterableType = getName();
		}
		return iterable;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("signature", this.getSignature());
		b.add("iterableOf", this.getIterableOf());
		b.add("iterableType", this.getIterableType());
		return b.toString();
	}

}
