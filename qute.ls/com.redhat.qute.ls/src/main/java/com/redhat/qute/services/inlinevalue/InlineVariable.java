package com.redhat.qute.services.inlinevalue;

import org.eclipse.lsp4j.Range;

public class InlineVariable {

	private final String name;
	private final Range range;
	
	private String value;

	public InlineVariable(String name, Range range) {
		this.name = name;
		this.range = range;
	}

	public String getName() {
		return name;
	}

	public Range getRange() {
		return range;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
