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
package com.redhat.microprofile.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.microprofile.commons.MicroProfileJavaCompletionParams;
import com.redhat.microprofile.commons.MicroProfileJavaCompletionResult;

/**
 * MicroProfile Java completion provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileJavaCompletionProvider {

	@JsonRequest("microprofile/java/completion")
	default CompletableFuture<MicroProfileJavaCompletionResult> getJavaCompletion(
			MicroProfileJavaCompletionParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}
