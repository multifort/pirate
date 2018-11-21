package com.bocloud.paas.dao.environment.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.environment.JenkinsCredentialDao;
import com.bocloud.paas.entity.JenkinsCredential;

@Service("jenkinsCredentialDao")
public class JenkinsCredentialDaoImpl extends JdbcGenericDao<JenkinsCredential, Long> implements JenkinsCredentialDao {

	@Override
	public JenkinsCredential query(Long id) throws Exception {
		String sql = "select * from jenkins_credential jc where jc.id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<JenkinsCredential> jenkins_credentials = this.list(JenkinsCredential.class, sql, params);
		if (!ListTool.isEmpty(jenkins_credentials)) {
			return jenkins_credentials.get(0);
		}
		return null;
	}

	@Override
	public JenkinsCredential query(String credentialId) throws Exception {
		String sql = "select * from jenkins_credential jc where jc.credential_id = :credentialId and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("credentialId", credentialId);
		List<JenkinsCredential> jenkins_credentials = this.list(JenkinsCredential.class, sql, params);
		return jenkins_credentials.get(0);
	}

	@Override
	public List<JenkinsCredential> queryByUsernameAndPassword(String username, String password) throws Exception {
		String sql = "select * from jenkins_credential jc where jc.credential_username = :username "
				+ "and jc.credential_password= :password and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("username", username);
		params.put("password", password);
		List<JenkinsCredential> jenkins_credentials = this.list(JenkinsCredential.class, sql, params);
		return jenkins_credentials;
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {
		String sql = "update jenkins_credential set is_deleted = true , gmt_modify = :gmtModify where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);
	}

	@Override
	public Integer count(List<Param> params) throws Exception {
		String sql = "select count(1) from jenkins_credential jc where is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select jc.credential_id, jc.credential_username from jenkins_credential jc where jc.is_deleted = 0";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<JenkinsCredential> jenkins_credentials = this.list(JenkinsCredential.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (JenkinsCredential jenkins_credential : jenkins_credentials) {
			beans.add(new SimpleBean(jenkins_credential.getId(), jenkins_credential.getName()));
		}
		return beans;
	}

	@Override
	public List<JenkinsCredential> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select * from jenkins_credential jc where jc.is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(JenkinsCredential.class, sql, paramMap);
	}

}
