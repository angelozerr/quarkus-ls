package com.redhat.microprofile.jdt.core;

import java.util.List;

import com.redhat.microprofile.commons.MetadataProperty;
import com.redhat.quarkus.commons.EnumItem;

import io.quarkus.runtime.annotations.ConfigPhase;

public interface IPropertiesCollector {

	MetadataProperty addMetadataProperty(String referenceType, String propertyName, String type, String defaultValue,
			String docs, String location, String extensionName, String source, List<EnumItem> enums,
			ConfigPhase configPhase);
}
