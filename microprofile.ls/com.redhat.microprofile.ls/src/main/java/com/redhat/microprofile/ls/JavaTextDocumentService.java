/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.JavaSnippetContext;
import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.commons.MicroProfileJavaCompletionParams;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.ls.commons.TextDocuments;
import com.redhat.microprofile.ls.commons.client.CommandKind;
import com.redhat.microprofile.ls.commons.snippets.Snippet;
import com.redhat.microprofile.ls.commons.snippets.TextDocumentSnippetRegistry;
import com.redhat.microprofile.settings.MicroProfileCodeLensSettings;
import com.redhat.microprofile.settings.SharedSettings;
import com.redhat.microprofile.snippets.LanguageId;
import com.redhat.microprofile.snippets.SnippetContextForJava;

/**
 * LSP text document service for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentService extends AbstractTextDocumentService {

	private static final Logger LOGGER = Logger.getLogger(JavaTextDocumentService.class.getName());

	private final MicroProfileLanguageServer microprofileLanguageServer;
	private final SharedSettings sharedSettings;

	private final TextDocuments<?> documents;

	private TextDocumentSnippetRegistry snippetRegistry;

	public JavaTextDocumentService(MicroProfileLanguageServer microprofileLanguageServer,
			SharedSettings sharedSettings) {
		this.microprofileLanguageServer = microprofileLanguageServer;
		this.sharedSettings = sharedSettings;
		this.documents = new TextDocuments<>();
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		documents.onDidOpenTextDocument(params);
		String uri = params.getTextDocument().getUri();
		triggerValidationFor(Arrays.asList(uri));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		documents.onDidChangeTextDocument(params);
		String uri = params.getTextDocument().getUri();
		triggerValidationFor(Arrays.asList(uri));
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		String uri = params.getTextDocument().getUri();
		microprofileLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		triggerValidationFor(documents.all().stream().map(TextDocument::getUri).collect(Collectors.toList()));
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		MicroProfileJavaCompletionParams javaParams = new MicroProfileJavaCompletionParams(
				params.getTextDocument().getUri(), params.getPosition());
		List<Snippet> snippets = getSnippetRegistry().getSnippets();
		List<JavaSnippetContext> contexts = snippets.stream().map(snippet -> (JavaSnippetContext) snippet.getContext())
				.collect(Collectors.toList());
		javaParams.setContexts(contexts);

		return microprofileLanguageServer.getLanguageClient().getJavaCompletion(javaParams). //
				thenApply(result -> {
					try {
						List<Boolean> resolved = result.getResolvedContexts();

						// Returns java snippets
						TextDocument document = documents.get(params.getTextDocument().getUri());
						int completionOffset = document.offsetAt(params.getPosition());
						boolean canSupportMarkdown = true;
						CompletionList list = new CompletionList();
						list.setItems(new ArrayList<>());

						AtomicInteger i = new AtomicInteger();
						getSnippetRegistry()
								.getCompletionItems(document, completionOffset, canSupportMarkdown, context -> {
									int j = i.getAndIncrement();
									if (context == null || resolved == null) {
										return true;
									}
									if (context instanceof SnippetContextForJava) {
										return ((SnippetContextForJava) context)
												.isMatch(resolved.get(j));
									}
									return false;
								}).forEach(item -> {
									list.getItems().add(item);
								});
						return Either.forRight(list);
					} catch (BadLocationException e) {
						LOGGER.log(Level.SEVERE, "Error while getting java completions", e);
						return Either.forRight(null);
					}
				});
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		boolean urlCodeLensEnabled = sharedSettings.getCodeLensSettings().isUrlCodeLensEnabled();
		if (!urlCodeLensEnabled) {
			// Don't consume JDT LS extension if all code lens are disabled.
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		MicroProfileJavaCodeLensParams javaParams = new MicroProfileJavaCodeLensParams(
				params.getTextDocument().getUri());
		if (sharedSettings.getCommandCapabilities().isCommandSupported(CommandKind.COMMAND_OPEN_URI)) {
			javaParams.setOpenURICommand(CommandKind.COMMAND_OPEN_URI);
		}
		javaParams.setCheckServerAvailable(true);
		javaParams.setUrlCodeLensEnabled(urlCodeLensEnabled);
		// javaParams.setLocalServerPort(8080); // TODO : manage this server port from
		// the settings
		return microprofileLanguageServer.getLanguageClient().getJavaCodelens(javaParams);
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		MicroProfileJavaCodeActionParams javaParams = new MicroProfileJavaCodeActionParams();
		javaParams.setTextDocument(params.getTextDocument());
		javaParams.setRange(params.getRange());
		javaParams.setContext(params.getContext());
		javaParams.setResourceOperationSupported(microprofileLanguageServer.getCapabilityManager()
				.getClientCapabilities().isResourceOperationSupported());
		return microprofileLanguageServer.getLanguageClient().getJavaCodeAction(javaParams). //
				thenApply(codeActions -> {
					return codeActions.stream() //
							.map(ca -> {
								Either<Command, CodeAction> e = Either.forRight(ca);
								return e;
							}) //
							.collect(Collectors.toList());
				});
	}

	public void updateCodeLensSettings(MicroProfileCodeLensSettings newCodeLens) {
		sharedSettings.getCodeLensSettings().setUrlCodeLensEnabled(newCodeLens.isUrlCodeLensEnabled());
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
		DocumentFormat documentFormat = markdownSupported ? DocumentFormat.Markdown : DocumentFormat.PlainText;
		MicroProfileJavaHoverParams javaParams = new MicroProfileJavaHoverParams(params.getTextDocument().getUri(),
				params.getPosition(), documentFormat);
		return microprofileLanguageServer.getLanguageClient().getJavaHover(javaParams);
	}

	private void triggerValidationFor(List<String> uris) {
		MicroProfileJavaDiagnosticsParams javaParams = new MicroProfileJavaDiagnosticsParams(uris);
		boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
		if (markdownSupported) {
			javaParams.setDocumentFormat(DocumentFormat.Markdown);
		}
		microprofileLanguageServer.getLanguageClient().getJavaDiagnostics(javaParams). //
				thenApply(diagnostics -> {
					if (diagnostics == null) {
						return null;
					}
					for (PublishDiagnosticsParams diagnostic : diagnostics) {
						microprofileLanguageServer.getLanguageClient().publishDiagnostics(diagnostic);
					}
					return null;
				});
	}

	private TextDocumentSnippetRegistry getSnippetRegistry() {
		if (snippetRegistry == null) {
			snippetRegistry = new TextDocumentSnippetRegistry(LanguageId.java.name());
		}
		return snippetRegistry;
	}
}
