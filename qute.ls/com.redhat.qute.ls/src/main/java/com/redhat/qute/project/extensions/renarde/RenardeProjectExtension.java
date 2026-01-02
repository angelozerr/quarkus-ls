package com.redhat.qute.project.extensions.renarde;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.resolvers.MessageTypeValueResolver;
import com.redhat.qute.project.extensions.CompletionParticipant;
import com.redhat.qute.project.extensions.DefinitionParticipant;
import com.redhat.qute.project.extensions.DiagnosticsParticipant;
import com.redhat.qute.project.extensions.ProjectExtension;

public class RenardeProjectExtension implements ProjectExtension {

	private List<MessagesFileInfo> messagesFileInfos;

	private boolean enabled;
	private Set<String> renardeMessageResolvers;

	private CompletionParticipant completionParticipant;
	private DefinitionParticipant definitionParticipant ;
	private DiagnosticsParticipant diagnosticsParticipant;

	@Override
	public void start(ExtendedDataModelProject project) {
		enabled = project.getNamespaceResolver("m") != null && project.getSourceFolders() != null;
		this.messagesFileInfos = new ArrayList<>();

		this.renardeMessageResolvers = new HashSet<>();
		if (enabled) {
			for (String sourceFolder : project.getSourceFolders()) {
				try {
					Path sourceFolderPath = Paths.get(sourceFolder);
					try (Stream<Path> stream = Files.list(sourceFolderPath)) {
						stream.forEach(path -> {
							String fileName = path.getName(path.getNameCount() - 1).toString();
							if (fileName.equals("messages.properties")) {
								try {
									messagesFileInfos.add(new MessagesFileInfo(path));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				} catch (Exception e) {

				}
			}
			completionParticipant = new RenardeCompletionParticipant(this);
			project.registerCompletionParticipant(completionParticipant);
			definitionParticipant = new RenardeDefinitionParticipant(this);
			project.registerDefinitionParticipant(definitionParticipant);
			diagnosticsParticipant = new RenardeDiagnosticsParticipant(this);
			project.registerDiagnosticsParticipant(diagnosticsParticipant);
		}
	}

	@Override
	public void stop(ExtendedDataModelProject project) {

	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public boolean hasMessage(String expression) {
		for (MessagesFileInfo messagesFileInfo : messagesFileInfos) {
			if (messagesFileInfo.hasMessage(expression)) {
				return true;
			}
		}
		return false;
	}
	
	public List<MessagesFileInfo> getMessagesFileInfos() {
		return messagesFileInfos;
	}

}
