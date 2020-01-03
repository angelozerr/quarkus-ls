package com.redhat.microprofile.ls;

import static com.redhat.microprofile.ls.MicroProfileLanguageServerUtils.createServer;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class MicroProfileProjectInfoAccessTest {

	@Test
	public void severalAccess() throws InterruptedException, ExecutionException {
		MicroProfileLanguageServer server = createServer();
		MockMicroProfileLanguageClient client = (MockMicroProfileLanguageClient) server.getLanguageClient();
	}
}
