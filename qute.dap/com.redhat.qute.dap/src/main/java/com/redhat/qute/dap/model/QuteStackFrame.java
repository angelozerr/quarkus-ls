package com.redhat.qute.dap.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Variable;

import com.redhat.qute.dap.ConnectionManager;

import io.quarkus.qute.debug.Scope;

public class QuteStackFrame extends StackFrame {

	private final transient ConnectionManager connectionManager;
	private Set<QuteScope> scopes;

	public QuteStackFrame(int frameId, String name, Integer line, Source source, ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		setId(frameId);
		setName(name);
		setLine(line);
		setSource(source);
	}

	public Set<QuteScope> createScopes() {
		this.scopes = Stream.of(connectionManager.getScopes(getId())) //
				.map(s -> toDAPScope(s)) //
				.collect(Collectors.toSet());
		return scopes;
	}

	private QuteScope toDAPScope(Scope s) {
		return new QuteScope(s.getName(), s.getVariablesReference(), connectionManager);
	}

	public void collectVariables(int variablesReference, Set<Variable> variables) {
		for (QuteScope scope : getScopes()) {
			scope.collectVariables(variablesReference, variables);
		}
	}

	private Set<QuteScope> getScopes() {
		if (scopes == null) {
			createScopes();
		}
		return scopes;
	}

}
