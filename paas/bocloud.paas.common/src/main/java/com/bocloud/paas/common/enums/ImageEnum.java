package com.bocloud.paas.common.enums;

public final class ImageEnum {

	//镜像类型
	public static enum Type {
		/**
		 * 镜像
		 */
		CONTAINER,
		/**
		 * 虚拟机
		 */
		VIRTUAL,
		/**
		 * 安装包
		 */
		INSTALL_PACK,
		/**
		 * 应用包
		 */
		APPLICATION_PACK,
		/**
		 * 其他
		 */
		OTHER,
	}
	
}
