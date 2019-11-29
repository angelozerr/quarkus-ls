package com.redhat.microprofile.commons;

import java.util.List;

import com.redhat.quarkus.commons.ClasspathKind;

public class MicroProfileProjectInfo {

	public static final String DEFAULT_REFERENCE_TYPE = "${property}";

	private String projectURI;

	private ClasspathKind classpathKind;

	private List<ReferenceProperties> properties;

	/**
	 * Returns the project URI.
	 * 
	 * @return the project URI.
	 */
	public String getProjectURI() {
		return projectURI;
	}

	/**
	 * Set the project URI.
	 * 
	 * @param projectURI the project URI.
	 */
	public void setProjectURI(String projectURI) {
		this.projectURI = projectURI;
	}

	public ClasspathKind getClasspathKind() {
		return classpathKind;
	}

	public void setClasspathKind(ClasspathKind classpathKind) {
		this.classpathKind = classpathKind;
	}

	public List<ReferenceProperties> getProperties() {
		return properties;
	}

	public void setProperties(List<ReferenceProperties> properties) {
		this.properties = properties;
	}

}
