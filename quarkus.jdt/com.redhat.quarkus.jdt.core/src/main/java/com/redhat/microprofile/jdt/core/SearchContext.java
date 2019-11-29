package com.redhat.microprofile.jdt.core;

import java.util.HashMap;
import java.util.Map;

public class SearchContext {

	private final Map<String, Object> cache;

	public SearchContext() {
		cache = new HashMap<>();
	}

	public void put(String key, Object value) {
		cache.put(key, value);
	}

	public Object get(String key) {
		return cache.get(key);
	}

}
