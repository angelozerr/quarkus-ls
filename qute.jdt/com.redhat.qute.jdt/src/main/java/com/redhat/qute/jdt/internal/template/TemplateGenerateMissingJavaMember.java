/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.manipulation.CodeGeneration;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.manipulation.StubUtility;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jdt.internal.corext.dom.BodyDeclarationRewrite;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.jdt.internal.QuteJavaConstants;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.TextEditConverter;

/**
 * Resolves workspace edits for generating missing java members.
 *
 * @author datho7561
 */
public class TemplateGenerateMissingJavaMember {

	private static final Logger LOGGER = Logger.getLogger(TemplateGenerateMissingJavaMember.class.getName());

	private static final Pattern BROKEN_FILE_PROTOCOL = Pattern.compile("file:/(?!//)");

	private static final Range PREPEND_RANGE = new Range(new Position(0, 0), new Position(0, 0));

	private TemplateGenerateMissingJavaMember() {

	}

	/**
	 * Returns the WorkspaceEdit needed to generate the requested member, or null if
	 * it cannot be computed.
	 *
	 * @param params  the parameters needed to construct the workspace edit
	 * @param utils   the jdt utils
	 * @param monitor the progress monitor
	 * @return the WorkspaceEdit needed to generate the requested member, or null if
	 *         it cannot be computed
	 */
	public static WorkspaceEdit handleGenerateMissingJavaMember(GenerateMissingJavaMemberParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		switch (params.getMemberType()) {
		case Field:
			return handleMissingField(params, utils, monitor);
		case Getter:
			return handleCreateMissingGetterCodeAction(params, utils, monitor);
		case AppendTemplateExtension:
			return handleCreateMissingTemplateExtension(params, utils, monitor);
		case CreateTemplateExtension:
			return createNewTemplateExtensionsFile(params, utils, monitor);
		default:
			return null;
		}
	}

	private static WorkspaceEdit handleMissingField(GenerateMissingJavaMemberParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		IJavaProject project = getJavaProjectFromProjectUri(params.getProjectUri());
		IType javaType;
		try {
			javaType = project.findType(params.getJavaType());
		} catch (JavaModelException e) {
			return null;
		}

		IField currentlyField = javaType.getField(params.getMissingProperty());
		if (currentlyField != null && currentlyField.exists()) {
			return handleUpdatePermissionsOfExistingField(params, utils, project, javaType, monitor);
		} else {
			return handleCreateMissingField(params, utils, project, javaType, monitor);
		}

	}

	private static WorkspaceEdit handleCreateMissingField(GenerateMissingJavaMemberParams params, IJDTUtils utils,
			IJavaProject project, IType javaType, IProgressMonitor monitor) {
		CompilationUnit cu = createQuickFixAST(javaType);
		if (cu == null) {
			return null;
		}
		ASTNode typeNode = cu.findDeclaringNode("L" + params.getJavaType().replace('.', '/') + ";");
		if (typeNode == null) {
			return null;
		}
		AST ast = typeNode.getAST();
		var rewrite = ASTRewrite.create(ast);

		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(params.getMissingProperty()));

		Modifier modifier = ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD);

		FieldDeclaration decl = ast.newFieldDeclaration(fragment);
		decl.setType(ast.newSimpleType(ast.newSimpleName("String")));
		decl.modifiers().add(modifier);

		insertDeclaration(decl, rewrite, typeNode);

		org.eclipse.text.edits.TextEdit jdtTextEdit;
		try {
			jdtTextEdit = rewrite.rewriteAST();
		} catch (JavaModelException e) {
			return null;
		}
		TextDocumentEdit textDocumentEdit = new TextEditConverter((ICompilationUnit) javaType.getTypeRoot(),
				jdtTextEdit, utils).convertToTextDocumentEdit(0);
		return new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
	}

	private static WorkspaceEdit handleUpdatePermissionsOfExistingField(GenerateMissingJavaMemberParams params,
			IJDTUtils utils, IJavaProject project, IType javaType, IProgressMonitor monitor) {
		CompilationUnit cu = createQuickFixAST(javaType);
		if (cu == null) {
			return null;
		}
		ASTNode typeNode = cu.findDeclaringNode("L" + params.getJavaType().replace('.', '/') + ";");
		if (typeNode == null) {
			return null;
		}
		AST ast = typeNode.getAST();
		var rewrite = ASTRewrite.create(ast);

		FieldDeclFinder fieldDeclFinder = new FieldDeclFinder(params.getMissingProperty());
		typeNode.accept(fieldDeclFinder);
		if (fieldDeclFinder.getFieldDeclaration() == null) {
			return null;
		}
		FieldDeclaration oldFieldDeclaration = fieldDeclFinder.getFieldDeclaration();

		// This is needed to prevent an exception, since the language client might send
		// a new CodeAction request before the user saves the modifications to the Java
		// class from accepting the previous CodeAction
		boolean alreadyPublic = oldFieldDeclaration.modifiers().stream().anyMatch((mod) -> {
			if (!(mod instanceof Modifier)) {
				return false;
			}
			Modifier asMod = (Modifier) mod;
			return asMod.getKeyword() == ModifierKeyword.PUBLIC_KEYWORD;
		});
		if (alreadyPublic) {
			return null;
		}

		if (fieldDeclFinder.hasMultiDecl()) {
			// eg. worst (most complex) case
			// ```java
			// private String prefix = "aaa", suffix = "bbb";
			// ```
			// in order to make `prefix` public,
			// instead of changing the `private` modifier to `public`,
			// we want to remove `prefix` from the list,
			// then create a new, separate field declaration that has `public`.
			// We also need to copy over the declaration of the value
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) rewrite
					.createMoveTarget(fieldDeclFinder.getOldBadFragment());
			rewrite.remove(fieldDeclFinder.getOldBadFragment(), null);

			Modifier modifier = ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD);

			FieldDeclaration decl = ast.newFieldDeclaration(fragment);
			// Preserve the type of the field we are making public
			decl.setType(getCloneOfType(oldFieldDeclaration, ast, rewrite));
			decl.modifiers().add(modifier);

			insertDeclaration(decl, rewrite, typeNode);
		} else {
			Modifier oldModifier = null;
			for (Object modifierUncast : oldFieldDeclaration.modifiers()) {
				if (modifierUncast instanceof Modifier) {
					Modifier modifier = (Modifier) modifierUncast;
					if (modifier.isPrivate() || modifier.isProtected()) {
						oldModifier = modifier;
						break;
					}
				}
			}
			Modifier newModifier = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			rewrite.replace(oldModifier, newModifier, null);
		}

		org.eclipse.text.edits.TextEdit jdtTextEdit;
		try {
			jdtTextEdit = rewrite.rewriteAST();
		} catch (JavaModelException e) {
			return null;
		}
		TextDocumentEdit textDocumentEdit = new TextEditConverter((ICompilationUnit) javaType.getTypeRoot(),
				jdtTextEdit, utils).convertToTextDocumentEdit(0);
		return new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
	}

	private static WorkspaceEdit handleCreateMissingGetterCodeAction(GenerateMissingJavaMemberParams params,
			IJDTUtils utils, IProgressMonitor monitor) {
		IJavaProject project = getJavaProjectFromProjectUri(params.getProjectUri());
		IType javaType;
		try {
			javaType = project.findType(params.getJavaType());
		} catch (JavaModelException e) {
			return null;
		}
		CompilationUnit cu = createQuickFixAST(javaType);
		if (cu == null) {
			return null;
		}
		ASTNode typeNode = cu.findDeclaringNode("L" + params.getJavaType().replace('.', '/') + ";");
		if (typeNode == null) {
			return null;
		}
		AST ast = typeNode.getAST();
		var rewrite = ASTRewrite.create(ast);

		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		String methodName = "get" + getCapitalized(params.getMissingProperty());
		methodDeclaration.setName(ast.newSimpleName(methodName));
		methodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

		Block block = ast.newBlock();
		ReturnStatement returnStatement = ast.newReturnStatement();
		IField field = javaType.getField(params.getMissingProperty());
		if (field != null && field.exists()) {
			FieldDeclFinder declFinder = new FieldDeclFinder(params.getMissingProperty());
			typeNode.accept(declFinder);
			FieldDeclaration fieldDeclaration = declFinder.getFieldDeclaration();
			FieldAccess fieldAccess = ast.newFieldAccess();
			fieldAccess.setName(ast.newSimpleName(params.getMissingProperty()));
			fieldAccess.setExpression(ast.newThisExpression());
			returnStatement.setExpression(fieldAccess);
			methodDeclaration.setReturnType2(getCloneOfType(fieldDeclaration, ast, rewrite));
		} else {
			Expression expression = ast.newNullLiteral();
			returnStatement.setExpression(expression);
			methodDeclaration.setReturnType2(ast.newSimpleType(ast.newSimpleName("String")));
		}
		block.statements().add(returnStatement);
		methodDeclaration.setBody(block);

		insertDeclaration(methodDeclaration, rewrite, typeNode);

		org.eclipse.text.edits.TextEdit jdtTextEdit;
		try {
			jdtTextEdit = rewrite.rewriteAST();
		} catch (JavaModelException e) {
			return null;
		}
		TextDocumentEdit textDocumentEdit = new TextEditConverter((ICompilationUnit) javaType.getTypeRoot(),
				jdtTextEdit, utils).convertToTextDocumentEdit(0);
		return new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
	}

	// TODO: caching scheme
	private static WorkspaceEdit handleCreateMissingTemplateExtension(GenerateMissingJavaMemberParams params,
			IJDTUtils utils, IProgressMonitor monitor) {

		IJavaProject project = getJavaProjectFromProjectUri(params.getProjectUri());
		IType type = null;
		try {
			type = project.findType(params.getTemplateClass());
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					String.format("JavaModelException while trying to locate template extension class {0}",
							params.getTemplateClass()),
					e);
		}

		if (type == null) {
			return null;
		}

		return addTemplateExtensionToFile(params, utils, project, type, monitor);
	}

	private static WorkspaceEdit createNewTemplateExtensionsFile(GenerateMissingJavaMemberParams params,
			IJDTUtils utils, IProgressMonitor monitor) {
		IJavaProject project = getJavaProjectFromProjectUri(params.getProjectUri());
		return createNewTemplateExtensionFile(params, utils, project, monitor);
	}

	private static WorkspaceEdit addTemplateExtensionToFile(GenerateMissingJavaMemberParams params, IJDTUtils utils,
			IJavaProject project, IType templateExtensionType, IProgressMonitor monitor) {
		CompilationUnit cu = createQuickFixAST(templateExtensionType);
		ASTNode typeNode = cu.findDeclaringNode(templateExtensionType.getKey());
		if (typeNode == null) {
			return null;
		}
		AST ast = typeNode.getAST();
		var rewrite = ASTRewrite.create(ast);

		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		String methodName = params.getMissingProperty();
		methodDeclaration.setName(ast.newSimpleName(methodName));
		methodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		methodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
		SingleVariableDeclaration singleVariableDeclaration = ast.newSingleVariableDeclaration();
		singleVariableDeclaration.setType(ast.newSimpleType(ast.newName(params.getJavaType())));
		singleVariableDeclaration.setName(ast.newSimpleName(getParamNameFromFullyQualifiedType(params.getJavaType())));
		methodDeclaration.parameters().add(singleVariableDeclaration);

		Block block = ast.newBlock();
		ReturnStatement returnStatement = ast.newReturnStatement();
		Expression expression = ast.newNullLiteral();
		returnStatement.setExpression(expression);
		methodDeclaration.setReturnType2(ast.newSimpleType(ast.newSimpleName("String")));
		block.statements().add(returnStatement);
		methodDeclaration.setBody(block);

		insertDeclaration(methodDeclaration, rewrite, typeNode);

		org.eclipse.text.edits.TextEdit jdtTextEdit;
		try {
			jdtTextEdit = rewrite.rewriteAST();
		} catch (JavaModelException e) {
			return null;
		}
		TextDocumentEdit textDocumentEdit = new TextEditConverter(
				(ICompilationUnit) templateExtensionType.getTypeRoot(), jdtTextEdit, utils)
				.convertToTextDocumentEdit(0);
		return new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
	}

	private static WorkspaceEdit createNewTemplateExtensionFile(GenerateMissingJavaMemberParams params, IJDTUtils utils,
			IJavaProject project, IProgressMonitor monitor) {

		IPackageFragment destPackage = null;
		try {

			IPath targetFolder = project.getOutputLocation().removeLastSegments(1);

			List<IClasspathEntry> sourceClassPathEntries = Stream.of(project.getRawClasspath()) //
					.filter(cpe -> {
						cpe.getExtraAttributes();
						return cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE && !cpe.isTest() && !targetFolder.isPrefixOf(cpe.getPath());
					}) //
					.toList();
			if (sourceClassPathEntries.isEmpty()) {
				throw new UnsupportedOperationException(
						"Cannot locate source code folder");
			}
			Optional<IPackageFragmentRoot> sourceFragmentRootOptional = Stream.of(project.getPackageFragmentRoots()) //
					.filter(pfr -> {
						try {
							if (pfr.getKind() == IPackageFragmentRoot.K_BINARY || pfr.getResource() == null) {
								return false;
							}
							return Stream.of(pfr.getChildren()).anyMatch(child -> child.getElementType() == IJavaElement.PACKAGE_FRAGMENT);
						} catch (JavaModelException e) {
							return false;
						}
					}) //
					.filter(pfr -> {
						for (var cpe : sourceClassPathEntries) {
							if (cpe.getPath().equals(pfr.getResource().getFullPath())) {
								return true;
							}
						}
						return false;
					}) //
					.findFirst();
			if (sourceFragmentRootOptional.isEmpty()) {
				throw new UnsupportedOperationException(
						"Cannot locate source code folder");
			}
			IPackageFragmentRoot fragmentRoot = sourceFragmentRootOptional.get();
			// TODO: longest substring match of package of original class
			// OR: figure out the "correct" one from the groupId?
			destPackage = fragmentRoot.getPackageFragment("");
		} catch (JavaModelException e) {
		}
		// TODO: just use the default package I guess?
		if (destPackage == null) {
			return null;
		}

		String baseName = "TemplateExtensions";
		String name = baseName;
		ICompilationUnit cu = destPackage.getCompilationUnit(baseName + ".java");
		int i = 0;
		while (cu.exists()) {
			name = baseName + ++i;
			cu = destPackage.getCompilationUnit(name + ".java");
		}

		ResourceOperation createFileOperation = new CreateFile(
				fixBrokenUri(cu.getResource().getRawLocationURI().toString()));
		TextDocumentEdit addContentEdit;
		try {
			addContentEdit = createNewTemplateExtensionsContent(cu, name, params.getMissingProperty(),
					params.getJavaType(), fixBrokenUri(cu.getResource().getRawLocationURI().toString()));
		} catch (CoreException e) {
			throw new RuntimeException("Failure while constructing new Java file content", e);
		}

		WorkspaceEdit makeTemplateExtensions = new WorkspaceEdit();
		makeTemplateExtensions.setDocumentChanges(
				Arrays.asList(Either.forRight(createFileOperation), Either.forLeft(addContentEdit)));
		return makeTemplateExtensions;

	}

	private static TextDocumentEdit createNewTemplateExtensionsContent(ICompilationUnit cu, String typeName,
			String methodName, String methodParamFullyQualifiedType, String uri) throws CoreException {
		String lineDelimiter = StubUtility.getLineDelimiterUsed(cu.getJavaProject());
		String typeStub = constructTypeStub(cu, typeName, Flags.AccPublic, methodName, methodParamFullyQualifiedType,
				lineDelimiter);
		String cuContent = constructCUContent(cu, typeStub, lineDelimiter);
		TextDocumentEdit tde = new TextDocumentEdit();
		tde.setTextDocument(new VersionedTextDocumentIdentifier(uri, 0));
		tde.setEdits(Arrays.asList(new TextEdit(PREPEND_RANGE, cuContent)));
		return tde;
	}

	/*
	 * Copied & modified from JDT-LS
	 */
	private static String constructTypeStub(ICompilationUnit parentCU, String name, int modifiers, String methodName,
			String methodParamFullyQualifiedType, String lineDelimiter) {
		StringBuilder buf = new StringBuilder();

		buf.append("@");
		buf.append(QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION);
		buf.append(lineDelimiter);

		buf.append(Flags.toString(modifiers));
		if (modifiers != 0) {
			buf.append(' ');
		}

		buf.append("class ");
		buf.append(name);
		buf.append(" {").append(lineDelimiter); //$NON-NLS-1$
		buf.append(constructMethodStub(parentCU, methodName, methodParamFullyQualifiedType, lineDelimiter));
		buf.append('}').append(lineDelimiter);
		return buf.toString();
	}

	private static String constructMethodStub(ICompilationUnit compilationUnit, String methodName,
			String methodParamFullyQualifiedType, String lineDelimiter) {
		StringBuilder buf = new StringBuilder("\tpublic static String ");
		buf.append(methodName).append("(").append(methodParamFullyQualifiedType);
		buf.append(" ");
		buf.append(getParamNameFromFullyQualifiedType(methodParamFullyQualifiedType));
		buf.append(") {").append(lineDelimiter);
		buf.append("\t\treturn null;").append(lineDelimiter);
		buf.append("\t}").append(lineDelimiter);
		return buf.toString();
	}

	/**
	 * Copied from JDT-LS
	 */
	private static String constructCUContent(ICompilationUnit cu, String typeContent, String lineDelimiter)
			throws CoreException {
		String fileComment = CodeGeneration.getFileComment(cu, lineDelimiter);
		String typeComment = CodeGeneration.getTypeComment(cu, JavaCore.removeJavaLikeExtension(cu.getElementName()), lineDelimiter);
		IPackageFragment pack = (IPackageFragment) cu.getParent();
		String content = CodeGeneration.getCompilationUnitContent(cu, fileComment, typeComment, typeContent,
				lineDelimiter);
		if (content != null) {
			ASTParser parser = ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
			parser.setProject(cu.getJavaProject());
			parser.setSource(content.toCharArray());
			CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			if ((pack.isDefaultPackage() || unit.getPackage() != null) && !unit.types().isEmpty()) {
				return content;
			}
		}
		StringBuilder buf = new StringBuilder();
		if (!pack.isDefaultPackage()) {
			buf.append("package ").append(pack.getElementName()).append(';'); //$NON-NLS-1$
		}
		buf.append(lineDelimiter).append(lineDelimiter);
		buf.append(typeContent);
		return buf.toString();
	}

	private static CompilationUnit createQuickFixAST(IType javaType) {
		if (javaType.isBinary()) {
			return null;
		}
		return ASTResolving.createQuickFixAST(javaType.getCompilationUnit(), null);
	}

	private static IJavaProject getJavaProjectFromProjectUri(String projectName) {
		if (projectName == null) {
			return null;
		}
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return javaProject.exists() ? javaProject : null;
	}

	/**
	 * Returns the given camelCaseName with the first letter capitalized.
	 *
	 * @param camelCaseName the camelCaseVariableName to capitalize
	 * @return the given camelCaseName with the first letter capitalized
	 */
	private static String getCapitalized(String camelCaseName) {
		return camelCaseName.substring(0, 1).toUpperCase() + camelCaseName.substring(1);
	}

	/**
	 * Gets the type of the field declaration, then recreates it on the given ast.
	 *
	 * @param toClone the field declaration to clone the type of
	 * @param ast     the ast on which to create the clone of the type
	 * @return the cloned type
	 */
	private static Type getCloneOfType(FieldDeclaration toClone, AST ast, ASTRewrite rewrite) {
		return (Type) rewrite.createCopyTarget(toClone.getType());
	}

	private static String getParamNameFromFullyQualifiedType(String fullyQualifiedType) {
		int lastDot = fullyQualifiedType.lastIndexOf(".");
		return fullyQualifiedType.substring(lastDot + 1, lastDot + 2).toLowerCase()
				+ fullyQualifiedType.substring(lastDot + 2);
	}

	private static String fixBrokenUri(String uri) {
		Matcher m = BROKEN_FILE_PROTOCOL.matcher(uri);
		return m.replaceFirst("file:///");
	}

	private static void insertDeclaration(BodyDeclaration decl, ASTRewrite rewrite, ASTNode typeNode) {
		BodyDeclarationRewrite bodyDeclarationRewrite = BodyDeclarationRewrite.create(rewrite, typeNode);
		bodyDeclarationRewrite.insert(decl, null);
	}

	/**
	 * An ASTVisitor that visits a type declaration and finds the field declaration
	 * for the given field name
	 */
	static class FieldDeclFinder extends ASTVisitor {

		private boolean hasEnteredRootType = false;

		private FieldDeclaration fieldDeclaration = null;
		private boolean multiDecl = false;
		private VariableDeclarationFragment oldBadFragment = null;
		private final String privatedProperty;

		public FieldDeclFinder(String privatedProperty) {
			this.privatedProperty = privatedProperty;
		}

		@Override
		public boolean visit(FieldDeclaration fieldDeclaration) {
			if (this.fieldDeclaration != null) {
				return false;
			}
			Collection<VariableDeclarationFragment> fragments = (Collection<VariableDeclarationFragment>) fieldDeclaration
					.getStructuralProperty(FieldDeclaration.FRAGMENTS_PROPERTY);
			for (VariableDeclarationFragment fragment : fragments) {
				if (fragment.getName().getFullyQualifiedName().equals(this.privatedProperty)) {
					this.fieldDeclaration = fieldDeclaration;
					this.multiDecl = fragments.size() > 1;
					this.oldBadFragment = fragment;
					return false;
				}
			}
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration typeDeclaration) {
			if (hasEnteredRootType) {
				return false;
			}
			hasEnteredRootType = true;
			return true;
		}

		public FieldDeclaration getFieldDeclaration() {
			return this.fieldDeclaration;
		}

		public boolean hasMultiDecl() {
			return this.multiDecl;
		}

		public VariableDeclarationFragment getOldBadFragment() {
			return this.oldBadFragment;
		}
	}

}
