package com.redhat.microprofile.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.jdt.core.AbstractPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;

public class MicroProfileRestClientProvider extends AbstractPropertiesProvider {

	private static final String[] ANNOTATION_NAMES = {
			"org.eclipse.microprofile.rest.client.inject.RegisterRestClient" };

	private static final String MP_REST_CLASS_REFERENCE_TYPE = "${mp-rest-class}";

	private static final String MP_REST_ADDED = MicroProfileRestClientProvider.class.getName() + "#mp-rest";

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor)
			throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			if (context.get(MP_REST_ADDED) == null) {
				// /mp-rest/url
				String docs = "The base URL to use for this service,\r\n"
						+ "the equivalent of the baseUrl method. This property is considered required, however\r\n"
						+ "implementations may have other ways to define these URLs.\r\n" + "";
				collector.addMetadataProperty(MicroProfileProjectInfo.DEFAULT_REFERENCE_TYPE,
						MP_REST_CLASS_REFERENCE_TYPE + "/mp-rest/url", "java.lang.String", null, docs, null, null, null,
						null, null);
				// /mp-rest/scope
				docs = "The fully qualified classname to a\r\n"
						+ "CDI scope to use for injection, defaults to javax.enterprise.context.Dependent as mentioned\r\n"
						+ "above.";
				collector.addMetadataProperty(MicroProfileProjectInfo.DEFAULT_REFERENCE_TYPE,
						MP_REST_CLASS_REFERENCE_TYPE + "/mp-rest/scope", "java.lang.String",
						"javax.enterprise.context.Dependent", docs, null, null, null, null, null);
				context.put(MP_REST_ADDED, Boolean.TRUE);
			}
			IType type = (IType) javaElement;
			String docs = null;
			String location = null;
			String source = type.getFullyQualifiedName();
			collector.addMetadataProperty(MP_REST_CLASS_REFERENCE_TYPE, type.getFullyQualifiedName(),
					type.getFullyQualifiedName(), null, docs, location, null, source, null, null);

		}

	}

}
