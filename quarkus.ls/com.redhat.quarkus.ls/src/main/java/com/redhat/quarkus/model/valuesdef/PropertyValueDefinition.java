package com.redhat.quarkus.model.valuesdef;

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

public class PropertyValueDefinition {

	private PropertyMatcher matcher;

	private List<EnumItem> values;

	private transient boolean valuesCleaned;

	public PropertyMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(PropertyMatcher matcher) {
		this.matcher = matcher;
	}

	public void setValues(List<EnumItem> values) {
		this.values = values;
	}

	public List<EnumItem> getValues() {
		cleanValues();
		return values;
	}

	private void cleanValues() {
		if (valuesCleaned || values == null || values.isEmpty()) {
			return;
		}
		try {
			values = values.stream().filter(e -> e.getName() != null && !e.getName().isEmpty())
					.collect(Collectors.toList());
		} finally {
			valuesCleaned = true;
		}
	}

	public boolean match(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		return getMatcher().match(metadata, model);
	}

}
