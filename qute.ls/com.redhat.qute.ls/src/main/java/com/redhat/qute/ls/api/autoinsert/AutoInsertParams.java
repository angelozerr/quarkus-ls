package com.redhat.qute.ls.api.autoinsert;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class AutoInsertParams {

	/**
	 * The auto insert kind
	 */
	private AutoInsertKind kind;
	/**
	 * The text document.
	 */
	private TextDocumentIdentifier textDocument;
	/**
	 * The position inside the text document.
	 */
	private Position position;

	public AutoInsertKind getKind() {
		return kind;
	}

	public void setKind(AutoInsertKind kind) {
		this.kind = kind;
	}

	public TextDocumentIdentifier getTextDocument() {
		return textDocument;
	}

	public void setTextDocument(TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
