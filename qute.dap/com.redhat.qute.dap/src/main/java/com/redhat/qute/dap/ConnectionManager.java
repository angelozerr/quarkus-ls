package com.redhat.qute.dap;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArgumentsReason;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import com.redhat.qute.dap.model.QuteStackFrame;
import com.redhat.qute.dap.model.QuteThread;

import io.quarkus.qute.debug.AbstractDebuggerListener;
import io.quarkus.qute.debug.Debugger;
import io.quarkus.qute.debug.Scope;
import io.quarkus.qute.debug.StackTrace;
import io.quarkus.qute.debug.StoppedEvent;
import io.quarkus.qute.debug.StoppedEvent.StoppedReason;
import io.quarkus.qute.debug.ThreadEvent;
import io.quarkus.qute.debug.ThreadEvent.ThreadStatus;
import io.quarkus.qute.debug.Variable;
import io.quarkus.qute.debug.client.RemoteDebuggerClient;

public class ConnectionManager {

	private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());

	public static final String ATTACH_PORT = "port";

	private IDebugProtocolClient client;

	private RemoteDebuggerClient debuggerClient;

	private final Map<String, Source> templateIdToSource = new HashMap<>();

	private final Map<Integer, QuteThread> threads = new HashMap<>();

	private class Listener extends AbstractDebuggerListener {

		protected Listener() throws RemoteException {
			super();
		}

		@Override
		public void onStopped(StoppedEvent event) throws RemoteException {
			handleStopped(event);
		}

		@Override
		public void onThreadChanged(ThreadEvent event) throws RemoteException {
			handleThreadChanged(event);
		}

		@Override
		public void onTerminate() throws RemoteException {
			handleTerminate();
		}

	};

	public boolean attach(Map<String, Object> args, IDebugProtocolClient client) {
		this.client = client;
		Integer port = (Integer) args.getOrDefault(ATTACH_PORT, Debugger.DEFAULT_PORT);
		try {
			debuggerClient = RemoteDebuggerClient.connect(port);
			debuggerClient.addDebuggerListener(new Listener());
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error trying to attach", e);
			sendAttachErrorOutput(client, e.getMessage());
		}
		return false;
	}

	private static void sendAttachErrorOutput(IDebugProtocolClient client, String specificErrorMessage) {
		OutputEventArguments errorEvent = new OutputEventArguments();
		errorEvent.setCategory(OutputEventArgumentsCategory.STDERR);
		errorEvent.setOutput("Error when trying to connect the Qute debugger: " + specificErrorMessage + "");
		client.output(errorEvent);
	}

	public String setBreakpoint(Breakpoint breakpoint, Source source) {
		int line = breakpoint.getLine();
		String templateId = toTemplateId(source);
		templateIdToSource.put(templateId, source);
		try {
			debuggerClient.setBreakpoint(templateId, line);
			return null;
		} catch (Throwable e) {
			return e.getMessage() != null ? e.getMessage() : "Error";
		}
	}

	public Source getSource(String templateId) {
		return templateIdToSource.get(templateId);
	}

	private static String toTemplateId(Source source) {
		String path = source.getPath().replace("\\", "/");
		int index = path.indexOf("src/main/resources/templates/");
		if (index != -1) {
			String templateId = path.substring(index + "src/main/resources/templates/".length());
			/*
			 * int dotIndex = templateId.lastIndexOf('.'); templateId =
			 * templateId.substring(0, dotIndex); if (templateId.endsWith(".qute")) {
			 * templateId = templateId.substring(0, templateId.length() - ".qute".length());
			 * }
			 */
			return templateId;
		}
		return null;
	}

	public Collection<QuteThread> getQuteThreads(boolean refresh) {
//		if (refresh) {
//			
//		}
//		if (debuggerClient != null) {
//			if (threads == null) {
//				threads = collectThreads();
//			}
//			return threads;
//		}
//		return Collections.emptySet();
		return threads.values();
	}

//	private synchronized Set<QuteThread> collectThreads() {
//		if (threads != null) {
//			return threads;
//		}
//		return debuggerClient.getThreads() //
//				.stream() //
//				.map(t -> new QuteThread(t, this)) //
//				.collect(Collectors.toSet());
//	}

	public QuteThread findThread(int threadId) {
		return threads.get(threadId);
	}

	public void terminate(boolean restart) {
		if (debuggerClient != null) {
			debuggerClient.terminate();
		}
	}

	public StackTrace getStackTrace(long threadId) {
		if (debuggerClient != null) {
			return debuggerClient.getStackTrace(threadId);
		}
		return null;
	}

	public Scope[] getScopes(int frameId) {
		if (debuggerClient != null) {
			return debuggerClient.getScopes(frameId);
		}
		return new Scope[0];
	}

	public Variable[] getVariables(int variablesReference) {
		if (debuggerClient != null) {
			return debuggerClient.getVariables(variablesReference);
		}
		return new Variable[0];
	}

	private void handleStopped(StoppedEvent event) {
		try {
			int threadId = (int) event.getThreadId();
			// Create the Qute threads if required
			if (findThread(threadId) == null) {
				io.quarkus.qute.debug.Thread t = debuggerClient.getThread(event.getThreadId());
				threads.put(threadId, new QuteThread(t, this));
			}
			String reason = getReason(event.getReason());
			sendStopEvent(threadId, reason);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private static String getReason(StoppedReason reason) {
		switch (reason) {
		case BREAKPOINT:
			return StoppedEventArgumentsReason.BREAKPOINT;
		case EXCEPTION:
			return StoppedEventArgumentsReason.EXCEPTION;
		case PAUSE:
			return StoppedEventArgumentsReason.PAUSE;
		case STEP:
			return StoppedEventArgumentsReason.STEP;
		}
		return null;
	}

	public void handleThreadChanged(ThreadEvent event) {
		try {
			int threadId = (int) event.getThreadId();
			if (event.getThreadStatus() == ThreadStatus.EXITED) {
				// Remove the exited Qute threads
				threads.remove(threadId);
			}
			String reason = getReason(event.getThreadStatus());
			sendThreadEvent(threadId, reason);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private static String getReason(ThreadStatus threadStatus) {
		switch (threadStatus) {
		case STARTED:
			return ThreadEventArgumentsReason.STARTED;
		case EXITED:
			return ThreadEventArgumentsReason.EXITED;
		}
		return null;
	}

	public void handleTerminate() {
		if (debuggerClient != null) {
			debuggerClient.terminate();
		}
		sendExitEvent();
	}

	public void stepIn(int threadId) {
		if (debuggerClient != null) {
			debuggerClient.stepIn(threadId);
		}
	}

	public void stepOut(int threadId) {
		if (debuggerClient != null) {
			debuggerClient.stepOut(threadId);
		}
	}

	public void stepOver(int threadId) {
		if (debuggerClient != null) {
			debuggerClient.stepOver(threadId);
		}
	}

	public void pause(int threadId) {
		if (debuggerClient != null) {
			debuggerClient.pause(threadId);
			sendStopEvent(threadId, StoppedEventArgumentsReason.PAUSE);
		}
	}

	public void resume(int threadId) {
		if (debuggerClient != null) {
			debuggerClient.resume(threadId);
		}
	}

	public void resumeAll() {
		// TODO Auto-generated method stub

	}

	private void sendStopEvent(int threadId, String reason) {
		if (client == null) {
			return;
		}
		StoppedEventArguments args = new StoppedEventArguments();
		args.setThreadId(threadId);
		args.setReason(reason);
		client.stopped(args);
	}

	private void sendThreadEvent(int threadId, String reason) {
		if (client == null) {
			return;
		}
		ThreadEventArguments args = new ThreadEventArguments();
		args.setThreadId(threadId);
		args.setReason(reason);
		client.thread(args);
	}

	private void sendExitEvent() {
		if (client == null) {
			return;
		}
		ExitedEventArguments args = new ExitedEventArguments();
		client.exited(args);
		client.terminated(new TerminatedEventArguments());
	}

	public Set<QuteStackFrame> getStackFrames(int threadId) {
		QuteThread thread = findThread(threadId);
		if (thread == null) {
			return Collections.emptySet();
		}
		return thread.getStackFrames();
	}

	public QuteStackFrame findStackFrame(int frameId) {
		for (QuteThread thread : threads.values()) {
			QuteStackFrame frame = thread.findStackFrame(frameId);
			if (frame != null) {
				return frame;
			}
		}
		return null;
	}

}
