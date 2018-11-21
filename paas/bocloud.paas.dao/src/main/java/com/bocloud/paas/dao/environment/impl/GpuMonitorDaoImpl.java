package com.bocloud.paas.dao.environment.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.entity.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.environment.GpuMonitorDao;
import com.bocloud.paas.entity.GpuMonitor;

/**
 * @author Zaney
 * @data:2018年3月38日
 * @describe: GPU监控数据库交互实现层
 */
@Component
public class GpuMonitorDaoImpl extends BasicDao implements GpuMonitorDao {

	private static Logger logger = LoggerFactory.getLogger(GpuMonitorDao.class);
	@Override
	public List<GpuMonitor> list(String hostName, String num, String timeUnit) throws Exception {
		System.out.println("------------------------");
//		String sql = "select * from gpu_monitor where  host_name = :hostName";
		String sql = "select * from gpu_monitor where host_name = :hostName "
				+ "and time > (SELECT DATE_SUB(now(),INTERVAL " + num + " " + timeUnit + ")) ORDER BY time DESC";
		Map<String, Object> paramMap = MapTools.simpleMap("hostName", hostName);
		List<Object> list = this.queryForList(sql, paramMap, GpuMonitor.class);
		logger.info("------gpu tables list:"+JSONObject.toJSONString(list));
		List<GpuMonitor> result = new ArrayList<>();
		for (Object object : list) {
			logger.info("-------gpu object:" + JSONObject.toJSONString(object));
			result.add((GpuMonitor) object);
		}
		logger.info("------gpu result :"+JSONObject.toJSONString(result));
		return result;
	}

}
