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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.manipulation.CoreASTProvider;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMarkerAnnotationName;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import com.redhat.microprofile.commons.JavaKind;
import com.redhat.microprofile.commons.JavaSnippetContext;
import com.redhat.microprofile.commons.MicroProfileJavaCompletionParams;
import com.redhat.microprofile.commons.MicroProfileJavaCompletionResult;
import com.redhat.microprofile.jdt.core.utils.AnnotationUtils;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;
import com.redhat.microprofile.jdt.internal.core.java.completion.JavaScopeFinder.JavaScope;

/**
 * Java completion handler.
 * 
 * @author Angelo ZERR
 *
 */
public class CompletionHandler {

	/**
	 * Returns completion result for the given uri and position.
	 * 
	 * @param params  the completion parameters
	 * @param utils   the utilities class
	 * @param monitor the monitor
	 * @return completion result for the given uri and position.
	 * @throws JavaModelException
	 */
	public MicroProfileJavaCompletionResult completion(MicroProfileJavaCompletionParams params, IJDTUtils utils,
			IProgressMonitor monitor) throws JavaModelException {
		MicroProfileJavaCompletionResult result = new MicroProfileJavaCompletionResult();

		String uri = params.getUri();
		ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		if (unit == null) {
			return result;
		}

		IJavaProject javaProject = unit.getJavaProject();
		int offset = utils.toOffset(unit.getBuffer(), params.getPosition().getLine(),
				params.getPosition().getCharacter());
		JavaScope scope = getJavaScope(unit, offset, monitor);

		List<Boolean> resolvedContexts = new ArrayList<>();
		result.setResolvedContexts(resolvedContexts);
		for (JavaSnippetContext context : params.getContexts()) {
			resolvedContexts.add(resolve(context, scope, javaProject));
		}

		/*
		 * JavaScope scope = getJavaScope(unit, offset, monitor);
		 * result.setScope(scope);
		 * 
		 * if (params.isCollectProjectDependencies()) { List<String> projectDependencies
		 * = Stream.of(((JavaProject) javaProject).getResolvedClasspath()) .map(entry ->
		 * getExtensionName(entry.getPath().toString())).filter(Objects::nonNull).
		 * collect(Collectors.toList());
		 * result.setProjectDependencies(projectDependencies); }
		 */
		/*
		 * CompletionProposalRequestor collector = new CompletionProposalRequestor(unit,
		 * offset); unit.codeComplete(offset, collector);
		 * 
		 * CompletionContext completionContext = collector.getContext(); if
		 * (completionContext instanceof InternalCompletionContext) { ASTNode node =
		 * ((InternalCompletionContext) completionContext).getCompletionNode();
		 * result.setScope(getJavaScope(node, unit, completionContext.getTokenStart(),
		 * offset, monitor)); }
		 */
		return result;
	}

	private boolean resolve(JavaSnippetContext context, JavaScope scope, IJavaProject javaProject) {
		if (context == null) {
			return true;
		}
		if (context.getKind() == JavaKind.ANNOTATION) {
			String type = context.getType().get(0);
			IType annotationType = JDTTypeUtils.findType(javaProject, type);
			if (annotationType != null) {
				try {
					IAnnotation target = AnnotationUtils.getAnnotation(annotationType, "java.lang.annotation.Target");
					if (target != null) {
						List<String> all = null;
						IMemberValuePair pair = target.getMemberValuePairs()[0];
						if (pair.getValue() instanceof String) {
							all = Arrays.asList(getName(((String) pair.getValue())));
						} else if (pair.getValue() instanceof String[]) {
							all = Stream.of((String[]) pair.getValue()).map(v -> getName(v))
									.collect(Collectors.toList());
						}
						if (all.isEmpty()) {
							return true;
						}
						switch (scope) {
						case TYPE:
							return all.contains("TYPE");
						case FIELD:
							return all.contains("FIELD");
						case METHOD:
							return all.contains("METHOD");
						case PARAMETER:
							return all.contains("PARAMETER");
						default:
							return true;
						}

					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;

		}
		if (context.getType() != null) {
			for (String type : context.getType()) {
				if (JDTTypeUtils.findType(javaProject, type) == null) {
					return false;
				}
			}
		}
		return true;
	}

	private static String getName(String v) {
		return v.substring(v.lastIndexOf('.') + 1);
	}

	private static String getExtensionName(String location) {
		if (location == null) {
			return null;
		}
		if (!location.endsWith(".jar")) {
			return null;
		}
		int start = location.lastIndexOf('/');
		if (start == -1) {
			return null;
		}
		start++;
		int end = location.lastIndexOf('-');
		if (end == -1) {
			end = location.lastIndexOf('.');
		}
		if (end < start) {
			return null;
		}
		String extensionName = location.substring(start, end);
		if (extensionName.endsWith("-deployment")) {
			extensionName = extensionName.substring(0, extensionName.length() - "-deployment".length());
		}
		return extensionName;
	}

	private static JavaScope getJavaScope(ICompilationUnit unit, int start, IProgressMonitor monitor) {
		CompilationUnit ast = CoreASTProvider.getInstance().getAST(unit, CoreASTProvider.WAIT_YES, monitor);
		JavaScopeFinder finder = new JavaScopeFinder(ast, start);
		return finder.getScope();
	}

	private static JavaScope getOLDJavaScope(ASTNode node, ICompilationUnit unit, int start, int end,
			IProgressMonitor monitor) {
		CompilationUnit ast = CoreASTProvider.getInstance().getAST(unit, CoreASTProvider.WAIT_YES, monitor);
		JavaScopeFinder finder = new JavaScopeFinder(ast, start);
		JavaScope scope = finder.getScope();
		if (true) {
			return scope;
		}
		if (node instanceof CompletionOnMarkerAnnotationName) {

			NodeFinder nodeFinder = new NodeFinder(ast, start, 100);
			org.eclipse.jdt.core.dom.ASTNode coveredNode = nodeFinder.getCoveredNode();
			org.eclipse.jdt.core.dom.ASTNode coveringNode = nodeFinder.getCoveringNode();
			// nodeFinder.getCoveredNode()
			if (coveredNode != null) {

			}
//			if (coveringNode != null) {
//				switch (coveringNode.getNodeType()) {
//				case org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT:
//					return JavaKind.TYPE;
//				case org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION:
//					return JavaKind.METHOD;
//				case org.eclipse.jdt.core.dom.ASTNode.METHOD_DECLARATION:
//					return JavaKind.PARAMETER;
//				}
//			}
			/*
			 * CompletionOnMarkerAnnotationName marker = (CompletionOnMarkerAnnotationName)
			 * node; if ((node.bits & ASTNode.T_JavaLangAnnotationTarget) != 0) { // marker.
			 * }
			 * 
			 * TypeReference typeReference = marker.type; Binding recipent =
			 * marker.resolvedType; if (recipent != null) { int kind = recipent.kind();
			 * switch (kind) { case Binding.FIELD: return JavaScope.FIELD; case
			 * Binding.METHOD: return JavaScope.METHOD; case Binding.TYPE_PARAMETER: return
			 * JavaScope.PARAMETER; case Binding.TYPE: return JavaScope.TYPE; default:
			 * break; } }
			 */
		}
		return JavaScope.UNKNOWN;
	}

}
