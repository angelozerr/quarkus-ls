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
package com.redhat.qute.project.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;

/**
 * Test with {@link UserTagInfoCollector} which collects parameters (name,
 * required, default value) of a given user tag.
 */
public class UserTagInfoCollectorTest {

	@Test
	public void paramFromObjectPart() {
		assertUserTagParameter("{foo}", //
				p("foo", true));
	}

	@Test
	public void optionalParamFromObjectPart() {
		assertUserTagParameter("{foo??}", //
				p("foo", false));
	}

	private static void assertUserTagParameter(String content, UserTagParameter... expected) {
		Template template = TemplateParser.parse(content, "test.qute");
		UserTagInfoCollector collector = new UserTagInfoCollector(null);
		template.accept(collector);

		var actual = collector.getParameters();
		assertEquals(expected.length, actual.size(), "Wrong parameters length");
		for (UserTagParameter expectedParameter : expected) {
			String name = expectedParameter.getName();
			UserTagParameter actualParameter = actual.get(name);
			assertNotNull(actualParameter, "Cannot find parameter with name=" + name);
			assertEquals(expectedParameter.isRequired(), actualParameter.isRequired(),
					"Wrong required for parameter name=" + name);
			assertEquals(expectedParameter.getDefaultValue(), actualParameter.getDefaultValue(),
					"Wrong defaultValue for parameter name=" + name);
		}
	}

	private static UserTagParameter p(String name, boolean required) {
		return p(name, required, null);
	}

	private static UserTagParameter p(String name, boolean required, String defaultValue) {
		UserTagParameter p = new UserTagParameter(name);
		p.setRequired(required);
		p.setDefaultValue(defaultValue);
		return p;
	}
}
