package com.bocloud.paas.dao.environment.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.environment.LoadBalanceDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.LoadBalance;

@Service("loadBalanceDao")
public class LoadBalanceDaoImpl extends JdbcGenericDao<LoadBalance, Long> implements LoadBalanceDao {
	
	@Autowired
	private ApplicationDao applicationDao;

	@Override
	public List<LoadBalance> query(Long id) throws Exception {
		String sql = "select * from loadbalance a where a.id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<LoadBalance> loadbalances = this.list(LoadBalance.class, sql, params);
		return loadbalances;
	}

	@Override
	public List<LoadBalance> queryByName(String name) throws Exception {
		String sql = "select * from loadbalance a where a.name = :name and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<LoadBalance> loadbalances = this.list(LoadBalance.class, sql, params);
		return loadbalances;
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {
		String sql = "update loadbalance set is_deleted = 1 , gmt_modify = :gmtModify where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);
	}

	@Override
	public Integer count(List<Param> params) throws Exception {
		String sql = "select count(1) from loadbalance a where is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select a.id,a.name from loadbalance a where a.is_deleted = 0";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<LoadBalance> loadbalances = this.list(LoadBalance.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (LoadBalance loadbalance : loadbalances) {
			beans.add(new SimpleBean(loadbalance.getId(), loadbalance.getName()));
		}
		return beans;
	}

	@Override
	public List<LoadBalance> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select * from loadbalance a where a.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(LoadBalance.class, sql, paramMap);
	}
	
	@Override
	public int countApps(Long loadbalanceId) throws Exception {
		String sql = "select count(*) from (select a.application_id,a.loadbalance_id from application_service_loadbalance_info a group by application_id) b where b.loadbalance_id= :loadbalanceId";
		Map<String, Object> paramMap = MapTools.simpleMap("loadbalanceId", loadbalanceId);
		return this.countQuery(sql, paramMap).intValue();
	}
	
	@Override
	public int countApps(List<Param> params) throws Exception {
		String sql="select count(*) from (select a.application_id,a.loadbalance_id from application_service_loadbalance_info a group by application_id) b where 1=1";
		sql = SQLHelper.buildRawSql(sql, params, null, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}
	
	@Override
	public List<Application> listApps(int page, int rows, List<Param> params, Map<String, String> sorter)
			throws Exception {
		String sql = "select a.* from application a,(select c.application_id as id,c.loadbalance_id from application_service_loadbalance_info c group by application_id) b WHERE a.id=b.id AND a.is_deleted=0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return applicationDao.list(Application.class, sql, paramMap);
	}
	
}
