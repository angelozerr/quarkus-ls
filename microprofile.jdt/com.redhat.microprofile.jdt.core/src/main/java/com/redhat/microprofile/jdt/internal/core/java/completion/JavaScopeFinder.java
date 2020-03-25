package com.redhat.microprofile.jdt.internal.core.java.completion;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JavaScopeFinder {

	static enum JavaScope {
		UNKNOWN, TYPE, METHOD, FIELD, PARAMETER
		
	}
	
	private static class JavaScopeFinderVisitor extends ASTVisitor {

		private JavaScope scope;
		private int fStart;
		private ASTNode fCoveringNode;
		private ASTNode fCoveredNode;

		JavaScopeFinderVisitor(int offset) {
			super(true); // include Javadoc tags
			this.fStart = offset;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			int nodeStart = node.getStartPosition();
			int nodeEnd = nodeStart + node.getLength();
			if (nodeEnd < this.fStart /* || this.fEnd < nodeStart */) {
				return false;
			}
			if (nodeStart <= this.fStart /* && this.fEnd <= nodeEnd */) {
				this.fCoveringNode = node;
			}
			if (this.fStart <= nodeStart /* && nodeEnd <= this.fEnd */) {
				if (this.fCoveringNode == node) { // nodeStart == fStart && nodeEnd == fEnd
					this.fCoveredNode = node;
					return true; // look further for node with same length as parent
				} else if (this.fCoveredNode == null) { // no better found
					this.fCoveredNode = node;
				}
				return false;
			}
			return true;
		}

		public JavaScope getScope() {
			return scope;
		}

	}

	private JavaScope scope;

	/**
	 * Instantiate a new node finder using the given root node, the given start and
	 * the given length.
	 *
	 * @param root   the given root node
	 * @param start  the given start
	 * @param length the given length
	 */
	public JavaScopeFinder(ASTNode root, int start) {
		JavaScopeFinderVisitor nodeFinderVisitor = new JavaScopeFinderVisitor(start);
		root.accept(nodeFinderVisitor);
		this.scope = computeScope(nodeFinderVisitor.fCoveredNode);
	}

	private static JavaScope computeScope(ASTNode coveredNode) {
		if (coveredNode == null) {
			return JavaScope.UNKNOWN;
		}
		if (coveredNode instanceof SimpleName) {
			ASTNode parent = coveredNode.getParent();
			if (parent instanceof Annotation) {
				parent = parent.getParent();
				return computeScope(parent);
			}
			return JavaScope.UNKNOWN;
		} else if (coveredNode instanceof MethodDeclaration) {
			return JavaScope.METHOD;
		} else if (coveredNode instanceof TypeDeclaration) {
			return JavaScope.TYPE;
		} else if (coveredNode instanceof FieldDeclaration) {
			return JavaScope.FIELD;
		} else if (coveredNode instanceof Block) {
			ASTNode parent = coveredNode.getParent();
			if (parent instanceof MethodDeclaration) {
				return JavaScope.PARAMETER;
			}	
		} else if (coveredNode instanceof Modifier) {
			ASTNode parent = coveredNode.getParent();
			return computeScope(parent);
		}

		return JavaScope.UNKNOWN;
	}

	public JavaScope getScope() {
		return scope;
	}
}
