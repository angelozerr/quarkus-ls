package com.redhat.qute.project.datamodel.resolvers;

public class MessageTypeValueResolver extends TypeValueResolver implements MessageValueResolver {

	private String locale;

	private String message;

	/**
	 * Returns the locale of the message and null otherwise.
	 * 
	 * @return the locale of the message and null otherwise.
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale the locale.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * Returns the message content and null otherwise.
	 * 
	 * @return the message content and null otherwise.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message content.
	 * 
	 * @param message the message content.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
