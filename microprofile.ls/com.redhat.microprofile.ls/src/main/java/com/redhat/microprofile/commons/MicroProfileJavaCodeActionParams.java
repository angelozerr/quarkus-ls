/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import org.eclipse.lsp4j.CodeActionParams;

/**
 * MicroProfile Java codeAction parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaCodeActionParams extends CodeActionParams {

	public String getUri() {
		return getTextDocument().getUri();
	}

}
