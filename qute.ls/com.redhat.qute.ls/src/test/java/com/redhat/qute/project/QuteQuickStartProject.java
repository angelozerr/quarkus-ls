/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.commons.datamodel.DataModelFragment;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.MessageResolverData;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.commons.jaxrs.JaxRsMethodKind;
import com.redhat.qute.commons.jaxrs.JaxRsParamKind;
import com.redhat.qute.commons.jaxrs.RestParam;

/**
 * Qute quick start project.
 *
 * @author Angelo ZERR
 *
 */
public class QuteQuickStartProject extends BaseQuteProject {

	public final static String PROJECT_URI = "qute-quickstart";

	public static final String ITEMRESOURCE_ITEMS_TEMPLATE_URI = "src/main/resources/templates/ItemResource/items";
	public static final String ITEMRESOURCE_ITEMS_WITH_FRAGMENTS_TEMPLATE_URI = "src/main/resources/templates/ItemResourceWithFragments/items";
	public static final String NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI = "src/main/resources/templates/NativeItemResource/items";

	public QuteQuickStartProject(ProjectInfo projectInfo, QuteProjectRegistry projectRegistry) {
		super(projectInfo, projectRegistry);
	}

	@Override
	protected void fillResolvedJavaTypes(List<ResolvedJavaTypeInfo> cache) {
		super.fillResolvedJavaTypes(cache);
		createSourceTypes(cache);
	}

	private void createSourceTypes(List<ResolvedJavaTypeInfo> cache) {
		createResolvedJavaTypeInfo("org.acme", cache, true).setJavaTypeKind(JavaTypeKind.Package);

		ResolvedJavaTypeInfo bean = createResolvedJavaTypeInfo("org.acme.Bean", cache, false);
		registerField("bean : java.lang.String", bean);

		ResolvedJavaTypeInfo review = createResolvedJavaTypeInfo("org.acme.Review", cache, false);
		registerField("name : java.lang.String", review);
		registerField("average : java.lang.Integer", review);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", review);

		// Item <- BaseItem <- AbstractItem
		ResolvedJavaTypeInfo abstractItem = createResolvedJavaTypeInfo("org.acme.AbstractItem", cache, false);
		registerField("abstractName : java.lang.String", abstractItem);
		registerMethod("convert(item : org.acme.AbstractItem) : int", abstractItem);

		ResolvedJavaTypeInfo baseItem = createResolvedJavaTypeInfo("org.acme.BaseItem", cache, false,
				abstractItem.getSignature());
		registerField("base : java.lang.String", baseItem);
		registerField("name : java.lang.String", baseItem);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", baseItem);

		// org.acme.Item
		ResolvedJavaTypeInfo item = createResolvedJavaTypeInfo("org.acme.Item", cache, false, baseItem.getSignature());
		JavaMemberInfo itemNameField = registerField("name : java.lang.String", item); // Override BaseItem#name
		itemNameField.setDocumentation("The name of the item");
		registerField("price : java.math.BigInteger", item);
		registerField("review : org.acme.Review", item);
		JavaMemberInfo itemIsAvailableMethod = registerMethod("isAvailable() : java.lang.Boolean", item);
		itemIsAvailableMethod.setDocumentation("Returns true if the item is available and false otherwise");
		JavaMemberInfo itemIsAvailableMethodOverload = registerMethod("isAvailable(index : int) : java.lang.Boolean",
				item);
		itemIsAvailableMethodOverload
				.setDocumentation("Returns true if the item at the given index is available and false otherwise");
		registerMethod("getReview2() : org.acme.Review", item);
		// Override BaseItem#getReviews()
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);

		registerField("derivedItems : java.util.List<org.acme.Item>", item);
		registerField("derivedItemArray : org.acme.Item[]", item);
		item.setInvalidMethod("staticMethod", InvalidMethodReason.Static); // public static BigDecimal
																			// staticMethod(Item item)

		ResolvedJavaTypeInfo classA = createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassA", cache, false,
				"org.acme.qute.cyclic.ClassC");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassB", cache, false, "org.acme.qute.cyclic.ClassA");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassC", cache, false, "org.acme.qute.cyclic.ClassB");
		registerMethod("convert() : java.lang.String", classA);
		registerField("name : java.lang.String", classA);

		ResolvedJavaTypeInfo classAWithGeneric = createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassAWithGeneric<T>",
				cache, false,
				"org.acme.qute.cyclic.ClassCWithGeneric<T>");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassBWithGeneric<T>", cache, false,
				"org.acme.qute.cyclic.ClassAWithGeneric<T>");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassCWithGeneric<T>", cache, false,
				"org.acme.qute.cyclic.ClassBWithGeneric<T>");
		registerMethod("convert() : java.lang.String", classAWithGeneric);
		registerField("name : java.lang.String", classAWithGeneric);

		// org.acme.MachineStatus
		ResolvedJavaTypeInfo machineStatus = createResolvedJavaTypeInfo("org.acme.MachineStatus", cache, false);
		machineStatus.setJavaTypeKind(JavaTypeKind.Enum);
		registerField("ON : org.acme.MachineStatus", machineStatus);
		registerField("OFF : org.acme.MachineStatus", machineStatus);
		registerField("BROKEN : org.acme.MachineStatus", machineStatus);
		registerField("in : org.acme.MachineStatus", machineStatus);

		// org.acme.Machine
		ResolvedJavaTypeInfo machine = createResolvedJavaTypeInfo("org.acme.Machine", cache, false);
		registerField("status : org.acme.MachineStatus", machine);
		registerMethod("getMachine() : org.acme.MachineStatus", machine);
		registerMethod("getCount() : java.lang.Integer", machine);

		// @TemplateData
		// public class ItemWithTemplateData
		ResolvedJavaTypeInfo itemWithTemplateData = createResolvedJavaTypeInfo("org.acme.ItemWithTemplateData", cache,
				false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateData); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateData);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateData);
		registerMethod("getSubClasses() : int", itemWithTemplateData);
		itemWithTemplateData.setTemplateDataAnnotations(Arrays.asList(new TemplateDataAnnotation()));

		// @TemplateData
		// @TemplateData(target = BigInteger.class)
		// public class ItemWithTemplateDataWithTarget
		ResolvedJavaTypeInfo itemWithTemplateDataWithTarget = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataWithTarget", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataWithTarget); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataWithTarget);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataWithTarget);
		registerMethod("getSubClasses() : int", itemWithTemplateDataWithTarget);
		TemplateDataAnnotation templateDataAnnotationWithTarget = new TemplateDataAnnotation();
		templateDataAnnotationWithTarget.setTarget("java.lang.String");
		itemWithTemplateDataWithTarget.setTemplateDataAnnotations(
				Arrays.asList(new TemplateDataAnnotation(), templateDataAnnotationWithTarget));

		// @TemplateData(properties = true)
		// public class ItemWithTemplateDataProperties
		ResolvedJavaTypeInfo itemWithTemplateDataProperties = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataProperties", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataProperties); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataProperties);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataProperties);
		registerMethod("getSubClasses() : int", itemWithTemplateDataProperties);
		TemplateDataAnnotation propertiesTemplateDataAnnotation = new TemplateDataAnnotation();
		propertiesTemplateDataAnnotation.setProperties(true);
		itemWithTemplateDataProperties.setTemplateDataAnnotations(Arrays.asList(propertiesTemplateDataAnnotation));

		// @TemplateData(ignoreSuperclasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		ResolvedJavaTypeInfo itemWithTemplateDataIgnoreSubClasses = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataIgnoreSubClasses", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataIgnoreSubClasses); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getSubClasses() : int", itemWithTemplateDataIgnoreSubClasses);
		TemplateDataAnnotation templateDataAnnotation = new TemplateDataAnnotation();
		templateDataAnnotation.setIgnoreSuperclasses(true);
		itemWithTemplateDataIgnoreSubClasses.setTemplateDataAnnotations(Arrays.asList(templateDataAnnotation));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		ResolvedJavaTypeInfo itemWithRegisterForReflection = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflection", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflection); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflection);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflection);
		RegisterForReflectionAnnotation registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		itemWithRegisterForReflection.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflectionNoFields
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoFields = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoFields", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoFields); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoFields);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoFields);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setFields(false);
		itemWithRegisterForReflectionNoFields.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoMethods = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoMethods", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoMethods); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoMethods);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoMethods);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setMethods(false);
		itemWithRegisterForReflectionNoMethods.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// Renarde controller
		createResolvedJavaTypeInfo(
				"javax.ws.rs.core.Response", cache, true);
		ResolvedJavaTypeInfo renardeLogin = createResolvedJavaTypeInfo(
				"rest.Login", cache, false, "io.quarkiverse.renarde.oidc.ControllerWithUser<model.User>");

		JavaMethodInfo loginMethod = registerMethod("login() : io.quarkus.qute.TemplateInstance", renardeLogin);
		loginMethod.setJaxRsMethodKind(JaxRsMethodKind.POST);

		JavaMethodInfo manualLoginMethod = registerMethod(
				"manualLogin(userName : java.lang.String, password : java.lang.String, webAuthnResponse : io.quarkus.security.webauthn.WebAuthnLoginResponse, ctx : io.vertx.ext.web.RoutingContext) : javax.ws.rs.core.Response",
				renardeLogin);
		manualLoginMethod.setJaxRsMethodKind(JaxRsMethodKind.POST);
		Map<String, RestParam> restParameters = new HashMap<>();
		restParameters.put("userName", new RestParam("userName", JaxRsParamKind.FORM, true));
		restParameters.put("password", new RestParam("password", JaxRsParamKind.FORM, false));
		manualLoginMethod.setRestParameters(restParameters);

		JavaMethodInfo confirmMethod = registerMethod("confirm(confirmationCode : java.lang.String) : void",
				renardeLogin);
		restParameters = new HashMap<>();
		restParameters.put("confirmationCode", new RestParam("confirmationCode", JaxRsParamKind.PATH, false));
		confirmMethod.setJaxRsMethodKind(JaxRsMethodKind.GET);
		confirmMethod.setRestParameters(restParameters);

		JavaMethodInfo completeMethod = registerMethod(
				"complete(confirmationCode : java.lang.String, userName : java.lang.String, password : java.lang.String, password2 : java.lang.String, webAuthnResponse : io.quarkus.security.webauthn.WebAuthnRegisterResponse, firstName : java.lang.String, lastName : java.lang.String, ctx : io.vertx.ext.web.RoutingContext) : javax.ws.rs.core.Response",
				renardeLogin);
		completeMethod.setJaxRsMethodKind(JaxRsMethodKind.POST);
		restParameters = new HashMap<>();
		restParameters.put("confirmationCode", new RestParam("confirmationCode", JaxRsParamKind.QUERY, false));
		restParameters.put("userName", new RestParam("userName", JaxRsParamKind.FORM, true));
		restParameters.put("password", new RestParam("password", JaxRsParamKind.FORM, false));
		restParameters.put("password2", new RestParam("password2", JaxRsParamKind.FORM, false));
		restParameters.put("firstName", new RestParam("firstName", JaxRsParamKind.FORM, true));
		restParameters.put("lastName", new RestParam("lastName", JaxRsParamKind.FORM, true));
		completeMethod.setRestParameters(restParameters);

		// void method
		registerMethod("timeoutGame() : void", renardeLogin);

		// https://quarkus.io/guides/qute-reference#evaluation-of-completionstage-and-uni-objects
		ResolvedJavaTypeInfo completionStagePOJO = createResolvedJavaTypeInfo("org.acme.CompletionStagePOJO", cache,
				false);
		registerMethod("getMyStrings() : io.smallrye.mutiny.Uni<java.util.List<java.lang.String>>",
				completionStagePOJO);
		registerMethod("getOtherStrings() : java.util.concurrent.CompletableFuture<java.util.List<java.lang.String>>",
				completionStagePOJO);
	}

	@Override
	protected void fillTemplates(List<DataModelTemplate<DataModelParameter>> templates) {
		createItemsTemplate(templates);
		createItemsNativeTemplate(templates);
	}

	private static void createItemsTemplate(List<DataModelTemplate<DataModelParameter>> templates) {
		// Simple template
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri(ITEMRESOURCE_ITEMS_TEMPLATE_URI);
		template.setSourceType("org.acme.qute.ItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		// ItemResource$Templates#items(Item item)
		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		template.addParameter(parameter);

		// Template with fragments
		DataModelTemplate<DataModelParameter> templateWithFragment = new DataModelTemplate<DataModelParameter>();
		templateWithFragment.setTemplateUri(ITEMRESOURCE_ITEMS_WITH_FRAGMENTS_TEMPLATE_URI);
		templateWithFragment.setSourceType("org.acme.qute.ItemResourceWithFragments$Templates");
		templateWithFragment.setSourceMethod("items");
		templates.add(templateWithFragment);

		DataModelFragment<DataModelParameter> fragment = new DataModelFragment<>();
		fragment.setId("id2");
		fragment.setSourceType("org.acme.qute.ItemResourceWithFragments$Templates");
		fragment.setSourceMethod("items$id2");
		fragment.addParameter(parameter);
		templateWithFragment.setFragments(Arrays.asList(fragment));

		// ItemResource$Templates#items(Item item)
		templateWithFragment.addParameter(parameter);
	}

	private static void createItemsNativeTemplate(List<DataModelTemplate<DataModelParameter>> templates) {
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri(NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI);
		template.setSourceType("org.acme.qute.NativeItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		// template.data("item", ...)
		DataModelParameter parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("review");
		parameter.setSourceType("org.acme.Review");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateData");
		parameter.setSourceType("org.acme.ItemWithTemplateData");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataWithTarget");
		parameter.setSourceType("org.acme.ItemWithTemplateDataWithTarget");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataIgnoreSubClasses");
		parameter.setSourceType("org.acme.ItemWithTemplateDataIgnoreSubClasses");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflectionNoFields");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflectionNoFields");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflectionNoMethods");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflectionNoMethods");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflection");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflection");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataProperties");
		parameter.setSourceType("org.acme.ItemWithTemplateDataProperties");
		template.addParameter(parameter);
	}

	@Override
	protected void fillValueResolvers(List<ValueResolverInfo> resolvers) {
		// io.quarkus.qute.runtime.extensions.NumberTemplateExtensions
		resolvers.add(createValueResolver(null, null, (String) null,
				"io.quarkus.qute.runtime.extensions.NumberTemplateExtensions",
				"mod(number : java.lang.Integer, mod : java.lang.Integer) : java.lang.Integer",
				ValueResolverKind.TemplateExtensionOnMethod,
				false, true));
		resolvers.add(createValueResolver(null, null, List.of("plus", "+"),
				"io.quarkus.qute.runtime.extensions.NumberTemplateExtensions",
				"addToInt(number : java.lang.Integer, name : java.lang.String, other : java.lang.Integer) : java.lang.Integer",
				ValueResolverKind.TemplateExtensionOnMethod,
				false, true));
		resolvers.add(createValueResolver(null, null, List.of("minus", "-"),
				"io.quarkus.qute.runtime.extensions.NumberTemplateExtensions",
				"subtractFromInt(number : java.lang.Integer, name : java.lang.String, other : java.lang.Integer) : java.lang.Integer",
				ValueResolverKind.TemplateExtensionOnMethod,
				false, true));
		
		// Type value resolvers
		resolvers.add(createValueResolver("inject", "plexux", (String) null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator", ValueResolverKind.InjectedBean,
				false, true));
		resolvers.add(createValueResolver("inject", "plexux", (String) null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider", ValueResolverKind.InjectedBean,
				false, true));

		// Method value resolvers
		// No namespace
		resolvers.add(createValueResolver(null, null, (String) null, "org.acme.ItemResource",
				"discountedPrice(item : org.acme.Item) : java.math.BigDecimal",
				ValueResolverKind.TemplateExtensionOnMethod, false, false));
		resolvers.add(
				createValueResolver(null, null, (String) null,
						"io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions",
						"getByIndex(list : java.util.List<T>, index : int) : T",
						ValueResolverKind.TemplateExtensionOnClass, false, true));
		resolvers.add(createValueResolver(null, null, (String) null, "org.acme.ItemResource",
				"pretty(item : org.acme.Item, elements : java.lang.String...) : java.lang.String",
				ValueResolverKind.TemplateExtensionOnMethod, false, false));

		// @TemplateExtension
		// org.acme.TemplateExtensions
		resolvers.add(createValueResolver(null, null, (String) null, "org.acme.TemplateExtensions", "",
				ValueResolverKind.TemplateExtensionOnClass, false, false));
		// @TemplateExtension
		// org.acme.foo.TemplateExtensions
		resolvers.add(createValueResolver(null, null, (String) null, "org.acme.foo.TemplateExtensions", "",
				ValueResolverKind.TemplateExtensionOnClass, false, false));

		// 'config' namespace
		resolvers.add(
				createValueResolver("config", null, "*", "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"getConfigProperty(propertyName : java.lang.String) : java.lang.Object",
						ValueResolverKind.TemplateExtensionOnMethod, false, true));
		resolvers.add(
				createValueResolver("config", null, (String) null,
						"io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"property(propertyName : java.lang.String) : java.lang.Object",
						ValueResolverKind.TemplateExtensionOnMethod, false, true));

		// Static method value resolvers
		resolvers.add(createValueResolver(null, "VARCHAR_SIZE", null, "util.Globals", "VARCHAR_SIZE() : int",
				ValueResolverKind.TemplateGlobal, true));

		// Field value resolvers
		resolvers.add(createValueResolver("inject", "bean", null, "org.acme.Bean", "bean : java.lang.String",
				ValueResolverKind.InjectedBean));

		// Static field value resolvers
		resolvers.add(createValueResolver(null, "GLOBAL", null, "org.acme.Bean", "bean : java.lang.String",
				ValueResolverKind.TemplateGlobal, true));

		// Renarde controller
		resolvers.add(createValueResolver("uri", "Login", (String) null, "rest.Login", "rest.Login",
				ValueResolverKind.Renarde, false, false));

		// Web bundler 'bundle" field as global
		resolvers.add(createValueResolver(null, "bundle", null, "util.Globals",
				"bundle : java.util.Map<java.lang.String,java.lang.String>",
				ValueResolverKind.TemplateGlobal, true));

		// Type-safe Message Bundles support
		ValueResolverInfo hello_name = createValueResolver("msg", null, (String) null, "org.acme.AppMessages",
				"hello_name(name : java.lang.String) : java.lang.String",
				ValueResolverKind.Message, false, false);
		MessageResolverData hello_nameData = new MessageResolverData();
		hello_nameData.setMessage("Hello {name ?: 'Qute'}");
		hello_name.setData(hello_nameData);
		resolvers.add(hello_name);

		ValueResolverInfo hello = createValueResolver("msg2", null, (String) null, "org.acme.App2Messages",
				"hello() : java.lang.String",
				ValueResolverKind.Message, false, false);
		MessageResolverData helloData = new MessageResolverData();
		helloData.setMessage("Hello!");
		hello.setData(helloData);
		resolvers.add(hello);

	}

	@Override
	protected void fillNamespaceResolverInfos(Map<String, NamespaceResolverInfo> infos) {
		NamespaceResolverInfo inject = new NamespaceResolverInfo();
		inject.setNamespaces(Arrays.asList("inject", "cdi"));
		inject.setDescription(
				"A CDI bean annotated with `@Named` can be referenced in any template through `cdi` and/or `inject` namespaces.");
		inject.setUrl("https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates");
		infos.put("inject", inject);
	}
}