package com.bocloud.paas.dao.application.impl;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.application.LayoutTemplateDao;
import com.bocloud.paas.entity.LayoutTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component("layoutTemplateDao")
public class LayoutTemplateDaoImpl extends JdbcGenericDao<LayoutTemplate, Long>
		implements LayoutTemplateDao {

	@Override
	public List<LayoutTemplate> list(int page, int rows, List<Param> params,
                                     Map<String, String> sorter) throws Exception {
		String sql = "select a.id,a.name,a.remark,a.props,a.creater_id,a.mender_id,a.owner_id,a.gmt_create,a.gmt_modify,a.`status` from layout_template a where a.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(LayoutTemplate.class, sql, paramMap);
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter)
			throws Exception {
		String sql = "select a.id,a.name from layout_template a where a.is_deleted = 0";
		sql = SQLHelper
				.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<LayoutTemplate> layoutTemplates = this.list(LayoutTemplate.class,
				sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (LayoutTemplate layoutTemplate : layoutTemplates) {
			beans.add(new SimpleBean(layoutTemplate.getId(), layoutTemplate
					.getName()));
		}
		return beans;
	}

	@Override
	public boolean remove(Long id, Long userId) throws Exception {
		String sql = "update layout_template set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}

	@Override
	public int count(List<Param> params) throws Exception {
		String sql = "select count(1) from layout_template a where a.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public LayoutTemplate query(Long id) throws Exception {
		String sql = "select a.id,a.name,a.remark,a.props,a.creater_id,a.mender_id,a.owner_id,a.gmt_create,a.gmt_modify,a.`status` from layout_template a where a.is_deleted = 0 and a.id = :id";
		Map<String, Object> param = MapTools.simpleMap(Common.ID, id);
		List<LayoutTemplate> list = this.list(LayoutTemplate.class, sql, param);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public LayoutTemplate checkName(String name) throws Exception {
		String sql = "select a.id,a.name,a.remark,a.props,a.creater_id,a.mender_id,a.owner_id,a.gmt_create,a.gmt_modify,a.`status` from layout_template a where a.is_deleted = 0 and name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<LayoutTemplate> list = this
				.list(LayoutTemplate.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<LayoutTemplate> getList() throws Exception {
		String sql = "select * from layout_template where is_deleted = 0";
		return this.list(LayoutTemplate.class, sql);
	}

}
