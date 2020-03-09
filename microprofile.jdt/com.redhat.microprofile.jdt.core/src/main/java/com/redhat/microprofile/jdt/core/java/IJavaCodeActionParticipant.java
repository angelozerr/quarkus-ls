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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

/**
 * Java codeAction participants API.
 * 
 * @author Angelo ZERR
 *
 */
public interface IJavaCodeActionParticipant {

	/**
	 * Begin codeAction collection.
	 * 
	 * @param diagnostic
	 * 
	 * @param context    the java codeAction context
	 * @param monitor    the progress monitor
	 * 
	 * @throws CoreException
	 */
	default void beginCodeAction(Diagnostic diagnostic, JavaCodeActionContext context, IProgressMonitor monitor)
			throws CoreException {

	}

	/**
	 * Collect codeAction according to the context.
	 * 
	 * @param diagnostic
	 * 
	 * @param context    the java codeAction context
	 * @param monitor    the progress monitor
	 * 
	 * @return the codeAction list and null otherwise.
	 * 
	 * @throws CoreException
	 */
	List<CodeAction> collectCodeAction(Diagnostic diagnostic, JavaCodeActionContext context, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * End codeAction collection.
	 * 
	 * @param diagnostic
	 * 
	 * @param context    the java codeAction context
	 * @param monitor    the progress monitor
	 * 
	 * @throws CoreException
	 */
	default void endCodeAction(Diagnostic diagnostic, JavaCodeActionContext context, IProgressMonitor monitor)
			throws CoreException {

	}
}
