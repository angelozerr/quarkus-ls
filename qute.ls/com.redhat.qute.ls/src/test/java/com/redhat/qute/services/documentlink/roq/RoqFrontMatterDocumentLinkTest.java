/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.documentlink.roq;

import static com.redhat.qute.QuteAssert.dl;
import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.DocumentLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.DocumentLinkParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Tests for Roq Yaml frontmatter document link with layout.
 * 
 * @author Angelo ZERR
 *
 */
public class RoqFrontMatterDocumentLinkTest {

	// --------------- layout

	@Test
	public void layout() throws Exception {
		String layoutUri = RoqProject.getFileUri("/templates/layouts/default.html");
		String template = "---\r\n" + //
				"layout: default\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDocumentLinkFor(template, //
				dl(r(1, 8, 1, 15), layoutUri));
	}

	@Test
	public void layoutInResources() throws Exception {
		// resources-layout.html exists in
		// roq\src\main\resources\templates\layouts\resources-layout.html
		String layoutUri = RoqProject.getFileUri("/src/main/resources/templates/layouts/resources-layout.html");
		String template = "---\r\n" + //
				"layout: resources-layout\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDocumentLinkFor(template, //
				dl(r(1, 8, 1, 24), layoutUri));
	}

	@Test
	public void theme() throws Exception {
		String template = "---\r\n" + //
				"layout: :theme/main\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDocumentLinkFor(template);
	}

	// --------------- image

	@Test
	public void image() throws Exception {
		String imageUri = RoqProject.getFileUri("/public/images/ico.png");
		String template = "---\r\n" + //
				"image: ico.png\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDocumentLinkFor(template, //
				dl(r(1, 7, 1, 14), imageUri));
	}

	@Test
	public void invalidImage() throws Exception {
		String imageUri = RoqProject.getFileUri("/public/images/invalid.png");
		String template = "---\r\n" + //
				"image: invalid.png\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDocumentLinkFor(template, //
				dl(r(1, 7, 1, 18), imageUri));
	}

	private static void testDocumentLinkFor(String value, DocumentLink... expected) throws Exception {
		DocumentLinkParameters p = new DocumentLinkParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.testDocumentLinkFor(value, p, expected);
	}

}
