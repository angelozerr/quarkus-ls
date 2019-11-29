package com.redhat.microprofile.commons;

import java.util.List;

public class ReferenceProperties {

	private String type;
	private List<MetadataProperty> properties;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<MetadataProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<MetadataProperty> properties) {
		this.properties = properties;
	}

}
