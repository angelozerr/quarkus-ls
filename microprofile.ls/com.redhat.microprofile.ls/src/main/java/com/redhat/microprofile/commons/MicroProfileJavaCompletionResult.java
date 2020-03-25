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
 * MicroProfile Java completion result.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaCompletionResult {

	private List<Boolean> resolvedContexts;

	public List<Boolean> getResolvedContexts() {
		return resolvedContexts;
	}

	public void setResolvedContexts(List<Boolean> resolvedContexts) {
		this.resolvedContexts = resolvedContexts;
	}
}
