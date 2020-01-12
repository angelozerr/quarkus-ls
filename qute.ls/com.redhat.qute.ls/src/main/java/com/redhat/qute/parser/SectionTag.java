package com.redhat.qute.parser;

public class SectionTag extends Node {

	private String tag;

	private int startTagOpenOffset;

	private int startTagCloseOffset;

	private int endTagOpenOffset;

	private int endTagCloseOffset;

	private boolean selfClosed;

	SectionTag(int start, int end) {
		super(start, end);
		this.startTagOpenOffset = NULL_VALUE;
		this.startTagCloseOffset = NULL_VALUE;
		this.endTagOpenOffset = NULL_VALUE;
		this.endTagCloseOffset = NULL_VALUE;
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.SectionTag;
	}

	public int getStartTagOpenOffset() {
		return startTagOpenOffset;
	}

	void setStartTagOpenOffset(int startTagOpenOffset) {
		this.startTagOpenOffset = startTagOpenOffset;
	}

	public int getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	void setStartTagCloseOffset(int startTagCloseOffset) {
		this.startTagCloseOffset = startTagCloseOffset;
	}

	public String getTag() {
		return tag;
	}

	void setTag(String tag) {
		this.tag = tag;
	}

	public int getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	void setEndTagOpenOffset(int endTagOpenOffset) {
		this.endTagOpenOffset = endTagOpenOffset;
	}

	public int getEndTagCloseOffset() {
		return endTagCloseOffset;
	}

	void setEndTagCloseOffset(int endTagCloseOffset) {
		this.endTagCloseOffset = endTagCloseOffset;
	}

	public boolean isSelfClosed() {
		return selfClosed;
	}

	void setSelfClosed(boolean selfClosed) {
		this.selfClosed = selfClosed;
	}

	@Override
	public String getNodeName() {
		return "#" + getTag();
	}

	/**
	 * Returns true if has a start tag.
	 * 
	 * @return true if has a start tag.
	 */
	public boolean hasStartTag() {
		return getStartTagOpenOffset() != NULL_VALUE;
	}

	/**
	 * Returns true if has an end tag.
	 * 
	 * @return true if has an end tag.
	 */
	public boolean hasEndTag() {
		return getEndTagOpenOffset() != NULL_VALUE;
	}

	public boolean isInStartTag(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
			// case <|
			return true;
		}
		if (offset > startTagOpenOffset && offset <= startTagCloseOffset) {
			// case <bean | >
			return true;
		}
		return false;
	}

	public boolean isInEndTag(int offset) {
		if (endTagOpenOffset == NULL_VALUE) {
			// case >|
			return false;
		}
		if (offset > endTagOpenOffset && offset < getEnd()) {
			// case <\bean | >
			return true;
		}
		return false;
	}

}
