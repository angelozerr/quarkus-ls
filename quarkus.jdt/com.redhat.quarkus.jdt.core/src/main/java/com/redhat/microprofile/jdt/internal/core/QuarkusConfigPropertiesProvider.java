package com.redhat.microprofile.jdt.internal.core;

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyEnumerations;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertySource;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isList;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isMap;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isOptional;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isPrimitiveType;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_PROPERTY_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_EXTENSION_PROPERTIES;
import static com.redhat.quarkus.jdt.internal.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.quarkus.jdt.internal.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.jdt.core.AbstractPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.jdt.internal.core.QuarkusDeploymentJavaProject.ArtifactResolver;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusSearchUtils;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.deployment.bean.JavaBeanUtil;

public class QuarkusConfigPropertiesProvider extends AbstractPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(QuarkusConfigPropertiesProvider.class.getName());

	private static final String[] ANNOTATION_NAMES = { "io.quarkus.arc.config.ConfigProperties" };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	public void contributeToClasspath(IJavaProject project, boolean excludeTestCode, ArtifactResolver artifactResolver,
			List<IClasspathEntry> deploymentJarEntries) throws JavaModelException {
		IClasspathEntry[] entries = project.getResolvedClasspath(true);
		List<String> existingJars = Stream.of(entries)
				// filter entry to collect only JAR
				.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
				// filter Quarkus deployment JAR marked as test scope. Ex:
				// 'quarkus-core-deployment' can be marked as test scope, we must exclude them
				// to avoid to ignore it in the next step.
				.filter(entry -> !excludeTestCode || (excludeTestCode && !entry.isTest())) //
				.map(entry -> entry.getPath().lastSegment()).collect(Collectors.toList());
		for (IClasspathEntry entry : entries) {
			if (excludeTestCode && entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:

				try {
					String jarPath = entry.getPath().toOSString();
					IPackageFragmentRoot root = project.getPackageFragmentRoot(jarPath);
					if (root != null) {
						IJarEntryResource resource = JDTQuarkusSearchUtils.findPropertiesResource(root,
								QUARKUS_EXTENSION_PROPERTIES);
						if (resource != null) {
							Properties properties = new Properties();
							properties.load(resource.getContents());
							// deployment-artifact=io.quarkus\:quarkus-undertow-deployment\:0.21.1
							String deploymentArtifact = properties.getProperty("deployment-artifact");
							String[] result = deploymentArtifact.split(":");
							String groupId = result[0];
							String artifactId = result[1];
							String version = result[2];
							// Get or download deployment JAR
							String deploymentJarFile = artifactResolver.getArtifact(groupId, artifactId, version);
							if (deploymentJarFile != null) {
								IPath deploymentJarFilePath = new Path(deploymentJarFile);
								String deploymentJarName = deploymentJarFilePath.lastSegment();
								if (!existingJars.contains(deploymentJarName)) {
									// The *-deployment JAR is not included in the classpath project, add it.
									existingJars.add(deploymentJarName);
									IPath sourceAttachmentPath = null;
									// Get or download deployment sources JAR
									String sourceJarFile = artifactResolver.getSources(groupId, artifactId, version);
									if (sourceJarFile != null) {
										sourceAttachmentPath = new Path(sourceJarFile);
									}
									deploymentJarEntries.add(JavaCore.newLibraryEntry(deploymentJarFilePath,
											sourceAttachmentPath, null));
								}
							}
						}
					}
				} catch (Exception e) {
					// do nothing
				}

				break;
			}
		}
		// Add the Quarkus project in classpath to resolve dependencies of deployment
		// Quarkus JARs.
		deploymentJarEntries.add(JavaCore.newProjectEntry(project.getProject().getLocation()));
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		processConfigProperties(javaElement, annotation, collector, monitor);
	}

	// ------------- Process Quarkus ConfigProperties -------------

	private static void processConfigProperties(IJavaElement javaElement, IAnnotation configPropertiesAnnotation,
			IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() != IJavaElement.TYPE) {
			return;
		}
		IType configPropertiesType = (IType) javaElement;
		// Location (JAR, src)
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) javaElement
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		String location = packageRoot.getPath().toString();
		// Quarkus Extension name
		String extensionName = JDTQuarkusUtils.getExtensionName(location);

		String prefix = determinePrefix(configPropertiesType, configPropertiesAnnotation);
		if (configPropertiesType.isInterface()) {
			// See
			// https://github.com/quarkusio/quarkus/blob/0796d712d9a3cf8251d9d8808b705f1a04032ee2/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/InterfaceConfigPropertiesUtil.java#L89
			List<IType> allInterfaces = new ArrayList<>(Arrays.asList(findInterfaces(configPropertiesType, monitor)));
			allInterfaces.add(0, configPropertiesType);

			for (IType configPropertiesInterface : allInterfaces) {
				// Loop for each methods.
				IJavaElement[] elements = configPropertiesInterface.getChildren();
				// Loop for each fields.
				for (IJavaElement child : elements) {
					if (child.getElementType() == IJavaElement.METHOD) {
						IMethod method = (IMethod) child;
						if (Flags.isDefaultMethod(method.getFlags())) { // don't do anything with default methods
							continue;
						}
						if (method.getNumberOfParameters() > 0) {
							LOGGER.log(Level.INFO,
									"Method " + method.getElementName() + " of interface "
											+ method.getDeclaringType().getFullyQualifiedName()
											+ " is not a getter method since it defined parameters");
							continue;
						}
						if ("Void()".equals(method.getReturnType())) {
							LOGGER.log(Level.INFO,
									"Method " + method.getElementName() + " of interface "
											+ method.getDeclaringType().getFullyQualifiedName()
											+ " is not a getter method since it returns void");
							continue;
						}
						String name = null;
						String defaultValue = null;
						IAnnotation configPropertyAnnotation = getAnnotation(method, CONFIG_PROPERTY_ANNOTATION);
						if (configPropertyAnnotation != null) {
							name = getAnnotationMemberValue(configPropertyAnnotation, "name");
							defaultValue = getAnnotationMemberValue(configPropertyAnnotation, "defaultValue");
						}
						if (name == null) {
							name = getPropertyNameFromMethodName(method);
						}
						if (name == null) {
							continue;
						}

						String propertyName = prefix + "." + name;
						String methodResultTypeName = getResolvedResultTypeName(method);
						IType returnType = findType(method.getJavaProject(), methodResultTypeName);

						// Method result type
						String type = getPropertyType(returnType, methodResultTypeName);

						// TODO: extract Javadoc from Java sources
						String docs = null;

						// Method source
						String source = getPropertySource(method);

						// Enumerations
						List<EnumItem> enumerations = getPropertyEnumerations(returnType);

						if (isSimpleFieldType(returnType, methodResultTypeName)) {
							collector.addMetadataProperty(MicroProfileProjectInfo.DEFAULT_REFERENCE_TYPE, propertyName,
									type, defaultValue, docs, location, extensionName, source, enumerations, null);
						} else {
							populateConfigObject(returnType, propertyName, location, extensionName, new HashSet<>(),
									collector, monitor);
						}

					}
				}
			}
		} else {
			// See
			// https://github.com/quarkusio/quarkus/blob/e8606513e1bd14f0b1aaab7f9969899bd27c55a3/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/ClassConfigPropertiesUtil.java#L117
			// TODO : validation
			populateConfigObject(configPropertiesType, prefix, location, extensionName, new HashSet<>(), collector,
					monitor);
		}
	}

	private static boolean isSimpleFieldType(IType type, String typeName) {
		return type == null || isPrimitiveType(typeName) || isList(typeName) || isMap(typeName) || isOptional(typeName);
	}

	private static IType[] findInterfaces(IType type, IProgressMonitor progressMonitor) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getAllSuperInterfaces(type);
	}

	private static void populateConfigObject(IType configPropertiesType, String prefixStr, final String location,
			String extensionName, Set<IType> typesAlreadyProcessed, IPropertiesCollector collector,
			IProgressMonitor monitor) throws JavaModelException {
		if (typesAlreadyProcessed.contains(configPropertiesType)) {
			return;
		}
		typesAlreadyProcessed.add(configPropertiesType);
		IJavaElement[] elements = configPropertiesType.getChildren();
		// Loop for each fields.
		for (IJavaElement child : elements) {
			if (child.getElementType() == IJavaElement.FIELD) {
				// The following code is an adaptation for JDT of
				// Quarkus arc code:
				// https://github.com/quarkusio/quarkus/blob/e8606513e1bd14f0b1aaab7f9969899bd27c55a3/extensions/arc/deployment/src/main/java/io/quarkus/arc/deployment/configproperties/ClassConfigPropertiesUtil.java#L211
				IField field = (IField) child;
				boolean useFieldAccess = false;
				String setterName = JavaBeanUtil.getSetterName(field.getElementName());
				String configClassInfo = configPropertiesType.getFullyQualifiedName();
				IMethod setter = findMethod(configPropertiesType, setterName, field.getTypeSignature());
				if (setter == null) {
					if (!Flags.isPublic(field.getFlags()) || Flags.isFinal(field.getFlags())) {
						LOGGER.log(Level.INFO,
								"Configuration properties class " + configClassInfo
										+ " does not have a setter for field " + field
										+ " nor is the field a public non-final field");
						continue;
					}
					useFieldAccess = true;
				}
				if (!useFieldAccess && !Flags.isPublic(setter.getFlags())) {
					LOGGER.log(Level.INFO, "Setter " + setterName + " of class " + configClassInfo + " must be public");
					continue;
				}

				String name = field.getElementName();
				// The default value is managed with assign like : 'public String suffix = "!"';
				// Getting "!" value is possible but it requires to re-parse the Java file to
				// build a DOM CompilationUnit to extract assigned value.
				final String defaultValue = null;
				String propertyName = prefixStr + "." + name;

				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);
				if (isSimpleFieldType(fieldClass, fieldTypeName)) {

					// Class type
					String type = getPropertyType(fieldClass, fieldTypeName);

					// Javadoc
					String docs = null;

					// field and class source
					String source = getPropertySource(field);

					// Enumerations
					List<EnumItem> enumerations = getPropertyEnumerations(fieldClass);

					collector.addMetadataProperty(MicroProfileProjectInfo.DEFAULT_REFERENCE_TYPE, propertyName, type,
							defaultValue, docs, location, extensionName, source, enumerations, null);
				} else {
					populateConfigObject(fieldClass, propertyName, location, extensionName, typesAlreadyProcessed,
							collector, monitor);
				}
			}
		}
	}

	private static String getPropertyNameFromMethodName(IMethod method) {
		try {
			return JavaBeanUtil.getPropertyNameFromGetter(method.getElementName());
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.INFO, "Method " + method.getElementName() + " of interface "
					+ method.getDeclaringType().getElementName()
					+ " is not a getter method. Either rename the method to follow getter name conventions or annotate the method with @ConfigProperty");
			return null;
		}
	}

	private static IMethod findMethod(IType configPropertiesType, String setterName, String fieldTypeSignature) {
		IMethod method = configPropertiesType.getMethod(setterName, new String[] { fieldTypeSignature });
		return method.exists() ? method : null;
	}

	private static String determinePrefix(IType configPropertiesType, IAnnotation configPropertiesAnnotation)
			throws JavaModelException {
		String fromAnnotation = getPrefixFromAnnotation(configPropertiesAnnotation);
		if (fromAnnotation != null) {
			return fromAnnotation;
		}
		return getPrefixFromClassName(configPropertiesType);
	}

	private static String getPrefixFromAnnotation(IAnnotation configPropertiesAnnotation) throws JavaModelException {
		String value = getAnnotationMemberValue(configPropertiesAnnotation, "prefix");
		if (value == null) {
			return null;
		}
		if (ConfigProperties.UNSET_PREFIX.equals(value) || value.isEmpty()) {
			return null;
		}
		return value;
	}

	private static String getPrefixFromClassName(IType className) {
		String simpleName = className.getElementName(); // className.isInner() ? className.local() :
														// className.withoutPackagePrefix();
		return join("-", withoutSuffix(lowerCase(camelHumpsIterator(simpleName)), "config", "configuration",
				"properties", "props"));
	}

}
