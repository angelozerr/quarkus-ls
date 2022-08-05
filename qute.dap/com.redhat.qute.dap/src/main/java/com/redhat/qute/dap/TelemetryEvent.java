package com.redhat.qute.dap;

import java.util.Map;

public class TelemetryEvent {

	public final String name;
	public final Map<String, Object> properties;

	public TelemetryEvent(String name, Map<String, Object> properties) {
		this.name = name;
		this.properties = properties;
	}

}