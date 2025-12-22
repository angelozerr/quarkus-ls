package com.redhat.qute.project.tags;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Parameter;

public class CallUserTag {

	private final String userTagName;
	private final Map<String, List<Parameter>> parameters;

	public CallUserTag(String userTagName) {
		this.userTagName = userTagName;
		this.parameters = new HashMap<>();
	}

	public JavaTypeInfoProvider inferType(String parameterName) {
		for (Collection<Parameter> parameters : this.parameters.values()) {
			for (Parameter parameter : parameters) {
				if (parameter.getName().equals(parameterName)) {
					return parameter;
				}
			}
		}
		return null;
	}

	public void registerCall(String uri, List<Parameter> parameters) {
		this.parameters.put(uri, parameters);
	}

}
