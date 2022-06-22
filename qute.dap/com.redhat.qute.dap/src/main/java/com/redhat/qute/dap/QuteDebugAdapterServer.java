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
package com.redhat.qute.dap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.TerminateArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;

import com.redhat.qute.dap.model.QuteBreakpoint;
import com.redhat.qute.dap.model.QuteScope;
import com.redhat.qute.dap.model.QuteStackFrame;
import com.redhat.qute.dap.model.QuteThread;

/**
 * Qute Debugger Adapter Protocol (DAP) server .
 *
 * @author Angelo ZERR
 *
 */
public class QuteDebugAdapterServer implements IDebugProtocolServer {

	private IDebugProtocolClient client;
	private final ConnectionManager connectionManager;

	public QuteDebugAdapterServer() {
		connectionManager = new ConnectionManager();
	}

	public void connect(IDebugProtocolClient clientProxy) {
		this.client = clientProxy;
	}

	@Override
	public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			Capabilities capabilities = new Capabilities();
			// capabilities.setSupportsSetVariable(Boolean.TRUE);
			// capabilities.setSupportsConditionalBreakpoints(Boolean.TRUE);
			return capabilities;
		});
	}

	@Override
	public CompletableFuture<Void> attach(Map<String, Object> args) {
		return CompletableFuture.runAsync(() -> {
			boolean attached = connectionManager.attach(args, client);
			if (attached) {
				client.initialized();
			}
			OutputEventArguments telemetryEvent = new OutputEventArguments();
			telemetryEvent.setCategory(OutputEventArgumentsCategory.TELEMETRY);
			telemetryEvent.setOutput("qute.dap.attach");
			telemetryEvent
					.setData(new TelemetryEvent("qute.dap.attach", Collections.singletonMap("success", attached)));
			client.output(telemetryEvent);
		});
	}

	@Override
	public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			SetBreakpointsResponse response = new SetBreakpointsResponse();
			Source source = args.getSource();
			SourceBreakpoint[] sourceBreakpoints = args.getBreakpoints();
			Breakpoint[] breakpoints = new Breakpoint[sourceBreakpoints.length];
			for (int i = 0; i < sourceBreakpoints.length; i++) {
				SourceBreakpoint sourceBreakpoint = sourceBreakpoints[i];
				int line = sourceBreakpoint.getLine();
				QuteBreakpoint breakpoint = new QuteBreakpoint(source, line);
				breakpoint.setMessage("the breakpoint " + i);
				breakpoints[i] = breakpoint;
				String errorMessage = connectionManager.setBreakpoint(breakpoint, source);
				if (errorMessage == null) {
					breakpoint.setVerified(true);
				} else {
					breakpoint.setMessage(errorMessage);
				}
			}
			response.setBreakpoints(breakpoints);
			return response;
		});
	}

	@Override
	public CompletableFuture<ThreadsResponse> threads() {
		return CompletableFuture.supplyAsync(() -> {
			ThreadsResponse response = new ThreadsResponse();
			Collection<QuteThread> threads = connectionManager.getQuteThreads(true);
			response.setThreads(threads.toArray(new QuteThread[threads.size()]));
			return response;
		});
	}

	@Override
	public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			int threadId = args.getThreadId();
			Set<QuteStackFrame> stackFrames = connectionManager.getStackFrames(threadId);
			StackTraceResponse response = new StackTraceResponse();
			response.setStackFrames(stackFrames.toArray(new StackFrame[stackFrames.size()]));
			response.setTotalFrames(stackFrames.size());
			return response;
		});
	}

	@Override
	public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			ScopesResponse response = new ScopesResponse();
			int frameId = args.getFrameId();
			QuteStackFrame frame = connectionManager.findStackFrame(frameId);
			Set<QuteScope> scopes = frame.createScopes();
			response.setScopes(scopes.toArray(new Scope[scopes.size()]));
			return response;
		});
	}

	@Override
	public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			VariablesResponse response = new VariablesResponse();
			int variablesReference = args.getVariablesReference();
			Set<Variable> variables = new HashSet<>();
			for (QuteThread quteThread : connectionManager.getQuteThreads(false)) {
				quteThread.collectVariables(variablesReference, variables);
			}
			response.setVariables(variables.toArray(new Variable[variables.size()]));
			return response;
		});
	}

	@Override
	public CompletableFuture<Void> terminate(TerminateArguments args) {
		return CompletableFuture.runAsync(() -> {
			connectionManager.terminate(args.getRestart() != null ? args.getRestart() : false);
		});
	}

	@Override
	public CompletableFuture<Void> disconnect(DisconnectArguments args) {
		return CompletableFuture.runAsync(() -> {
			connectionManager.terminate(args.getRestart() != null ? args.getRestart() : false);
		});
	}

	@Override
	public CompletableFuture<Void> stepIn(StepInArguments args) {
		return CompletableFuture.runAsync(() -> {
			connectionManager.stepIn(args.getThreadId());
		});
	}

	@Override
	public CompletableFuture<Void> stepOut(StepOutArguments args) {
		return CompletableFuture.runAsync(() -> {
			connectionManager.stepOut(args.getThreadId());
		});
	}

	@Override
	public CompletableFuture<Void> pause(PauseArguments args) {
		return CompletableFuture.runAsync(() -> {
			connectionManager.pause(args.getThreadId());
		});
	}

	@Override
	public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
		return CompletableFuture.supplyAsync(() -> {
			ContinueResponse response = new ContinueResponse();
			int threadId = args.getThreadId();
			if (threadId != 0) {
				response.setAllThreadsContinued(Boolean.FALSE);
				QuteThread thread = connectionManager.findThread(threadId);
				if (thread != null) {
					connectionManager.stepOver(thread.getId());
				}
			} else {
				connectionManager.resumeAll();
				response.setAllThreadsContinued(Boolean.TRUE);
			}
			return response;
		});
	}

}
