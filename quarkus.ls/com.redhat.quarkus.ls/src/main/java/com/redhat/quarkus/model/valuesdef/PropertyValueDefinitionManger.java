package com.redhat.quarkus.model.valuesdef;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

public class PropertyValueDefinitionManger {

	private final List<PropertyValueDefinition> definitions;

	public PropertyValueDefinitionManger(boolean withDefault) {
		this.definitions = new ArrayList<>();
		if (withDefault) {
			load(PropertyValueDefinitionManger.class.getResourceAsStream("quarkus-values.json"));
		}
	}

	private void load(InputStream in) {
		ValuesDef def = new Gson().fromJson(new InputStreamReader(in), ValuesDef.class);
		if (def.getDefinitions() != null) {
			registerDefinitions(def.getDefinitions());
		}
	}

	public void registerDefinitions(List<PropertyValueDefinition> definitions) {
		this.definitions.addAll(definitions);
	}

	public void unregisterDefinitions(List<PropertyValueDefinition> definitions) {
		this.definitions.removeAll(definitions);
	}

	public List<EnumItem> getValues(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		for (PropertyValueDefinition definition : definitions) {
			if (definition.match(metadata, model)) {
				return definition.getValues();
			}
		}
		return null;
	}

	public boolean isValidEnum(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model, String value) {
		List<EnumItem> enums = getValues(metadata, model);
		return ExtendedConfigDescriptionBuildItem.isValidEnum(value, enums);
	}

	public EnumItem getEnumItem(String propertyValue, ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		List<EnumItem> enums = getValues(metadata, model);
		return ExtendedConfigDescriptionBuildItem.getEnumItem(propertyValue, enums);
	}
}
