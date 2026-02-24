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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Tests for {@link AsciidocScanner}.
 */
public class AsciidocScannerTest {

	@Test
	public void testDocumentTitle() {
		String content = "= My Document Title";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.DocumentTitle, "= ");
		assertToken(scanner, AsciidocTokenType.Text, "My Document Title");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testSectionTitles() {
		String content = "== Section Level 2\n=== Section Level 3";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.SectionTitle, "== ");
		assertToken(scanner, AsciidocTokenType.Text, "Section Level 2");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.SectionTitle, "=== ");
		assertToken(scanner, AsciidocTokenType.Text, "Section Level 3");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testUnorderedList() {
		String content = "* Item 1\n** Item 1.1\n* Item 2";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.UnorderedListItem, "* ");
		assertToken(scanner, AsciidocTokenType.Text, "Item 1");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.UnorderedListItem, "** ");
		assertToken(scanner, AsciidocTokenType.Text, "Item 1.1");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.UnorderedListItem, "* ");
		assertToken(scanner, AsciidocTokenType.Text, "Item 2");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testOrderedList() {
		String content = ". First\n.. Nested\n. Second";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.OrderedListItem, ". ");
		assertToken(scanner, AsciidocTokenType.Text, "First");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.OrderedListItem, ".. ");
		assertToken(scanner, AsciidocTokenType.Text, "Nested");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.OrderedListItem, ". ");
		assertToken(scanner, AsciidocTokenType.Text, "Second");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testBlockDelimiter() {
		String content = "----\nCode block\n----";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.BlockDelimiter, "----");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.Text, "Code block");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlockDelimiter, "----");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testLineComment() {
		String content = "// This is a comment\nNormal text";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.LineComment, "//");
		assertToken(scanner, AsciidocTokenType.Text, " This is a comment");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.Text, "Normal");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "text");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testBoldText() {
		String content = "This is *bold* text";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "This");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "is");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Bold, "*bold*");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "text");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testItalicText() {
		String content = "This is _italic_ text";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "This");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "is");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Italic, "_italic_");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "text");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testMonospaceText() {
		String content = "Use `code` here";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "Use");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Monospace, "`code`");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "here");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testLink() {
		String content = "Visit https://example.com for more";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "Visit");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Link, "https://example.com");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "for");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "more");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testAttributeEntry() {
		String content = ":author: John Doe";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.AttributeEntry, ":author:");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "John Doe");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testAttributeReference() {
		String content = "Hello {name}!";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "Hello");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.AttributeReference, "{name}");
		assertToken(scanner, AsciidocTokenType.Text, "!");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testAdmonition() {
		String content = "NOTE: This is important";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Admonition, "NOTE:");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "This is important");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testTable() {
		String content = "|===\n|Cell 1|Cell 2\n|===";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.TableDelimiter, "|===");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.TableCell, "|");
		assertToken(scanner, AsciidocTokenType.Text, "Cell 1");
		assertToken(scanner, AsciidocTokenType.TableCell, "|");
		assertToken(scanner, AsciidocTokenType.Text, "Cell 2");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.TableDelimiter, "|===");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testAnchor() {
		String content = "[[section-id]]";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Anchor, "[[section-id]]");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testCrossReference() {
		String content = "See <<section-id>>";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "See");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.CrossReference, "<<section-id>>");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testBlankLine() {
		String content = "Line 1\n\nLine 2";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "Line");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "1");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlankLine, "\n");
		assertToken(scanner, AsciidocTokenType.Text, "Line");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "2");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testSuperscript() {
		String content = "E=mc^2^";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "E=mc");
		assertToken(scanner, AsciidocTokenType.Superscript, "^2^");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testSubscript() {
		String content = "H~2~O";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Text, "H");
		assertToken(scanner, AsciidocTokenType.Subscript, "~2~");
		assertToken(scanner, AsciidocTokenType.Text, "O");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testIncludeDirective() {
		String content = "include::chapter1.adoc[]";
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		assertToken(scanner, AsciidocTokenType.Include, "include::chapter1.adoc[]");
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	@Test
	public void testComplexDocument() {
		String content = "= Document Title\n\n" +
				"== Section 1\n\n" +
				"This is a paragraph with *bold* and _italic_ text.\n\n" +
				"* List item 1\n" +
				"* List item 2\n\n" +
				"----\n" +
				"Code block\n" +
				"----";
		
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content);
		
		// Document title
		assertToken(scanner, AsciidocTokenType.DocumentTitle, "= ");
		assertToken(scanner, AsciidocTokenType.Text, "Document Title");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlankLine, "\n");
		
		// Section
		assertToken(scanner, AsciidocTokenType.SectionTitle, "== ");
		assertToken(scanner, AsciidocTokenType.Text, "Section 1");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlankLine, "\n");
		
		// Paragraph with formatting
		assertToken(scanner, AsciidocTokenType.Text, "This");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "is");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "a");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "paragraph");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "with");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Bold, "*bold*");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "and");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Italic, "_italic_");
		assertToken(scanner, AsciidocTokenType.Whitespace, " ");
		assertToken(scanner, AsciidocTokenType.Text, "text.");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlankLine, "\n");
		
		// List
		assertToken(scanner, AsciidocTokenType.UnorderedListItem, "* ");
		assertToken(scanner, AsciidocTokenType.Text, "List item 1");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.UnorderedListItem, "* ");
		assertToken(scanner, AsciidocTokenType.Text, "List item 2");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlankLine, "\n");
		
		// Code block
		assertToken(scanner, AsciidocTokenType.BlockDelimiter, "----");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.Text, "Code block");
		assertToken(scanner, AsciidocTokenType.Newline, "\n");
		assertToken(scanner, AsciidocTokenType.BlockDelimiter, "----");
		
		assertToken(scanner, AsciidocTokenType.EOS, "");
	}

	private void assertToken(Scanner<AsciidocTokenType, AsciidocScannerState> scanner, 
			AsciidocTokenType expectedType, String expectedText) {
		AsciidocTokenType actualType = scanner.scan();
		String actualText = scanner.getTokenText();
		
		assertEquals(expectedType, actualType, 
				"Expected token type " + expectedType + " but got " + actualType + 
				" with text '" + actualText + "'");
		assertEquals(expectedText, actualText, 
				"Expected token text '" + expectedText + "' but got '" + actualText + "'");
	}
}

// Made with Bob
