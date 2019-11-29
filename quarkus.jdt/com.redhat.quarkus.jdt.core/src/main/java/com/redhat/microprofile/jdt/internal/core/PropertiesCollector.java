package com.redhat.microprofile.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.microprofile.commons.MetadataProperty;
import com.redhat.microprofile.commons.ReferenceProperties;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;

public class PropertiesCollector implements IPropertiesCollector {

	private final Map<String, ReferenceProperties> referenceProperties;

	public PropertiesCollector() {
		this.referenceProperties = new HashMap<>();
	}

	@Override
	public MetadataProperty addMetadataProperty(String referenceType, String propertyName, String type,
			String defaultValue, String docs, String location, String extensionName, String source,
			List<EnumItem> enums, ConfigPhase configPhase) {

		if (ConfigItem.NO_DEFAULT.equals(defaultValue)) {
			defaultValue = null;
		}
		MetadataProperty property = new MetadataProperty();
		property.setPropertyName(propertyName);
		property.setType(type);
		property.setDefaultValue(defaultValue);
		property.setDocs(docs);

		// Extra properties

		property.setExtensionName(extensionName);
		property.setLocation(location);
		property.setSource(source);
		if (configPhase != null) {
			property.setPhase(getPhase(configPhase));
		}
		property.setRequired(defaultValue == null);
		property.setEnums(enums);

		ReferenceProperties reference = referenceProperties.get(referenceType);
		if (reference == null) {
			reference = new ReferenceProperties();
			reference.setType(referenceType);
			reference.setProperties(new ArrayList<>());
			referenceProperties.put(referenceType, reference);
		}
		reference.getProperties().add(property);
		return property;
	}

	private static int getPhase(ConfigPhase configPhase) {
		switch (configPhase) {
		case BUILD_TIME:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
		case BUILD_AND_RUN_TIME_FIXED:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
		case RUN_TIME:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_RUN_TIME;
		default:
			return ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
		}
	}

	public List<ReferenceProperties> getReferenceProperties() {
		return new ArrayList<>(referenceProperties.values());
	}
}
