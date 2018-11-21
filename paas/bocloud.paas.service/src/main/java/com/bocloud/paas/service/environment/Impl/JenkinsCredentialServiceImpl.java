package com.bocloud.paas.service.environment.Impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.Result;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.IDFactory;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.dao.environment.JenkinsCredentialDao;
import com.bocloud.paas.entity.JenkinsCredential;
import com.bocloud.paas.service.environment.JenkinsCredentialService;
import com.bocloud.paas.service.process.config.JenkinsConfig;
import com.bocloud.paas.service.process.util.JenkinsClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service("jenkinsCredentialService")
public class JenkinsCredentialServiceImpl implements JenkinsCredentialService {

	private static Logger logger = LoggerFactory.getLogger(JenkinsCredentialServiceImpl.class);
	@Autowired
	private JenkinsCredentialDao jenkinsCredentialDao;
	@Autowired
	private JenkinsConfig jenkinsConfig;

	private final static String SCOPE_GLOBAL = "GLOBAL";
	@SuppressWarnings("unused")
	private final static String SCOPE_SYSTEM = "SYSTEM";

	@Override
	public BsmResult create(JenkinsCredential jenkinsCredential, Long userId) {
		try {
			// 根据凭证id查询jenkins凭证是否存在
			JenkinsCredential exitJenkinsCredential = jenkinsCredentialDao.query(jenkinsCredential.getCreaterId());
			if (null != exitJenkinsCredential) {
				return new BsmResult(false, "此jenkins凭证已存在");
			}
			if (null == jenkinsCredential.getCredentialDescription()) {
				jenkinsCredential.setCredentialDescription("");
			}
			// 创建时间
			jenkinsCredential.setGmtCreate(new Date());
			// 创建者id
			jenkinsCredential.setCreaterId(userId);
			// 所属者id
			jenkinsCredential.setOwnerId(userId);
			// 更改时间
			jenkinsCredential.setGmtModify(new Date());
			// 修改者id
			jenkinsCredential.setMenderId(userId);
			// 保存jenkins凭证变量
			jenkinsCredentialDao.save(jenkinsCredential);

			// 在jenkins中添加凭证
			JenkinsClient jenkinsClient = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(),
					jenkinsConfig.getPassword());
			Result result = jenkinsClient.createCredentials(jenkinsCredential.getCredentialScope(),
					jenkinsCredential.getCredentialId(), jenkinsCredential.getCredentialUsername(),
					jenkinsCredential.getCredentialPassword(), jenkinsCredential.getCredentialDescription());
			if (result.isSuccess()) {
				return new BsmResult(true, jenkinsCredential, "创建jenkins凭证成功");
			} else {
				List<Long> ids = new LinkedList<Long>();
				ids.add(jenkinsCredential.getId());
				remove(ids, userId);
				return new BsmResult(false, "创建jenkins凭证失败");
			}
		} catch (Exception e) {
			logger.error("创建jenkins凭证失败", e);
			List<Long> ids = new LinkedList<Long>();
			ids.add(jenkinsCredential.getId());
			remove(ids, userId);
			return new BsmResult(false, "创建jenkins凭证失败");
		}
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {
		try {
			String failResult = "";
			for (Long id : ids) {
				int delResult = jenkinsCredentialDao.remove(id, userId);
				if (delResult <= 0) {
					JenkinsCredential jenkinsCredential = jenkinsCredentialDao.query(id);
					if (null != jenkinsCredential) {
						failResult += jenkinsCredential.getCredentialId() + ",";
					}
				}
			}
			if (failResult.equals("")) {
				return new BsmResult(true, "删除成功");
			} else {
				failResult = failResult.substring(0, failResult.lastIndexOf(","));
				return new BsmResult(true, failResult + "删除失败，其余删除成功");
			}
		} catch (Exception e) {
			logger.error("删除失败，发生异常", e);
			return new BsmResult(false, "删除时发生异常");
		}
	}

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple) {
		GridBean gridBean = null;
		try {
			if (null == params) {
				params = Lists.newArrayList();
			}
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = jenkinsCredentialDao.count(params);
			if (simple) {
				List<SimpleBean> beans = jenkinsCredentialDao.list(params, sorter);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<JenkinsCredential> jenkinsCredentials = jenkinsCredentialDao.list(page, rows, params, sorter);
				gridBean = GridHelper.getBean(page, rows, total, jenkinsCredentials);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询出错", e);
			return new BsmResult(false, "查询出错");
		}
	}

	@Override
	public BsmResult modify(JenkinsCredential jenkinsCredential, Long userId) {
		try {
			// 查询是否存在该jenkins凭证变量
			JenkinsCredential exitJenkinsCredential = jenkinsCredentialDao.query(jenkinsCredential.getId());
			if (null == exitJenkinsCredential) {
				return new BsmResult(false, "修改失败：不存在该jenkins凭证变量");
			}
			// 更新时间
			jenkinsCredential.setGmtModify(new Date());
			jenkinsCredential.setMenderId(userId);
			// 更新jenkins凭证变量
			Boolean updateResult = jenkinsCredentialDao.update(jenkinsCredential);
			if (updateResult) {
				return new BsmResult(true, "修改成功");
			} else {
				return new BsmResult(false, "修改失败：数据库操作失败");
			}
		} catch (Exception e) {
			logger.error("更改jenkins凭证变量失败", e);
			return new BsmResult(false, "更改jenkins凭证变量失败");
		}
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			// 查询该id对应的jenkins凭证变量
			JenkinsCredential jenkinsCredential = jenkinsCredentialDao.query(id);
			if (null == jenkinsCredential) {// 若为空，则不存在该条记录
				return new BsmResult(false, "查询失败，不存在该条记录");
			}
			return new BsmResult(true, jenkinsCredential, "查询成功");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "查询详情失败");
		}
	}

	@Override
	public BsmResult queryByCredentialId(String credentialId) {
		BsmResult bsmResult = new BsmResult();
		JenkinsCredential jenkinsCredential = null;
		try {
			jenkinsCredential = jenkinsCredentialDao.query(credentialId);
			if (null != jenkinsCredential) {
				bsmResult.setData(jenkinsCredential);
			}
			bsmResult.setSuccess(true);
		} catch (Exception e) {
			logger.error("查询jenkins凭证失败", e);
			bsmResult.setMessage("查询jenkins凭证失败");
		}
		return bsmResult;
	}

	@Override
	public BsmResult queryCredentialId(String username, String password, Long userId) {
		BsmResult bsmResult = new BsmResult();
		try {
			List<JenkinsCredential> jenkinsCredentials = jenkinsCredentialDao.queryByUsernameAndPassword(username,
					password);
			if (ListTool.isEmpty(jenkinsCredentials)) {
				JenkinsCredential jenkinsCredential = new JenkinsCredential();
				jenkinsCredential.setCredentialId(IDFactory.instance().uuid());
				jenkinsCredential.setCredentialUsername(username);
				jenkinsCredential.setCredentialPassword(password);
				jenkinsCredential.setCredentialScope(SCOPE_GLOBAL);
				bsmResult = create(jenkinsCredential, userId);
				if (bsmResult.isSuccess()) {
					bsmResult.setData(((JenkinsCredential) bsmResult.getData()).getCredentialId());
				}
			} else {
				bsmResult.setSuccess(true);
				bsmResult.setData(jenkinsCredentials.get(0).getCredentialId());
			}
		} catch (Exception e) {
			logger.error("query jenkins credential fail: \n", e);
			bsmResult.setMessage("查询jenkins凭证失败！");
		}
		return bsmResult;
	}

}
