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
package com.redhat.qute.services.completions.roq;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.CompletionParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test completion with Roq Quarkus extension.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqPageCompletionsTest {

	@Test
	public void page() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.NormalPage page}\r\n" + //
				"{page.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 6, 1, 6)), //
				c("data() : JsonObject", "data", r(1, 6, 1, 6)));
	}

	@Test
	public void pageData() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.NormalPage page}\r\n" + //
				"{page.data.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 11, 1, 11)), //
				c("mergeIn(other : JsonObject, deep : boolean) : JsonObject", "mergeIn(other, deep)", r(1, 11, 1, 11)));
	}

	@Test
	public void pageDataWithYamlFrontMatter() throws Exception {
		String template = "---\r\n" + //
				"foo: 1\r\n" + //
				"---\r\n" + //
				"{@io.quarkiverse.roq.frontmatter.runtime.model.NormalPage page}\r\n" + //
				"{page.data.|}";
		
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(4, 11, 4, 11)), //
				c("mergeIn(other : JsonObject, deep : boolean) : JsonObject", "mergeIn(other, deep)", r(4, 11, 4, 11)), //
				c("foo : Integer", r(4, 11, 4, 11)));
	}
	
	private static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		CompletionParameters p = new CompletionParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.getCompletionSettings().getCompletionCapabilities().getCompletionItem().setSnippetSupport(false);
		QuteAssert.testCompletionFor(value, p, expectedItems);
	}


}