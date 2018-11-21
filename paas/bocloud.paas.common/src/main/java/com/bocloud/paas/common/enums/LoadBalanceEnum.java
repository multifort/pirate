package com.bocloud.paas.common.enums;
/**
 * 仓库枚举类型
 * @author Zaney
 * @data:2017年3月24日
 * @describe:
 */
public final class LoadBalanceEnum {
	private LoadBalanceEnum() {
		
	}
	//仓库类型
	public static enum Type {
		NGINX, F5, OTHER
	}

}
