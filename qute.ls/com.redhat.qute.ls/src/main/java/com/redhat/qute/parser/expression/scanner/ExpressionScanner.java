/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.expression.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;

public class ExpressionScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> PART_PREDICATE = ch -> {
		return Character.isJavaIdentifierPart(ch);
	};

	private static final Predicate<Integer> OBJECT_PART_PREDICATE = ch -> {
		return Character.isJavaIdentifierPart(ch) || ch == '-';
	};

	public static ExpressionScanner createScanner(String input, boolean canSupportInfixNotation) {
		return createScanner(input, canSupportInfixNotation, 0, input.length());
	}

	public static ExpressionScanner createScanner(String input, boolean canSupportInfixNotation, int initialOffset,
			int endOffset) {
		return createScanner(input, canSupportInfixNotation, initialOffset, endOffset, ScannerState.WithinExpression);
	}

	public static ExpressionScanner createScanner(String input, boolean canSupportInfixNotation, int initialOffset,
			int endOffset, ScannerState initialState) {
		return new ExpressionScanner(input, canSupportInfixNotation, initialOffset, endOffset, initialState);
	}

	private final boolean canSupportInfixNotation;

	private boolean inMethod;

	private int bracket;

	private boolean isInInfixNotation;
	private int nbParts;

	ExpressionScanner(String input, boolean canSupportInfixNotation, int initialOffset, int endOffset,
			ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
		this.canSupportInfixNotation = canSupportInfixNotation;
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinExpression: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar('?')) {
				if (stream.advanceIfChar(':')) {
					return finishToken(offset, TokenType.ElvisOperator);
				}
				return finishToken(offset, TokenType.TernaryOperator);
			}
			if (stream.advanceIfChar('"') || stream.advanceIfChar('\'')) {
				state = ScannerState.WithinString;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			}
			if (hasNextJavaIdentifierPart()) {
				return finishTokenPart(offset);
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case WithinParts:
		case AfterNamespace: {
			if (stream.skipWhitespace()) {
				nbParts++;
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('?')) {
				if (stream.advanceIfChar(':')) {
					return finishToken(offset, TokenType.ElvisOperator);
				}
				return finishToken(offset, TokenType.TernaryOperator);
			}
			if (stream.advanceIfChar('"') || stream.advanceIfChar('\'')) {
				state = ScannerState.WithinString;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			}
			if (stream.advanceIfChar('.')) {
				// item.|
				return finishToken(offset, TokenType.Dot);
			}
			if (stream.advanceIfChar('[')) {
				// item[|
				state = ScannerState.WithinPropertyAngleBracket;
				return finishToken(offset, TokenType.OpenAngleBracket);
			}
			if (stream.advanceIfChar(':')) {
				// data:|
				return finishToken(offset, TokenType.ColonSpace);
			}
			if (hasNextJavaIdentifierPart()) {
				// item.name|
				return finishTokenPart(offset);
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case WithinPropertyAngleBracket: {
			if (stream.advanceIfChar(']')) {
				// item['property'|]
				state = ScannerState.WithinParts;
				return finishToken(offset, TokenType.CloseAngleBracket);
			}
			if (stream.advanceIfChar('"') || stream.advanceIfChar('\'')) {
				state = ScannerState.WithinString;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			}
			if (hasNextJavaIdentifierPart()) {
				// item.name|
				return finishTokenPart(offset);
			}
		}
		
		case WithinMethod: {
			if (stream.advanceIfChar('(')) {
				bracket++;
				return finishToken(offset, TokenType.OpenBracket);
			}
			stream.advanceUntilChar('(', ')', '"', '\'');
			if (stream.peekChar() == '(') {
				stream.advance(1);
				bracket++;
				return internalScan();
			}
			if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
				stream.advance(1);
				state = ScannerState.WithinString;
				inMethod = true;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			} else if (stream.peekChar() == ')') {
				stream.advance(1);
				bracket--;
				if (bracket > 0) {
					return internalScan();
				}
				state = ScannerState.WithinParts;
				inMethod = false;
				return finishToken(stream.pos() - 1, TokenType.CloseBracket);
			}
			return internalScan();
		}

		case WithinString: {
			if (stream.advanceIfAnyOfChars('"', '\'')) {
				if (inMethod) {
					state = ScannerState.WithinMethod;
				} else {
					state = ScannerState.WithinParts;
				}
				return finishToken(offset, TokenType.EndString);
			}
			stream.advanceUntilChar('"', '\'');
			return finishToken(offset, TokenType.String);
		}

		default:
			inMethod = false;
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private TokenType finishTokenPart(int offset) {
		int next = stream.peekChar();
		if (next == ':') {
			// config|:
			state = ScannerState.AfterNamespace;
			return finishToken(offset, TokenType.NamespacePart);
		}
		if (state == ScannerState.WithinParts || state == ScannerState.AfterNamespace) {
			TokenType lastTokenType = getTokenType();
			if (lastTokenType == TokenType.Dot) {
				// item.|
				if (next == '(') {
					// item.compute|(
					state = ScannerState.WithinMethod;
					return finishToken(offset, TokenType.MethodPart);
				}
				// item.compute|
				return finishToken(offset, TokenType.PropertyPart);
			}

			if (state == ScannerState.AfterNamespace) {
				// config:property|...
				if (next == '(') {
					// config:property|(
					state = ScannerState.WithinMethod;
					return finishToken(offset, TokenType.MethodPart);
				}
				// config:property|
				state = ScannerState.WithinParts;
				return finishToken(offset, TokenType.ObjectPart);
			}

			if (canSupportInfixNotation) {
				// name or ...
				if (next == '.') {
					// name or item|.name or
					stream.advanceUntilChar(' ');
					// name or item.name| or
				}
				if (nbParts % 2 == 0) {
					// name or param|
					return finishToken(offset, TokenType.InfixParameter);
				}
				// name or| param
				return finishToken(offset, TokenType.InfixMethodPart);
			}
		}
		state = ScannerState.WithinParts;
		return finishToken(offset, TokenType.ObjectPart);
	}

	private boolean hasNextJavaIdentifierPart() {
		TokenType lastTokenType = getTokenType();
		if (lastTokenType == TokenType.Dot) {
			// A method, property part is scanning
			return stream.advanceWhileChar(PART_PREDICATE) > 0;
		}
		// An object part is scanning, the '-' is accepted (ex : nested-content)
		return stream.advanceWhileChar(OBJECT_PART_PREDICATE) > 0;
	}

	/**
	 * Returns true if the current expression scan an Infix Notation and false
	 * otherwise.
	 * 
	 * @return true if the current expression scan an Infix Notation and false
	 *         otherwise.
	 */
	public boolean isInInfixNotation() {
		return isInInfixNotation;
	}
}
