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
package com.redhat.qute.project.tags;

import java.nio.file.Path;
import java.util.Arrays;

import com.redhat.qute.ls.commons.snippets.Snippet;

public class UserTag extends Snippet {

	private final Path path;

	public UserTag(String name, Path path) {
		this.path = path;
		super.setLabel(name);
		super.setPrefixes(Arrays.asList(name));
		super.setBody(Arrays.asList("{#" + name + " /}$0"));
	}

	public String getName() {
		return getLabel();
	}

	public String getUri() {
		return path.toUri().toString();
	}

	public Path getPath() {
		return path;
	}
}
