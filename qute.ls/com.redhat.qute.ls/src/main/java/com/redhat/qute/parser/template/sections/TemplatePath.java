package com.redhat.qute.parser.template.sections;

import java.nio.file.Path;

public class TemplatePath {

	private final String uri;
	private final boolean valid;

	public TemplatePath(Path templatePath, boolean valid) {
		this(templatePath.toUri().toASCIIString(), valid);
	}

	public TemplatePath(String uri, boolean valid) {
		this.uri = uri;
		this.valid = valid;
	}

	public String getUri() {
		return uri;
	}

	public boolean isValid() {
		return valid;
	}

}
