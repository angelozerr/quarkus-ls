package com.redhat.qute.commons;

public class TemplatePath {
	
	public final static String DEFAULT_TAGS_DIR_NAME = "tags";
	
	private String templateBaseDir;
	
	private String tagsDirName;
	
	// Used by Gson
	public TemplatePath() {
		
	}
	
	public TemplatePath(String templateBaseDir) {
		this(templateBaseDir, DEFAULT_TAGS_DIR_NAME);
	}
	
	public TemplatePath(String templateBaseDir, String tagsDirName) {
		this.templateBaseDir = templateBaseDir;
		this.tagsDirName = tagsDirName;
	}

	public String getTemplateBaseDir() {
		return templateBaseDir;
	}

	public void setTemplateBaseDir(String templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
	}

	public String getTagsDirName() {
		return tagsDirName;
	}

	public void setTagsDirName(String tagsDirName) {
		this.tagsDirName = tagsDirName;
	}
	
	

}
