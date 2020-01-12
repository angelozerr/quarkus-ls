package com.redhat.qute.parser;

public class Template extends Node {

	private final String templateId;
	
	Template(String templateId, int start, int end) {
		super(start, end);
		super.setClosed(true);
		this.templateId = templateId;
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Template;
	}
	
	public String getNodeName() {
		return "#template";
	}

	public String getId() {
		return templateId;
	}
}
