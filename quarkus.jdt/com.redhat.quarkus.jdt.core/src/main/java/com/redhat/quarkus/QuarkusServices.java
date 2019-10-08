package com.redhat.quarkus;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.managers.IBuildSupport;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.commons.QuarkusPropertyDefinitionParams;
import com.redhat.quarkus.jdt.core.DocumentationConverter;
import com.redhat.quarkus.jdt.core.JDTQuarkusManager;

@JsonSegment("quarkus")
public class QuarkusServices {

	@JsonRequest("projectInfo")
	public CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(QuarkusProjectInfoParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = new NullProgressMonitor() {
				public boolean isCanceled() {
					cancelChecker.checkCanceled();
					return false;
				};
			};
			try {
				String applicationPropertiesUri = params.getUri();
				IFile file = JDTUtils.findFile(applicationPropertiesUri);
				if (file == null) {
					throw new UnsupportedOperationException(
							String.format("Cannot find IFile for '%s'", applicationPropertiesUri));
				}
				QuarkusPropertiesScope scope = params.getScope();
				return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(file, scope,
						DocumentationConverter.DEFAULT_CONVERTER, monitor);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@JsonRequest("propertyDefinition")
	public CompletableFuture<Location> getPropertyDefinition(QuarkusPropertyDefinitionParams params) {
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = new NullProgressMonitor() {
				public boolean isCanceled() {
					cancelChecker.checkCanceled();
					return false;
				};
			};
			String applicationPropertiesUri = params.getUri();
			IFile file = JDTUtils.findFile(applicationPropertiesUri);
			if (file == null) {
				throw new UnsupportedOperationException(
						String.format("Cannot find IFile for '%s'", applicationPropertiesUri));
			}
			String propertySource = params.getPropertySource();
			try {
				IField field = JDTQuarkusManager.getInstance().findDeclaredQuarkusProperty(file, propertySource,
						monitor);
				if (field != null) {
					IClassFile classFile = field.getClassFile();
					if (classFile != null) {
						// Try to download source if required
						Optional<IBuildSupport> bs = JavaLanguageServerPlugin.getProjectsManager()
								.getBuildSupport(file.getProject());
						if (bs.isPresent()) {
							bs.get().discoverSource(classFile, monitor);
						}
					}
					return JDTUtils.toLocation(field);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return null;
		});
	}
}
