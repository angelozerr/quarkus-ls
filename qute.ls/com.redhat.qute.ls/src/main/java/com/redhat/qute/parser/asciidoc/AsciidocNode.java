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

import java.util.List;

import com.redhat.qute.parser.NodeBase;

/**
 * Base class for all AsciiDoc AST nodes.
 */
public abstract class AsciidocNode extends NodeBase<AsciidocNode> {

	public AsciidocNode(int start, int end) {
		super(start, end);
	}

	public AsciidocDocument getOwnerDocument() {
		AsciidocNode node = getParent();
		while (node != null) {
			if (node.getKind() == AsciidocNodeKind.AsciidocDocument) {
				return (AsciidocDocument) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public String getText() {
		AsciidocDocument doc = getOwnerDocument();
		if (doc == null) {
			return null;
		}
		return doc.getText(getStart(), getEnd());
	}

	@Override
	protected void setParent(AsciidocNode parent) {
		super.setParent(parent);
	}

	@Override
	protected void addChild(AsciidocNode child) {
		super.addChild(child);
	}

	public void setStart(int start) {
		super.setStart(start);
	}

	public void setEnd(int end) {
		super.setEnd(end);
	}

	public void setClosed(boolean closed) {
		super.setClosed(closed);
	}

	public final void accept(AsciidocASTVisitor visitor) {
		if (visitor == null) {
			return;
		}
		visitor.preVisit(this);
		accept0(visitor);
		visitor.postVisit(this);
	}

	protected abstract void accept0(AsciidocASTVisitor visitor);

	protected final void acceptChild(AsciidocASTVisitor visitor, AsciidocNode child) {
		if (child == null) {
			return;
		}
		child.accept(visitor);
	}

	protected final void acceptChildren(AsciidocASTVisitor visitor, List<AsciidocNode> children) {
		for (AsciidocNode child : children) {
			child.accept(visitor);
		}
	}

	public abstract AsciidocNodeKind getKind();
}

// Made with Bob
