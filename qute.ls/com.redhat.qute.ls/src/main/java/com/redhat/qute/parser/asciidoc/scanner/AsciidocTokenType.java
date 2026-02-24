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
package com.redhat.qute.parser.asciidoc.scanner;

/**
 * AsciiDoc token types.
 */
public enum AsciidocTokenType {

	// Document structure
	DocumentTitle,        // = Title
	SectionTitle,         // ==, ===, ====, =====, ======
	
	// Block delimiters
	BlockDelimiter,       // ----, ****, ====, ...., ____, ++++, ////
	
	// Lists
	UnorderedListItem,    // *, **, ***, ****, *****
	OrderedListItem,      // ., .., ..., ...., .....
	DescriptionListTerm,  // term::
	DescriptionListDesc,  // description after ::
	
	// Inline formatting
	Bold,                 // *text* or **text**
	Italic,               // _text_ or __text__
	Monospace,            // `text` or ``text``
	Superscript,          // ^text^
	Subscript,            // ~text~
	
	// Links and references
	Link,                 // http://example.com or link:url[text]
	CrossReference,       // <<id>> or <<id,text>>
	Anchor,               // [[id]]
	
	// Attributes
	AttributeEntry,       // :attr-name: value
	AttributeReference,   // {attr-name}
	
	// Blocks
	BlockMacro,           // image::path[attrs]
	InlineMacro,          // kbd:[key] or btn:[button]
	
	// Comments
	LineComment,          // //
	BlockComment,         // ////
	
	// Tables
	TableDelimiter,       // |===
	TableCell,            // |
	
	// Paragraphs and text
	Paragraph,
	Text,
	Whitespace,
	Newline,
	BlankLine,
	
	// Admonitions
	Admonition,           // NOTE:, TIP:, IMPORTANT:, WARNING:, CAUTION:
	
	// Include
	Include,              // include::file.adoc[]
	
	// Passthrough
	Passthrough,          // ++++, +++, pass:[]
	
	// Other
	Unknown,
	EOS;
}

// Made with Bob
