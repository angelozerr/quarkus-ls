package com.redhat.microprofile.jdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

import com.redhat.microprofile.jdt.core.IPropertiesProvider;
import com.redhat.quarkus.jdt.core.MicroProfileActivator;

public class PropertiesProviderRegistry implements IRegistryChangeListener {

	private static final Logger LOGGER = Logger.getLogger(PropertiesProviderRegistry.class.getName());

	private static final PropertiesProviderRegistry INSTANCE = new PropertiesProviderRegistry();

	private static final String EXTENSION_PROPERTIES_PROVIDERS = "propertiesProviders";

	public static PropertiesProviderRegistry getInstance() {
		return INSTANCE;
	}

	private boolean extensionPropertiesProvidersLoaded;
	private boolean registryListenerIntialized;
	private final List<IPropertiesProvider> propertiesProviders;

	public PropertiesProviderRegistry() {
		super();
		this.extensionPropertiesProvidersLoaded = false;
		this.registryListenerIntialized = false;
		this.propertiesProviders = new ArrayList<>();
	}

	public List<IPropertiesProvider> getPropertiesProviders() {
		loadExtensionProviders();
		return propertiesProviders;
	}

	private synchronized void loadExtensionProviders() {
		if (extensionPropertiesProvidersLoaded)
			return;

		// Immediately set the flag, as to ensure that this method is never
		// called twice
		extensionPropertiesProvidersLoaded = true;

		LOGGER.log(Level.INFO, "->- Loading .propertiesProviders extension point ->-");

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(MicroProfileActivator.PLUGIN_ID,
				EXTENSION_PROPERTIES_PROVIDERS);
		addExtensionPropertiesProviders(cf);
		addRegistryListenerIfNeeded();

		LOGGER.log(Level.INFO, "-<- Done loading .propertiesProviders extension point -<-");
	}

	@Override
	public void registryChanged(final IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(MicroProfileActivator.PLUGIN_ID,
				EXTENSION_PROPERTIES_PROVIDERS);
		if (deltas != null) {
			synchronized (this) {
				for (IExtensionDelta delta : deltas) {
					IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();
					if (delta.getKind() == IExtensionDelta.ADDED) {
						addExtensionPropertiesProviders(cf);
					} else {
						removeExtensionPropertiesProviders(cf);
					}
				}
			}
		}
	}

	private void addExtensionPropertiesProviders(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				IPropertiesProvider provider = (IPropertiesProvider) ce.createExecutableExtension("class");
				synchronized (propertiesProviders) {
					propertiesProviders.add(provider);
				}
				LOGGER.log(Level.INFO, "  Loaded propertiesProviders: " + provider.getClass().getName());
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, "  Loaded while loading  propertiesProviders", t);
			}
		}
	}

	private void removeExtensionPropertiesProviders(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				IPropertiesProvider provider = (IPropertiesProvider) ce.createExecutableExtension("class");
				synchronized (propertiesProviders) {
					propertiesProviders.remove(provider);
				}
				LOGGER.log(Level.INFO, "  Unloaded propertiesProviders: " + provider.getClass().getName());
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, "  Unloaded while loading  propertiesProviders", t);
			}
		}
	}

	private void addRegistryListenerIfNeeded() {
		if (registryListenerIntialized)
			return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, MicroProfileActivator.PLUGIN_ID);
		registryListenerIntialized = true;
	}

	public void initialize() {

	}

	public void destroy() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}
}