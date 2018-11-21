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
import com.bocloud.paas.dao.environment.PersistentVolumeDao;
import com.bocloud.paas.entity.Volume;

@Service("persistentVolumeDao")
public class PersistentVolumeDaoImpl extends JdbcGenericDao<Volume, Long> implements PersistentVolumeDao {

	@Override
	public Integer count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from volume a where is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from volume a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<Volume> pvs = this.list(Volume.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (Volume pv : pvs) {
			beans.add(new SimpleBean(pv.getId(), pv.getName()));
		}
		return beans;
	}

	@Override
	public List<Volume> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select * from volume a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(Volume.class, sql, paramMap);
	}

	@Override
	public List<Volume> queryByName(String name, Long envId) throws Exception {
		String sql = "select * from volume where is_deleted = 0 and name = :name and env_id = :envId";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("envId", envId);
		List<Volume> pvs = this.list(Volume.class, sql, params);
		return pvs;
	}

	@Override
	public Volume queryById(Long id) throws Exception {
		String sql = "select * from volume where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Volume> pvs = this.list(Volume.class, sql, params);
		return pvs.get(0);
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {
		String sql = "update volume set is_deleted = true , gmt_modify = :gmtModify , mender_id = :menderId where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);
	}

	@Override
	public List<Volume> queryByEnvId(Long envId) throws Exception {
		String sql = "select * from volume where is_deleted = 0 and env_id = :envId";
		Map<String, Object> params = MapTools.simpleMap("envId", envId);
		return this.list(Volume.class, sql, params);
	}

}
