package com.redhat.qute.dap;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;

public class QuteDebugAdapterLauncher {

	public static void main(String[] args) {
		QuteDebugAdapterServer debugServer = new QuteDebugAdapterServer();
		Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(debugServer, System.in,
				System.out);
		IDebugProtocolClient clientProxy = serverLauncher.getRemoteProxy();
		debugServer.connect(clientProxy);
		serverLauncher.startListening();
	}
}
