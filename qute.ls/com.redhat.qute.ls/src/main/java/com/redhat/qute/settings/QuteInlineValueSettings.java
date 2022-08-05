/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

/**
 * Qute inline value settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteInlineValueSettings {

	public static final QuteInlineValueSettings DEFAULT = new QuteInlineValueSettings();

	private boolean enabled;

	public QuteInlineValueSettings() {
		setEnabled(true);
	}

	/**
	 * Returns true if inline value support is enabled and false otherwise.
	 * 
	 * @return true if inline value support is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void update(QuteInlineValueSettings newInlayHint) {
		this.setEnabled(newInlayHint.isEnabled());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuteInlineValueSettings other = (QuteInlineValueSettings) obj;
		if (enabled != other.enabled)
			return false;
		return true;
	}

}
