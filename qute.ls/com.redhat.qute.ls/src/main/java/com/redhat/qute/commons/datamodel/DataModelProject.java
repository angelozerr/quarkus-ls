/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import java.util.List;
import java.util.Optional;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.commons.ValueResolver;

/**
 * Data model project hosts for a given Qute project:
 * 
 * <ul>
 * <li>data model templates</li>
 * <li>value resolvers</li>
 * </ul>
 * .
 * 
 * @author Angelo ZERR
 *
 * @param <T> data model template.
 */
public class DataModelProject<T extends DataModelTemplate<?>> {

	private static final String QUTE_FILE_EXTENSION = ".qute";

	private List<T> templates;

	private List<ValueResolver> valueResolvers;

	/**
	 * Returns the list of data model templates which belong to this project.
	 * 
	 * @return the list of data model templates which belong to this project.
	 */
	public List<T> getTemplates() {
		return templates;
	}

	/**
	 * Set the list of data model templates which belong to this project.
	 * 
	 * @param templates the list of data model templates which belong to this
	 *                  project.
	 */
	public void setTemplates(List<T> templates) {
		this.templates = templates;
	}

	/**
	 * Return the list of value resolvers which belong to this project.
	 * 
	 * @return the list of value resolvers which belong to this project.
	 */
	public List<ValueResolver> getValueResolvers() {
		return valueResolvers;
	}

	/**
	 * Set the list of value resolvers which belong to this project.
	 * 
	 * @param valueResolvers the list of value resolvers which belong to this
	 *                       project.
	 */
	public void setValueResolvers(List<ValueResolver> valueResolvers) {
		this.valueResolvers = valueResolvers;
	}

	/**
	 * Returns data model template find by the given template uri and null
	 * otherwise.
	 * 
	 * @param templateUri the template uri.
	 * 
	 * @return data model template find by the given template uri and null
	 *         otherwise.
	 */
	public T findDataModelTemplate(String templateUri) {
		List<T> templates = this.getTemplates();
		if (templates == null || templates.isEmpty()) {
			return null;
		}

		// Try to find template by the given uri (with extension)

		// @Location("detail/items2_v1.html")
		// Template items2;
		T template = findDataModelTemplate(templateUri, templates);
		if (template != null) {
			return template;
		}

		// Try to find template by the given uri (without extension)

		// @Location("detail/items2_v1.html")
		// Template items2;

		String templateUriWithoutExtension = getUriWithoutExtension(templateUri);
		return findDataModelTemplate(templateUriWithoutExtension, templates);
	}

	public String getUriWithoutExtension(String templateUri) {
		int dotIndex = templateUri.lastIndexOf('.');
		if (dotIndex != -1) {
			templateUri = templateUri.substring(0, dotIndex);
		}
		if (templateUri.endsWith(QUTE_FILE_EXTENSION)) {
			templateUri = templateUri.substring(0, templateUri.length() - QUTE_FILE_EXTENSION.length());
		}
		return templateUri;
	}

	private T findDataModelTemplate(String templateUri, List<T> templates) {
		Optional<T> dataModelForTemplate = templates.stream() //
				.filter(t -> templateUri.endsWith(t.getTemplateUri())) //
				.findFirst();
		if (dataModelForTemplate.isPresent()) {
			return dataModelForTemplate.get();
		}
		return null;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("templates", this.templates);
		b.add("valueResolvers", this.valueResolvers);
		return b.toString();
	}
}
