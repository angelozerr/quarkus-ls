package com.redhat.qute.dap.model;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Source;

public class QuteBreakpoint extends Breakpoint {

	public QuteBreakpoint(Source source, int line) {
		setSource(source);
		setLine(line);
	}

}
