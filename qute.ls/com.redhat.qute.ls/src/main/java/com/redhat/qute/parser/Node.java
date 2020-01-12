package com.redhat.qute.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class Node {

	protected static final int NULL_VALUE = -1;

	private int start;
	private int end;
	private boolean closed;
	private Node parent;
	private List<Node> children;

	Node(int start, int end) {
		this.start = start;
		this.end = end;
		this.closed = false;
	};

	public abstract NodeKind getKind();

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	void setEnd(int end) {
		this.end = end;
	}

	void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isClosed() {
		return closed;
	}

	void addChild(Node child) {
		child.setParent(this);
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return parent;
	}

	public List<Node> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}

	public abstract String getNodeName();

	public Node findNodeAt(int offset) {
		return findNodeAt(this, offset);
	}

	public static Node findNodeAt(Node node, int offset) {
		List<Node> children = node.getChildren();
		int idx = findFirst(children, c -> offset <= c.getStart()) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (isIncluded(child, offset)) {
				return findNodeAt(child, offset);
			}
		}
		return node;
	}

	public static boolean isIncluded(Node node, int offset) {
		if (node == null) {
			return false;
		}
		return isIncluded(node.getStart(), node.getEnd(), offset);
	}

	public static boolean isIncluded(int start, int end, int offset) {
		return offset >= start && offset <= end;
	}

	/**
	 * Takes a sorted array and a function p. The array is sorted in such a way that
	 * all elements where p(x) is false are located before all elements where p(x)
	 * is true.
	 * 
	 * @returns the least x for which p(x) is true or array.length if no element
	 *          full fills the given function.
	 */
	private static <T> int findFirst(List<T> array, Function<T, Boolean> p) {
		int low = 0, high = array.size();
		if (high == 0) {
			return 0; // no children
		}
		while (low < high) {
			int mid = (int) Math.floor((low + high) / 2);
			if (p.apply(array.get(mid))) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}

}