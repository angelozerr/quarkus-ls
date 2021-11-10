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

import java.util.ArrayList;
import java.util.List;

/**
 * Java method information.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaMethodInfo extends JavaMemberInfo {

	private static final String NO_VALUE = "~";

	private String signature;

	private String returnType;

	private String getterName;

	private List<JavaMethodParameterInfo> parameters;

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getReturnType() {
		if (returnType == null) {
			String signature = getSignature();
			int index = signature.lastIndexOf(':');
			returnType = index != -1 ? signature.substring(index + 1, signature.length()).trim() : NO_VALUE;
		}
		return NO_VALUE.equals(returnType) ? null : returnType;
	}

	@Override
	public JavaMemberKind getKind() {
		return JavaMemberKind.METHOD;
	}

	@Override
	public String getMemberType() {
		return getReturnType();
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) {
			return name;
		}
		String signature = getSignature();
		int index = signature != null ? signature.indexOf('(') : -1;
		if (index != -1) {
			super.setName(signature.substring(0, index));
		}
		return super.getName();
	}

	public String getGetterName() {
		if (getterName == null) {
			getterName = computeGetterName();
		}
		return NO_VALUE.equals(getterName) ? null : getterName;
	}

	private String computeGetterName() {
		if (hasParameters()) {
			return NO_VALUE;
		}
		String methodName = getName();
		int index = -1;
		if (methodName.startsWith("get")) {
			index = 3;
		} else if (methodName.startsWith("is")) {
			index = 2;
		}
		if (index == -1) {
			return NO_VALUE;
		}
		return (methodName.charAt(index) + "").toLowerCase() + methodName.substring(index + 1, methodName.length());
	}

	public boolean hasParameters() {
		String signature = getSignature();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		return end - start > 1;
	}

	public JavaMethodParameterInfo getParameterAt(int index) {
		List<JavaMethodParameterInfo> parameters = getParameters();
		return parameters.size() > index ? parameters.get(index) : null;
	}

	public List<JavaMethodParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = parseParameters();
		}
		return parameters;
	}

	private List<JavaMethodParameterInfo> parseParameters() {
		List<JavaMethodParameterInfo> parameters = new ArrayList<>();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		String content = signature.substring(start + 1, end);
		// query : java.lang.String, params :
		// java.util.Map<java.lang.String,java.lang.Object>
		boolean paramTypeParsing = false;
		StringBuilder paramName = new StringBuilder();
		StringBuilder paramType = new StringBuilder();
		int daemon = 0;
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (!paramTypeParsing) {
				// ex query :
				switch (c) {
				case ' ':
					// ignore space
					break;
				case ':':
					paramTypeParsing = true;
					break;
				default:
					paramName.append(c);
				}
			} else {
				// ex java.lang.String,
				switch (c) {
				case ' ':
					// ignore space
					break;
				case '<':
					daemon++;
					paramType.append(c);
					break;
				case '>':
					daemon--;
					paramType.append(c);
					break;
				case ',':
					if (daemon == 0) {
						parameters.add(new JavaMethodParameterInfo(paramName.toString(), paramType.toString()));
						paramName.setLength(0);
						paramType.setLength(0);
						paramTypeParsing = false;
						daemon = 0;
					} else {
						paramType.append(c);
					}
					break;
				default:
					paramType.append(c);
				}
			}
		}
		if (paramName.length() > 0) {
			parameters.add(new JavaMethodParameterInfo(paramName.toString(), paramType.toString()));
		}
		return parameters;
	}

}
