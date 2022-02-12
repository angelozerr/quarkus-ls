package com.redhat.qute.project.tags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.services.completions.QuteCompletionsForSnippets;

public class QuteCompletionsForUserTagSection extends QuteCompletionsForSnippets<UserTag> {

	public QuteCompletionsForUserTagSection() {
		super(false);
	}

	public void refresh(Collection<UserTag> userTags) {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		snippetRegistry.getSnippets().clear();
		for (UserTag userTag : userTags) {
			snippetRegistry.registerSnippet(userTag);
		}
	}

	public Collection<UserTag> getUserTags() {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		return snippetRegistry.getSnippets();
	}

	public void refresh(Path tagsDir) {
		if (!Files.exists(tagsDir)) {
			return;
		}
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		List<UserTag> snippets = snippetRegistry.getSnippets();
		try {
			if (snippets.isEmpty()) {
				// create all user tags
				Files.list(tagsDir) //
						.forEach(path -> {
							snippetRegistry.registerSnippet(createUserTag(path));
						});
			} else {
				// Remove all user tags which doesn't exist anymore
				List<UserTag> existingSnippets = new ArrayList<UserTag>(snippets);
				for (UserTag userTag : existingSnippets) {
					if (!Files.exists(userTag.getPath())) {
						snippets.remove(userTag);
					}
				}
				// Add new snippets
				Set<Path> existingSnippetPaths = snippets.stream().map(snippet -> snippet.getPath())
						.collect(Collectors.toSet());
				Files.list(tagsDir) //
						.forEach(path -> {
							if (!existingSnippetPaths.contains(path)) {
								snippetRegistry.registerSnippet(createUserTag(path));
							}
						});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static UserTag createUserTag(Path path) {
		String tag = path.getName(path.getNameCount() - 1).toString();
		String tagName = tag.contains(".") ? tag.substring(0, tag.lastIndexOf('.')) : tag;
		return new UserTag(tagName, path);
	}
}
