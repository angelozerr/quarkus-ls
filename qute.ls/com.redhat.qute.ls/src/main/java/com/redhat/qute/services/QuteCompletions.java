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
package com.redhat.qute.services;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.completions.QuteCompletionForTemplateIds;
import com.redhat.qute.services.completions.QuteCompletionsForExpression;
import com.redhat.qute.services.completions.QuteCompletionsForParameterDeclaration;
import com.redhat.qute.services.completions.tags.QuteCompletionForTagSection;
import com.redhat.qute.services.completions.tags.QuteCompletionsForSnippets;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * The Qute completions
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletions.class.getName());

	public static final CompletionList EMPTY_COMPLETION = new CompletionList();

	public static final CompletableFuture<CompletionList> EMPTY_FUTURE_COMPLETION = CompletableFuture
			.completedFuture(EMPTY_COMPLETION);

	private final QuteCompletionsForParameterDeclaration completionsForParameterDeclaration;

	private final QuteCompletionsForExpression completionForExpression;

	private final QuteCompletionsForSnippets<Snippet> completionsForSnippets;

	private final QuteCompletionForTagSection completionForTagSection;

	private final QuteCompletionForTemplateIds completionForTemplateIds;

	public QuteCompletions(JavaDataModelCache javaCache, SnippetRegistryProvider<Snippet> snippetRegistryProvider) {
		this.completionsForParameterDeclaration = new QuteCompletionsForParameterDeclaration(javaCache);
		this.completionsForSnippets = new QuteCompletionsForSnippets<Snippet>(snippetRegistryProvider);
		this.completionForTagSection = new QuteCompletionForTagSection(completionsForSnippets);
		this.completionForExpression = new QuteCompletionsForExpression(completionForTagSection, javaCache);
		this.completionForTemplateIds = new QuteCompletionForTemplateIds();
	}

	/**
	 * Returns completion list for the given position
	 *
	 * @param template             the Qute template
	 * @param position             the position where completion was triggered
	 * @param completionSettings   the completion settings.
	 * @param formattingSettings   the formatting settings.
	 * @param nativeImagesSettings the native image settings.
	 * @param cancelChecker        the cancel checker
	 * @return completion list for the given position
	 */
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			QuteNativeSettings nativeImagesSettings, CancelChecker cancelChecker) {
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(template, position, completionSettings, formattingSettings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of CompletionRequest failed", e);
			return EMPTY_FUTURE_COMPLETION;
		}
		Node node = completionRequest.getNode();
		if (node == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		String text = template.getText();
		int offset = completionRequest.getOffset();

		if (node.getKind() == NodeKind.Expression || node.getKind() == NodeKind.ExpressionParts
				|| node.getKind() == NodeKind.ExpressionPart) {
			Expression expression = null;
			Node nodeExpression = null;
			if (node.getKind() == NodeKind.Expression) {
				expression = (Expression) node;
			} else if (node.getKind() == NodeKind.ExpressionParts) {
				nodeExpression = node;
				expression = ((Parts) node).getParent();
			} else if (node.getKind() == NodeKind.ExpressionPart) {
				nodeExpression = node;
				expression = ((Part) node).getParent().getParent();
			}
			if (expression != null && Section.isIncludeSection(expression.getOwnerSection())) {
				// {#include | }
				return completionForTemplateIds.doCompleteTemplateId(completionRequest,
						completionSettings, formattingSettings, cancelChecker);
			}
			return completionForExpression.doCompleteExpression(completionRequest, expression, nodeExpression, template,
					offset, completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Text) {
			// The completion is triggered in text node (before node)
			Section parent = node.getParentSection();
			if (parent != null && (parent.isInEndTagName(offset))) {
				// The completion is triggered inside end tag
				return EMPTY_FUTURE_COMPLETION;
			}
			// The completion is triggered in text node

			// Check if completion is triggered after a start bracket character and if it's
			// a valid expression
			int nbBrackets = 0;
			int bracketOffset = offset - 1;
			char previousChar = text.charAt(bracketOffset);
			if (previousChar == '#') {
				// {#
				bracketOffset--;
			}
			while (bracketOffset >= 0 && text.charAt(bracketOffset) == '{') {
				bracketOffset--;
				nbBrackets++;
			}
			if (nbBrackets > 0) {
				// The completion is triggered after bracket character
				// {| --> valid expression
				// {{| --> invalid expression
				// {{{| --> valid expression

				// or after an hash
				// {#| --> valid section
				// {{#| --> invalid section
				// {{{#| --> valid section

				if (nbBrackets % 2 != 0) {
					// The completion is triggered in text node after bracket '{' character
					return completionForExpression.doCompleteExpression(completionRequest, null, node, template, offset,
							completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
				}
				return EMPTY_FUTURE_COMPLETION;
			}
		} else if (node.getKind() == NodeKind.ParameterDeclaration) {
			return completionsForParameterDeclaration.doCollectJavaClassesSuggestions((ParameterDeclaration) node,
					template, offset, completionSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Section) {
			// {#|}
			return completionForTagSection.doCompleteTagSection(completionRequest, completionSettings,
					formattingSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Parameter) {
			Parameter parameter = (Parameter) node;
			if (isJavaModelCompletionAllowed(parameter, offset)) {
				// {# let name=|
				return completionForExpression.doCompleteExpression(completionRequest, null, null, template, offset,
						completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
			} else if (Section.isIncludeSection(parameter.getOwnerSection())) {
				// {#include ba|se }
				return completionForTemplateIds.doCompleteTemplateId(completionRequest,
						completionSettings, formattingSettings, cancelChecker);
			}
		}
		return collectSnippetSuggestions(completionRequest);
	}

	/**
	 * Returns true if completion is allowed at the current offset and section.
	 *
	 * @param parameter the parameter.
	 * @param offset    the offset.
	 *
	 * @return true if completion is allowed at the current offset and section.
	 */
	public boolean isJavaModelCompletionAllowed(Parameter parameter, int offset) {
		if (Section.isCaseSection(parameter.getOwnerSection())) {
			// {#case O|FF}
			return true;
		}
		if (parameter.isAfterAssign(offset)) {
			// {#let name=va|lue}
			return true;
		}
		return false;
	}

	private CompletableFuture<CompletionList> collectSnippetSuggestions(CompletionRequest completionRequest) {
		Template template = completionRequest.getTemplate();
		QuteProject project = template.getProject();
		Set<CompletionItem> completionItems = new HashSet<>();
		if (project != null) {
			project.collectUserTagSuggestions(completionRequest, "", null, completionItems);
		}
		completionsForSnippets.collectSnippetSuggestions(completionRequest, "", null, completionItems);
		CompletionList list = new CompletionList();
		list.setItems(completionItems.stream().collect(Collectors.toList()));
		return CompletableFuture.completedFuture(list);
	}

	public String doTagComplete(Template template, Position position, CancelChecker cancelChecker) {
		int offset = -1;
		try {
			offset = template.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of doTagComplete failed", e);
			return null;
		}
		if (offset <= 0) {
			return null;
		}
		char c = template.getText().charAt(offset - 1);
		if (c == '}') {
			Node node = template.findNodeBefore(offset);
			if (node == null || node.getKind() != NodeKind.Section) {
				return null;
			}
			Section section = (Section) node;
			if (section.hasTag() && section.getStart() < offset
					&& (!section.isEndTagClosed() || section.getEndTagCloseOffset() > offset)) {
				Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(template.getText(),
						node.getStart());
				TokenType token = scanner.scan();
				while (token != TokenType.EOS && scanner.getTokenEnd() <= offset) {
					cancelChecker.checkCanceled();
					if (token == TokenType.StartTagClose && scanner.getTokenEnd() == offset) {
						StringBuilder closedTag = new StringBuilder("$0");
						closedTag.append("{/");
						closedTag.append(section.getTag());
						closedTag.append("}");
						return closedTag.toString();
					}
					token = scanner.scan();
				}
			}
		} else if (c == '/') {
			Node node = template.findNodeBefore(offset);
			if (node == null || node.getKind() != NodeKind.Section) {
				return null;
			}
			Section section = (Section) node;
			while (section != null && section.isClosed()
					&& !(section.hasEndTag() && (section.getEndTagOpenOffset() > offset))) {
				section = section.getParentSection();
			}
			if (section != null && section.hasTag()) {
				Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(template.getText(),
						node.getStart());
				TokenType token = scanner.scan();
				while (token != TokenType.EOS && scanner.getTokenEnd() <= offset) {
					if (token == TokenType.EndTagOpen && scanner.getTokenEnd() == offset) {
						StringBuilder closedTag = new StringBuilder("$");
						closedTag.append(section.getTag());
						closedTag.append("}");
						return closedTag.toString();
					}
					token = scanner.scan();
				}
			}
		}
		return null;
	}
}
