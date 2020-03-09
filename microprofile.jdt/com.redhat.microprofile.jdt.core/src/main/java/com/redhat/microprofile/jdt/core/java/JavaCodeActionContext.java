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
package com.redhat.microprofile.jdt.core.java;

import org.eclipse.jdt.core.ITypeRoot;

import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Java codeAction context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaCodeActionContext extends AbtractJavaContext {

	private final MicroProfileJavaCodeActionParams params;

	public JavaCodeActionContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, MicroProfileJavaCodeActionParams params) {
		super(uri, typeRoot, utils);
		this.params = params;
	}

	public MicroProfileJavaCodeActionParams getParams() {
		return params;
	}

}
