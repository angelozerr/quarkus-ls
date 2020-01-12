package com.redhat.qute.utils;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.SectionTag;

public class QutePositionUtility {

	public static Range selectStartTag(SectionTag sectionTag, TextDocument document) throws BadLocationException {
		String tag = sectionTag.getTag();
		if (tag == null) {
			return null;
		}
		int start = sectionTag.getStartTagOpenOffset() + 1;
		int end = tag.length() + start + 1;
		return new Range(document.positionAt(start), document.positionAt(end));
	}

	public static Range selectEndTag(SectionTag sectionTag, TextDocument document) throws BadLocationException {
		int start = sectionTag.getEndTagOpenOffset();
		if (start == -1) {
			return null;
		}
		start++;
		int end = sectionTag.getEndTagCloseOffset();
		if (end == -1) {
			return null;
		}
		return new Range(document.positionAt(start), document.positionAt(end));
	}

	public static Location toLocation(LocationLink locationLink) {
		return new Location(locationLink.getTargetUri(), locationLink.getTargetRange());
	}
}
