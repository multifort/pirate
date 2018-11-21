package com.bocloud.paas.dao.process.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.process.WorkflowDao;
import com.bocloud.paas.entity.Workflow;
/**
 * @Describe: 流程编排Dao层接口实现类
 * @author Zaney
 * @2017年6月15日
 */
@Repository("workflowDao")
public class WorkflowDaoImpl extends JdbcGenericDao<Workflow, Long> implements WorkflowDao {

	@Override
	public Workflow checkName(String name, Integer version) throws Exception {
		String sql = "select * from workflow where is_deleted = 0 and name = :name and version = :version";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("version", version);
		List<Workflow> list = this.list(Workflow.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public Workflow selectWorkflow(String name, Integer version) throws Exception {
		String sql = "select * from workflow where is_deleted = 0 and name = :name and version = :version";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("version", version);
		List<Workflow> list = this.list(Workflow.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<Workflow> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select * from workflow a where a.is_deleted = 0 "
				+ "and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(Workflow.class, sql, paramMap);
	}
	
	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name,a.version from workflow a where a.is_deleted = 0 "
				+ "and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<Workflow> workflows = this.list(Workflow.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (Workflow workflow : workflows) {
			beans.add(new SimpleBean(workflow.getId(), workflow.getName()+" / "+workflow.getVersion()));
		}
		return beans;
	}
	
	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from workflow a where a.is_deleted = 0 "
				+ "and (a.dept_id is null or a.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}
	
	@Override
	public Workflow query(Long id) throws Exception {
		String sql = "select * from workflow where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Workflow> list = this.list(Workflow.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return this.get(Workflow.class, id);
	}

}
