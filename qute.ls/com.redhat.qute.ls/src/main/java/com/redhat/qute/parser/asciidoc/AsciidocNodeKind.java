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
 * AsciiDoc AST node kinds.
 */
public enum AsciidocNodeKind {
	AsciidocDocument,
	AsciidocSection,
	AsciidocParagraph,
	AsciidocList,
	AsciidocListItem,
	AsciidocDescriptionList,
	AsciidocDescriptionListEntry,
	AsciidocTable,
	AsciidocTableRow,
	AsciidocTableCell,
	AsciidocBlock,
	AsciidocText,
	AsciidocInlineFormatting,
	AsciidocLink,
	AsciidocAnchor,
	AsciidocCrossReference,
	AsciidocAttribute,
	AsciidocAttributeReference,
	AsciidocMacro,
	AsciidocComment,
	AsciidocAdmonition;
}

// Made with Bob
