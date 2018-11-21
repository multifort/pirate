package com.bocloud.paas.web.upload;

public class PictureBean {
	
	private String name;
	
	private String targetPath; //文件路径（包括文件名）
	
	private String dirPath; //文件目录路径（不包括文件名）
	
	private String type; //功能模块类型

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
