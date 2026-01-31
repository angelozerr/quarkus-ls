package com.redhat.qute.project.extensions;

import java.nio.file.Path;
import java.util.Collection;

import com.redhat.qute.parser.injection.InjectionDetector;

public interface TemplateLanguageInjectionParticipant {

	Collection<InjectionDetector> getInjectionDetectorsFor(Path path);

	boolean isEnabled();

}
