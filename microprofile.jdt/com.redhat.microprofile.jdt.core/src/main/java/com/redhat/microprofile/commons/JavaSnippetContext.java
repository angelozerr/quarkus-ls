package com.redhat.microprofile.commons;

import java.util.List;

public class JavaSnippetContext {

	private JavaKind kind;
	
	private List<String> type;

	public JavaKind getKind() {
		return kind;
	}

	public void setKind(JavaKind kind) {
		this.kind = kind;
	}

	public List<String> getType() {
		return type;
	}

	public void setType(List<String> type) {
		this.type = type;
	}
	
	
}
