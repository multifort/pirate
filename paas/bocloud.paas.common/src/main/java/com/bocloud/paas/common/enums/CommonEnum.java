package com.bocloud.paas.common.enums;

/**
 * 应用平台公共枚举类
 * @author zjm
 * @date 2017年3月31日
 */
public class CommonEnum {
	
	public static enum Status {
		/**
		 * 正常
		 */
		NORMAL, 
		/**
		 * 异常
		 */
		ABNORMAL,
		/**
		 * 锁定
		 */
		LOCK
	}
	
	public static enum Property {
		/**
		 * 公共
		 */
		PUBLIC, 
		/**
		 * 私有
		 */
		PRIVATE
	}
	
	public static enum IsDeleted {
		/**
		 * 未删除
		 */
		NOT,
		/**
		 * 删除
		 */
		DELETE
	}
}
