/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.MicroProfileProjectInfoCache.MicroProfileProjectInfoWrapper;

/**
 * Test with {@link MicroProfileProjectInfoWrapper}.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfoWrapperTest {

	@Test
	public void expandWithOneDynamicPart() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		info.setProperties(new ArrayList<>());
		info.setHints(new ArrayList<>());

		// fill properties with one dynamic part (${...})
		ItemMetadata p = new ItemMetadata();
		p.setName("${mp.register.rest.client.class}/mp-rest/connectTimeout");
		p.setDescription("Timeout specified in milliseconds to wait to connect to the remote endpoint.");
		p.setType("long");
		info.getProperties().add(p);

		MicroProfileProjectInfoWrapper wrapper = new MicroProfileProjectInfoWrapper(info);
		Assert.assertEquals(0, wrapper.getProperties().size());

		// fill hints
		ItemHint hint = new ItemHint();
		hint.setName("${mp.register.rest.client.class}");
		hint.setValues(new ArrayList<>());
		info.getHints().add(hint);

		ValueHint value = new ValueHint();
		value.setValue("org.acme.restclient.CountriesService");
		value.setSourceType("org.acme.restclient.CountriesService");
		hint.getValues().add(value);

		value = new ValueHint();
		value.setValue("org.acme.restclient.StreetsService");
		value.setSourceType("org.acme.restclient.StreetsService");
		hint.getValues().add(value);

		wrapper = new MicroProfileProjectInfoWrapper(info);
		Assert.assertEquals(2, wrapper.getProperties().size());

		ItemMetadata first = wrapper.getProperties().get(0);
		Assert.assertEquals("org.acme.restclient.CountriesService/mp-rest/connectTimeout", first.getName());
		Assert.assertEquals("org.acme.restclient.CountriesService", first.getSourceType());
		Assert.assertEquals("long", first.getType());
		Assert.assertEquals("Timeout specified in milliseconds to wait to connect to the remote endpoint.",
				first.getDescription());
	}

	@Test
	public void expandWithTwoDynamicPart() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		info.setProperties(new ArrayList<>());
		info.setHints(new ArrayList<>());

		// fill properties with two dynamic part (${...})
		ItemMetadata p = new ItemMetadata();
		p.setName("${mp.register.rest.client.class}/mp-rest/providers/${mp.register.provider.class}/priority");
		p.setDescription("Override the priority of the provider for the given interface.");
		p.setType("java.lang.String");
		info.getProperties().add(p);

		MicroProfileProjectInfoWrapper wrapper = new MicroProfileProjectInfoWrapper(info);
		Assert.assertEquals(0, wrapper.getProperties().size());

		// fill hints ${mp.register.rest.client.class}
		ItemHint hint = new ItemHint();
		hint.setName("${mp.register.rest.client.class}");
		hint.setValues(new ArrayList<>());
		info.getHints().add(hint);

		ValueHint value = new ValueHint();
		value.setValue("org.acme.restclient.CountriesService");
		value.setSourceType("org.acme.restclient.CountriesService");
		hint.getValues().add(value);

		value = new ValueHint();
		value.setValue("org.acme.restclient.StreetsService");
		value.setSourceType("org.acme.restclient.StreetsService");
		hint.getValues().add(value);

		wrapper = new MicroProfileProjectInfoWrapper(info);
		Assert.assertEquals(0, wrapper.getProperties().size());

		// fill hints ${mp.register.provider.class}
		hint = new ItemHint();
		hint.setName("${mp.register.provider.class}");
		hint.setValues(new ArrayList<>());
		info.getHints().add(hint);

		value = new ValueHint();
		value.setValue("org.acme.provider.MyProvider");
		value.setSourceType("org.acme.provider.MyProvider");
		hint.getValues().add(value);

		wrapper = new MicroProfileProjectInfoWrapper(info);
		Assert.assertEquals(2, wrapper.getProperties().size());

		ItemMetadata first = wrapper.getProperties().get(0);
		Assert.assertEquals(
				"org.acme.restclient.CountriesService/mp-rest/providers/org.acme.provider.MyProvider/priority",
				first.getName());
		Assert.assertEquals("org.acme.restclient.CountriesService", first.getSourceType());
		Assert.assertEquals("java.lang.String", first.getType());
		Assert.assertEquals("Override the priority of the provider for the given interface.", first.getDescription());

		ItemMetadata second = wrapper.getProperties().get(1);
		Assert.assertEquals(
				"org.acme.restclient.StreetsService/mp-rest/providers/org.acme.provider.MyProvider/priority",
				second.getName());
		Assert.assertEquals("org.acme.restclient.StreetsService", second.getSourceType());
		Assert.assertEquals("java.lang.String", second.getType());
		Assert.assertEquals("Override the priority of the provider for the given interface.", second.getDescription());
	}

}
