/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel.resolvers;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.commons.JavaElementKind;

/**
 * Value resolver information.
 * 
 * @author Angelo ZERR
 *
 */
public class ValueResolverInfo {

	private String named;

	private String namespace;

	private String signature;

	private String sourceType;

	/**
	 * Returns the named of the resolver.
	 * 
	 * @return the named of the resolver.
	 */
	public String getNamed() {
		return named;
	}

	/**
	 * Set the named of the resolver.
	 * 
	 * @param named the named of the resolver.
	 */
	public void setNamed(String named) {
		this.named = named;
	}

	/**
	 * Returns the namespace of the resolver and null otherwise.
	 *
	 * @return the namespace of the resolver and null otherwise.
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Set the namespace of the resolver.
	 * 
	 * @param namespace the namespace of the resolver.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Returns the Java element signature.
	 *
	 * @return the Java element signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Set the Java element signature.
	 * 
	 * @param signature the Java element signature.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Returns the java source type and null otherwise.
	 * 
	 * @return the java source type and null otherwise.
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Set the java source type.
	 * 
	 * @param sourceType the java source type
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns the Java element kind (type, method, field).
	 *
	 * @return the Java element kind (type, method, field).
	 */
	public JavaElementKind getJavaElementKind() {
		if (signature.contains("(")) {
			return JavaElementKind.METHOD;
		}
		return JavaElementKind.FIELD;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((named == null) ? 0 : named.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueResolverInfo other = (ValueResolverInfo) obj;
		if (named == null) {
			if (other.named != null)
				return false;
		} else if (!named.equals(other.named))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		if (sourceType == null) {
			if (other.sourceType != null)
				return false;
		} else if (!sourceType.equals(other.sourceType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("named", this.named);
		b.add("namespace", this.namespace);
		b.add("signature", signature);
		b.add("sourceType", sourceType);
		return b.toString();
	}
}
