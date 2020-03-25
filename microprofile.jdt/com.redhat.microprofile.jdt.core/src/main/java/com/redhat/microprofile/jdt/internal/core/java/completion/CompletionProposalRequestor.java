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
package com.redhat.microprofile.jdt.internal.core.java.completion;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * JDT completion request to get the JDT completion context.
 * 
 * @author Angelo ZERR
 *
 */
public class CompletionProposalRequestor extends CompletionRequestor {

	private CompletionContext context;

	public void acceptContext(CompletionContext context) {
		super.acceptContext(context);
		this.context = context;
	}

	public CompletionProposalRequestor(ICompilationUnit unit, int offset) {
		super.setRequireExtendedContext(true);
	}

	@Override
	public void accept(CompletionProposal proposal) {

	}

	public CompletionContext getContext() {
		return context;
	}
}
