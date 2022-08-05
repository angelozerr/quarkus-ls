package com.redhat.qute.dap.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.Variable;

import com.redhat.qute.dap.ConnectionManager;

public class QuteScope extends Scope {

	private final transient ConnectionManager connectionManager;

	public QuteScope(String name, int variablesReference, ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		setName(name);
		setVariablesReference(variablesReference);
	}

	public void collectVariables(int variablesReference, Set<Variable> variables) {
		if (variablesReference != getVariablesReference()) {
			return;
		}
		variables.addAll(Stream.of(connectionManager.getVariables(variablesReference)) //
				.map(v -> toDAPVariable(v)) //
				.collect(Collectors.toList()));
	}

	private Variable toDAPVariable(io.quarkus.qute.debug.Variable v) {
		Variable variable = new Variable();
		variable.setName(v.getName());
		variable.setValue(v.getValue());
		variable.setType(v.getType());
		return variable;
	}

}
