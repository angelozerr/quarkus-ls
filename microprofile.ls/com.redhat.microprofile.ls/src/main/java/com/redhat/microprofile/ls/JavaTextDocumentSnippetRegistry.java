package com.redhat.microprofile.ls;

import java.util.ArrayList;
import java.util.List;

import com.redhat.microprofile.ls.commons.snippets.Snippet;
import com.redhat.microprofile.ls.commons.snippets.TextDocumentSnippetRegistry;
import com.redhat.microprofile.snippets.LanguageId;
import com.redhat.microprofile.snippets.SnippetContextForJava;

public class JavaTextDocumentSnippetRegistry extends TextDocumentSnippetRegistry {

	private List<String> types;

	public JavaTextDocumentSnippetRegistry() {
		super(LanguageId.java.name());
	}

	public List<String> getTypes() {
		if (types != null) {
			return types;
		}
		types = collectTypes();
		return types;
	}

	private synchronized List<String> collectTypes() {
		if (types != null) {
			return types;
		}
		List<String> types = new ArrayList<>();
		for (Snippet snippet : getSnippets()) {
			if (snippet.getContext() != null && snippet.getContext() instanceof SnippetContextForJava) {
				List<String> t = ((SnippetContextForJava) snippet.getContext()).getTypes();
				if (t != null) {
					types.addAll(t);
				}
			}
		}
		return types;
	}
}
