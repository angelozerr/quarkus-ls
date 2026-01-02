package com.redhat.qute.project.extensions;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.Part;

public interface DefinitionParticipant {

	void findDefinition(Part part, List<LocationLink> locationLinks, CancelChecker cancelChecker);

}
