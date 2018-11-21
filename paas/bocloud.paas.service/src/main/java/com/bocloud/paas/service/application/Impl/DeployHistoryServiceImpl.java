package com.bocloud.paas.service.application.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.paas.dao.application.DeployHistoryDao;
import com.bocloud.paas.entity.DeployHistory;
import com.bocloud.paas.service.application.DeployHistoryService;
import com.google.common.collect.Maps;

/**
 * @author zjm
 * @date 2017年3月17日
 */
@Service("deployHistoryService")
public class DeployHistoryServiceImpl implements DeployHistoryService {

	private static Logger logger = LoggerFactory.getLogger(DeployHistoryServiceImpl.class);

	@Autowired
	private DeployHistoryDao deployHistoryDao;

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple) {
		GridBean gridBean;
		List<DeployHistory> list = new ArrayList<>();
		try {
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = deployHistoryDao.count(params);
			if (simple) {
				list = deployHistoryDao.list(params, sorter);
			} else {
				list = deployHistoryDao.list(page, rows, params, sorter);
			}
			
			gridBean = GridHelper.getBean(page, rows, total, list);
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("list deployHistory failure:", e);
			return new BsmResult(false, "查询失败");
		}
	}

}
