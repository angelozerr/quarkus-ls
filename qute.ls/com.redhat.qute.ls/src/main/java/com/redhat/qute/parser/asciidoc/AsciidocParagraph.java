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

/**
 * Represents an AsciiDoc paragraph.
 */
public class AsciidocParagraph extends AsciidocNode {

	public AsciidocParagraph(int start, int end) {
		super(start, end);
	}

	@Override
	public AsciidocNodeKind getKind() {
		return AsciidocNodeKind.AsciidocParagraph;
	}

	@Override
	public String getNodeName() {
		return "paragraph";
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
