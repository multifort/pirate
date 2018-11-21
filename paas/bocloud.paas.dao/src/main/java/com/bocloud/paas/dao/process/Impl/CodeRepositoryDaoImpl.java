package com.bocloud.paas.dao.process.Impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.process.CodeRepositoryDao;
import com.bocloud.paas.entity.CodeRepository;

/**
 * describe: 代码仓库DAO层接口实现类
 * @author Zaney
 * @data 2017年10月27日
 */
@Repository("codeRepositoryDao")
public class CodeRepositoryDaoImpl extends JdbcGenericDao<CodeRepository, Long> implements CodeRepositoryDao {

	@Override
	public CodeRepository existed(String name) throws Exception {
		String sql = "select * from code_repository where is_deleted = 0 and name =:name ";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<CodeRepository> list = this.list(CodeRepository.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public CodeRepository detail(Long id) throws Exception {
		String sql = "select user.name as creator_name, mu.name as mender_name, "
				+ "code_repository.* from code_repository "
				+ "LEFT JOIN user on user.id = code_repository.creater_id "
				+ "LEFT JOIN user mu on  mu.id = code_repository.mender_id "
				+ "where code_repository.is_deleted = 0 and code_repository.id =:id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<CodeRepository> list = this.list(CodeRepository.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<CodeRepository> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds)
			throws Exception {
		String sql = "select user.name as creator_name, mu.name as mender_name, "
				+ "code_repository.* from code_repository "
				+ "LEFT JOIN user on user.id = code_repository.creater_id "
				+ "LEFT JOIN user mu on  mu.id = code_repository.mender_id "
				+ "where code_repository.is_deleted = 0 "
				+ "and (code_repository.dept_id is null or code_repository.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "code_repository");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(CodeRepository.class, sql, paramMap);
	}

	@Override
	public List<CodeRepository> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select cr.* from code_repository cr "
				+ "where cr.is_deleted = 0 and cr.status = 0 "
				+ "and (cr.dept_id is null or cr.dept_id in (:deptId)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "cr");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(CodeRepository.class, sql, paramMap);
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from code_repository cr "
				+ "where cr.is_deleted = 0 "
				+ "and (cr.dept_id is null or cr.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "cr");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}
	
}
