/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isSimpleFieldType;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.redhat.microprofile.jdt.core.AbstractClassPropertiesProvider;
import com.redhat.microprofile.jdt.core.ArtifactResolver;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;

import io.quarkus.runtime.util.StringUtil;

/**
 * Properties provider to collect Quarkus properties from the io dekorate config
 * class for Kubernetes, OpenSihift, Docker and S2i and generates the same
 * properties than https://quarkus.io/guides/kubernetes#configuration-options
 * 
 * <p>
 * As Quarkus Kubernetes doesn't use a standard mechanism with
 * Quarkus @ConfigRoot annotation to collect properties. Indeed the
 * io.quarkus.kubernetes.deployment.KubernetesProcessor which manages Quarkus
 * kubernetes.*, openshift.* properties uses the io dekorate project, so we need
 * to introspect the io dekorate config class like
 * io.dekorate.kubernetes.config.KubernetesConfig to generate kubernetes,
 * openshift, docker and s2i properties.
 * </p>
 * 
 * @author Angelo ZERR
 * @see https://quarkus.io/guides/kubernetes#configuration-options
 */
public class QuarkusKubernetesProvider extends AbstractClassPropertiesProvider {

	private static final String S2I_PREFIX = "s2i";
	private static final String DOCKER_PREFIX = "docker";
	private static final String OPENSHIFT_PREFIX = "openshift";
	private static final String KUBERNETES_PREFIX = "kubernetes";
	private static final String KUBERNETES_CONFIG_CLASS = "io.dekorate.kubernetes.config.KubernetesConfig";
	private static final String OPENSHIFT_CONFIG_CLASS = "io.dekorate.openshift.config.OpenshiftConfig";
	private static final String S2I_BUILD_CONFIG_CLASS = "io.dekorate.s2i.config.S2iBuildConfig";
	private static final String DOCKER_BUILD_CONFIG_CLASS = "io.dekorate.docker.config.DockerBuildConfig";

	@Override
	protected String[] getClassNames() {
		return new String[] { KUBERNETES_CONFIG_CLASS, DOCKER_BUILD_CONFIG_CLASS, OPENSHIFT_CONFIG_CLASS,
				S2I_BUILD_CONFIG_CLASS };
	}

	@Override
	public void contributeToClasspath(IJavaProject project, boolean excludeTestCode, ArtifactResolver artifactResolver,
			List<IClasspathEntry> deploymentJarEntries, IProgressMonitor monitor) throws JavaModelException {
		IClasspathEntry[] entries = project.getResolvedClasspath(true);
		for (IClasspathEntry entry : entries) {
			if (excludeTestCode && entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:

				try {
					String jarPath = entry.getPath().toOSString();
					if (jarPath.contains("quarkus-kubernetes")) {
						// FIXME : how to download io dekorate kubernetes-annotations with the proper
						// version ???
						String f = artifactResolver.getArtifact("io.dekorate", "kubernetes-annotations", "0.9.4",
								"noapt", monitor);
						deploymentJarEntries.add(JavaCore.newLibraryEntry(new Path(f), null, null));

						f = artifactResolver.getArtifact("io.dekorate", "openshift-annotations", "0.9.4", "noapt",
								monitor);
						deploymentJarEntries.add(JavaCore.newLibraryEntry(new Path(f), null, null));

						return;
					}
				} catch (Exception e) {
					// do nothing
				}

				break;
			}
		}
	}

	@Override
	protected void processClass(IType configType, String className, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException {
		String configPrefix = getConfigPrefix(className);
		if (configPrefix != null) {
			IPropertiesCollector collector = context.getCollector();
			collectProperties(configPrefix, configType, collector, monitor);
			IType[] types = getAllSuperclasses(configType, monitor);
			for (IType type : types) {
				collectProperties(configPrefix, type, collector, monitor);
			}
			// We need to hard code some properties because the KubernetesProcessor does
			// that
			// too
			switch (configPrefix) {
			case KUBERNETES_PREFIX:
				// kubernetes.deployment.target
				// see
				// https://github.com/quarkusio/quarkus/blob/44e5e2e3a642d1fa7af9ddea44b6ff8d37e862b8/extensions/kubernetes/deployment/src/main/java/io/quarkus/kubernetes/deployment/KubernetesProcessor.java#L94
				super.addItemMetadata(collector, "kubernetes.deployment.target", "java.lang.String", //
						"To enable the generation of OpenShift resources, you need to include OpenShift in the target platforms: `kubernetes.deployment.target=openshift`.\r\n"
								+ "If you need to generate resources for both platforms (vanilla Kubernetes and OpenShift), then you need to include both (coma separated).\r\n"
								+ "`kubernetes.deployment.target=kubernetes, openshift`.",
						null, null, null, KUBERNETES_PREFIX, null, true);
				// kubernetes.registry
				// see
				// https://github.com/quarkusio/quarkus/blob/44e5e2e3a642d1fa7af9ddea44b6ff8d37e862b8/extensions/kubernetes/deployment/src/main/java/io/quarkus/kubernetes/deployment/KubernetesProcessor.java#L103
				super.addItemMetadata(collector, "kubernetes.registry", "java.lang.String", //
						"Specify the `docker registry`.", null, null, null, null, null, true);
				break;
			case OPENSHIFT_PREFIX:
				// openshift.registry
				super.addItemMetadata(collector, "openshift.registry", "java.lang.String", //
						"Specify the `docker registry`.", null, null, null, null, null, true);
				break;
			}
		}
	}

	private void collectProperties(String prefix, IType configType, IPropertiesCollector collector,
			IProgressMonitor monitor) throws JavaModelException {
		String sourceType = configType.getFullyQualifiedName();
		IField[] fields = configType.getFields();
		for (IField field : fields) {
			String fieldTypeName = getResolvedTypeName(field);
			IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
			String fieldName = field.getElementName();
			String propertyName = prefix + "." + StringUtil.hyphenate(fieldName);
			boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;
			if (isArray) {
				propertyName += "[*]";
			}
			if (isSimpleFieldType(fieldClass, fieldTypeName)) {
				String type = getPropertyType(fieldClass, fieldTypeName);
				String description = null;
				String sourceField = fieldName;
				String defaultValue = null;
				String extensionName = null;
				super.updateHint(collector, fieldClass);

				super.addItemMetadata(collector, propertyName, type, description, sourceType, sourceField, null,
						defaultValue, extensionName, field.isBinary());
			} else {
				collectProperties(propertyName, fieldClass, collector, monitor);
			}
		}
	}

	private static IType[] getAllSuperclasses(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getAllSuperclasses(type);
	}

	private static String getConfigPrefix(String configClassName) {
		switch (configClassName) {
		case KUBERNETES_CONFIG_CLASS:
			return KUBERNETES_PREFIX;
		case OPENSHIFT_CONFIG_CLASS:
			return OPENSHIFT_PREFIX;
		case DOCKER_BUILD_CONFIG_CLASS:
			return DOCKER_PREFIX;
		case S2I_BUILD_CONFIG_CLASS:
			return S2I_PREFIX;
		default:
			return null;
		}
	}

}
