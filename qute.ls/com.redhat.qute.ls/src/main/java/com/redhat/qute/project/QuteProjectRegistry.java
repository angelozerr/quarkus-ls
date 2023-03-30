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

import static com.redhat.qute.services.QuteCompletableFutures.EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteJavadocProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolversRegistry;
import com.redhat.qute.project.documents.QuteOpenedTextDocument;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.ReflectionJavaTypeFilter;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.utils.FileUtils;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry
		implements QuteDataModelProjectProvider, QuteUserTagProvider, QuteJavadocProvider {

	private final ValueResolversRegistry valueResolversRegistry;

	private final Map<String /* project uri */, QuteProject> projects;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteResolvedJavaTypeProvider resolvedTypeProvider;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final QuteUserTagProvider userTagProvider;

	private final QuteJavaTypesProvider javaTypeProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	private final QuteJavadocProvider javadocProvider;

	private final TemplateValidator validator;

	private boolean didChangeWatchedFilesSupported;

	public QuteProjectRegistry(QuteProjectInfoProvider projectInfoProvider, QuteJavaTypesProvider javaTypeProvider,
			QuteJavaDefinitionProvider definitionProvider,
			QuteResolvedJavaTypeProvider resolvedClassProvider, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider userTagsProvider, QuteJavadocProvider javadocProvider,
			TemplateValidator validator) {
		this.projectInfoProvider = projectInfoProvider;
		this.javaTypeProvider = javaTypeProvider;
		this.definitionProvider = definitionProvider;
		this.projects = new HashMap<>();
		this.resolvedTypeProvider = resolvedClassProvider;
		this.dataModelProvider = dataModelProvider;
		this.userTagProvider = userTagsProvider;
		this.javadocProvider = javadocProvider;
		this.valueResolversRegistry = new ValueResolversRegistry();
		this.validator = validator;
	}

	/**
	 * Enable/disable did change watched file support.
	 *
	 * @param didChangeWatchedFilesSupported true if did changed file is supported
	 *                                       by the LSP client and false otherwise.
	 */
	public void setDidChangeWatchedFilesSupported(boolean didChangeWatchedFilesSupported) {
		this.didChangeWatchedFilesSupported = didChangeWatchedFilesSupported;
	}

	/**
	 * Returns true if did changed file is supported by the LSP client and false
	 * otherwise.
	 *
	 * @return true if did changed file is supported by the LSP client and false
	 *         otherwise.
	 */
	public boolean isDidChangeWatchedFilesSupported() {
		return didChangeWatchedFilesSupported;
	}

	/**
	 * Returns the Qute project by the given uri <code>projectUri</code> and null
	 * otherwise.
	 *
	 * @param projectUri the project Uri.
	 *
	 * @return the Qute project by the given uri <code>projectUri</code> and null
	 *         otherwise.
	 */
	public QuteProject getProject(String projectUri) {
		return projects.get(projectUri);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo the project information.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	public QuteProject getProject(ProjectInfo projectInfo) {
		return getProject(projectInfo, true);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo    the project information.
	 * @param validateOnLoad true if validation of templates must be validated if
	 *                       project is created and
	 *                       false otherwise.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	private QuteProject getProject(ProjectInfo projectInfo, boolean validateOnLoad) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			project = registerProjectSync(projectInfo);
			if (validator != null && validateOnLoad) {
				QuteProject newProject = project;
				// Validate closed Qute template on project load.
				CompletableFuture.runAsync(() -> newProject.validateClosedTemplates());
			}
		}
		return project;
	}

	private synchronized QuteProject registerProjectSync(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			return project;
		}
		project = createProject(projectInfo);
		registerProject(project);
		return project;
	}

	protected QuteProject createProject(ProjectInfo projectInfo) {
		return new QuteProject(projectInfo, this, validator);
	}

	protected void registerProject(QuteProject project) {
		projects.put(project.getUri(), project);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidOpenTextDocument(document);
		}
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidCloseTextDocument(document);
		}
	}

	public void onDidSaveTextDocument(QuteOpenedTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidSaveTextDocument(document);
		}
	}

	protected CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return resolvedTypeProvider.getResolvedJavaType(params);
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		Set<String> projectUris = event.getProjectURIs();
		for (String projectUri : projectUris) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.resetJavaTypes();
			}
		}
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		QuteProject existingProject = template.getProject();
		if (existingProject == null) {
			// The project uri is not already get (it occurs when Qute template is opened
			// and the project information takes some times).
			// Load the project information and call the data model.
			return template.getProjectFuture() //
					.thenCompose(projectInfo -> {
						if (projectInfo == null) {
							return EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
						}
						QuteProject project = getProject(projectInfo);
						return project.getDataModelTemplate(template);
					});
		}
		return existingProject.getDataModelTemplate(template);
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return javaTypeProvider.getJavaTypes(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return dataModelProvider.getDataModelProject(params);
	}

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return userTagProvider.getUserTags(params);
	}

	/**
	 * Returns the commons value resolvers available for any Qute project.
	 * 
	 * @return the commons value resolvers available for any Qute project.
	 */
	List<MethodValueResolver> getCommmonsResolvers() {
		return valueResolversRegistry.getResolvers();
	}

	/**
	 * Returns the java type filter according the given root java type and the
	 * native mode.
	 *
	 * @param rootJavaType         the Java root type.
	 * @param nativeImagesSettings the native images settings.
	 *
	 * @return the java type filter according the given root java type and the
	 *         native mode.
	 */
	public JavaTypeFilter getJavaTypeFilter(String projectUri, QuteNativeSettings nativeImagesSettings) {
		if (nativeImagesSettings != null && nativeImagesSettings.isEnabled()) {
			if (projectUri != null) {
				QuteProject project = getProject(projectUri);
				if (project != null) {
					return project.getJavaTypeFilterInNativeMode();
				}
			}
		}
		return ReflectionJavaTypeFilter.INSTANCE;
	}

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return javadocProvider.getJavadoc(params);
	}

	private QuteProject findProjectFor(Path path) {
		for (QuteProject project : projects.values()) {
			if (isBelongToProject(path, project)) {
				return project;
			}
		}
		return null;
	}

	private static boolean isBelongToProject(Path path, QuteProject project) {
		return path.startsWith(project.getTemplateBaseDir());
	}

	public Collection<QuteProject> getProjects() {
		return projects.values();
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		Set<QuteProject> projects = new HashSet<>();
		List<FileEvent> changes = params.getChanges();
		// Some qute templates are deleted, created, or changed
		// Collect impacted Qute projects
		for (FileEvent fileEvent : changes) {
			String fileUri = fileEvent.getUri();
			Path templatePath = FileUtils.createPath(fileUri);
			QuteProject project = findProjectFor(templatePath);
			if (project != null) {
				String templateId = project.getTemplateId(templatePath);
				if (project.isTemplateOpened(templateId)) {
					projects.add(project);
				} else {
					// In case of closed document, we collect the project and update the cache
					switch (fileEvent.getType()) {
						case Changed:
						case Created: {
							// The template is created, update the cache and collect the project
							QuteTextDocument closedTemplate = project
									.onDidCreateTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
							}
							break;
						}
						case Deleted: {
							// The template is deleted, update the cache, collect the project and publish
							// empty diagnostics for this file
							QuteTextDocument closedTemplate = project.onDidDeleteTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
								if (validator != null) {
									validator.clearDiagnosticsFor(fileUri);
								}
							}
							break;
						}
					}
				}
			}
		}

		if (projects.isEmpty()) {
			return;
		}

		// trigger validation for all opened and closed Qute template files which belong
		// to the project list.
		if (validator != null) {
			validator.triggerValidationFor(projects);
		}
	}

	public void dispose() {
		for (QuteProject project : projects.values()) {
			project.dispose();
		}
	}

	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return projectInfoProvider.getProjectInfo(params);
	}

	/**
	 * Try to load the Qute project of the given workspace folder.
	 * 
	 * @param workspaceFolder the workspace folder.
	 * @param progressSupport the LSP client progress support and null otherwise.
	 */
	public void tryToLoadQuteProject(WorkspaceFolder workspaceFolder, ProgressSupport progressSupport) {
		String projectName = workspaceFolder.getName();

		String progressId = createAndStartProgress(projectName, progressSupport);

		// Load Qute project from the Java component (collect Java data model)
		String projectUri = workspaceFolder.getUri();
		QuteProjectParams params = new QuteProjectParams(projectUri);
		getProjectInfo(params)
				.thenAccept(projectInfo -> {
					if (projectInfo == null) {
						// The workspace folder is not a Qute project, end the process
						endProgress(progressId, progressSupport);
						return;
					}

					// The workspace folder is a Qute project, load the data model from Java
					// component
					QuteProject project = getProject(projectInfo, false);
					if (progressSupport != null) {
						WorkDoneProgressReport report = new WorkDoneProgressReport();
						report.setMessage("Loading data model for '" + projectName + "' Qute project.");
						report.setPercentage(10);
						progressSupport.notifyProgress(progressId, report);
					}
					project.getDataModelProject()
							.thenAccept(dataModel -> {
								// The Java data model is collected for the project, validate all templates of
								// the project
								if (progressSupport != null) {
									WorkDoneProgressReport report = new WorkDoneProgressReport();
									report.setMessage(
											"Validating Qute templates for '" + projectName + "' Qute project.");
									report.setPercentage(80);
									progressSupport.notifyProgress(progressId, report);
								}
								// Validate Qute templates
								project.validateClosedTemplates();

								// End progress
								endProgress(progressId, progressSupport);

							}).exceptionally((a) -> {
								endProgress(progressId, progressSupport);
								return null;
							});

				}).exceptionally((a) -> {
					endProgress(progressId, progressSupport);
					return null;
				});

	}

	private static String createAndStartProgress(String projectName, ProgressSupport progressSupport) {
		if (progressSupport == null) {
			return null;
		}
		String progressId = UUID.randomUUID().toString();
		// Initialize progress
		WorkDoneProgressCreateParams create = new WorkDoneProgressCreateParams(Either.forLeft(progressId));
		progressSupport.createProgress(create);

		// Start progress
		WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
		begin.setMessage("Trying to load '" + projectName + "' as Qute project.");
		begin.setPercentage(100);
		progressSupport.notifyProgress(progressId, begin);
		return progressId;
	}

	private static void endProgress(String progressId, ProgressSupport progressSupport) {
		if (progressSupport != null) {
			WorkDoneProgressEnd end = new WorkDoneProgressEnd();
			progressSupport.notifyProgress(progressId, end);
		}
	}
}