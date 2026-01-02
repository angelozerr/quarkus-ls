package com.redhat.qute.project.extensions;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.QuteValidationSettings;

public interface DiagnosticsParticipant {

	boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics);
}
