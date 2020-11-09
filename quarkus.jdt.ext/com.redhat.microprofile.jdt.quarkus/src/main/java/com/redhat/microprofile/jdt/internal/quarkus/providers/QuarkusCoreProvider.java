/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

/**
 * Properties provider that provides static Quarkus properties
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/eclipse/microprofile-health/blob/master/spec/src/main/asciidoc/protocol-wireformat.adoc
 *
 */
public class QuarkusCoreProvider extends AbstractStaticQuarkusPropertiesProvider {

	public QuarkusCoreProvider() {
		super("/static-properties/quarkus-core-metadata.json");
	}
}