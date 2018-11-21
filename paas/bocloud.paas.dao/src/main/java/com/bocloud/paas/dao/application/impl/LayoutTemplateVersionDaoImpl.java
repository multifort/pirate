package com.bocloud.paas.dao.application.impl;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.application.LayoutTemplateVersionDao;
import com.bocloud.paas.entity.LayoutTemplateVersion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component("layoutTemplateVersionDao")
public class LayoutTemplateVersionDaoImpl extends JdbcGenericDao<LayoutTemplateVersion, Long> implements LayoutTemplateVersionDao {

	@Override
	public List<LayoutTemplateVersion> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select a.id,a.version,a.layout_template_id,a.template_file_path,a.props,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.`status`,a.remark from layout_template_version a where a.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(LayoutTemplateVersion.class, sql, paramMap);
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select a.id,a.version from layout_template_version a where a.is_deleted = 0";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<LayoutTemplateVersion> layoutTemplateVersions = this.list(LayoutTemplateVersion.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (LayoutTemplateVersion layoutTemplateVersion : layoutTemplateVersions) {
			beans.add(new SimpleBean(layoutTemplateVersion.getId(), layoutTemplateVersion.getVersion()));
		}
		return beans;
	}

	@Override
	public boolean remove(Long id, Long userId) throws Exception {
		String sql = "update layout_template_version set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}

	@Override
	public int count(List<Param> params) throws Exception {
		String sql = "select count(1) from layout_template_version a where a.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public LayoutTemplateVersion query(Long id) throws Exception {
		String sql = "select a.id,a.version,a.template_file_path,a.props,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.`status` from layout_template_version a where a.is_deleted = 0 and a.id = :id";
		Map<String, Object> param = MapTools.simpleMap("id", id);
		List<LayoutTemplateVersion> list = this.list(LayoutTemplateVersion.class, sql, param);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public LayoutTemplateVersion getTemplateVersion(String version, Long layoutTemplateId) throws Exception {
		String sql = "select a.id,a.version,a.creater_id,a.mender_id,a.gmt_create,a.gmt_modify,a.remark,a.`status` from layout_template_version a where a.is_deleted = 0 and version = :version and layout_template_id = :layoutTemplateId";
		Map<String, Object> params = MapTools.simpleMap("version", version);
		params.put("layoutTemplateId",layoutTemplateId);
		List<LayoutTemplateVersion> list = this.list(LayoutTemplateVersion.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
