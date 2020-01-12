/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.Template;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * The Qute language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLanguageService {
	private final QuteHighlighting highlighting;
	private final QuteDiagnostics diagnostics;

	private final QuteDefinition definition;

	public QuteLanguageService() {
		this.highlighting = new QuteHighlighting();
		this.definition = new QuteDefinition();
		this.diagnostics = new QuteDiagnostics();
	}

	public List<DocumentHighlight> findDocumentHighlights(Template template, TextDocument document, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(template, document, position, cancelChecker);
	}

	public List<? extends LocationLink> findDefinition(Template template, TextDocument document, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(template, document, position, cancelChecker);
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template           the Qute template.
	 * @param document
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, TextDocument document,
			QuteValidationSettings validationSettings, CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(template, document, validationSettings, cancelChecker);
	}

}
