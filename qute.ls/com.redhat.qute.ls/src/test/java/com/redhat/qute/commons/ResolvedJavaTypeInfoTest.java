package com.redhat.qute.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ResolvedJavaTypeInfoTest {

	@Test
	public void listOfGeneric() {
		String signature = "java.util.Map<K,V>";
		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature(signature);

		type.setMethods(new ArrayList<JavaMethodInfo>());
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature("keySet() : java.util.Set<K>");
		type.getMethods().add(method);
		method = new JavaMethodInfo();
		method.setSignature("values() : java.util.Collection<V>");
		type.getMethods().add(method);

		JavaTypeInfo typeInfo = new JavaTypeInfo();
		typeInfo.setSignature("java.util.Map<java.lang.String,org.acme.Item>");

		type.applyGeneric(typeInfo.getTypeParameters());

		assertEquals("keySet() : java.util.Set<java.lang.String>", type.getMethods().get(0).getSignature());
		assertEquals("values() : java.util.Collection<org.acme.Item>", type.getMethods().get(1).getSignature());
	}
}
