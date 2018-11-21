package com.bocloud.paas.common.enums;

/**
 * describe: 资源配额枚举类
 * @author Zaney
 * @data 2017年12月4日
 */
public class ResourceQuotaEnum {
	public static enum type {
		COMPUTE_RESOURCE,  //计算资源类型
		OBJECT_COUNT,   //对象数
		LIMIT_RANGE,   // limitRange
		LIMIT_RANGE_POD,  //limitRange中配置的pod资源限制
		LIMIT_RANGE_CONTAINER,   //limitRange中配置的container资源限制
		LIMIT_RANGE_DEFAULT_CONTAINER; //limitRange中配置的默认的container资源限制
	}
	
	/**
	 * 计算资源
	 */
	public static enum computeResource {
		REQUESTS_CPU, //cpu请求
		LIMITS_CPU,   //cpu限制
		REQUESTS_MEMORY,  //内存请求
		LIMITS_MEMORY;  //内存限制
	}
	
	/**
	 * 对象数
	 */
	public static enum objectCount {
		PODS, 
		CONFIGMAPS, 
		PERSISTENTVOLUMECLAIMS, 
		SERVICES, 
		SERVICES_NODEPORTS;
	}
	
	public static enum limitRange {
		POD,  //pod类型，对pod进行资源限制
		CONTAINER,  //容器类型，对容器进行资源限制
	}

}
