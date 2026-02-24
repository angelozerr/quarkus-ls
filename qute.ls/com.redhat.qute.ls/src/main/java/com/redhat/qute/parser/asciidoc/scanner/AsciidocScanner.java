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

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;
import com.redhat.qute.parser.scanner.Scanner;

/**
 * AsciiDoc scanner (fault-tolerant).
 */
public class AsciidocScanner extends AbstractScanner<AsciidocTokenType, AsciidocScannerState> {

	private static final int[] NEWLINE_CHARS = new int[] { '\n', '\r' };
	
	private static final String[] ADMONITIONS = {
		"NOTE:", "TIP:", "IMPORTANT:", "WARNING:", "CAUTION:"
	};
	
	private static final Predicate<Integer> TEXT_CHAR_PREDICATE = ch -> {
		return ch != '\n' && ch != '\r' && ch != '*' && ch != '_' && ch != '`' 
			&& ch != '^' && ch != '~' && ch != '[' && ch != ']' && ch != '<' && ch != '>'
			&& ch != '{' && ch != '}' && ch != ':' && ch != '|';
	};

	public static Scanner<AsciidocTokenType, AsciidocScannerState> createScanner(String input) {
		return createScanner(input, 0, input.length());
	}

	public static Scanner<AsciidocTokenType, AsciidocScannerState> createScanner(String input, int initialOffset,
			int endOffset) {
		return createScanner(input, initialOffset, endOffset, AsciidocScannerState.AfterNewline);
	}

	public static Scanner<AsciidocTokenType, AsciidocScannerState> createScanner(String input, int initialOffset, 
			int endOffset, AsciidocScannerState initialState) {
		return new AsciidocScanner(input, initialOffset, endOffset, initialState);
	}

	private boolean atLineStart = true;
	private int blockDelimiterCount = 0;
	private char blockDelimiterChar = 0;

	AsciidocScanner(String input, int initialOffset, int endOffset, AsciidocScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, AsciidocTokenType.Unknown, AsciidocTokenType.EOS);
	}

	@Override
	protected AsciidocTokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, AsciidocTokenType.EOS);
		}

		switch (state) {

		case AfterNewline:
		case WithinContent: {
			// Handle newlines
			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
					stream.advance(1);
				}
				atLineStart = true;
				
				// Check for blank line (two consecutive newlines)
				if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
					state = AsciidocScannerState.AfterNewline;
					return finishToken(offset, AsciidocTokenType.BlankLine);
				}
				
				state = AsciidocScannerState.AfterNewline;
				return finishToken(offset, AsciidocTokenType.Newline);
			}

			// Handle whitespace at start of line (indentation)
			if (atLineStart && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
				while (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
					stream.advance(1);
				}
				return finishToken(offset, AsciidocTokenType.Whitespace);
			}

			// At line start, check for special structures
			if (atLineStart) {
				atLineStart = false;
				
				// Document/Section titles (=, ==, ===, etc.)
				if (stream.peekChar() == '=') {
					int count = 0;
					while (!stream.eos() && stream.peekChar() == '=' && count < 6) {
						stream.advance(1);
						count++;
					}
					
					// Must be followed by space
					if (stream.peekChar() == ' ' || stream.peekChar() == '\t') {
						stream.advance(1);
						state = AsciidocScannerState.WithinTitle;
						return finishToken(offset, count == 1 ? AsciidocTokenType.DocumentTitle : AsciidocTokenType.SectionTitle);
					}
					// Not a title, treat as text
					stream.goBackTo(offset);
				}
				
				// Block delimiters (----, ****, ====, etc.)
				char ch = (char) stream.peekChar();
				if (ch == '-' || ch == '*' || ch == '=' || ch == '.' || ch == '_' || ch == '+' || ch == '/') {
					int count = 0;
					int startPos = stream.pos();
					while (!stream.eos() && stream.peekChar() == ch) {
						stream.advance(1);
						count++;
					}
					
					// Block delimiter must be at least 4 chars and on its own line
					if (count >= 4 && (stream.peekChar() == '\n' || stream.peekChar() == '\r' || stream.eos())) {
						blockDelimiterChar = ch;
						blockDelimiterCount = count;
						state = AsciidocScannerState.WithinBlockDelimiter;
						return finishToken(offset, AsciidocTokenType.BlockDelimiter);
					}
					
					// Not a block delimiter, go back
					stream.goBackTo(startPos);
				}
				
				// Line comment (//)
				if (stream.peekChar() == '/' && stream.peekChar(1) == '/' && stream.peekChar(2) != '/') {
					stream.advance(2);
					state = AsciidocScannerState.WithinLineComment;
					return finishToken(offset, AsciidocTokenType.LineComment);
				}
				
				// Unordered list items (*, **, ***, etc.)
				if (stream.peekChar() == '*') {
					int count = 0;
					int startPos = stream.pos();
					while (!stream.eos() && stream.peekChar() == '*' && count < 5) {
						stream.advance(1);
						count++;
					}
					
					// Must be followed by space
					if (stream.peekChar() == ' ' || stream.peekChar() == '\t') {
						stream.advance(1);
						state = AsciidocScannerState.WithinListItem;
						return finishToken(offset, AsciidocTokenType.UnorderedListItem);
					}
					
					stream.goBackTo(startPos);
				}
				
				// Ordered list items (., .., ..., etc.)
				if (stream.peekChar() == '.') {
					int count = 0;
					int startPos = stream.pos();
					while (!stream.eos() && stream.peekChar() == '.' && count < 5) {
						stream.advance(1);
						count++;
					}
					
					// Must be followed by space
					if (stream.peekChar() == ' ' || stream.peekChar() == '\t') {
						stream.advance(1);
						state = AsciidocScannerState.WithinListItem;
						return finishToken(offset, AsciidocTokenType.OrderedListItem);
					}
					
					stream.goBackTo(startPos);
				}
				
				// Attribute entry (:attr-name:)
				if (stream.peekChar() == ':') {
					int startPos = stream.pos();
					stream.advance(1);
					
					// Scan attribute name
					int nameStart = stream.pos();
					while (!stream.eos() && stream.peekChar() != ':' && stream.peekChar() != '\n' && stream.peekChar() != '\r') {
						stream.advance(1);
					}
					
					if (stream.peekChar() == ':' && stream.pos() > nameStart) {
						stream.advance(1);
						state = AsciidocScannerState.WithinAttributeEntry;
						return finishToken(offset, AsciidocTokenType.AttributeEntry);
					}
					
					stream.goBackTo(startPos);
				}
				
				// Table delimiter (|===)
				if (stream.peekChar() == '|' && stream.peekChar(1) == '=' && stream.peekChar(2) == '=' && stream.peekChar(3) == '=') {
					stream.advance(4);
					state = AsciidocScannerState.WithinTable;
					return finishToken(offset, AsciidocTokenType.TableDelimiter);
				}
				
				// Admonitions (NOTE:, TIP:, etc.)
				for (String admonition : ADMONITIONS) {
					if (matchesAtPosition(admonition)) {
						stream.advance(admonition.length());
						state = AsciidocScannerState.WithinAdmonition;
						return finishToken(offset, AsciidocTokenType.Admonition);
					}
				}
				
				// Include directive
				if (matchesAtPosition("include::")) {
					int startPos = stream.pos();
					stream.advance(9); // "include::"
					
					// Scan until [
					while (!stream.eos() && stream.peekChar() != '[' && stream.peekChar() != '\n') {
						stream.advance(1);
					}
					
					if (stream.peekChar() == '[') {
						// Scan until ]
						stream.advance(1);
						while (!stream.eos() && stream.peekChar() != ']') {
							stream.advance(1);
						}
						if (stream.peekChar() == ']') {
							stream.advance(1);
						}
						return finishToken(offset, AsciidocTokenType.Include);
					}
					
					stream.goBackTo(startPos);
				}
			}

			// Handle whitespace (not at line start)
			if (stream.peekChar() == ' ' || stream.peekChar() == '\t') {
				while (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
					stream.advance(1);
				}
				return finishToken(offset, AsciidocTokenType.Whitespace);
			}

			// Inline formatting and special characters
			
			// Attribute reference {attr-name}
			if (stream.peekChar() == '{') {
				int startPos = stream.pos();
				stream.advance(1);
				
				while (!stream.eos() && stream.peekChar() != '}' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '}') {
					stream.advance(1);
					return finishToken(offset, AsciidocTokenType.AttributeReference);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Anchor [[id]]
			if (stream.peekChar() == '[' && stream.peekChar(1) == '[') {
				int startPos = stream.pos();
				stream.advance(2);
				
				while (!stream.eos() && stream.peekChar() != ']') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == ']' && stream.peekChar(1) == ']') {
					stream.advance(2);
					return finishToken(offset, AsciidocTokenType.Anchor);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Cross reference <<id>>
			if (stream.peekChar() == '<' && stream.peekChar(1) == '<') {
				int startPos = stream.pos();
				stream.advance(2);
				
				while (!stream.eos() && stream.peekChar() != '>') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '>' && stream.peekChar(1) == '>') {
					stream.advance(2);
					return finishToken(offset, AsciidocTokenType.CrossReference);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Links (http://, https://, link:)
			if (matchesAtPosition("http://") || matchesAtPosition("https://") || matchesAtPosition("link:")) {
				int startPos = stream.pos();
				
				// Scan URL
				while (!stream.eos() && !Character.isWhitespace(stream.peekChar()) && stream.peekChar() != '[') {
					stream.advance(1);
				}
				
				// Optional [text]
				if (stream.peekChar() == '[') {
					stream.advance(1);
					while (!stream.eos() && stream.peekChar() != ']') {
						stream.advance(1);
					}
					if (stream.peekChar() == ']') {
						stream.advance(1);
					}
				}
				
				return finishToken(offset, AsciidocTokenType.Link);
			}
			
			// Inline macros (kbd:[], btn:[], etc.)
			if (stream.peekChar() != '\n' && stream.peekChar() != '\r') {
				int startPos = stream.pos();
				
				// Check for macro pattern: word::
				while (!stream.eos() && Character.isLetterOrDigit(stream.peekChar())) {
					stream.advance(1);
				}
				
				if (stream.peekChar() == ':' && stream.peekChar(1) == ':') {
					stream.advance(2);
					
					// Scan until [
					while (!stream.eos() && stream.peekChar() != '[' && stream.peekChar() != '\n') {
						stream.advance(1);
					}
					
					if (stream.peekChar() == '[') {
						stream.advance(1);
						int bracketDepth = 1;
						while (!stream.eos() && bracketDepth > 0) {
							if (stream.peekChar() == '[') {
								bracketDepth++;
							} else if (stream.peekChar() == ']') {
								bracketDepth--;
							}
							stream.advance(1);
						}
						return finishToken(offset, AsciidocTokenType.InlineMacro);
					}
				}
				
				stream.goBackTo(startPos);
			}
			
			// Bold (*text* or **text**)
			if (stream.peekChar() == '*') {
				int startPos = stream.pos();
				boolean doubleAsterisk = stream.peekChar(1) == '*';
				stream.advance(doubleAsterisk ? 2 : 1);
				
				// Scan until closing
				while (!stream.eos() && stream.peekChar() != '*' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '*') {
					stream.advance(1);
					if (doubleAsterisk && stream.peekChar() == '*') {
						stream.advance(1);
					}
					return finishToken(offset, AsciidocTokenType.Bold);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Italic (_text_ or __text__)
			if (stream.peekChar() == '_') {
				int startPos = stream.pos();
				boolean doubleUnderscore = stream.peekChar(1) == '_';
				stream.advance(doubleUnderscore ? 2 : 1);
				
				while (!stream.eos() && stream.peekChar() != '_' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '_') {
					stream.advance(1);
					if (doubleUnderscore && stream.peekChar() == '_') {
						stream.advance(1);
					}
					return finishToken(offset, AsciidocTokenType.Italic);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Monospace (`text` or ``text``)
			if (stream.peekChar() == '`') {
				int startPos = stream.pos();
				boolean doubleBacktick = stream.peekChar(1) == '`';
				stream.advance(doubleBacktick ? 2 : 1);
				
				while (!stream.eos() && stream.peekChar() != '`' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '`') {
					stream.advance(1);
					if (doubleBacktick && stream.peekChar() == '`') {
						stream.advance(1);
					}
					return finishToken(offset, AsciidocTokenType.Monospace);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Superscript (^text^)
			if (stream.peekChar() == '^') {
				int startPos = stream.pos();
				stream.advance(1);
				
				while (!stream.eos() && stream.peekChar() != '^' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '^') {
					stream.advance(1);
					return finishToken(offset, AsciidocTokenType.Superscript);
				}
				
				stream.goBackTo(startPos);
			}
			
			// Subscript (~text~)
			if (stream.peekChar() == '~') {
				int startPos = stream.pos();
				stream.advance(1);
				
				while (!stream.eos() && stream.peekChar() != '~' && stream.peekChar() != '\n') {
					stream.advance(1);
				}
				
				if (stream.peekChar() == '~') {
					stream.advance(1);
					return finishToken(offset, AsciidocTokenType.Subscript);
				}
				
				stream.goBackTo(startPos);
			}

			// Regular text
			int consumed = stream.advanceWhileChar(TEXT_CHAR_PREDICATE);
			if (consumed > 0) {
				return finishToken(offset, AsciidocTokenType.Text);
			}

			// Unknown character
			stream.advance(1);
			return finishToken(offset, AsciidocTokenType.Unknown);
		}

		case WithinTitle: {
			// Scan until end of line
			stream.advanceUntilChar(NEWLINE_CHARS);
			state = AsciidocScannerState.WithinContent;
			return finishToken(offset, AsciidocTokenType.Text);
		}

		case WithinAttributeEntry: {
			// Scan attribute value until end of line
			stream.advanceUntilChar(NEWLINE_CHARS);
			state = AsciidocScannerState.WithinContent;
			return finishToken(offset, AsciidocTokenType.Text);
		}

		case WithinLineComment: {
			// Scan until end of line
			stream.advanceUntilChar(NEWLINE_CHARS);
			state = AsciidocScannerState.WithinContent;
			return finishToken(offset, AsciidocTokenType.Text);
		}

		case WithinListItem:
		case WithinAdmonition: {
			// Scan content until end of line
			stream.advanceUntilChar(NEWLINE_CHARS);
			state = AsciidocScannerState.WithinContent;
			return finishToken(offset, AsciidocTokenType.Text);
		}

		case WithinBlockDelimiter: {
			// Inside a delimited block, scan until matching closing delimiter
			while (!stream.eos()) {
				if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
					stream.advance(1);
					if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
						stream.advance(1);
					}
					
					// Check if next line is closing delimiter
					int startPos = stream.pos();
					int count = 0;
					while (!stream.eos() && stream.peekChar() == blockDelimiterChar) {
						stream.advance(1);
						count++;
					}
					
					if (count == blockDelimiterCount && (stream.peekChar() == '\n' || stream.peekChar() == '\r' || stream.eos())) {
						// Found closing delimiter
						stream.goBackTo(startPos);
						state = AsciidocScannerState.AfterNewline;
						return finishToken(offset, AsciidocTokenType.Text);
					}
					
					stream.goBackTo(startPos);
				} else {
					stream.advance(1);
				}
			}
			
			state = AsciidocScannerState.WithinContent;
			return finishToken(offset, AsciidocTokenType.Text);
		}

		case WithinTable: {
			// Handle table cells
			if (stream.peekChar() == '|') {
				stream.advance(1);
				
				// Check for table end (|===)
				if (stream.peekChar() == '=' && stream.peekChar(1) == '=' && stream.peekChar(2) == '=') {
					stream.advance(3);
					state = AsciidocScannerState.WithinContent;
					return finishToken(offset, AsciidocTokenType.TableDelimiter);
				}
				
				return finishToken(offset, AsciidocTokenType.TableCell);
			}
			
			// Scan cell content
			while (!stream.eos() && stream.peekChar() != '|' && stream.peekChar() != '\n' && stream.peekChar() != '\r') {
				stream.advance(1);
			}
			
			if (stream.pos() > offset) {
				return finishToken(offset, AsciidocTokenType.Text);
			}
			
			// Newline in table
			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
					stream.advance(1);
				}
				return finishToken(offset, AsciidocTokenType.Newline);
			}
			
			state = AsciidocScannerState.WithinContent;
			return internalScan();
		}

		default:
			stream.advance(1);
			return finishToken(offset, AsciidocTokenType.Unknown);
		}
	}

	private boolean matchesAtPosition(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (stream.peekChar(i) != text.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}

// Made with Bob
