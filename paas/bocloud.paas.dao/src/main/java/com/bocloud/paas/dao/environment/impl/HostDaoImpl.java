package com.bocloud.paas.dao.environment.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.environment.HostDao;
import com.bocloud.paas.entity.Host;

@Service("hostDao")
public class HostDaoImpl extends JdbcGenericDao<Host, Long> implements HostDao {

	@Override
	public Host queryById(Long id) throws Exception {
		String sql = "select * from host a where a.id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Host> hosts = this.list(Host.class, sql, params);
		return hosts.get(0);
	}

	@Override
	public List<Host> queryByIp(String ip) throws Exception {
		String sql = "select * from host a where a.ip = :ip and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("ip", ip);
		List<Host> hosts = this.list(Host.class, sql, params);
		return hosts;
	}

	@Override
	public Integer count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from host a where is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from host a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<Host> hosts = this.list(Host.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (Host host : hosts) {
			beans.add(new SimpleBean(host.getId(), host.getName()));
		}
		return beans;
	}

	@Override
	public List<Host> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select * from host a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(Host.class, sql, paramMap);
	}

	@Override
	public List<Host> queryByEnvId(Long envId) throws Exception {
		String sql = "select * from host a where a.env_id = :envId and a.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("envId", envId);
		List<Host> hosts = this.list(Host.class, sql, params);
		return hosts;
	}

	@Override
	public List<Host> queryByName(String name) throws Exception {
		String sql = "select * from host a where a.name = :name and a.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Host> hosts = this.list(Host.class, sql, params);
		return hosts;
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {
		String sql = "update host set is_deleted = true , gmt_modify = :gmtModify , mender_id = :menderId where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);
	}

	@Override
	public List<Host> queryNormalHost(Long userId) throws Exception {
		String sql = "select * from host a where a.env_id is null and a.is_deleted = 0  and a.status = '1'";
		// Map<String, Object> params = MapTools.simpleMap("createrId", userId);
		return this.list(Host.class, sql);
	}

	@Override
	public List<Host> queryAll() throws Exception {
		String sql = "select * from host a where a.is_deleted = 0";
		return this.list(Host.class, sql);
	}
	
	@Override
	public List<Host> queryHostNotInEnv() throws Exception {
		String sql = "select * from host where is_deleted = 0 and env_id is null";
		return this.list(Host.class, sql);
	}

}
