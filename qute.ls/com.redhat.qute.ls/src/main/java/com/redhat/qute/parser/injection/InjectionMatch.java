package com.redhat.qute.parser.injection;

/**
 * Represents the result of an injection detection.
 *
 * Design idea:
 * - Detecting an injection is an atomic operation.
 * - Once detected, we must keep BOTH:
 *   - the detector that knows how to scan it
 *   - the metadata describing the injected language
 *
 * This avoids re-detecting the injection later and prevents ambiguity.
 */
public final class InjectionMatch {

	// The detector responsible for this injection
	private final InjectionDetector detector;

	// Metadata describing the injected language (id, mode, etc.)
	private final InjectionMetadata metadata;

	public InjectionMatch(InjectionDetector detector, InjectionMetadata metadata) {
		this.detector = detector;
		this.metadata = metadata;
	}

	public InjectionDetector getDetector() {
		return detector;
	}

	public InjectionMetadata getMetadata() {
		return metadata;
	}
}
