/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.asciidoc;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;

/**
 * Root node of an AsciiDoc AST.
 */
public class AsciidocDocument extends AsciidocNode {

	private final TextDocument textDocument;
	private CancelChecker cancelChecker;

	public AsciidocDocument(TextDocument textDocument) {
		super(0, textDocument.getText().length());
		this.textDocument = textDocument;
		super.setClosed(true);
	}

	@Override
	public AsciidocNodeKind getKind() {
		return AsciidocNodeKind.AsciidocDocument;
	}

	@Override
	public String getNodeName() {
		return "#asciidoc-document";
	}

	@Override
	public AsciidocDocument getOwnerDocument() {
		return this;
	}

	public String getUri() {
		return textDocument.getUri();
	}

	public String getText() {
		return textDocument.getText();
	}

	public TextDocument getTextDocument() {
		return textDocument;
	}

	public String getText(int start, int end) {
		String text = getText();
		return text.substring(start, end);
	}

	public void setCancelChecker(CancelChecker cancelChecker) {
		this.cancelChecker = cancelChecker;
	}

	public CancelChecker getCancelChecker() {
		return cancelChecker;
	}

	public Position positionAt(int offset) throws BadLocationException {
		checkCanceled();
		return textDocument.positionAt(offset);
	}

	public int offsetAt(Position position) throws BadLocationException {
		checkCanceled();
		return textDocument.offsetAt(position);
	}

	public void checkCanceled() {
		if (cancelChecker != null) {
			cancelChecker.checkCanceled();
		}
	}

	@Override
	protected void accept0(AsciidocASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}
}

// Made with Bob
