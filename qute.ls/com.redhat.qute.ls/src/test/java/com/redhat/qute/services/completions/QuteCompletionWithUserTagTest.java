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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.RESOLVERS_SIZE;
import static com.redhat.qute.QuteAssert.SECTION_SNIPPET_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static java.lang.System.lineSeparator;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with user tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithUserTagTest {

	@Test
	public void bundleStyle() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("bundleStyle", //
						"{#bundleStyle /}", //
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("bundleStyle", //
						"{#bundleStyle /}$0", //
						r(0, 0, 0, 2)));
	}
	
	@Test
	public void bundleStyleWithingParameter() throws Exception {
		String template = "{#bundleStyle |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				1, //
				c("name", //
						"name=\"main.css\"", //
						r(0, 14, 0, 14)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				1, //
				c("name", //
						"name=\"${1:main.css}\"$0", //
						r(0, 14, 0, 14)));
	}

	@Test
	public void input() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("input", "{#input name=\"name\" /}", r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("input", "{#input name=\"${1:name}\" /}$0", r(0, 0, 0, 2)));
	}

	@Test
	public void inputAndParameters() throws Exception {
		String template = "{#input |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				5, //
				c("name", "name=\"name\"", r(0, 8, 0, 8)), //
				c("type", "type=\"type\"", r(0, 8, 0, 8)), //
				c("placeholder", "placeholder=\"placeholder\"", r(0, 8, 0, 8)), //
				c("id", "id=\"id\"", r(0, 8, 0, 8)), //
				c("value", "value=\"value\"", r(0, 8, 0, 8)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				5, //
				c("name", "name=\"${1:name}\"$0", r(0, 8, 0, 8)), //
				c("type", "type=\"${1:type}\"$0", r(0, 8, 0, 8)), //
				c("placeholder", "placeholder=\"${1:placeholder}\"$0", r(0, 8, 0, 8)), //
				c("id", "id=\"${1:id}\"$0", r(0, 8, 0, 8)), //
				c("value", "value=\"${1:value}\"$0", r(0, 8, 0, 8)));
	}

	@Test
	public void form() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("form", //
						"{#form it }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/form}",
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("form", //
						"{#form ${1:it} }" + lineSeparator() + //
								"	$2" + lineSeparator() + //
								"{/form}$0",
						r(0, 0, 0, 2)));
	}

	@Test
	public void formAndParameters() throws Exception {
		String template = "{#form |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				RESOLVERS_SIZE + 3, //
				c("method", "method=\"method\"", r(0, 7, 0, 7)), //
				c("class", "class=\"class\"", r(0, 7, 0, 7)), //
				c("id", "id=\"id\"", r(0, 7, 0, 7)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				RESOLVERS_SIZE + 3, //
				c("method", "method=\"${1:method}\"$0", r(0, 7, 0, 7)), //
				c("class", "class=\"${1:class}\"$0", r(0, 7, 0, 7)), //
				c("id", "id=\"${1:id}\"$0", r(0, 7, 0, 7)));
	}

	@Test
	public void formAndParametersWithExistingIt() throws Exception {
		String template = "{#form uri:Login.confirm() |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				3, //
				c("method", "method=\"method\"", r(0, 27, 0, 27)), //
				c("class", "class=\"class\"", r(0, 27, 0, 27)), //
				c("id", "id=\"id\"", r(0, 27, 0, 27)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				3, //
				c("method", "method=\"${1:method}\"$0", r(0, 27, 0, 27)), //
				c("class", "class=\"${1:class}\"$0", r(0, 27, 0, 27)), //
				c("id", "id=\"${1:id}\"$0", r(0, 27, 0, 27)));

		template = "{#form | uri:Login.confirm()";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				3, //
				c("method", "method=\"method\"", r(0, 7, 0, 7)), //
				c("class", "class=\"class\"", r(0, 7, 0, 7)), //
				c("id", "id=\"id\"", r(0, 7, 0, 7)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				3, //
				c("method", "method=\"${1:method}\"$0", r(0, 7, 0, 7)), //
				c("class", "class=\"${1:class}\"$0", r(0, 7, 0, 7)), //
				c("id", "id=\"${1:id}\"$0", r(0, 7, 0, 7)));
	}

	@Test
	public void formElement() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"name\" label=\"label\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/formElement}", //
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"${1:name}\" label=\"${2:label}\" }" + lineSeparator() + //
								"	$3" + lineSeparator() + //
								"{/formElement}$0", //
						r(0, 0, 0, 2)));
	}

	@Test
	public void formElementAndParameters() throws Exception {
		String template = "{#formElement |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				3, //
				c("name", "name=\"name\"", r(0, 14, 0, 14)), //
				c("label", "label=\"label\"", r(0, 14, 0, 14)), //
				c("class", "class=\"class\"", r(0, 14, 0, 14)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				3, //
				c("name", "name=\"${1:name}\"$0", r(0, 14, 0, 14)), //
				c("label", "label=\"${1:label}\"$0", r(0, 14, 0, 14)), //
				c("class", "class=\"${1:class}\"$0", r(0, 14, 0, 14)));
	}

	@Test
	public void formElementAndParametersInParamName() throws Exception {
		String template = "{#formElement cl|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				3, //
				c("name", "name=\"name\"", r(0, 14, 0, 16)), //
				c("label", "label=\"label\"", r(0, 14, 0, 16)), //
				c("class", "class=\"class\"", r(0, 14, 0, 16)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				3, //
				c("name", "name=\"${1:name}\"$0", r(0, 14, 0, 16)), //
				c("label", "label=\"${1:label}\"$0", r(0, 14, 0, 16)), //
				c("class", "class=\"${1:class}\"$0", r(0, 14, 0, 16)));
	}

	@Test
	public void formElementAndParametersInParamValue() throws Exception {
		String template = "{#formElement class=|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				RESOLVERS_SIZE);

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				RESOLVERS_SIZE);
	}

	@Test
	public void simpleTitle() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("simpleTitle", //
						"{#simpleTitle title=\"title\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/simpleTitle}",
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("simpleTitle", //
						"{#simpleTitle title=\"${1:title}\" }" + lineSeparator() + //
								"	$2" + lineSeparator() + //
								"{/simpleTitle}$0",
						r(0, 0, 0, 2)));
	}

	@Test
	public void simpleTitleAndParameters() throws Exception {
		String template = "{#simpleTitle |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				1, //
				c("title", "title=\"title\"", r(0, 14, 0, 14)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				1, //
				c("title", "title=\"${1:title}\"$0", r(0, 14, 0, 14)));
	}

	@Test
	public void titleAndParameters() throws Exception {
		String template = "{#title |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				1, //
				c("titularTitle", "titularTitle=\"titularTitle\"", r(0, 8, 0, 8)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				1, //
				c("titularTitle", "titularTitle=\"${1:titularTitle}\"$0", r(0, 8, 0, 8)));
	}

	@Test
	public void userTagAndCloseBracket() throws Exception {
		String template = "{#f|or   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"name\" label=\"label\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/formElement}", //
						r(0, 0, 0, 9)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"${1:name}\" label=\"${2:label}\" }" + lineSeparator() + //
								"	$3" + lineSeparator() + //
								"{/formElement}$0", //
						r(0, 0, 0, 9)));
	}

	@Test
	public void parameterExpressionInUserTag() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user name=| /}";
		testCompletionFor(template, //
				c("item", "item", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"{#user name=item.| /}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 17, 1, 17)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 17, 1, 17)), //
				c("price : BigInteger", "price", r(1, 17, 1, 17)), //
				c("review : Review", "review", r(1, 17, 1, 17)), //
				c("review2 : Review", "review2", r(1, 17, 1, 17)), //
				c("getReview2() : Review", "getReview2", r(1, 17, 1, 17)));
	}

	@Test
	public void itParameterExpressionInUserTag() throws Exception {
		// With user tag
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user | /}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"{#user item.| /}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : Review", "review", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		// With #let
		template = "{@org.acme.Item item}\r\n" + //
				"{#let item.| }";
		testCompletionFor(template, 0);
	}

	@Test
	public void emptyStartTagAndEndTag() throws Exception {
		String template = "{#|}{/}";

		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement name=\"name\" label=\"label\" }", //
						r(0, 0, 0, 3)));

		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement name=\"${1:name}\" label=\"${2:label}\" }", //
						r(0, 0, 0, 3)));
	}

	@Test
	public void existingStartTagAndEmptyEndTag() throws Exception {
		String template = "{#fo|r}{/}";

		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}", //
						r(0, 0, 0, 6)),
				c("formElement", "{#formElement name=\"name\" label=\"label\" }", //
						r(0, 0, 0, 6)));

		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}", //
						r(0, 0, 0, 6)),
				c("formElement", "{#formElement name=\"${1:name}\" label=\"${2:label}\" }", //
						r(0, 0, 0, 6)));
	}

	@Test
	public void enclosed() throws Exception {
		String template = "{#for}{#fo|r}{/}";

		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE + 1 /* #else (see #for-else inqute-nested.json) */, //
				c("for", "{#for item in items}" + System.lineSeparator()
						+ "	{item.name}" + System.lineSeparator()
						+ "{/for}", //
						r(0, 6, 0, 12)),
				c("formElement", "{#formElement name=\"name\" label=\"label\" }" + System.lineSeparator()
						+ "	" + System.lineSeparator()
						+ "{/formElement}", //
						r(0, 6, 0, 12)));

		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE + 1 /* #else (see #for-else inqute-nested.json) */, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator()
						+ "	{${1:item}.${3:name}}$0" + System.lineSeparator()
						+ "{/for}", //
						r(0, 6, 0, 12)),
				c("formElement", "{#formElement name=\"${1:name}\" label=\"${2:label}\" }" + System.lineSeparator()
						+ "	$3" + System.lineSeparator()
						+ "{/formElement}$0", //
						r(0, 6, 0, 12)));
	}

	@Test
	public void existingStartTagAndEmptyEndTagWithParameter() throws Exception {
		String template = "{#fo|r it }{/}";

		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for", //
						r(0, 0, 0, 5)),
				c("formElement", "{#formElement", //
						r(0, 0, 0, 5)));

		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for", //
						r(0, 0, 0, 5)),
				c("formElement", "{#formElement", //
						r(0, 0, 0, 5)));
	}

	@Test
	public void existingStartTagAndExistingEndTag() throws Exception {
		String template = "{#fo|r}{/for}";

		testCompletionFor(template, //
				false, // no snippet support
				0);

		testCompletionFor(template, //
				true, // snippet support
				0);
	}

	@Test
	public void tagWithArgs() throws Exception {
		// Here the content of tagWithArgs.html:
		
		// {_args.filter('readonly').asHtmlAttributes}
		// {_args}
		// {foo}
		
		String template = "{#tagWithArgs |}";

		testCompletionFor(template, //
				false, // no snippet support
				1, //
				c("foo", "foo=\"foo\"", r(0, 14, 0, 14)));

		testCompletionFor(template, //
				true, // snippet support
				1, //
				c("foo", "foo=\"${1:foo}\"$0", r(0, 14, 0, 14)));
	}
	
	@Test
	public void number_in_section_name() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("ga4", "{#ga4 /}", r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("ga4", "{#ga4 /}$0", r(0, 0, 0, 2)));
	}
	
	@Test
	public void underscore_in_section_name() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("reunion-card", "{#reunion-card /}", r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("reunion-card", "{#reunion-card /}$0", r(0, 0, 0, 2)));
	}
}