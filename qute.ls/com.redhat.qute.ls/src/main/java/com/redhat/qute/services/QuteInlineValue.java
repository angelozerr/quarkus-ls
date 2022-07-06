package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InlineValue;
import org.eclipse.lsp4j.InlineValueContext;
import org.eclipse.lsp4j.InlineValueText;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteDebugResolveVariablesParams;
import com.redhat.qute.ls.api.QuteDebugResolveVariablesProvider;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.inlinevalue.InlineValueASTVistitor;
import com.redhat.qute.services.inlinevalue.InlineVariable;

public class QuteInlineValue {

	public CompletableFuture<List<InlineValue>> getInlineValue(Template template, Range range,
			InlineValueContext context, QuteDebugResolveVariablesProvider resolveVariablesProvider,
			CancelChecker cancelChecker) {
		int frameId = context.getFrameId();
		if (frameId > 0) {

			InlineValueASTVistitor vistitor = new InlineValueASTVistitor();
			template.accept(vistitor);
			List<InlineVariable> variables = vistitor.getVariables();
			if (variables.isEmpty()) {
				return CompletableFuture.completedFuture(null);
			}

			QuteDebugResolveVariablesParams params = new QuteDebugResolveVariablesParams();
			params.setFrameId(frameId);
			return resolveVariablesProvider.resolveVariables(params) //
					.thenApply(resolved -> {

						List<InlineValue> inlineValues = new ArrayList<>();
						for (InlineVariable variable : variables) {
							InlineValueText text = new InlineValueText();
							text.setRange(variable.getRange());
							String value = getValue(variable.getName(), resolved);
							text.setText(variable.getName() + " = " + (value != null ? value : "?"));
							inlineValues.add(new InlineValue(text));
						}
						return inlineValues;
					});
		}

		return CompletableFuture.completedFuture(null);
	}

	private static String getValue(String name, List<? extends Variable> resolved) {
		if (resolved == null) {
			return null;
		}
		for (Variable variable : resolved) {
			if (name.equals(variable.getName())) {
				return variable.getValue();
			}
		}
		return null;
	}

}
