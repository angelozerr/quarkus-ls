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
 * AsciiDoc scanner states.
 * Represents the current scanning context.
 */
public enum AsciidocScannerState {
	WithinContent,           // Normal content scanning
	WithinTitle,             // After = or == etc.
	WithinAttributeEntry,    // After :attr-name:
	WithinBlockDelimiter,    // Inside ----, ****, etc.
	WithinListItem,          // After *, ., etc.
	WithinDescriptionTerm,   // Before ::
	WithinLineComment,       // After //
	WithinBlockComment,      // Inside ////
	WithinInlineFormatting,  // Inside *bold*, _italic_, etc.
	WithinLink,              // Inside link:[] or http://
	WithinMacro,             // Inside macro::[]
	WithinTable,             // Inside |===
	WithinAdmonition,        // After NOTE:, TIP:, etc.
	WithinPassthrough,       // Inside ++++
	AfterNewline;            // Just after newline (to detect block structures)
}

// Made with Bob
