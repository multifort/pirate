package com.bocloud.paas.service.application.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.Relation;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.Sign;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.log.dao.LogDao;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.application.AppLogService;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;
/**
 * @author Zaney
 * @data  2017年5月4日
 * describe: 历史记录业务层实现类
 */
@Service("appLogService")
public class AppLogServiceImpl implements AppLogService {
	private static Logger logger = LoggerFactory.getLogger(AppLogService.class);
	@Autowired
	private UserService userService;
	@Autowired
	private LogDao logDao;
	
	@Transactional
	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			Long userId) {
		StringBuffer userIds = new StringBuffer();
		//获取该用户的组织ID及该组织id下的子组织id
		String deptId = userService.listDept(userId);
		String[] deptIds = deptId.split(",");
		//循环根据组织机构id获取用户id
		for (int i = 0; i < deptIds.length; i++) {
			BsmResult usersResult = userService.listByDid(Long.valueOf(deptIds[i]));
			JSONArray array = JSONObject.parseArray(usersResult.getData().toString());
			for (Object object : array) {
				if (userIds.length() > 0) {
					userIds.append(",");
				}
				User user = JSONObject.parseObject(object.toString(), User.class);
				userIds.append(user.getId());
			}
		}
		//根据用户id获取日志信息
		return getLogs(page, rows, params, sorter, simple, userIds.toString());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BsmResult getLogs(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, String userIds){
		GridBean gridBean = null;
		try {
			if (ListTool.isEmpty(params)) {
				params = new ArrayList<Param>();
			}
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", "desc");
			//param参数
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", userIds);
			Param param = new Param(Relation.AND, paramMap, Sign.IN);
			params.add(param);
			//获取total
			int total = this.logDao.count((List) params);
			Object e = new ArrayList();
			if (total > 0) {
				e = this.logDao.list(page, rows, (List) params, (Map) sorter);
			}
			gridBean = GridHelper.getBean(page, rows, total, (List) e);
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("Query app Log list fail:", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return new BsmResult(false, "查询失败", (String) null, (String) null);
		}
	}
	
}
