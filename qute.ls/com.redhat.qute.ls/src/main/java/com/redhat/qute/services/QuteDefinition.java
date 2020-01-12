/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.Node;
import com.redhat.qute.parser.NodeKind;
import com.redhat.qute.parser.SectionTag;
import com.redhat.qute.parser.Template;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute definition support.
 *
 */
class QuteDefinition {

	private static final Logger LOGGER = Logger.getLogger(QuteDefinition.class.getName());

	public List<? extends LocationLink> findDefinition(Template template, TextDocument document, Position position,
			CancelChecker cancelChecker) {
		try {
			int offset = document.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return Collections.emptyList();
			}
			List<LocationLink> locations = new ArrayList<>();
			// Start end tag definition
			findStartEndTagDefinition(node, template, document, offset, locations);
			return locations;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteDefinition the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}

	}

	/**
	 * Find start end tag definition.
	 * 
	 * @param document
	 * @param template
	 * 
	 * @param request   the definition request
	 * @param locations the locations
	 * @throws BadLocationException
	 */
	private static void findStartEndTagDefinition(Node node, Template template, TextDocument document, int offset,
			List<LocationLink> locations) throws BadLocationException {
		if (node != null && node.getKind() == NodeKind.SectionTag) {
			// Node is a Qute section tag
			SectionTag sectionTag = (SectionTag) node;
			if (sectionTag.hasStartTag() && sectionTag.hasEndTag()) {
				// The DOM element has end and start tag
				Range startRange = QutePositionUtility.selectStartTag(sectionTag, document);
				if (startRange == null) {
					return;
				}
				Range endRange = QutePositionUtility.selectEndTag(sectionTag, document);
				if (endRange == null) {
					return;
				}
				if (sectionTag.isInStartTag(offset)) {
					// Start tag was clicked, jump to the end tag
					locations.add(new LocationLink(template.getId(), endRange, endRange, startRange));
				} else if (sectionTag.isInEndTag(offset)) {
					// End tag was clicked, jump to the start tag
					locations.add(new LocationLink(template.getId(), startRange, startRange, endRange));
				}
			}
		}
	}

}
