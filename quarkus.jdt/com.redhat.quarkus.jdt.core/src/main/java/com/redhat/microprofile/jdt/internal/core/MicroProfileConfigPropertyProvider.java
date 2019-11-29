package com.redhat.microprofile.jdt.internal.core;

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyEnumerations;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertySource;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_PROPERTY_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.utils.AnnotationUtils.getAnnotationMemberValue;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.jdt.core.AbstractPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.quarkus.commons.EnumItem;

import io.quarkus.runtime.annotations.ConfigPhase;

public class MicroProfileConfigPropertyProvider extends AbstractPropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { CONFIG_PROPERTY_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor)
			throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.FIELD) {
			String propertyName = getAnnotationMemberValue(configPropertyAnnotation, "name");
			if (propertyName != null && !propertyName.isEmpty()) {
				IField field = (IField) javaElement;
				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
				String defaultValue = getAnnotationMemberValue(configPropertyAnnotation, "defaultValue");
				// Location (JAR, src)
				IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				String location = packageRoot.getPath().toString();

				String extensionName = null;
				ConfigPhase configPhase = null;
				String type = getPropertyType(fieldClass, fieldTypeName);
				String docs = null;
				String source = getPropertySource(field);
				List<EnumItem> enums = getPropertyEnumerations(fieldClass);

				collector.addMetadataProperty(MicroProfileProjectInfo.DEFAULT_REFERENCE_TYPE, propertyName, type,
						defaultValue, docs, location, extensionName, source, enums, configPhase);
			}
		}

	}

}
