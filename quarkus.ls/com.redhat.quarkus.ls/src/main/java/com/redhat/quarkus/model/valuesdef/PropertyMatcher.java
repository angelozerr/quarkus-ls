package com.redhat.quarkus.model.valuesdef;

import java.util.Collection;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

public class PropertyMatcher {

	private Collection<String> names;

	private Collection<String> types;

	public Collection<String> getNames() {
		return names;
	}

	public void setNames(Collection<String> names) {
		this.names = names;
	}

	public Collection<String> getTypes() {
		return types;
	}

	public void setTypes(Collection<String> types) {
		this.types = types;
	}

	public boolean match(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		String propertyName = metadata.getPropertyName();
		String propertyType = metadata.getType();
		if (names != null && types != null) {
			return matchName(propertyName) && matchType(propertyType);
		} else if (names != null) {
			return matchName(propertyName);
		} else if (types != null) {
			return matchType(propertyType);
		}
		return false;
	}

	private boolean matchName(String propertyName) {
		return names.contains(propertyName);
	}

	private boolean matchType(String propertyType) {
		return types.contains(propertyType);
	}
}
