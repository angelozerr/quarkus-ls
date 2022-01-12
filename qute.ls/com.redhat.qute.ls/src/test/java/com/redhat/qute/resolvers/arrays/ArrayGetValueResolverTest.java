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
package com.redhat.qute.resolvers.arrays;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Array 'get' value resolver.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#arrays
 *
 */
public class ArrayGetValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"{items.get(0)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item[] items}\r\n" + //
				"{items.get(0).name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.|}";
		testCompletionFor(template, //
				c("get(base : T[], index : int) : T", "get(index)", r(1, 13, 1, 13)));

		template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.get(0).|}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 20, 1, 20)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 20, 1, 20)), //
				c("price : BigInteger", "price", r(1, 20, 1, 20)), //
				c("review : Review", "review", r(1, 20, 1, 20)), //
				c("review2 : Review", "review2", r(1, 20, 1, 20)), //
				c("getReview2() : Review", "getReview2", r(1, 20, 1, 20)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.ge|t(0)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"T get(int index)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Returns the element at the specified `index` from the given array." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{myArray.get(0)}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#arrays) for more informations.", //
				r(1, 13, 1, 16));

		template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.get(0).nam|e}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 20, 1, 24));
	}
}