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
 * Represents an AsciiDoc section (heading with content).
 */
public class AsciidocSection extends AsciidocNode {

	private int level; // 1-6 for =, ==, ===, etc.
	private String title;

	public AsciidocSection(int start, int end, int level) {
		super(start, end);
		this.level = level;
	}

	@Override
	public AsciidocNodeKind getKind() {
		return AsciidocNodeKind.AsciidocSection;
	}

	@Override
	public String getNodeName() {
		return "section";
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
