package com.bocloud.paas.common.enums;

public class ApplicationEnum {

	public static enum Status {
		/**
		 * 未部署
		 */
		NOT_DEPLOY,
		/**
		 * 已部署
		 */
		DEPLOY;
		// /**
		// * 部署中
		// */
		// EXECUTE_DEPLOY,
		// /**
		// * 运行
		// */
		// RUN,
		// /**
		// * 异常
		// */
		// ABNORMAL;
	}

	public static enum RESOURCE {
		NODE, NAMESPACE, REPLICATIONCONTROLLER, DEPLOYMENT, SERVICE, INGRESS, PERSISTENTVOLUME, 
		POD, HORIZONTALPODAUTOSCALER, EVENT, PERSISTENTVOLUMECLAIMS, CONFIGMAP, DAEMONSETS, STATEFULSETS, RESOURCEQUOTA, 
		LIMITRANGE, JOB;
	}
	
	/**
	 * describe: 该应用是否配置了资源配额
	 * @author Zaney
	 * @data 2017年12月4日
	 */
	public static enum QuotaStatus {
		QUOTA, NOT_QUOTA
	}
}
