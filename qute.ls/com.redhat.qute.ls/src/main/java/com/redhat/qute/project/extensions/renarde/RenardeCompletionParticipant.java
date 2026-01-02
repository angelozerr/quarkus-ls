package com.redhat.qute.project.extensions.renarde;

import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.project.extensions.CompletionParticipant;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.QutePositionUtility;

public class RenardeCompletionParticipant implements CompletionParticipant {

	private final RenardeProjectExtension renardeProjectExtension;

	public RenardeCompletionParticipant(RenardeProjectExtension renardeProjectExtension) {
		this.renardeProjectExtension = renardeProjectExtension;
	}

	@Override
	public void doComplete(CompletionRequest completionRequest, Part part, Parts parts,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<CompletionItem> completionItems) {
		Range range = createRange(completionRequest, part, parts);
		for (MessagesFileInfo messagesFileInfo : renardeProjectExtension.getMessagesFileInfos()) {
			messagesFileInfo.getProperties() //
					.forEach((k, v) -> {
						String key = k.toString();
						String value = v.toString();

						String messageKey = "m:" + key;
						String label = messageKey + " = " + value;
						CompletionItem item = new CompletionItem();
						item.setLabel(label);
						item.setFilterText(messageKey);
						item.setKind(CompletionItemKind.Property);
						TextEdit textEdit = new TextEdit();
						textEdit.setRange(range);

						item.setInsertTextFormat(
								completionSettings.isCompletionSnippetsSupported() ? InsertTextFormat.Snippet
										: InsertTextFormat.PlainText);
						int nbArgs = countPercentS(value);
						textEdit.setNewText(
								createMMessageSnippet(messageKey, nbArgs, completionSettings, formattingSettings));

						item.setTextEdit(Either.forLeft(textEdit));
						completionItems.add(item);
					});
		}

	}

	private Range createRange(CompletionRequest completionRequest, Part part, Parts parts) {
		if (parts != null) {
			return QutePositionUtility.createRange(parts);
		}
		if (part != null) {
			return QutePositionUtility.createRange(part);
		}
		return QutePositionUtility.createRange(completionRequest.getOffset(), completionRequest.getOffset(),
				completionRequest.getTemplate());
	}

	private static String createMMessageSnippet(String messageKey, int nbArgs,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings) {
		if (nbArgs == 0) {
			return messageKey;
		}
		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		StringBuilder snippet = new StringBuilder(messageKey);
		snippet.append('(');
		for (int i = 0; i < nbArgs; i++) {
			if (i > 0) {
				snippet.append(", ");
			}
			String paramName = "arg" + i;
			if (snippetsSupported) {
				SnippetsBuilder.placeholders(i + 1, paramName, snippet);
			} else {
				snippet.append(paramName);
			}
		}
		snippet.append(')');
		if (snippetsSupported) {
			SnippetsBuilder.tabstops(0, snippet);
		}
		return snippet.toString();
	}

	/**
	 * Counts the number of "%s" placeholders in the given string. Escaped percent
	 * sequences ("%%") are ignored.
	 *
	 * @param s the input string
	 * @return the number of "%s" occurrences
	 */
	static int countPercentS(String s) {
		int count = 0;

		// Iterate over the string, stopping at length - 1
		// because we always look at the current char and the next one
		for (int i = 0; i < s.length() - 1; i++) {

			// Look for a '%' character
			if (s.charAt(i) == '%') {
				char next = s.charAt(i + 1);

				// If the next character is also '%', this is an escaped percent (%%)
				// Skip both characters
				if (next == '%') {
					i++;
				}
				// If the next character is 's', we found a "%s" placeholder
				else if (next == 's') {
					count++;
					i++; // Skip the 's' since it has already been processed
				}
			}
		}

		return count;
	}

}
