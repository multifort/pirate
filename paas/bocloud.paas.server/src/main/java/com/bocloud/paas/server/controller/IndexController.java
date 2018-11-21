package com.bocloud.paas.server.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bocloud.common.model.BaseResult;
import com.bocloud.common.utils.SystemTool;
import com.bocloud.coordinator.cache.LeaderShip;

/**
 * 服务器状态控制器
 * 
 * @author dmw
 *
 */
@RestController
public class IndexController {

	/**
	 * 返回服务器当前的状态信息
	 * 
	 * @return
	 */
	@RequestMapping("/status")
	public BaseResult<Map<String, Object>> status() {
		SystemTool systemTool = new SystemTool();
		BaseResult<Map<String, Object>> status = systemTool.status();
		status.getData().put("isLeader", LeaderShip.isLeader());
		return status;
	}
}
