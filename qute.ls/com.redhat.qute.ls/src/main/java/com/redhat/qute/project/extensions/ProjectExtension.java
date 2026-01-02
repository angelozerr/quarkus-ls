package com.redhat.qute.project.extensions;

import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

public interface ProjectExtension {

	void start(ExtendedDataModelProject project);

	void stop(ExtendedDataModelProject project);

	boolean isEnabled();
}
