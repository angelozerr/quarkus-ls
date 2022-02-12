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
import java.util.Collection;

import org.eclipse.lsp4j.CompletionList;

import com.redhat.qute.services.completions.CompletionRequest;

public class UserTagRegistry {

	private final Path tagsDir;

	private final QuteCompletionsForUserTagSection completionsForSnippets;

	public UserTagRegistry(Path templateBaseDir) {
		this.tagsDir = templateBaseDir.resolve("tags");
		this.completionsForSnippets = new QuteCompletionsForUserTagSection();
	}

	public Collection<UserTag> getUserTags() {
		refresh();
		return completionsForSnippets.getUserTags();
	}

	private void refresh() {
		completionsForSnippets.refresh(tagsDir);
	}

	public Path getTagsDir() {
		return tagsDir;
	}

	public void collectSnippetSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			CompletionList list) {
		// refresh user tags
		refresh();
		completionsForSnippets.collectSnippetSuggestions(completionRequest, prefixFilter, suffixToFind, list);
	}
}
