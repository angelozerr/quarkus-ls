/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.assertDiagnostics;
import static com.redhat.qute.QuteAssert.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.project.MockQuteLanguageServer;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.utils.FileUtils;
import com.redhat.qute.utils.IOUtils;

/**
 * Diagnostics tests with closed/opened Qute template in a given project.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInProjectTest {

	private static class QuteQuickStartProjectLanguageServer extends MockQuteLanguageServer {

		private final Path templatesPath;

		public QuteQuickStartProjectLanguageServer() {
			templatesPath = FileUtils
					.createPath("src/test/resources/projects/qute-quickstart/src/main/resources/templates");
		}

		@Override
		public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
			ProjectInfo projectInfo = new ProjectInfo(QuteQuickStartProject.PROJECT_URI,
					templatesPath.toUri().toASCIIString());
			return CompletableFuture.completedFuture(projectInfo);
		};
	}

	@Test
	public void ValidateClosedAndOpenedTemplates() throws IOException {

		QuteQuickStartProjectLanguageServer server = new QuteQuickStartProjectLanguageServer();

		// 1) On load: load detail.html
		onLoadFilesTest(server);

		// 2) On delete: delete detail_error.html
		onDeleteFile(server);

		// 2) On create: create detail_error.html
		onCreateFile(server);
	}

	private void onLoadFilesTest(QuteQuickStartProjectLanguageServer server) throws IOException {
		server.getPublishDiagnostics().clear();

		// Open detail.html
		Path filePath = server.templatesPath.resolve("detail.html");
		String fileUri = filePath.toUri().toASCIIString();
		String template = IOUtils.getContent(filePath);
		server.didOpen(fileUri, template);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(),
				d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
						"Template not found: `bad`.", //
						DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Error));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

	private void onDeleteFile(QuteQuickStartProjectLanguageServer server) {
		server.getPublishDiagnostics().clear();

		Path detailErrorPath = server.templatesPath.resolve("detail_error.html");
		FileEvent deleteEvent = new FileEvent(detailErrorPath.toUri().toASCIIString(), FileChangeType.Deleted);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(deleteEvent));
		server.didChangeWatchedFiles(params);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

	}

	private void onCreateFile(QuteQuickStartProjectLanguageServer server) {
		server.getPublishDiagnostics().clear();

		Path detailErrorPath = server.templatesPath.resolve("detail_error.html");
		FileEvent createEvent = new FileEvent(detailErrorPath.toUri().toASCIIString(), FileChangeType.Created);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(createEvent));
		server.didChangeWatchedFiles(params);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(),
				d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
						"Template not found: `bad`.", //
						DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Error));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

	private void waitForDiagnostics() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	private static PublishDiagnosticsParams findPublishDiagnostics(List<PublishDiagnosticsParams> diagnostics,
			String fileName) {
		for (PublishDiagnosticsParams diagnostic : diagnostics) {
			if (diagnostic.getUri().endsWith(fileName)) {
				return diagnostic;
			}
		}
		return null;
	}
}
