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
package com.redhat.microprofile.jdt.internal.health.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import com.redhat.microprofile.jdt.core.java.IJavaCodeActionParticipant;
import com.redhat.microprofile.jdt.core.java.JavaCodeActionContext;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTCodeActionUtils;
import com.redhat.microprofile.jdt.core.utils.PositionUtils;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;

/**
 *
 * MicroProfile Health CodeAction for
 * {@link MicroProfileHealthErrorCode#HealthAnnotationMissing}
 * 
 * <p>
 * Those rules comes from
 * https://github.com/MicroShed/microprofile-language-server/blob/517056559d82244355b51365d6617caf5b36be1f/src/main/java/com/microprofile/lsp/internal/helper/QuickFixHelper.java
 * </p>
 * 
 * @author Angelo ZERR
 * 
 * @See https://github.com/eclipse/microprofile-health
 *
 */
public class HealthAnnotationMissingCodeActionParticipant implements IJavaCodeActionParticipant {

	@Override
	public List<CodeAction> collectCodeAction(Diagnostic diagnostic, JavaCodeActionContext context,
			IProgressMonitor monitor) throws CoreException {
		IJDTUtils utils = context.getUtils();
		Position position = diagnostic.getRange().getStart();
		IJavaElement element = PositionUtils.getJavaElementAt(context.getTypeRoot(), position, utils);
		if (element == null) {
			return null;
		}
		IType classType = (IType) element;
		classType.getSourceRange();
		List<CodeAction> codeActions = new ArrayList<>();

		// add @Liveness
		CodeAction codeAction = JDTCodeActionUtils.insertAnnotations("Add missing @Liveness annotation",
				context.getUri(), context.getTypeRoot(), diagnostic, utils,
				MicroProfileHealthConstants.LIVENESS_ANNOTATION);
		if (codeAction != null) {
			codeActions.add(codeAction);
		}
		// add @Readiness
		codeAction = JDTCodeActionUtils.insert("Add missing @Readiness annotation", position, "@Readiness \n",
				context.getUri(), diagnostic);
		codeActions.add(codeAction);

		// add @Health
		codeAction = JDTCodeActionUtils.insert("Add missing @Health annotation", position, "@Health \n",
				context.getUri(), diagnostic);
		codeActions.add(codeAction);

		return codeActions;
	}
}
