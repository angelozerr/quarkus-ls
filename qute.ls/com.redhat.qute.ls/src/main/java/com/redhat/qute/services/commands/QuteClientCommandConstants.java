/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.commands;

/**
 * Qute command IDs available on LSP client side.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteClientCommandConstants {

	private QuteClientCommandConstants() {
	}

	/**
	 * Client command to open Qute template by file Uri.
	 */
	public static final String COMMAND_OPEN_URI = "qute.command.open.uri";

	/**
	 * Client command to go to the definition of Java data model (field, method,
	 * method invocation of "data" method).
	 */
	public static final String COMMAND_JAVA_DEFINITION = "qute.command.java.definition";

	/**
	 * Client command to update client configuration settings.
	 */
	public static final String COMMAND_CONFIGURATION_UPDATE = "qute.command.configuration.update";

}