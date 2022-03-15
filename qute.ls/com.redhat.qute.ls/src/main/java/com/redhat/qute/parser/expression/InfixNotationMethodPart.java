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
package com.redhat.qute.parser.expression;

/**
 * Method part in Infix notation context.
 * 
 * <code>
 * 
 *  {name methodName 'param'} 
 * 
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#virtual_methods
 */
public class InfixNotationMethodPart extends MethodPart {

	public InfixNotationMethodPart(int start, int end) {
		super(start, end);
	}

	@Override
	public boolean hasOpenBracket() {
		return true;
	}

	@Override
	public boolean hasCloseBracket() {
		return true;
	}

	@Override
	public int getStartParametersOffset() {
		// {name or |param}
		return super.getEndName() + 1;
	}

	@Override
	public int getEndParametersOffset() {
		// {name or param|}
		return super.getEnd();
	}

	@Override
	public boolean isInfixNotation() {
		return true;
	}
}
