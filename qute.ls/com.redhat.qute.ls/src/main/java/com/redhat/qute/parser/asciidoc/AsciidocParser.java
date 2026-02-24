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

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.asciidoc.scanner.AsciidocScanner;
import com.redhat.qute.parser.asciidoc.scanner.AsciidocScannerState;
import com.redhat.qute.parser.asciidoc.scanner.AsciidocTokenType;
import com.redhat.qute.parser.scanner.Scanner;

/**
 * AsciiDoc parser (fault-tolerant).
 * 
 * This parser builds an Abstract Syntax Tree (AST) from AsciiDoc content.
 */
public class AsciidocParser {

	public static AsciidocDocument parse(String content, String uri) {
		return parse(new TextDocument(content, uri));
	}

	public static AsciidocDocument parse(TextDocument textDocument) {
		return parse(textDocument, 0, textDocument.getText().length(), CancelChecker.NO_CANCELLABLE);
	}

	public static AsciidocDocument parse(TextDocument textDocument, int start, int end, CancelChecker cancelChecker) {
		AsciidocDocument document = new AsciidocDocument(textDocument);
		document.setCancelChecker(cancelChecker);
		document.setStart(start);

		String content = textDocument.getText();
		Scanner<AsciidocTokenType, AsciidocScannerState> scanner = AsciidocScanner.createScanner(content, start, end);

		AsciidocNode curr = document;
		AsciidocSection currentSection = null;
		AsciidocParagraph currentParagraph = null;
		AsciidocList currentList = null;
		AsciidocBlock currentBlock = null;
		AsciidocTable currentTable = null;

		AsciidocTokenType token = scanner.scan();
		while (token != AsciidocTokenType.EOS) {
			cancelChecker.checkCanceled();

			switch (token) {

			case DocumentTitle:
			case SectionTitle: {
				// Close any open paragraph or list
				if (currentParagraph != null) {
					currentParagraph.setEnd(scanner.getTokenOffset());
					currentParagraph.setClosed(true);
					currentParagraph = null;
				}
				if (currentList != null) {
					currentList.setEnd(scanner.getTokenOffset());
					currentList.setClosed(true);
					currentList = null;
				}

				// Determine section level (count '=' characters)
				String tokenText = scanner.getTokenText();
				int level = 0;
				for (char c : tokenText.toCharArray()) {
					if (c == '=') {
						level++;
					} else {
						break;
					}
				}

				// Create section
				AsciidocSection section = new AsciidocSection(scanner.getTokenOffset(), scanner.getTokenEnd(), level);
				
				// Find appropriate parent based on level
				if (level == 1) {
					// Document title - add to document
					document.addChild(section);
					curr = section;
				} else {
					// Find parent section with lower level
					AsciidocNode parent = curr;
					while (parent != null && parent != document) {
						if (parent instanceof AsciidocSection) {
							AsciidocSection parentSection = (AsciidocSection) parent;
							if (parentSection.getLevel() < level) {
								break;
							}
						}
						parent = parent.getParent();
					}
					
					if (parent == null) {
						parent = document;
					}
					
					parent.addChild(section);
					curr = section;
				}
				
				currentSection = section;
				break;
			}

			case UnorderedListItem:
			case OrderedListItem: {
				// Close any open paragraph
				if (currentParagraph != null) {
					currentParagraph.setEnd(scanner.getTokenOffset());
					currentParagraph.setClosed(true);
					currentParagraph = null;
				}

				boolean ordered = (token == AsciidocTokenType.OrderedListItem);

				// Create or reuse list
				if (currentList == null || currentList.isOrdered() != ordered) {
					if (currentList != null) {
						currentList.setEnd(scanner.getTokenOffset());
						currentList.setClosed(true);
					}
					currentList = new AsciidocList(scanner.getTokenOffset(), scanner.getTokenEnd(), ordered);
					curr.addChild(currentList);
				}

				// Create list item
				AsciidocListItem item = new AsciidocListItem(scanner.getTokenOffset(), scanner.getTokenEnd());
				currentList.addChild(item);
				break;
			}

			case BlockDelimiter: {
				// Close any open paragraph or list
				if (currentParagraph != null) {
					currentParagraph.setEnd(scanner.getTokenOffset());
					currentParagraph.setClosed(true);
					currentParagraph = null;
				}
				if (currentList != null) {
					currentList.setEnd(scanner.getTokenOffset());
					currentList.setClosed(true);
					currentList = null;
				}

				if (currentBlock == null) {
					// Opening delimiter
					String delimiter = scanner.getTokenText();
					String blockType = getBlockType(delimiter);
					currentBlock = new AsciidocBlock(scanner.getTokenOffset(), scanner.getTokenEnd(), blockType);
					curr.addChild(currentBlock);
				} else {
					// Closing delimiter
					currentBlock.setEnd(scanner.getTokenEnd());
					currentBlock.setClosed(true);
					currentBlock = null;
				}
				break;
			}

			case TableDelimiter: {
				// Close any open paragraph or list
				if (currentParagraph != null) {
					currentParagraph.setEnd(scanner.getTokenOffset());
					currentParagraph.setClosed(true);
					currentParagraph = null;
				}
				if (currentList != null) {
					currentList.setEnd(scanner.getTokenOffset());
					currentList.setClosed(true);
					currentList = null;
				}

				if (currentTable == null) {
					// Opening table
					currentTable = new AsciidocTable(scanner.getTokenOffset(), scanner.getTokenEnd());
					curr.addChild(currentTable);
				} else {
					// Closing table
					currentTable.setEnd(scanner.getTokenEnd());
					currentTable.setClosed(true);
					currentTable = null;
				}
				break;
			}

			case LineComment: {
				AsciidocComment comment = new AsciidocComment(scanner.getTokenOffset(), scanner.getTokenEnd());
				curr.addChild(comment);
				break;
			}

			case Text: {
				// Add text to current context
				if (currentBlock != null) {
					AsciidocText text = new AsciidocText(scanner.getTokenOffset(), scanner.getTokenEnd());
					currentBlock.addChild(text);
				} else if (currentTable != null) {
					AsciidocText text = new AsciidocText(scanner.getTokenOffset(), scanner.getTokenEnd());
					currentTable.addChild(text);
				} else if (currentList != null && currentList.getChildCount() > 0) {
					// Add to last list item
					AsciidocNode lastItem = currentList.getChild(currentList.getChildCount() - 1);
					AsciidocText text = new AsciidocText(scanner.getTokenOffset(), scanner.getTokenEnd());
					lastItem.addChild(text);
				} else {
					// Regular paragraph text
					if (currentParagraph == null) {
						currentParagraph = new AsciidocParagraph(scanner.getTokenOffset(), scanner.getTokenEnd());
						curr.addChild(currentParagraph);
					}
					AsciidocText text = new AsciidocText(scanner.getTokenOffset(), scanner.getTokenEnd());
					currentParagraph.addChild(text);
					currentParagraph.setEnd(scanner.getTokenEnd());
				}
				break;
			}

			case BlankLine: {
				// Close current paragraph on blank line
				if (currentParagraph != null) {
					currentParagraph.setEnd(scanner.getTokenOffset());
					currentParagraph.setClosed(true);
					currentParagraph = null;
				}
				break;
			}

			case Newline:
			case Whitespace:
				// Ignore whitespace tokens
				break;

			default:
				// Handle other tokens (formatting, links, etc.) as text for now
				if (currentParagraph == null && currentBlock == null && currentTable == null) {
					currentParagraph = new AsciidocParagraph(scanner.getTokenOffset(), scanner.getTokenEnd());
					curr.addChild(currentParagraph);
				}
				break;
			}

			token = scanner.scan();
		}

		// Close any remaining open nodes
		if (currentParagraph != null) {
			currentParagraph.setEnd(end);
			currentParagraph.setClosed(true);
		}
		if (currentList != null) {
			currentList.setEnd(end);
			currentList.setClosed(true);
		}
		if (currentBlock != null) {
			currentBlock.setEnd(end);
			currentBlock.setClosed(true);
		}
		if (currentTable != null) {
			currentTable.setEnd(end);
			currentTable.setClosed(true);
		}
		if (currentSection != null) {
			currentSection.setEnd(end);
			currentSection.setClosed(true);
		}

		return document;
	}

	private static String getBlockType(String delimiter) {
		if (delimiter == null || delimiter.isEmpty()) {
			return "unknown";
		}
		
		char firstChar = delimiter.charAt(0);
		switch (firstChar) {
		case '-':
			return "listing";
		case '*':
			return "sidebar";
		case '=':
			return "example";
		case '.':
			return "literal";
		case '_':
			return "quote";
		case '+':
			return "passthrough";
		case '/':
			return "comment";
		default:
			return "unknown";
		}
	}
}

// Made with Bob
