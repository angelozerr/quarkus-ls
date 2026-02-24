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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Integration tests for AsciiDoc parser using real sample files.
 */
public class AsciidocIntegrationTest {

	@Test
	public void testParseSampleDocument() throws IOException {
		String content = loadResource("/asciidoc/sample.adoc");
		assertNotNull(content);
		assertTrue(content.length() > 0);
		
		AsciidocDocument doc = AsciidocParser.parse(content, "sample.adoc");
		
		assertNotNull(doc);
		assertTrue(doc.getChildCount() > 0, "Document should have children");
		
		// Verify document structure
		System.out.println("Document has " + doc.getChildCount() + " top-level children");
		
		// Count different node types
		NodeCounter counter = new NodeCounter();
		doc.accept(counter);
		
		System.out.println("Statistics:");
		System.out.println("  Sections: " + counter.sectionCount);
		System.out.println("  Paragraphs: " + counter.paragraphCount);
		System.out.println("  Lists: " + counter.listCount);
		System.out.println("  List Items: " + counter.listItemCount);
		System.out.println("  Blocks: " + counter.blockCount);
		System.out.println("  Tables: " + counter.tableCount);
		System.out.println("  Comments: " + counter.commentCount);
		System.out.println("  Text Nodes: " + counter.textCount);
		
		// Assertions
		assertTrue(counter.sectionCount > 0, "Should have sections");
		assertTrue(counter.paragraphCount > 0, "Should have paragraphs");
		assertTrue(counter.listCount > 0, "Should have lists");
		assertTrue(counter.blockCount > 0, "Should have blocks");
		assertTrue(counter.tableCount > 0, "Should have tables");
	}

	@Test
	public void testVisitAllNodes() throws IOException {
		String content = loadResource("/asciidoc/sample.adoc");
		AsciidocDocument doc = AsciidocParser.parse(content, "sample.adoc");
		
		// Visit all nodes and verify structure
		doc.accept(new AsciidocASTVisitor() {
			private int depth = 0;
			
			@Override
			public void preVisit(AsciidocNode node) {
				depth++;
			}
			
			@Override
			public void postVisit(AsciidocNode node) {
				depth--;
			}
			
			@Override
			public boolean visit(AsciidocDocument node) {
				assertNotNull(node.getUri());
				assertEquals(0, depth - 1); // Document should be at root
				return true;
			}
			
			@Override
			public boolean visit(AsciidocSection node) {
				assertTrue(node.getLevel() >= 1 && node.getLevel() <= 6);
				return true;
			}
			
			@Override
			public boolean visit(AsciidocList node) {
				assertTrue(node.getChildCount() > 0, "List should have items");
				return true;
			}
			
			@Override
			public boolean visit(AsciidocBlock node) {
				assertNotNull(node.getBlockType());
				return true;
			}
		});
	}

	@Test
	public void testExtractSectionTitles() throws IOException {
		String content = loadResource("/asciidoc/sample.adoc");
		AsciidocDocument doc = AsciidocParser.parse(content, "sample.adoc");
		
		SectionTitleExtractor extractor = new SectionTitleExtractor();
		doc.accept(extractor);
		
		System.out.println("\nExtracted Section Titles:");
		for (String title : extractor.titles) {
			System.out.println("  - " + title);
		}
		
		assertTrue(extractor.titles.size() > 0, "Should extract section titles");
	}

	@Test
	public void testNodePositions() throws IOException {
		String content = loadResource("/asciidoc/sample.adoc");
		AsciidocDocument doc = AsciidocParser.parse(content, "sample.adoc");
		
		// Verify all nodes have valid positions
		doc.accept(new AsciidocASTVisitor() {
			@Override
			public void preVisit(AsciidocNode node) {
				assertTrue(node.getStart() >= 0, "Start position should be non-negative");
				assertTrue(node.getEnd() >= node.getStart(), "End should be >= start");
				assertTrue(node.getEnd() <= content.length(), "End should be within document");
			}
		});
	}

	@Test
	public void testGetTextFromNodes() throws IOException {
		String content = loadResource("/asciidoc/sample.adoc");
		AsciidocDocument doc = AsciidocParser.parse(content, "sample.adoc");
		
		// Verify getText() works for all nodes
		doc.accept(new AsciidocASTVisitor() {
			@Override
			public void preVisit(AsciidocNode node) {
				String text = node.getText();
				if (text != null) {
					assertTrue(text.length() > 0 || node.getStart() == node.getEnd());
				}
			}
		});
	}

	private String loadResource(String path) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				throw new IOException("Resource not found: " + path);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Visitor that counts different node types.
	 */
	private static class NodeCounter extends AsciidocASTVisitor {
		int sectionCount = 0;
		int paragraphCount = 0;
		int listCount = 0;
		int listItemCount = 0;
		int blockCount = 0;
		int tableCount = 0;
		int commentCount = 0;
		int textCount = 0;
		
		@Override
		public boolean visit(AsciidocSection node) {
			sectionCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocParagraph node) {
			paragraphCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocList node) {
			listCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocListItem node) {
			listItemCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocBlock node) {
			blockCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocTable node) {
			tableCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocComment node) {
			commentCount++;
			return true;
		}
		
		@Override
		public boolean visit(AsciidocText node) {
			textCount++;
			return true;
		}
	}

	/**
	 * Visitor that extracts section titles.
	 */
	private static class SectionTitleExtractor extends AsciidocASTVisitor {
		java.util.List<String> titles = new java.util.ArrayList<>();
		
		@Override
		public boolean visit(AsciidocSection node) {
			String indent = "  ".repeat(node.getLevel() - 1);
			String title = node.getTitle();
			if (title == null) {
				title = node.getText();
				if (title != null) {
					// Extract title from text (remove leading = characters)
					title = title.replaceFirst("^=+\\s*", "").trim();
				}
			}
			if (title != null && !title.isEmpty()) {
				titles.add(indent + title);
			}
			return true;
		}
	}
}

// Made with Bob
