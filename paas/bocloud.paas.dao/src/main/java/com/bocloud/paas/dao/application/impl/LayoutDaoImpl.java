package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.bocloud.paas.dao.application.LayoutDao;
import com.bocloud.paas.entity.Layout;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;

/**
 * 編排文件处理接口实现
 * 
 * @author caidongqing
 * @version 1.0
 * @since 2016.12.28
 *
 */
@Component("layoutDao")
public class LayoutDaoImpl extends JdbcGenericDao<Layout, Long> implements LayoutDao {

	@Override
	public List<Layout> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name,a.type,a.file_path,a.props,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.`status` "
				+ "from layout a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(Layout.class, sql, paramMap);
	}

	@Override
	public List<Layout> listByAppId(Long appId, int page, int rows, List<Param> params, Map<String, String> sorter)
			throws Exception {
		String sql = "select a.id, a.name, a.type, a.file_path, a.props, a.creater_id, a.mender_id, "
				+ "a.gmt_create, a.gmt_modify, a.`status` " + "from layout a "
				+ "left join application_layout_info ali on ali.layout_id = a.id "
				+ "where a.is_deleted = 0 and ali.application_id = :app_id ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("app_id", appId);
		return this.list(Layout.class, sql, paramMap);
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from layout a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<Layout> layouts = this.list(Layout.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (Layout layout : layouts) {
			beans.add(new SimpleBean(layout.getId(), layout.getName()));
		}
		return beans;
	}

	@Override
	public List<SimpleBean> listByAppId(Long appId, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select a.id,a.name from layout a "
				+ "left join application_layout_info ali on ali.layout_id = a.id "
				+ "where a.is_deleted = 0 and ali.application_id = :app_id ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("app_id", appId);
		List<Layout> layouts = this.list(Layout.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (Layout layout : layouts) {
			beans.add(new SimpleBean(layout.getId(), layout.getName()));
		}
		return beans;
	}

	@Override
	public boolean remove(Long id, Long userId) throws Exception {
		String sql = "update layout set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from layout a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public int countByAppId(Long appId, List<Param> params) throws Exception {
		String sql = "select count(1) from layout a  "
				+ "left join application_layout_info ali on ali.layout_id = a.id "
				+ "where a.is_deleted = 0 and ali.application_id = :app_id ";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("app_id", appId);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public Layout query(Long id) throws Exception {
		String sql = "select a.id,a.name,a.file_name,a.file_path,a.type,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.remark,a.`status` from layout a where a.is_deleted = 0 and a.id = :id";
		Map<String, Object> param = MapTools.simpleMap(Common.ID, id);
		List<Layout> list = this.list(Layout.class, sql, param);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public boolean deleteLayoutAppInfo(Long id) throws Exception {
		String sql = "delete from application_layout_info where layout_id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		return this.execute(sql, params) >= 0;
	}

	@Override
	public Layout checkName(String name) throws Exception {
		String sql = "select a.id,a.name,a.file_name,a.file_path,a.type,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.remark,a.`status` from layout a where a.is_deleted = 0 and name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Layout> list = this.list(Layout.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
