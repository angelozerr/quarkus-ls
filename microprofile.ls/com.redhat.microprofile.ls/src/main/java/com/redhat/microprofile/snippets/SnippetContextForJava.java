/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.snippets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.redhat.microprofile.commons.JavaKind;
import com.redhat.microprofile.commons.JavaSnippetContext;
import com.redhat.microprofile.ls.commons.snippets.ISnippetContext;

/**
 * A snippet context for Java files which matches java scope and dependency.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetContextForJava extends JavaSnippetContext implements ISnippetContext<Boolean> {

	public static final TypeAdapter<SnippetContextForJava> TYPE_ADAPTER = new SnippetContextForJavaAdapter();

	public SnippetContextForJava(JavaKind kind, List<String> types) {
		super.setKind(kind);
		super.setType(types);
	}

	@Override
	public boolean isMatch(Boolean context) {
		if (context == null) {
			return true;
		}
		return context;
	}

	private static class SnippetContextForJavaAdapter extends TypeAdapter<SnippetContextForJava> {

		@Override
		public SnippetContextForJava read(final JsonReader in) throws IOException {
			JsonToken nextToken = in.peek();
			if (nextToken == JsonToken.NULL) {
				return null;
			}

			JavaKind kind = JavaKind.TYPE;
			List<String> types = new ArrayList<>();
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "type":
					if (in.peek() == JsonToken.BEGIN_ARRAY) {
						in.beginArray();
						while (in.peek() != JsonToken.END_ARRAY) {
							types.add(in.nextString());
						}
						in.endArray();
					} else {
						types.add(in.nextString());
					}
					break;
				case "kind":
					kind = JavaKind.getScope(in.nextString());
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return new SnippetContextForJava(kind, types);
		}

		@Override
		public void write(JsonWriter out, SnippetContextForJava value) throws IOException {
			// Do nothing
		}
	}

}
