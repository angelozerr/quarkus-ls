package com.redhat.qute.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface QuteDebugResolveVariablesProvider {

	@JsonRequest("qute/debug/resolveVariables")
	default CompletableFuture<List<? extends Variable>> resolveVariables(QuteDebugResolveVariablesParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
