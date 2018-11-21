package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.paas.dao.application.DeployHistoryDao;
import com.bocloud.paas.entity.DeployHistory;
import com.bocloud.common.model.Param;
import com.bocloud.database.core.BasicDao;
import com.bocloud.database.utils.SQLHelper;

/**
 * 
 * @author zjm
 * @date 2017年4月4日
 */
@Repository("deployHistoryDao")
public class DeployHistoryDaoImpl extends BasicDao implements DeployHistoryDao {
	
	@Override
	public boolean insert(DeployHistory deployHistory) throws Exception {
		return this.baseSaveEntity(deployHistory);
	}

	@Override
	public List<DeployHistory> list(int page, int rows, List<Param> params, Map<String, String> sorter)
			throws Exception {
		String sql = "select cu.name as creator_name, dh.* "
				+ "from deploy_history dh " + "LEFT JOIN `user` cu on cu.id = dh.creater_id where dh.id !=0 ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<Object> list = this.queryForList(sql, paramMap, DeployHistory.class);
		List<DeployHistory> result = new ArrayList<>();
		for (Object object : list) {
			result.add((DeployHistory) object);
		}
		return result;
	}
	
	@Override
	public List<DeployHistory> list(List<Param> params, Map<String, String> sorter)
			throws Exception {
		String sql = "select * from deploy_history where id !=0 ";
		sql = SQLHelper.buildRawSql(sql, 1, Integer.MAX_VALUE, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<Object> list = this.queryForList(sql, paramMap, DeployHistory.class);
		List<DeployHistory> result = new ArrayList<>();
		for (Object object : list) {
			result.add((DeployHistory) object);
		}
		return result;
	}
	
	@Override
	public int count(List<Param> params) throws Exception {
		String sql = "select count(1) from deploy_history where id !=0 ";
		sql = SQLHelper.buildRawSql(sql, params, null, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public boolean delete(String[] fileds) throws Exception {
		return this.deleteWithColumn(DeployHistory.class, fileds);
	}

}
