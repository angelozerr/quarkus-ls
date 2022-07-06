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
package com.redhat.qute.jdt.internal.java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.GenerateTemplateInfo;
import com.redhat.qute.jdt.QuteCommandConstants;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

/**
 * Report codelens for opening/creating Qute template for:
 * 
 * <ul>
 * <li>declared method which have class annotated with @CheckedTemplate.</li>
 * <li>declared field which have Template as type.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteJavaCodeLensCollector extends AbstractQuteTemplateLinkCollector {

	private static final String QUTE_COMMAND_OPEN_URI_MESSAGE = "Open `{0}`";

	private static final String QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE = "Create `{0}`";

	private final List<CodeLens> lenses;

	public QuteJavaCodeLensCollector(ITypeRoot typeRoot, List<CodeLens> lenses, IJDTUtils utils,
			IProgressMonitor monitor) {
		super(typeRoot, utils, monitor);
		this.lenses = lenses;
	}

	@Override
	protected void collectTemplateLink(ASTNode fieldOrMethod, ASTNode locationAnnotation, TypeDeclaration type, String className, String fieldOrMethodName,
			String location, IFile templateFile, String templateFilePath) throws JavaModelException {
		Command command = null;
		if (templateFile.exists()) {
			command = new Command(MessageFormat.format(QUTE_COMMAND_OPEN_URI_MESSAGE, templateFilePath), //
					QuteCommandConstants.QUTE_COMMAND_OPEN_URI,
					Arrays.asList(templateFile.getLocationURI().toString()));
		} else {
			List<DataModelParameter> parameters = createParameters(fieldOrMethod);
			GenerateTemplateInfo info = new GenerateTemplateInfo();
			info.setParameters(parameters);
			info.setProjectUri(JDTQuteProjectUtils.getProjectUri(typeRoot.getJavaProject()));
			info.setTemplateFileUri(templateFile.getLocationURI().toString());
			info.setTemplateFilePath(templateFilePath);
			command = new Command(MessageFormat.format(QUTE_COMMAND_GENERATE_TEMPLATE_MESSAGE, templateFilePath), //
					QuteCommandConstants.QUTE_COMMAND_GENERATE_TEMPLATE_FILE, Arrays.asList(info));
		}
		Range range = utils.toRange(typeRoot, fieldOrMethod.getStartPosition(), fieldOrMethod.getLength());
		CodeLens codeLens = new CodeLens(range, command, null);
		lenses.add(codeLens);
	}

	private static List<DataModelParameter> createParameters(ASTNode node) {
		if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
			return createParameter((MethodDeclaration) node);
		}
		return Collections.emptyList();
	}

	private static List<DataModelParameter> createParameter(FieldDeclaration node) {
		List<DataModelParameter> parameters = new ArrayList<>();
		return parameters;
	}

	private static List<DataModelParameter> createParameter(MethodDeclaration method) {
		List<DataModelParameter> parameters = new ArrayList<>();
		@SuppressWarnings("rawtypes")
		List methodParameters = method.parameters();
		for (Object methodParameter : methodParameters) {
			SingleVariableDeclaration variable = (SingleVariableDeclaration) methodParameter;
			String parameterName = variable.getName().getFullyQualifiedName();
			Type parameterType = variable.getType();
			ITypeBinding binding = parameterType.resolveBinding();
			DataModelParameter parameter = new DataModelParameter();
			parameter.setKey(parameterName);
			parameter.setSourceType(binding.getQualifiedName());
			parameters.add(parameter);
		}
		return parameters;
	}

}
