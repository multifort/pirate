package com.bocloud.paas.service.repository.model;

import java.util.List;

import com.bocloud.paas.entity.Image;

/**
 * docker镜像组装类
 * 
 * @author zjm
 * @date 2017年3月17日
 */
public class ImageInfo extends Image {
	// 服务器镜像信息
	public String env;
	public String exposedPort;
	// 镜像层
	public List<String> imageLayers;

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getExposedPort() {
		return exposedPort;
	}

	public void setExposedPort(String exposedPort) {
		this.exposedPort = exposedPort;
	}

	public List<String> getImageLayers() {
		return imageLayers;
	}

	public void setImageLayers(List<String> imageLayers) {
		this.imageLayers = imageLayers;
	}

}
