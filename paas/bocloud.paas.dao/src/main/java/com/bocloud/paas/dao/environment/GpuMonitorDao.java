package com.bocloud.paas.dao.environment;

import java.util.List;
import com.bocloud.paas.entity.GpuMonitor;

/**
 * @author Zaney
 * @data:2018年3月38日
 * @describe: GPU监控数据库交互层
 */
public interface GpuMonitorDao {
	
	List<GpuMonitor> list(String hostName, String num, String timeUnit) throws Exception;
}
