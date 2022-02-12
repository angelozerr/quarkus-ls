/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.tags.UserTag;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.UserTagSectionHelper;

public class QuteDiagnosticsForSyntax {

	public void validateWithRealQuteParser(Template template, List<Diagnostic> diagnostics) {
		EngineBuilder engineBuilder = Engine.builder().addDefaults();
		String templateContent = template.getText();
		try {
			QuteProject project = template.getProject();
			if (project != null) {
				Collection<UserTag> tags = project.getUserTags();
				for (UserTag userTag : tags) {
					String tagName = userTag.getName();
					String tagTemplateId = "tags/" + tagName;
					engineBuilder.addSectionHelper(new UserTagSectionHelper.Factory(tagName, tagTemplateId));
				}
			}
			Engine engine = engineBuilder.build();
			engine.parse(templateContent);
		} catch (TemplateException e) {
			String message = e.getMessage();
			if (message.contains("no section helper found for")) {
				return;
			}
			Range range = createRange(e, template);
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
					QuteErrorCode.SyntaxError);
			diagnostics.add(diagnostic);
		}
	}

	private static Range createRange(TemplateException e, Template template) {
		int line = e.getOrigin().getLine() - 1;
		Position start = new Position(line, e.getOrigin().getLineCharacterStart() - 1);
		Position end = new Position(line, e.getOrigin().getLineCharacterEnd() - 1);
		Range range = new Range(start, end);
		return range;
	}
}
