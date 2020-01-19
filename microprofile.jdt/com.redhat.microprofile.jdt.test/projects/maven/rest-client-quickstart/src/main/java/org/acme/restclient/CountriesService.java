package org.acme.restclient;

import java.util.Set;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/v2")
@RegisterRestClient
@RegisterProvider(value = MyProvider.class)
@RegisterProvider(value = MyProvider2.class)
public interface CountriesService {

	@GET
	@Path("/name/{name}")
	@Produces("application/json")
	Set<Country> getByName(@PathParam String name);

	@GET
	@Path("/name/{name}")
	@Produces("application/json")
	CompletionStage<Set<Country>> getByNameAsync(@PathParam String name);
}
