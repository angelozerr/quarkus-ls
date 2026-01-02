package com.redhat.qute.project.extensions.renarde;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.project.extensions.DefinitionParticipant;
import com.redhat.qute.utils.QutePositionUtility;

public class RenardeDefinitionParticipant implements DefinitionParticipant {

	private RenardeProjectExtension renardeProjectExtension;

	public RenardeDefinitionParticipant(RenardeProjectExtension renardeProjectExtension) {
		this.renardeProjectExtension = renardeProjectExtension;
	}

	@Override
	public void findDefinition(Part part, List<LocationLink> locations, CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		if ("m".equals(parts.getNamespace())) {
			String expression = parts.getContent();
			for (MessagesFileInfo info : renardeProjectExtension.getMessagesFileInfos()) {
				if (renardeProjectExtension.hasMessage(expression)) {
					String messagesFileUri = info.getMessageFile().toUri().toASCIIString();
					Range targetRange = new Range(new Position(0, 0), new Position(0, 0));
					Range originRange = QutePositionUtility.createRange(parts);
					locations.add(new LocationLink(messagesFileUri, targetRange, targetRange, originRange));					
				}
			}
		}
	}

}
