package com.redhat.qute.ls.api.autoinsert;

public enum AutoInsertKind {
	autoQuote(1), //
	autoClose(2);

	private final int value;

	AutoInsertKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static AutoInsertKind forValue(int value) {
		AutoInsertKind[] allValues = AutoInsertKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}
}
