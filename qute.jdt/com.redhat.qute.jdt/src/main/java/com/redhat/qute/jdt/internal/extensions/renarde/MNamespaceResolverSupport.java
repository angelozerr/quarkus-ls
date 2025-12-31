/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.renarde;

import static com.redhat.qute.jdt.internal.extensions.renarde.RenardeJavaConstants.RENARDE_CONTROLLER_TYPE;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.resolvers.MessageResolverData;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.jdt.template.datamodel.AbstractDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * uri, uriabs renarde support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/quarkiverse/quarkus-renarde/blob/main/docs/modules/ROOT/pages/index.adoc#obtaining-a-uri-in-qute-views
 *
 */
public class MNamespaceResolverSupport extends AbstractDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(MNamespaceResolverSupport.class.getName());

	private static final String M_NAMESPACE = "m";

	@Override
	public void beginSearch(SearchContext context, IProgressMonitor monitor) {
		try {
			// Find all classes which extends 'io.quarkiverse.renarde.Controller'
			collectRenardeMessages(context, monitor);
		} catch (IOException | CoreException e) {
			LOGGER.log(Level.SEVERE, "Error while collecting Renarde messages.", e);
		}
	}

	public void collectRenardeMessages(SearchContext context, IProgressMonitor monitor)
			throws IOException, CoreException {
		IFile messagesFile = findMessagesProperties(context.getJavaProject());
		if (messagesFile != null) {
			String locale = null;
			String fileUri = messagesFile.getLocation().toString();
			List<ValueResolverInfo> resolvers = context.getDataModelProject().getValueResolvers();
			Properties properties = new Properties();
			properties.load(messagesFile.getContents());
			properties.forEach((key, value) -> {
				addRenardeMessage(key.toString(), value != null ? value.toString() : null, fileUri, locale, resolvers);
			});
		}
	}

	/**
	 * Add renarde controller as Qute resolver.
	 * 
	 * @param locale2
	 * 
	 * @param namespace      the uri, uriabs renarde namespace.
	 * @param controllerType the controller type.
	 * @param resolvers      the resolvers to fill.
	 */
	private static void addRenardeMessage(String messageKey, String messageContent, String fileUri, String locale,
			List<ValueResolverInfo> resolvers) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamespace(M_NAMESPACE);
		resolver.setNamed(messageKey);
		resolver.setKind(ValueResolverKind.Message);
		resolver.setSignature(fileUri);

		// data message
		if (locale != null || messageContent != null) {
			MessageResolverData data = new MessageResolverData();
			data.setLocale(locale);
			data.setMessage(messageContent);
			resolver.setData(data);
		}

		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}

	@Override
	protected boolean isNamespaceAvailable(String namespace, SearchContext context, IProgressMonitor monitor) {
		// uri, and uriabs are available only for renarde project
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, RENARDE_CONTROLLER_TYPE) != null;
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	protected String[] getPatterns() {
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		return null;
	}

	public static IFile findMessagesProperties(IJavaProject javaProject) throws JavaModelException {
		for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
			if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
				continue;
			}

			IResource resource = root.getCorrespondingResource();
			if (!(resource instanceof IFolder folder)) {
				continue;
			}

			IFile file = folder.getFile("messages.properties");
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

}