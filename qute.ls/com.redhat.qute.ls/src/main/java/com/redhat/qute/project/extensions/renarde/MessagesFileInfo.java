package com.redhat.qute.project.extensions.renarde;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class MessagesFileInfo {

	private final Properties properties;
	private final Path messageFile;

	public MessagesFileInfo(Path messagesFile) throws FileNotFoundException, IOException {
		this.messageFile = messagesFile;
		properties = new Properties();
		properties.load(new FileInputStream(messagesFile.toFile()));
	}

	public boolean hasMessage(String expression) {
		String message = expression.substring(2);
		return properties.containsKey(message);
	}

	public Path getMessageFile() {
		return messageFile;
	}

	public Properties getProperties() {
		return properties;
	}

}
