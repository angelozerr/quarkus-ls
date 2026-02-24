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

import com.redhat.qute.parser.ASTVisitorBase;

/**
 * A visitor for AsciiDoc AST.
 */
public abstract class AsciidocASTVisitor extends ASTVisitorBase<AsciidocNode> {

	public boolean visit(AsciidocDocument node) {
		return true;
	}

	public boolean visit(AsciidocSection node) {
		return true;
	}

	public boolean visit(AsciidocParagraph node) {
		return true;
	}

	public boolean visit(AsciidocList node) {
		return true;
	}

	public boolean visit(AsciidocListItem node) {
		return true;
	}

	public boolean visit(AsciidocBlock node) {
		return true;
	}

	public boolean visit(AsciidocText node) {
		return true;
	}

	public boolean visit(AsciidocTable node) {
		return true;
	}

	public boolean visit(AsciidocComment node) {
		return true;
	}

	public void endVisit(AsciidocDocument node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocSection node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocParagraph node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocList node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocListItem node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocBlock node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocText node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocTable node) {
		// default implementation: do nothing
	}

	public void endVisit(AsciidocComment node) {
		// default implementation: do nothing
	}
}

// Made with Bob
