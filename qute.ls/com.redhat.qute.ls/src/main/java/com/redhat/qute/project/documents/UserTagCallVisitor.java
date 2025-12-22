package com.redhat.qute.project.documents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.project.tags.UserTagRegistry;
import com.redhat.qute.project.tags.UserTagRegistry.UserTagCall;

public class UserTagCallVisitor extends ASTVisitor {

	private final String uri;
	private final UserTagRegistry userTagRegistry;
	private final Map<String, List<Parameter>> calls;
	private Set<String> tagNames;

	public UserTagCallVisitor(String uri, UserTagRegistry userTagRegistry) {
		this.uri = uri;
		this.userTagRegistry = userTagRegistry;
		this.calls = new HashMap<>();
	}

	@Override
	public boolean visit(Template node) {
		tagNames = new HashSet<>(calls.keySet());
		calls.clear();
		return super.visit(node);
	}

	@Override
	public void endVisit(Template node) {
		userTagRegistry.registerCalls(uri, calls, tagNames);
		super.endVisit(node);
	}

	@Override
	public boolean visit(CustomSection node) {
		List<Parameter> parameters = node.getParameters();
		if (!parameters.isEmpty()) {
			String userTagName = node.getTag();
			tagNames.remove(userTagName);
			calls.put(userTagName, parameters);
		}
		return super.visit(node);
	}

}
