package com.redhat.qute.dap.model;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.Variable;

import com.redhat.qute.dap.ConnectionManager;

import io.quarkus.qute.debug.StackTrace;

public class QuteThread extends Thread {

	private final transient ConnectionManager connectionManager;
	private transient Set<QuteStackFrame> stackFrames;

	public QuteThread(io.quarkus.qute.debug.Thread t, ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		super.setId((int) t.getId());
		super.setName("Thread [" + t.getName() + "]");
	}

	public Set<QuteStackFrame> getStackFrames() {
		StackTrace stackTrace = connectionManager.getStackTrace(getId());
		if (stackTrace != null) {
			stackFrames = stackTrace.getStackFrames() //
					.stream() //
					.map(sf -> toDAPFrame(sf, connectionManager)) //
					.collect(Collectors.toSet());
		}
		return stackFrames;
	}

	public QuteStackFrame toDAPFrame(io.quarkus.qute.debug.StackFrame sf, ConnectionManager connectionManager) {
		int frameId = sf.getId();
		String name = sf.getName();
		int line = sf.getLine();
		Source source = connectionManager.getSource(sf.getTemplateId());
		return new QuteStackFrame(frameId, name, line, source, connectionManager);
	}

	public QuteStackFrame findStackFrame(int frameId) {
		if (stackFrames == null) {
			return null;
		}
		for (QuteStackFrame frame : stackFrames) {
			if (frameId == frame.getId()) {
				return frame;
			}
		}
		return null;
	}

	public void collectVariables(int variablesReference, Set<Variable> variables) {
		if (stackFrames == null) {
			return;
		}
		for (QuteStackFrame frame : stackFrames) {
			frame.collectVariables(variablesReference, variables);
		}
	}

}
