/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression with Java Map.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithMapTest {

	@Test
	public void map() throws Exception {
		String template = "{@java.util.Map<java.lang.String,org.acme.Item> map}\r\n" + //
				"Item: {|}";
		testCompletionFor(template, //
				c("map", "map", r(1, 7, 1, 7)));
	}
	
	@Test
	public void entrySet() throws Exception {
		String template = "{@java.util.Map<java.lang.String,org.acme.Item> map}\r\n" + //
				"Item: {map.|}";
		testCompletionFor(template, //
				c("entrySet", "entrySet", r(1, 7, 1, 7)));
	}
}