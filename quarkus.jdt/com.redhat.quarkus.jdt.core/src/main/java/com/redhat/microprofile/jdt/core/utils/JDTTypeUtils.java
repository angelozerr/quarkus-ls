package com.redhat.microprofile.jdt.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import com.redhat.quarkus.commons.EnumItem;

public class JDTTypeUtils {

	private static final List<String> NUMBER_TYPES = Arrays.asList("short", "int", "long", "double", "float");

	public static IType findType(IJavaProject project, String name) {
		try {
			return project.findType(name);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getResolvedTypeName(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getResolvedResultTypeName(IMethod method) {
		try {
			String signature = method.getReturnType();
			IType primaryType = method.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getPropertyType(IType type, String typeName) {
		return type != null ? type.getFullyQualifiedName() : typeName;
	}

	public static String getPropertySource(IField field) {
		// field and class source
		return field.getDeclaringType().getFullyQualifiedName() + "#" + field.getElementName();
	}

	public static String getPropertySource(IMethod method) throws JavaModelException {
		return method.getDeclaringType().getFullyQualifiedName() + "#" + method.getElementName()
				+ method.getSignature();
	}

	public static List<EnumItem> getPropertyEnumerations(IType fieldClass) throws JavaModelException {
		List<EnumItem> enumerations = null;
		if (fieldClass != null && fieldClass.isEnum()) {
			enumerations = new ArrayList<>();
			IJavaElement[] children = fieldClass.getChildren();
			for (IJavaElement c : children) {
				if (c.getElementType() == IJavaElement.FIELD && ((IField) c).isEnumConstant()) {
					String enumName = ((IField) c).getElementName();
					// TODO: extract Javadoc
					String enumDocs = null;
					enumerations.add(new EnumItem(enumName, enumDocs));
				}
			}
		}
		return enumerations;
	}

	public static boolean isOptional(String fieldTypeName) {
		return fieldTypeName.startsWith("java.util.Optional");
	}

	public static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.lastIndexOf(">");
		String keyValue = fieldTypeName.substring(start, end);
		int index = keyValue.indexOf(',');
		return new String[] { keyValue.substring(0, index), keyValue.substring(index + 1, keyValue.length()) };
	}

	public static boolean isPrimitiveType(String valueClass) {
		return valueClass.equals("java.lang.String") || valueClass.equals("java.lang.Boolean")
				|| valueClass.equals("java.lang.Integer") || valueClass.equals("java.lang.Long")
				|| valueClass.equals("java.lang.Double") || valueClass.equals("java.lang.Float");
	}

	public static boolean isMap(String mapValueClass) {
		return mapValueClass.startsWith("java.util.Map");
	}

	public static boolean isList(String valueClass) {
		return valueClass.startsWith("java.util.List");
	}

	public static boolean isNumber(String valueClass) {
		return NUMBER_TYPES.contains(valueClass);
	}
}
