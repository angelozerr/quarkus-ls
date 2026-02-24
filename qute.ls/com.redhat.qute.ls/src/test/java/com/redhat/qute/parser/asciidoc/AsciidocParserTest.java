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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AsciidocParser}.
 */
public class AsciidocParserTest {

	@Test
	public void testParseDocumentTitle() {
		String content = "= My Document";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocSection, child.getKind());
		
		AsciidocSection section = (AsciidocSection) child;
		assertEquals(1, section.getLevel());
	}

	@Test
	public void testParseSections() {
		String content = "= Document\n\n== Section 1\n\n=== Subsection 1.1\n\n== Section 2";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
		
		// First child should be document title (level 1)
		AsciidocNode firstChild = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocSection, firstChild.getKind());
		assertEquals(1, ((AsciidocSection) firstChild).getLevel());
	}

	@Test
	public void testParseParagraph() {
		String content = "This is a simple paragraph.";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocParagraph, child.getKind());
		
		AsciidocParagraph paragraph = (AsciidocParagraph) child;
		assertTrue(paragraph.getChildCount() > 0);
	}

	@Test
	public void testParseUnorderedList() {
		String content = "* Item 1\n* Item 2\n* Item 3";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocList, child.getKind());
		
		AsciidocList list = (AsciidocList) child;
		assertEquals(false, list.isOrdered());
		assertEquals(3, list.getChildCount());
		
		for (int i = 0; i < 3; i++) {
			assertEquals(AsciidocNodeKind.AsciidocListItem, list.getChild(i).getKind());
		}
	}

	@Test
	public void testParseOrderedList() {
		String content = ". First\n. Second\n. Third";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocList, child.getKind());
		
		AsciidocList list = (AsciidocList) child;
		assertEquals(true, list.isOrdered());
		assertEquals(3, list.getChildCount());
	}

	@Test
	public void testParseNestedLists() {
		String content = "* Level 1\n** Level 2\n*** Level 3\n* Back to Level 1";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocList, child.getKind());
	}

	@Test
	public void testParseBlockDelimiter() {
		String content = "----\nCode block content\n----";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocBlock, child.getKind());
		
		AsciidocBlock block = (AsciidocBlock) child;
		assertEquals("listing", block.getBlockType());
		assertTrue(block.isClosed());
	}

	@Test
	public void testParseTable() {
		String content = "|===\n|Cell 1|Cell 2\n|Cell 3|Cell 4\n|===";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(1, doc.getChildCount());
		
		AsciidocNode child = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocTable, child.getKind());
		
		AsciidocTable table = (AsciidocTable) child;
		assertTrue(table.isClosed());
	}

	@Test
	public void testParseComment() {
		String content = "// This is a comment\nNormal text";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() >= 2);
		
		AsciidocNode firstChild = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocComment, firstChild.getKind());
	}

	@Test
	public void testParseBlankLineSeparatesParagraphs() {
		String content = "First paragraph.\n\nSecond paragraph.";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(2, doc.getChildCount());
		
		assertEquals(AsciidocNodeKind.AsciidocParagraph, doc.getChild(0).getKind());
		assertEquals(AsciidocNodeKind.AsciidocParagraph, doc.getChild(1).getKind());
	}

	@Test
	public void testParseComplexDocument() {
		String content = "= Document Title\n\n" +
				"== Introduction\n\n" +
				"This is the introduction paragraph.\n\n" +
				"== Features\n\n" +
				"* Feature 1\n" +
				"* Feature 2\n" +
				"* Feature 3\n\n" +
				"== Code Example\n\n" +
				"----\n" +
				"public class Hello {\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(\"Hello\");\n" +
				"    }\n" +
				"}\n" +
				"----\n\n" +
				"== Conclusion\n\n" +
				"Final thoughts.";
		
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
		
		// Verify document structure
		AsciidocNode firstChild = doc.getChild(0);
		assertEquals(AsciidocNodeKind.AsciidocSection, firstChild.getKind());
		
		AsciidocSection titleSection = (AsciidocSection) firstChild;
		assertEquals(1, titleSection.getLevel());
	}

	@Test
	public void testParseSectionHierarchy() {
		String content = "= Title\n\n" +
				"== Level 2\n\n" +
				"Content for level 2.\n\n" +
				"=== Level 3\n\n" +
				"Content for level 3.\n\n" +
				"==== Level 4\n\n" +
				"Content for level 4.\n\n" +
				"== Another Level 2\n\n" +
				"More content.";
		
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
		
		// Verify we have sections
		int sectionCount = 0;
		for (AsciidocNode child : doc.getChildren()) {
			if (child.getKind() == AsciidocNodeKind.AsciidocSection) {
				sectionCount++;
			}
		}
		assertTrue(sectionCount > 0);
	}

	@Test
	public void testParseListWithParagraphs() {
		String content = "* First item\n\nParagraph after list.\n\n* Second item";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() >= 2);
	}

	@Test
	public void testParseMultipleBlocks() {
		String content = "----\nFirst block\n----\n\n" +
				"Text between blocks.\n\n" +
				"====\nSecond block\n====";
		
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() >= 3);
		
		// Count blocks
		int blockCount = 0;
		for (AsciidocNode child : doc.getChildren()) {
			if (child.getKind() == AsciidocNodeKind.AsciidocBlock) {
				blockCount++;
			}
		}
		assertEquals(2, blockCount);
	}

	@Test
	public void testParseEmptyDocument() {
		String content = "";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(0, doc.getChildCount());
	}

	@Test
	public void testParseOnlyWhitespace() {
		String content = "   \n\n   \n";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		// Should handle gracefully
	}

	@Test
	public void testParseWithAdmonition() {
		String content = "NOTE: This is important information.";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
	}

	@Test
	public void testVisitorPattern() {
		String content = "= Title\n\n== Section\n\nParagraph text.";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		final int[] sectionCount = {0};
		final int[] paragraphCount = {0};
		
		doc.accept(new AsciidocASTVisitor() {
			@Override
			public boolean visit(AsciidocSection node) {
				sectionCount[0]++;
				return true;
			}
			
			@Override
			public boolean visit(AsciidocParagraph node) {
				paragraphCount[0]++;
				return true;
			}
		});
		
		assertTrue(sectionCount[0] > 0, "Should have at least one section");
		assertTrue(paragraphCount[0] > 0, "Should have at least one paragraph");
	}

	@Test
	public void testGetText() {
		String content = "Simple text";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(content, doc.getText());
	}

	@Test
	public void testNodePositions() {
		String content = "= Title\n\nParagraph.";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		assertNotNull(doc);
		assertEquals(0, doc.getStart());
		assertEquals(content.length(), doc.getEnd());
		
		if (doc.getChildCount() > 0) {
			AsciidocNode firstChild = doc.getChild(0);
			assertTrue(firstChild.getStart() >= 0);
			assertTrue(firstChild.getEnd() <= content.length());
			assertTrue(firstChild.getStart() < firstChild.getEnd());
		}
	}

	@Test
	public void testFaultTolerance() {
		// Test with malformed content
		String content = "= Title\n\n* Unclosed list\n----\nUnclosed block";
		AsciidocDocument doc = AsciidocParser.parse(content, "test.adoc");
		
		// Should parse without throwing exception
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0);
	}
}

// Made with Bob
