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
package com.redhat.qute.services.codeactions;

import static com.redhat.qute.ls.commons.CodeActionFactory.createCommand;
import static com.redhat.qute.utils.StringUtils.isSimilar;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.client.ConfigurationItemEdit;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.QuteValidationSettings.Severity;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Abstract class for Qute code action.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractQuteCodeAction {

	private static final String SET_IGNORE_SEVERITY_TITLE = "Ignore `{0}` problem.";

	private static final String DID_YOU_MEAN_TITLE = "Did you mean `{0}`?";

	protected final JavaDataModelCache javaCache;

	public AbstractQuteCodeAction(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	/**
	 * Generate code action(s) for similar values (Did you mean ...?)
	 * 
	 * @param part            the part node.
	 * @param availableValues the available values.
	 * @param template        the Qute template.
	 * @param diagnostic      the diagnostic.
	 * @param codeActions     list of CodeActions.
	 */
	protected void doCodeActionsForSimilarValues(Part part, Collection<String> availableValues, Template template,
			Diagnostic diagnostic, List<CodeAction> codeActions) {
		if (availableValues.isEmpty()) {
			return;
		}

		Range rangeValue = QutePositionUtility.createRange(part);
		String partName = part.getPartName();
		for (String value : availableValues) {
			if (isSimilar(value, partName)) {
				CodeAction similarCodeAction = CodeActionFactory.replace(
						MessageFormat.format(DID_YOU_MEAN_TITLE, value), rangeValue, value, template.getTextDocument(),
						diagnostic);
				codeActions.add(similarCodeAction);
			}
		}
	}

	/**
	 * CodeAction to change severity setting value to "ignore"
	 *
	 * @param template        the Qute template.
	 * @param diagnostic      the diagnostic to set to ignore.
	 * @param errorCode       the Qute error code.
	 * @param codeActions     list of CodeActions.
	 * @param severitySetting the severity setting to set to ignore.
	 */
	protected static void doCodeActionToSetIgnoreSeverity(Template template, Diagnostic diagnostic,
			QuteErrorCode errorCode, List<CodeAction> codeActions, String severitySetting) {
		List<Diagnostic> diagnostics = Collections.singletonList(diagnostic);
		String title = MessageFormat.format(SET_IGNORE_SEVERITY_TITLE, errorCode.getCode());
		CodeAction setIgnoreSeverityQuickFix = createConfigurationUpdateCodeAction(title, template.getUri(),
				severitySetting, Severity.ignore.name(), ConfigurationItemEditType.update, diagnostics);
		codeActions.add(setIgnoreSeverityQuickFix);
	}

	/**
	 * Create the configuration update (done on client side) quick fix.
	 *
	 * @param title       the displayed name of the QuickFix.
	 * @param sectionName the section name of the settings to update.
	 * @param item        the section value of the settings to update.
	 * @param editType    the configuration edit type.
	 * @param diagnostic  the diagnostic list that this CodeAction will fix.
	 *
	 * @return the configuration update (done on client side) quick fix.
	 */
	public static CodeAction createConfigurationUpdateCodeAction(String title, String scopeUri, String sectionName,
			Object sectionValue, ConfigurationItemEditType editType, List<Diagnostic> diagnostics) {
		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit(sectionName, editType, sectionValue);
		configItemEdit.setScopeUri(scopeUri);
		return createCommand(title, QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE,
				Collections.singletonList(configItemEdit), diagnostics);
	}

	/**
	 * Create code actions.
	 * 
	 * @param request                  the code action request.
	 * @param codeActionResolveFutures the code action which contains text edit
	 *                                 which requires some resolve.
	 * @param codeActions              the code actions to fill.
	 */
	public abstract void doCodeActions(CodeActionRequest request,
			List<CompletableFuture<Void>> codeActionResolveFutures, List<CodeAction> codeActions);

}
