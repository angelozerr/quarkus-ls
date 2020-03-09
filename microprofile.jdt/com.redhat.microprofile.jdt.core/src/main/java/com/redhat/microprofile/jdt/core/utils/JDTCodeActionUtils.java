package com.redhat.microprofile.jdt.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class JDTCodeActionUtils {

	public static CodeAction insertAnnotations(String title, String uri, ITypeRoot typeRoot, Diagnostic diagnostic,
			IJDTUtils utils, String... annotations) throws JavaModelException {
		Position position = diagnostic.getRange().getStart();
		IJavaElement element = PositionUtils.getJavaElementAt(typeRoot, position, utils);
		if (element == null) {
			return null;
		}
		return null;
	}

	/**
	 * Create a CodeAction to insert a new content at the end of the given range.
	 * 
	 * @param title
	 * @param range
	 * @param insertText
	 * @param uri
	 * @param diagnostic
	 * @return
	 */
	public static CodeAction insert(String title, Position position, String insertText, String uri,
			Diagnostic diagnostic) {
		return insert(title, position, insertText, uri, Arrays.asList(diagnostic));
	}

	/**
	 * Create a CodeAction to insert a new content at the end of the given range.
	 * 
	 * @param title
	 * @param range
	 * @param insertText
	 * @param document
	 * @param diagnostics
	 * @return
	 */
	public static CodeAction insert(String title, Position position, String insertText, String uri,
			List<Diagnostic> diagnostics) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(diagnostics);
		TextEdit edit = new TextEdit(new Range(position, position), insertText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(uri, 0);

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier,
				Collections.singletonList(edit));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}

}
