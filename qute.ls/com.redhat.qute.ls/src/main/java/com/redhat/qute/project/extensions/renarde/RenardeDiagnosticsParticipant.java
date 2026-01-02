package com.redhat.qute.project.extensions.renarde;

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.validator.IQuteErrorCode;
import com.redhat.qute.project.extensions.DiagnosticsParticipant;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.utils.QutePositionUtility;

public class RenardeDiagnosticsParticipant implements DiagnosticsParticipant {

	private static final IQuteErrorCode ERROR = new IQuteErrorCode() {

		@Override
		public String getRawMessage() {
			return "Unkwown message ''{0}''.";
		}

		@Override
		public String getCode() {
			return "RenardeMessage";
		}
	};

	
	private final RenardeProjectExtension renardeProjectExtension;

	public RenardeDiagnosticsParticipant(RenardeProjectExtension renardeProjectExtension) {
		this.renardeProjectExtension = renardeProjectExtension;
	}

	@Override
	public boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		if ("m".equals(parts.getNamespace())) {
			String expression = parts.getContent();
			if (!renardeProjectExtension.hasMessage(expression)) {
				Range range = QutePositionUtility.createRange(parts);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning, ERROR, expression);
				diagnostics.add(diagnostic);
			}
			return true;
		}
		return false;
	}
}
