package com.bocloud.paas.service.resource.Impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.http.HttpClient;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.Result;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.environment.LoadBalanceDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.LoadBalance;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.common.enums.LoadBalanceEnum;
import com.bocloud.paas.service.resource.LoadBalanceService;
import com.bocloud.paas.service.resource.config.F5Config;
import com.bocloud.paas.service.system.F5OperateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service("loadBalanceService")
public class LoadBalanceServiceImpl implements LoadBalanceService {

	private static Logger logger = LoggerFactory.getLogger(LoadBalanceServiceImpl.class);
	private static final String protocol = "http://";
	@Autowired
	private LoadBalanceDao loadBalanceDao;
	@Autowired
	private EnvironmentDao envDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private F5Config f5Config;

	@Override
	public BsmResult create(LoadBalance loadBalance, Long userId) {
		try {
			HttpClient httpClient = new HttpClient();
			if (loadBalance.getType() == LoadBalanceEnum.Type.NGINX.ordinal()) {
				Result result = httpClient.get(protocol + loadBalance.getManagerIp() + ":" + loadBalance.getPort());
				if (result.isFailed()) {
					return new BsmResult(false, "请检查nginx是否安装");
				}
			} else if (loadBalance.getType() == LoadBalanceEnum.Type.F5.ordinal()) {
				F5OperateUtil f5OperateUtil = new F5OperateUtil();
				boolean flag = f5OperateUtil.login(f5Config.getF5User(), f5Config.getPassword(), f5Config.getF5Ip(),
						f5Config.getF5Port());
				if (flag == false) {
					return new BsmResult(false, "请检查F5是否正常");
				}
			}
			String name = loadBalance.getName();
			// 查询是此名称的负载变量是否已存在
			List<LoadBalance> loadBalances = loadBalanceDao.queryByName(name);
			if (!ListTool.isEmpty(loadBalances)) {
				return new BsmResult(false, "此名称已存在");
			}
			// 创建时间
			loadBalance.setGmtCreate(new Date());
			// 创建者id
			loadBalance.setCreaterId(userId);
			// 所属者id
			loadBalance.setOwnerId(userId);
			// 更改时间
			loadBalance.setGmtModify(new Date());
			// 修改者id
			loadBalance.setMenderId(userId);
			// 负载变量状态：首次创建，默认是不可用状态
			loadBalance.setStatus("0");
			// 保存负载变量
			loadBalanceDao.save(loadBalance);
			return new BsmResult(true, "创建成功");
		} catch (Exception e) {
			logger.error("创建负载变量失败", e);
			return new BsmResult(false, "创建负载变量失败");
		}
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {
		try {
			if (ListTool.isEmpty(ids)) {
				return new BsmResult(false, "删除失败：传值为空");
			}
			// 删除失败的负载变量的名称字符串
			String failResult = "";
			for (Long id : ids) {
				if (loadBalanceDao.countApps(id) > 0) {
					return new BsmResult(false, "负载【" + id + "】包含应用，不能删除");
				}
				int i = loadBalanceDao.remove(id, userId);
				if (i <= 0) {// 删除失败的负载变量
					List<LoadBalance> loadBalances = loadBalanceDao.query(id);
					if (!ListTool.isEmpty(loadBalances)) {
						failResult += loadBalances.get(0).getName();
					}
				}
			}
			if (failResult.equals("")) {
				return new BsmResult(true, "删除成功");
			} else {// 如果不为空，证明有删除失败的负载变量
				return new BsmResult(true, failResult + "删除失败，其余删除成功");
			}
		} catch (Exception e) {
			logger.error("删除失败", e);
			return new BsmResult(false, "删除失败");
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
			int total = loadBalanceDao.count(params);
			if (simple) {
				List<SimpleBean> beans = loadBalanceDao.list(params, sorter);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				// 查询数据库中符合条件的loadBalance
				List<LoadBalance> loadBalances = loadBalanceDao.list(page, rows, params, sorter);
				if (!ListTool.isEmpty(loadBalances)) {
					for (LoadBalance loadBalance : loadBalances) {
						if (null != loadBalance.getMenderId()) {
							// 获取修改者
							User mender = userDao.query(loadBalance.getMenderId());
							if (null != mender) {
								loadBalance.setMender(mender.getName());
							}
						}
						if (null != loadBalance.getCreaterId()) {
							// 获取创建者
							User creator = userDao.query(loadBalance.getCreaterId());
							if (null != creator) {
								loadBalance.setCreator(creator.getName());
							}
						}
						if (null != loadBalance.getEnvId()) {
							// 获取创建者
							Environment env = envDao.query(loadBalance.getEnvId());
							if (null != env) {
								loadBalance.setEnvName(env.getName());
							}
						}
						int count = loadBalanceDao.countApps(loadBalance.getId());
						loadBalance.setAppCount(count);
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, loadBalances);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询出错", e);
			return new BsmResult(false, "查询出错");
		}
	}

	@Override
	public BsmResult modify(LoadBalance loadBalance, Long userId) {
		try {
			// 查询是否存在该负载变量
			List<LoadBalance> envs = loadBalanceDao.query(loadBalance.getId());
			if (ListTool.isEmpty(envs)) {
				return new BsmResult(false, "修改失败：不存在该负载变量");
			}
			if (loadBalance.getType() == LoadBalanceEnum.Type.NGINX.ordinal()) {
				HttpClient httpClient = new HttpClient();
				Result result = httpClient.get(protocol + loadBalance.getManagerIp() + ":" + loadBalance.getPort());
				if (result.isFailed()) {
					return new BsmResult(false, "请检查nginx是否安装");
				}
			} else if (loadBalance.getType() == LoadBalanceEnum.Type.F5.ordinal()) {
				F5OperateUtil f5OperateUtil = new F5OperateUtil();
				boolean flag = f5OperateUtil.login(f5Config.getF5User(), f5Config.getPassword(), f5Config.getF5Ip(),
						f5Config.getF5Port());
				if (flag == false) {
					return new BsmResult(false, "请检查F5是否正常");
				}
			}
			// 更新时间
			loadBalance.setGmtModify(new Date());
			loadBalance.setMenderId(userId);
			loadBalance.setStatus("0");
			// 更新负载变量
			Boolean updateResult = loadBalanceDao.update(loadBalance);
			if (updateResult) {
				return new BsmResult(true, "修改成功");
			} else {
				return new BsmResult(false, "修改失败：数据库操作失败");
			}
		} catch (Exception e) {
			logger.error("更改负载变量失败", e);
			return new BsmResult(false, "更改负载变量失败");
		}
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			// 查询该id对应的负载变量
			List<LoadBalance> loadBalances = loadBalanceDao.query(id);
			if (ListTool.isEmpty(loadBalances)) {// 若为空，则不存在该条记录
				return new BsmResult(false, "查询失败，不存在该条记录");
			}
			if (!ListTool.isEmpty(loadBalances)) {
				// 获取负载创建者
				if (null != loadBalances.get(0).getCreaterId()) {
					User creator = userDao.query(loadBalances.get(0).getCreaterId());
					if (null != creator) {
						loadBalances.get(0).setCreator(creator.getName());
					}
				}
				// 获取修改者
				if (null != loadBalances.get(0).getMenderId()) {
					User mender = userDao.query(loadBalances.get(0).getMenderId());
					if (null != mender) {
						loadBalances.get(0).setMender(mender.getName());
					}
				}

				if (null != loadBalances.get(0).getEnvId()) {
					// 获取创建者
					Environment env = envDao.query(loadBalances.get(0).getEnvId());
					if (null != env) {
						loadBalances.get(0).setEnvName(env.getName());
					}
				}
			}
			return new BsmResult(true, loadBalances.get(0), "查询成功");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "查询详情失败");
		}
	}

	@Override
	public BsmResult listApps(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser) {
		List<Application> list;
		GridBean gridBean;
		try {
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = 0;
			total = loadBalanceDao.countApps(params);
			list = loadBalanceDao.listApps(page, rows, params, sorter);
			// 为所有应用设置实例数
			gridBean = GridHelper.getBean(page, rows, total, list);
			return new BsmResult(true, gridBean, "应用查询成功");
		} catch (Exception e) {
			logger.error("list application failure:", e);
			return new BsmResult(false, "应用查询失败");
		}
	}

	@Override
	public BsmResult checkName(String envName, Long userId) {
		try {
			if (null != envName && !"".equals(envName)) {
				List<LoadBalance> loadBalances = loadBalanceDao.queryByName(envName);
				if (!ListTool.isEmpty(loadBalances)) {
					return new BsmResult(false, "该名称已存在");
				}
			}
			return new BsmResult(true, "该名称未存在");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "校验名称出错");
		}
	}
}
