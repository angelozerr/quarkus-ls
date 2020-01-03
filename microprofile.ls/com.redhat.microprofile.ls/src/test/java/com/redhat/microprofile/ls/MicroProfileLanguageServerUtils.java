package com.redhat.microprofile.ls;

import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

import com.redhat.microprofile.ls.api.MicroProfileLanguageClientAPI;

public class MicroProfileLanguageServerUtils extends MicroProfileLanguageServer {

	public static MicroProfileLanguageServer createServer() {
		return createServer(null);
	}

	public static MicroProfileLanguageServer createServer(Integer timeout) {
		MicroProfileLanguageServer languageServer = new MicroProfileLanguageServer();
		MicroProfileLanguageClientAPI languageClient = new MockMicroProfileLanguageClient(languageServer, timeout);
		languageServer.setClient(languageClient);
		return languageServer;
	}

	public static void didOpen(String uri, MicroProfileLanguageServer server) {
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
		params.setTextDocument(new TextDocumentItem(uri, "", 1, ""));
		server.getTextDocumentService().didOpen(params);
	}

	public static CompletionList completion(String uri, MicroProfileLanguageServer server)
			throws InterruptedException, ExecutionException {
		CompletionParams params = new CompletionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setPosition(new Position(0, 0));
		return server.getTextDocumentService().completion(params).get().getRight();
	}

}
