package com.bocloud.paas.service.environment.Impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.enums.EnvironmentEnum;
import com.bocloud.paas.common.enums.HostEnum;
import com.bocloud.paas.common.util.AddressConUtil;
import com.bocloud.paas.common.util.RfcDateTimeParser;
import com.bocloud.paas.common.util.SSHUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.environment.HostDao;
import com.bocloud.paas.dao.environment.PersistentVolumeDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.entity.Volume;
import com.bocloud.paas.model.Cluster;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.environment.EnvironmentService;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.event.model.OperateResult;
import com.bocloud.paas.service.user.UserService;
import com.bocloud.paas.service.utils.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.NodeSpec;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service("environmentService")
public class EnvironmentServiceImpl implements EnvironmentService {

	private static Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private HostDao hostDao;
	@Autowired
	private EventPublisher resourceEventPublisher;
	@Autowired
	private Config config;
	@Autowired
	private PersistentVolumeDao persistentVolumeDao;
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private UserService userService;
	// 创建节点时
	ExecutorService exec = Executors.newSingleThreadExecutor();
	// 删除节点时
	ExecutorService delExec = Executors.newSingleThreadExecutor();
	// 添加节点时
	ExecutorService addExec = Executors.newSingleThreadExecutor();
	private static final Integer TIMEOUT = 3000;
	private String NODE_CONDITION_TYPE = "Ready";
	private String NODE_CONDITION_STATUS = "True";
	private String NODE_ADDRESS_TYPE = "InternalIP";
	private Integer CLUSTER_DEFAULT_PORT = 8080;

	@Override
	public BsmResult create(Environment environment, Long userId) {
		try {
			User user = null;
			if (null == (user = getUser(userId))) {
				return new BsmResult(false, "未获取到当前用户信息");
			}
			String name = environment.getName();
			// 查询是此名称的环境变量是否已存在
			List<Environment> environments = environmentDao.queryByName(name);
			if (!ListTool.isEmpty(environments)) {
				return new BsmResult(false, "此名称已存在");
			}
			// 创建时间
			environment.setGmtCreate(new Date());
			// 创建者id
			environment.setCreaterId(userId);
			// 所属者id
			environment.setOwnerId(userId);
			// 更改时间
			environment.setGmtModify(new Date());
			// 修改者id
			environment.setMenderId(userId);
			// 组织机构id
			environment.setDeptId(user.getDepartId());
			// 保存环境变量
			environmentDao.save(environment);
			return new BsmResult(true, "创建成功");
		} catch (Exception e) {
			logger.error("创建环境变量失败", e);
			return new BsmResult(false, "创建环境变量失败");
		}
	}

	/**
	 * 获取用户信息
	 * 
	 * @param id
	 * @return
	 */
	private User getUser(Long id) {
		User user = null;
		try {
			user = userDao.query(id);
			if (null == user) {
				logger.warn("该用户不存在");
			}
		} catch (Exception e) {
			logger.error("获取该用户信息异常", e);
		}
		return user;
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {
		try {
			// 删除失败的环境变量的名称字符串
			String failResult = "";
			String notBeDel = "";// 不能被删除的环境
			for (Long id : ids) {
				Environment env = environmentDao.query(id);
				if (EnvironmentEnum.UNAVAILABLE.getCode().equals(env.getStatus())) {
					int i = environmentDao.remove(id, userId);
					if (i <= 0) {// 删除失败的环境变量
						failResult += env.getName();
					}
					continue;
				}
				// 如果环境的代理IP无法连接失败，不允许删除
				boolean con = InetAddress.getByName(env.getProxy()).isReachable(TIMEOUT);
				if (!con) {
					failResult += env.getName();
					continue;
				}
				// 判断环境IP+port是否可用
				boolean conn = new AddressConUtil().connect(env.getProxy(), env.getPort());
				if (!conn) {
					failResult += env.getName();
					continue;
				}
				KubernetesClient client = new ApplicationClient(env.getProxy(), env.getPort().toString())
						.getKubeClient();
				List<Namespace> namespaces = client.namespaces().list().getItems();
				// 获取现在平台中的应用（为了获取命名空间）
				List<Application> applications = applicationDao.queryByEnvId(id);

				int i = 0;
				for (Namespace namespace : namespaces) {
					// 默认命名空间不予处理
					if (namespace.getMetadata().getName().equals("default")
							|| namespace.getMetadata().getName().equals("kube-public")
							|| namespace.getMetadata().getName().equals("kube-system")) {
						continue;
					}

					// 如果环境中含有通过平台创建的命名空间则不允许删除，如果不含有通过平台创建的命名空间则允许删除
					if (!ListTool.isEmpty(applications)) {
						for (Application application : applications) {
							if (namespace.getMetadata().getName().equals(application.getNamespace())) {
								i++;
							}
						}
					}
				}
				// 如果i==0证明该环境下没有任何已发布的应用，可删除
				if (i == 0) {
					int j = environmentDao.remove(id, userId);
					if (j <= 0) {
						failResult += env.getName();
					} else {
						List<Host> hosts = hostDao.queryByEnvId(id);
						int n = 0;
						for (Host host : hosts) {
							Integer delNum = hostDao.remove(host.getId(), userId);
							if (delNum > 0) {
								n++;
							}
							if (n == hosts.size()) {
								List<Volume> volumes = persistentVolumeDao.queryByEnvId(id);
								if (!ListTool.isEmpty(volumes)) {
									for (Volume volume : volumes) {
										persistentVolumeDao.delete(Volume.class, volume.getId());
									}
								}
							}
						}
					}
				} else {
					notBeDel += env.getName();
				}
				client.close();
			}
			if (failResult.equals("") && notBeDel.equals("")) {
				return new BsmResult(true, "删除成功");
			} else if (failResult.equals("") && !notBeDel.equals("")) {
				return new BsmResult(false, notBeDel + "删除失败，存在应用无法删除");
			} else if (!failResult.equals("") && notBeDel.equals("")) {
				return new BsmResult(false, failResult + "删除失败:请确认环境是否可达");
			} else {
				return new BsmResult(false, failResult + "删除失败：请确认环境是否可达，" + notBeDel + "存在应用无法删除");
			}
		} catch (Exception e) {
			logger.error("删除失败，发生异常", e);
			return new BsmResult(false, "删除时发生异常");
		}
	}

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			Long userId) {
		GridBean gridBean = null;
		try {
			if (null == params) {
				params = Lists.newArrayList();
			}
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			String deptId = userService.listDept(userId);
			int total = environmentDao.count(params, deptId);
			if (simple) {
				List<SimpleBean> beans = environmentDao.list(params, sorter, deptId);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				// 查询数据库中符合条件的environment
				List<Environment> environments = environmentDao.list(page, rows, params, sorter, deptId);
				if (!ListTool.isEmpty(environments)) {
					for (Environment environment : environments) {
						// 获取修改者
						User mender = userDao.query(environment.getMenderId());
						if (null != mender) {
							environment.setMender(mender.getName());
						}
						// 获取创建者
						User creator = userDao.query(environment.getCreaterId());
						if (null != creator) {
							environment.setCreator(creator.getName());
						}
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, environments);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询出错", e);
			return new BsmResult(false, "查询出错");
		}
	}

	@Override
	public BsmResult modify(Environment environment, Long userId) {
		try {
			// 查询是否存在该环境变量
			Environment env = environmentDao.query(environment.getId());
			if (null == env) {
				return new BsmResult(false, "修改失败：不存在该环境变量");
			}
			// 更新时间
			environment.setGmtModify(new Date());
			environment.setMenderId(userId);
			// 更新环境变量
			Boolean updateResult = environmentDao.update(environment);
			if (updateResult) {
				return new BsmResult(true, "修改成功");
			} else {
				return new BsmResult(false, "修改失败：数据库操作失败");
			}
		} catch (Exception e) {
			logger.error("更改环境变量失败", e);
			return new BsmResult(false, "更改环境变量失败");
		}
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			// 查询该id对应的环境变量
			Environment environment = environmentDao.query(id);
			if (null == environment) {// 若为空，则不存在该条记录
				return new BsmResult(false, "查询失败，不存在该条记录");
			}
			// 获取环境创建者
			User creator = userDao.query(environment.getCreaterId());
			if (null != creator) {
				environment.setCreator(creator.getName());
			}
			// 获取修改者
			User mender = userDao.query(environment.getMenderId());
			if (null != mender) {
				environment.setMender(mender.getName());
			}
			return new BsmResult(true, environment, "查询成功");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "查询详情失败");
		}
	}

	@Override
	public BsmResult operate(Environment environment, Long userId) {
		try {
			// 校验该环境是否存在
			Environment oldEnvironment = environmentDao.query(environment.getId());
			if (null == oldEnvironment) {
				return new BsmResult(false, "不存在该环境变量！");
			}
			if (null != environment.getStatus() && !"".equals(environment.getStatus())) {
				// 判断传递过来的environment的状态与数据库的状态是否一致
				if (environment.getStatus().equals(oldEnvironment.getStatus())) {
					return new BsmResult(false, "和数据库状态一致，无法操作");
				}
				// 若数据库中的environment的状态是：1--不可用或者是4--异常，这两种状态下不允许操作
				if (EnvironmentEnum.UNAVAILABLE.getCode().equals(oldEnvironment.getStatus())
						|| EnvironmentEnum.ABNORMAL.getCode().equals(oldEnvironment.getStatus())) {
					return new BsmResult(false, "该状态下不允许操作");
				}
				// 更新数据库中environment中的status；环境存在四中状态：1--不可用；2--激活；3--冻结；4--异常
				oldEnvironment.setStatus(environment.getStatus());
				boolean result = environmentDao.update(oldEnvironment);
				if (!result) {
					return new BsmResult(false, "更新数据库中环境状态失败！");
				}
			}
			return new BsmResult(true, "更新状态成功");
		} catch (Exception e) {
			logger.error("操作环境出错", e);
			return new BsmResult(false, "操作该环境出错");
		}
	}

	@Override
	public BsmResult receiveCluster(Host master, Long userId) {
		// 获取k8s客户端
		KubernetesClient client = new ApplicationClient(master.getIp(), master.getPort()).getKubeClient();
		try {
			// -----0：创建环境之前先判断主机是否可达 ---
			// 判断该主机是否可达
			boolean con = InetAddress.getByName(master.getIp()).isReachable(TIMEOUT);
			if (!con) {
				return new BsmResult(false, "主机不可达，接管节点失败");
			}
			// 判断IP+port是否可用
			boolean conn = new AddressConUtil().connect(master.getIp(), Integer.parseInt(master.getPort()));
			if (!conn) {
				return new BsmResult(false, "环境端口不可用，接管节点失败");
			}

			// 判断主机是否已存在与其他环境中
			List<Host> hosts = hostDao.queryByIp(master.getIp());
			// 判断主机是否已存在于其他环境中
			if (hosts.size() == 1 && null != hosts.get(0).getEnvId()) {
				return new BsmResult(false, "该主机已存在于其他环境中，接管失败");
			}

			// 添加节点之前先要创建环境
			Environment envir = new Environment();
			envir.setName(master.getEnvName());// 环境名称
			envir.setPlatform(Integer.parseInt(master.getPlatform()));// 平台类型
			envir.setRemark(master.getRemark());// 环境描述
			envir.setStatus(EnvironmentEnum.UNAVAILABLE.getCode());
			BsmResult result = create(envir, userId);
			if (result.isFailed()) {
				return result;
			}
			// 如果创建成功了，继续执行
			List<Environment> envs = environmentDao.queryByName(master.getEnvName());
			if (ListTool.isEmpty(envs)) {
				return result;
			}
			master.setEnvId(envs.get(0).getId());

			// -----2:环境创建完成之后，进行接管集群操作----

			// 判断环境是否处于不可用状态，只有当环境处于不可用状态时即状态值为1，才能接管集群
			Environment env = environmentDao.query(master.getEnvId());
			if (null == env) {
				return new BsmResult(false, "不存在该环境");
			}
			if (!EnvironmentEnum.UNAVAILABLE.getCode().equals(env.getStatus())) {
				return new BsmResult(false, "只有当环境处于不可用状态时，才能接管集群");
			}

			List<Node> nodeList = client.nodes().list().getItems();
			if (ListTool.isEmpty(nodeList)) {
				return new BsmResult(false, "不存在工作节点，集群不正常，请配置集群");
			}
			int i = 0; // 正常节点的数量
			for (Node node : nodeList) {
				Host host = new Host();
				// 主机名称
				host.setName(node.getMetadata().getName());
				host.setHostName(node.getMetadata().getName());
				List<NodeAddress> addresses = node.getStatus().getAddresses();
				for (NodeAddress address : addresses) {
					if (NODE_ADDRESS_TYPE.equals(address.getType())) {
						// 主机ip
						host.setIp(address.getAddress());
						List<Host> hosts2 = hostDao.queryByIp(address.getAddress());
						if (!ListTool.isEmpty(hosts2) && StringUtils.isEmpty(hosts2.get(0).getEnvId())) {
							host.setName(hosts2.get(0).getName());
							host.setCreaterId(hosts2.get(0).getCreaterId());
							List<JSONObject> labels = JSON.parseArray(hosts2.get(0).getLabels(), JSONObject.class);
							if (!ListTool.isEmpty(labels)
									&& !StringUtils.isEmpty(labels.get(0).get("keys").toString())) {
								List<Host> updateHosts = new ArrayList<>();
								updateHosts.add(hosts2.get(0));
								addNode(master.getIp(), master.getPort(), updateHosts);
							}
							hostDao.remove(Host.class, hosts2.get(0).getId());
						} else {
							host.setName(node.getMetadata().getName());
						}

					}
				}
				// 判断主机IP是否取到
				if (StringUtils.isEmpty(host.getIp())) {
					List<NodeAddress> addr = node.getStatus().getAddresses();
					if (!ListTool.isEmpty(addr)) {
						host.setIp(addr.get(0).getAddress());
					}
					if (StringUtils.isEmpty(host.getIp())) {
						host.setIp("");
					}
				}
				// 主机状态
				List<NodeCondition> conditions = node.getStatus().getConditions();
				for (NodeCondition condition : conditions) {
					if (NODE_CONDITION_TYPE.equals(condition.getType())) {
						if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
							// 节点状态，1--正常；2--异常
							// 如果是正常的话，判断主机是否可调度
							// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
							if (node.getSpec().getUnschedulable() != null && node.getSpec().getUnschedulable()) {
								host.setStatus(HostEnum.UNSCHEDUABLE.getCode());
							} else {
								host.setStatus(HostEnum.SCHEDUABLE.getCode());
							}
							i++;
						} else {
							host.setStatus(HostEnum.ABNORMAL.getCode());
						}
					}
				}

				// 创建者
				if (null == host.getCreaterId()) {
					host.setCreaterId(userId);
				}
				// 创建时间
				host.setGmtCreate(DateTools.stringToDate(DateTools
						.formatTime(RfcDateTimeParser.parseDateString(node.getMetadata().getCreationTimestamp()))));

				// 主机标签
				Node labelNode = client.nodes().withName(node.getMetadata().getName()).get();
				if (null != labelNode.getMetadata().getLabels()) {
					Map<String, String> labels = labelNode.getMetadata().getLabels();
					JSONArray array = new JSONArray();
					for (String key : labels.keySet()) {
						JSONObject obj = new JSONObject();
						obj.put("keys", labels.get(key));
						obj.put("values", labels.get(key));
						array.add(obj);
					}

					host.setLabels(array.toString());
				}
				// 环境id
				host.setEnvId(master.getEnvId());
				// 所属者
				host.setOwnerId(userId);
				// 修改者
				host.setMenderId(userId);
				// 修改时间
				host.setGmtModify(new Date());
				// 查询数据库是否已存在该IP的主机
				List<Host> oldHosts = hostDao.queryByIp(host.getIp());
				if (!ListTool.isEmpty(oldHosts)) {
					Host oldHost = oldHosts.get(0);
					hostDao.remove(oldHost.getId(), userId);
				}
				host.setSource("receive");
				// 数据插入到数据库
				hostDao.save(host);
			}

			// 更新环境状态
			Environment environment = environmentDao.query(master.getEnvId());
			if (null != environment) {
				environment.setProxy(master.getIp());
				// 默认使用的是8080端口
				environment.setPort(Integer.parseInt(master.getPort()));
				if (i == nodeList.size()) {// 如果i==nodeList.size(),证明节点全部都是正常的，此时环境是正常的
					// 环境状态：1--不可用；2--激活；3--冻结；4--异常
					environment.setStatus(EnvironmentEnum.ACTIVE.getCode());
				} else {
					environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
				}
				environment.setSource("receive");
				environmentDao.update(environment);
			}
			return new BsmResult(true, "集群接管成功");
		} catch (Exception e) {
			logger.error("接管集群失败", e);
			// 发生异常时，删除该环境以及该环境下主机
			try {
				List<Environment> environments = environmentDao.queryByName(master.getEnvName());
				if (null != environments) {
					boolean result = environmentDao.delete(Environment.class, environments.get(0).getId());
					if (result) {
						List<Host> hosts = hostDao.queryByEnvId(environments.get(0).getId());
						if (!ListTool.isEmpty(hosts)) {
							for (Host host2 : hosts) {
								hostDao.delete(Host.class, host2.getId());
							}
						}
					}
				}
			} catch (Exception e1) {
				logger.error("发生异常时，数据库数据回归错误", e);
			}
			return new BsmResult(false, "接管集群失败:请确认地址是否为环境可通信IP");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	@Override
	public BsmResult queryNormalEnv(Long userId) {
		try {
			// 查询状态正常的环境
			String deptId = userService.listDept(userId);
			List<Environment> environments = environmentDao.queryNormalEnv(deptId);
			if (ListTool.isEmpty(environments)) {
				return new BsmResult(false, "该用户下不存在可添加节点的环境");
			}
			return new BsmResult(true, environments, "查询成功");
		} catch (Exception e) {
			logger.error("查询正常状态得环境失败", e);
			return new BsmResult(false, "查询正常状态的环境失败");
		}
	}

	@Override
	public synchronized BsmResult createKubernetesCluser(Cluster cluster, Long userId) {
		try {
			// ------1:首先创建环境------
			// 判断传递参数是否为空
			if (null == cluster) {
				return new BsmResult(false, "参数传递错误，无法创建节点");
			}

			// 获取要添加的主机id
			List<Long> ids = cluster.getIds();
			if (ListTool.isEmpty(ids)) {
				return new BsmResult(false, "主机ID为空，无法创建节点");
			}

			//
			List<Host> hosts = new ArrayList<>();
			Map<String, Host> hostMap = new HashMap<>();
			for (Long id : ids) {
				Host host = hostDao.queryById(id);
				if (null == host) {
					return new BsmResult(false, "所选主机中某一主机不存在，无法创建节点,请刷新页面重新操作");
				}
				// 判断该主机是否可达
				boolean con = InetAddress.getByName(host.getIp()).isReachable(TIMEOUT);
				if (!con) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机不可达，无法添加节点");
				}
				// 判断主机的用户名和密码是否正确
				SSHUtil hostSsh = new SSHUtil(host.getIp(), host.getUsername(), host.getPassword());
				boolean conn = hostSsh.connect();
				if (!conn) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机用户名或密码不正确，无法添加节点");
				}
				// 判断主机是否已处于环境中
				if (host.getEnvId() != null) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机已处于环境中，无法实现添加");
				}

				hosts.add(host);
				hostMap.put(host.getIp(), host);
			}
			// 判断是否实现高可用
			if ("true".equals(cluster.getHighAvai())) {
				if (ids.size() < 3) {
					return new BsmResult(false, "实现高可用主机数必须大于3");
				}
			}

			// 判断ansible主机是否可达
			boolean ansibleCon = InetAddress.getByName(config.getAnsibleHostIp()).isReachable(TIMEOUT);
			if (!ansibleCon) {
				return new BsmResult(false, "ansible主机不可达，无法实现添加");
			}
			SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
					config.getAnsibleHostPassword());

			boolean ansible_con = ssh.connect();
			if (!ansible_con) {
				return new BsmResult(false, "ansible主机无法连接，请确认用户名和密码是否准确");
			}
			ssh.connect();
			/*
			 * // 向ansible主机中写入变量配置文件 if ("true".equals(cluster.getHighAvai()))
			 * { boolean writeAnsibleFileResult = writeAnsibleFile(hosts, 3,
			 * cluster.getVirtualIp(), cluster.getNetworkSegment()); if
			 * (!writeAnsibleFileResult) { return new BsmResult(false,
			 * "创建节点失败：写入ansible变量文件失败"); } for (int i = 0; i < 3; i++) {
			 * hosts.get(i).setEtcd("0"); // 当环境是高可用时，前三台主机作为etcd安装机器
			 * hostDao.update(hosts.get(i)); }
			 * 
			 * } else { boolean writeAnsibleFileReuslt = writeAnsibleFile(hosts,
			 * 1, cluster.getVirtualIp(), cluster.getNetworkSegment()); if
			 * (!writeAnsibleFileReuslt) { return new BsmResult(false,
			 * "创建节点失败：写入ansible变量文件失败"); } // 当环境不是高可用时，第一台机器作为etcd安装机器
			 * hosts.get(0).setEtcd("0"); }
			 */

			Environment envir = new Environment();
			envir.setName(cluster.getEnvName());// 环境名称
			envir.setPlatform(Integer.parseInt(cluster.getPlatform()));// 平台类型
			envir.setRemark(cluster.getRemark());// 环境描述
			envir.setStatus(EnvironmentEnum.CREATEING.getCode());// 环境状态：创建中
			// 判断环境名称是否已存在
			List<Environment> environments = environmentDao.queryByName(cluster.getEnvName());
			if (!ListTool.isEmpty(environments)) {
				return new BsmResult(false, "环境名称已存在，无法创建节点");
			}

			// 创建环境（此时环境的状态为创建中）
			BsmResult createEnvResult = create(envir, userId);
			if (createEnvResult.isFailed()) {
				return new BsmResult();
			}
			// 更新数据库中环境的状态；
			Environment updateEnv = environmentDao.queryByName(cluster.getEnvName()).get(0);
			// 更新envId
			cluster.setEnvId(updateEnv.getId());

			// 更新主机状态为添加中；
			for (Long id : cluster.getIds()) {
				updateHostStatus(id, HostEnum.ADDING.getCode());
			}

			exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						// 向ansible主机中写入变量配置文件
						if ("true".equals(cluster.getHighAvai())) {
							boolean writeAnsibleFileResult = writeAnsibleFile(hosts, 3, cluster.getVirtualIp(),
									cluster.getNetworkSegment());
							if (!writeAnsibleFileResult) {
								// 更新主机状态:ansible主机连接失败，更新主机状态
								for (Long id : cluster.getIds()) {
									updateHostStatus(id, HostEnum.NORMAL.getCode());
								}
								// 删除环境
								environmentDao.remove(cluster.getEnvId(), userId);
								resourceEventPublisher.send(new OperateResult(false, "ansible主机无法连接，无法创建环境",
										"cluster/create", MapTools.simpleMap("userId", userId), userId));
								resourceEventPublisher.send(new OperateResult(false, "写入ansible变量文件失败，创建环境失败",
										"cluster/create", MapTools.simpleMap("userId", userId), userId));
								return;
							}
							for (int i = 0; i < 3; i++) {
								hosts.get(i).setEtcd("0");
								hosts.get(i).setStatus(HostEnum.ADDING.getCode());
								// 当环境是高可用时，前三台主机作为etcd安装机器
								hostDao.update(hosts.get(i));
							}

						} else {
							boolean writeAnsibleFileReuslt = writeAnsibleFile(hosts, 1, cluster.getVirtualIp(),
									cluster.getNetworkSegment());
							if (!writeAnsibleFileReuslt) {
								// 更新主机状态:ansible主机连接失败，更新主机状态
								for (Long id : cluster.getIds()) {
									updateHostStatus(id, HostEnum.NORMAL.getCode());
								}
								// 删除环境
								environmentDao.remove(cluster.getEnvId(), userId);
								resourceEventPublisher.send(new OperateResult(false, "ansible主机无法连接，无法创建环境",
										"cluster/create", MapTools.simpleMap("userId", userId), userId));
								resourceEventPublisher.send(new OperateResult(false, "写入ansible变量文件失败，创建环境失败",
										"cluster/create", MapTools.simpleMap("userId", userId), userId));
								return;
							}
							// 当环境不是高可用时，第一台机器作为etcd安装机器
							hosts.get(0).setEtcd("0");
						}

						SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
								config.getAnsibleHostPassword());
						boolean ansible_con = ssh.connect();
						logger.info("ansible机器是否可连接:" + ansible_con);
						if (!ansible_con) {
							// 更新主机状态:ansible主机连接失败，更新主机状态
							for (Long id : cluster.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							// 删除环境
							environmentDao.remove(cluster.getEnvId(), userId);
							resourceEventPublisher.send(new OperateResult(false, "ansible主机无法连接，无法创建环境",
									"cluster/create", MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 检测ansible执行文件是否存在
						// 执行ansible安装命令
						boolean execResult = ssh.execute("ansible-playbook /root/ansible_install_k8s/main.yaml");
						logger.info("创建环境脚本执行结果：" + execResult);
						if (!execResult) {
							// 更新主机状态:执行脚本失败更新主机状态
							for (Long id : cluster.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							// 删除环境
							environmentDao.remove(cluster.getEnvId(), userId);
							// 删除环境
							environmentDao.remove(cluster.getEnvId(), userId);
							resourceEventPublisher.send(new OperateResult(false, "创建节点异常", "cluster/create",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 睡眠30秒
						// 部署k8s集群组件
						// 是否部署监控
						String message = "";// 关于组件部署结果
						// 前端已对组件之间是否是必需关系已做判断，后台不再做判断
						// 是否部署dns
						if ("true".equals(cluster.getDns())) {
							ssh.connect();
							boolean result = ssh
									.execute("ansible-playbook /root/ansible_install_k8s/deploy_kubernetes_dns.yaml");
							logger.info("部署DNS的脚本执行结果：" + result);
							if (!result) {
								message = "DNS部署失败";
							}
						}
						// 部署监控
						if ("true".equals(cluster.getMonitor())) {
							ssh.connect();
							boolean result = ssh.execute(
									"ansible-playbook /root/ansible_install_k8s/deploy_kubernetes_monitor.yaml");
							logger.info("部署监控的脚本执行结果" + result);
							if (!result) {
								message += " " + "监控部署失败";
							}
						}
						// 是否部署负载
						if ("true".equals(cluster.getLoad())) {
							ssh.connect();
							boolean result = ssh.execute(
									"ansible-playbook /root/ansible_install_k8s/deploy_kubernetes_ingress.yaml");
							logger.info("部署集群负载（ingress）脚本执行结果： " + result);
							if (!result) {
								message += " " + "负载部署失败";
							}
						}
						// 是否部署日志
						if ("true".equals(cluster.getLog())) {
							ssh.connect();
							boolean result = ssh
									.execute("ansible-playbook /root/ansible_install_k8s/deploy_kubernetes_efk.yaml");
							logger.info("集群部署日志组件脚本执行结果：" + result);
							if (!result) {
								message += " " + "日志部署失败";
							}
						}

						// 第一台主机作为环境的主节点，用来存放yaml文件(例如dns的yaml文件、监控的yaml文件)
						envir.setMaster(hosts.get(0).getIp());
						// 获取新创建的环境：为了使用环境ID
						Environment environment = environmentDao.queryByName(cluster.getEnvName()).get(0);
						if (null == environment) {
							resourceEventPublisher.send(new OperateResult(false, "创建节点失败", "cluster/create",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 获取kubernetes客户端
						KubernetesClient client = new ApplicationClient(cluster.getVirtualIp(),
								CLUSTER_DEFAULT_PORT.toString()).getKubeClient();
						List<Node> nodes = client.nodes().list().getItems();
						if (ListTool.isEmpty(nodes)) {
							resourceEventPublisher.send(new OperateResult(false, "创建节点失败", "cluster/create",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}

						for (Node node : nodes) {
							List<NodeAddress> addresses = node.getStatus().getAddresses();
							for (Host host : hosts) {
								for (NodeAddress address : addresses) {
									if (NODE_ADDRESS_TYPE.equals(address.getType())) {
										if (address.getAddress().equals(host.getIp())) {
											host.setHostName(node.getMetadata().getName());
											// 主机状态
											List<NodeCondition> conditions = node.getStatus().getConditions();
											for (NodeCondition condition : conditions) {
												if (NODE_CONDITION_TYPE.equals(condition.getType())) {
													if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
														// 节点状态，1--正常；2--异常
														// 如果是正常的话，判断主机是否可调度
														// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
														if (node.getSpec().getUnschedulable() != null
																&& node.getSpec().getUnschedulable()) {
															host.setStatus(HostEnum.UNSCHEDUABLE.getCode());
														} else {
															host.setStatus(HostEnum.SCHEDUABLE.getCode());
														}
													} else {
														host.setStatus(HostEnum.ABNORMAL.getCode());
													}
												}
											}
											JSONArray array = new JSONArray();
											// 主机标签

											if (null != node.getMetadata().getLabels()) {
												Map<String, String> labels = node.getMetadata().getLabels();
												if (!MapUtils.isEmpty(labels)) {
													for (String key : labels.keySet()) {
														JSONObject obj = new JSONObject();
														obj.put("keys", key);
														obj.put("values", labels.get(key));
														array.add(obj);
													}
												}
											}
											// 如果创建的节点原先有用户编辑的标签信息，更新该标签信息到实际节点上
											if (!StringUtils.isEmpty(host.getLabels())) {
												List<JSONObject> labs = JSON.parseArray(host.getLabels(),
														JSONObject.class);
												for (JSONObject jsonObject : labs) {
													if (!"".equals(jsonObject.get("keys").toString())
															&& null != jsonObject.get("keys").toString()) {
														array.add(jsonObject);
													}
												}
												List<Host> updateNode = new ArrayList<>();
												host.setLabels(array.toString());
												updateNode.add(host);
												// 更新实际集群中节点的标签信息
												addNode(cluster.getVirtualIp(), CLUSTER_DEFAULT_PORT.toString(),
														updateNode);
											}

											host.setLabels(array.toString());
											host.setGmtModify(new Date());
											host.setEnvId(environment.getId());
											hostDao.update(host);
											hostMap.remove(host.getIp());
										}
									}
								}
							}
						}

						// 创建集群的主机
						List<Host> updateHost = hostDao.queryByEnvId(environment.getId());
						int j = 0;
						for (Host host : updateHost) {
							if (host.getStatus().equals(HostEnum.SCHEDUABLE.getCode())
									|| host.getStatus().equals(HostEnum.UNSCHEDUABLE.getCode())) {
								j++;
							}
						}
						if (j == updateHost.size()) {
							environment.setStatus(EnvironmentEnum.ACTIVE.getCode());
						} else {
							environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
						}
						// 环境代理IP即集群的虚拟IP;
						environment.setProxy(cluster.getVirtualIp());
						// 标志环境为通过平台创建的环境，不是通过接管节点创建的环境
						environment.setSource("create");
						// 环境端口即集群端口，默认8080
						environment.setPort(8080);
						environment.setMaster(hosts.get(0).getIp());
						environmentDao.update(environment);
						if (environment.getStatus().equals(EnvironmentEnum.ACTIVE.getCode())) {
							resourceEventPublisher.send(new OperateResult(true, "创建节点成功" + message, "cluster/create",
									MapTools.simpleMap("userId", userId), userId));
						} else if (environment.getStatus().equals(EnvironmentEnum.ABNORMAL.getCode())) {
							resourceEventPublisher.send(new OperateResult(false, "创建节点异常" + message, "cluster/create",
									MapTools.simpleMap("userId", userId), userId));
						}
					} catch (Exception e) {
						try {
							// 更新主机状态
							// 更新主机状态
							for (Long id : cluster.getIds()) {
								Host host = hostDao.queryById(id);
								host.setEnvId(null);
								host.setStatus(HostEnum.NORMAL.getCode());
								hostDao.update(host);
							}
							// 删除环境
							environmentDao.remove(cluster.getEnvId(), userId);
						} catch (Exception e1) {
							logger.error("创建环境失败，");
						}
						logger.error("创建kubernetes集群失败", e);
						resourceEventPublisher.send(new OperateResult(false, "创建节点异常", "cluster/create",
								MapTools.simpleMap("userId", userId), userId));
					}
				}
			});
			return new BsmResult(true, "创建任务已下发，正在执行...");
		} catch (Exception e) {
			logger.error("部署kubernetes集群异常", e);
			try {
				// 更新主机状态:执行脚本失败更新主机状态
				for (Long id : cluster.getIds()) {
					Host host = hostDao.queryById(id);
					host.setEnvId(null);
					host.setStatus(HostEnum.NORMAL.getCode());
					hostDao.update(host);
				}
				if (null != environmentDao.queryByName(cluster.getEnvName())
						&& null != environmentDao.queryByName(cluster.getEnvName()).get(0).getId()) {
					environmentDao.remove(environmentDao.queryByName(cluster.getEnvName()).get(0).getId(), userId);
				}
			} catch (Exception e1) {
				logger.error("创建环境时，数据回归出错", e);
			}
			return new BsmResult(false, "部署kubernetes集群异常，部署失败");
		}
	}

	@Override
	public BsmResult addNode(Host hosts, Long userId) {
		try {
			if (ListTool.isEmpty(hosts.getIds())) {
				return new BsmResult(false, "要添加主机ID为空，无法添加");
			}

			Long environmentId = hosts.getEnvId();
			if (null == environmentId) {
				return new BsmResult(false, "环境ID为空，无法实现添加");
			}
			// 查询环境
			Environment environment = environmentDao.query(environmentId);
			if (null == environment) {
				return new BsmResult(false, "添加失败：要添加节点环境不存在，无法实现添加");
			}
			// 查看环境状态:只有当环境处于激活状态时允许添加节点
			if (!EnvironmentEnum.ACTIVE.getCode().equals(environment.getStatus())) {
				return new BsmResult(false, "只有当环境处于激活状态时才允许添加主机");
			}
			// 判断环境代理主机ip是否可达
			boolean conn = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
			if (!conn) {
				return new BsmResult(false, "环境代理IP不可达，无法添加节点");
			}
			// 确认端口是否可用，即进程是否正常
			boolean connect = new AddressConUtil().connect(environment.getProxy(), environment.getPort());
			if (!connect) {
				return new BsmResult(false, "环境端口不可达，无法添加节点");
			}

			// 查询要添加主机，并进行校验
//			List<Host> hostList = new ArrayList<>();
			for (Long id : hosts.getIds()) {
				Host host = hostDao.queryById(id);
				if (null == host) {
					return new BsmResult(false, "ID为" + id + "的主机不存在，无法实现节点添加");
				}
				// 判断主机是否可连接
				if (StringUtils.isEmpty(host.getUsername()) || StringUtils.isEmpty(host.getPassword())) {
					return new BsmResult(false, "IP" + host.getIp() + "的机器用户名或密码为空，无法实现添加");
				}
				SSHUtil ssh = new SSHUtil(host.getIp(), host.getUsername(), host.getPassword());
				boolean con = ssh.connect();
				if (!con) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机不可达，无法添加到集群");
				}

				// 关闭ssh
				if (null != ssh) {
					ssh.close();
				}
//				hostList.add(host);
			}


			// 更新主机状态，变更为添加中，即此时正在向环境中添加该主机
			for (Long id : hosts.getIds()) {
				updateHostStatus(id, HostEnum.ADDING.getCode());
			}

			addExec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
								config.getAnsibleHostPassword());
						// 执行安装节点脚本
						boolean ansibleHostCon = ssh.connect();
						logger.info("ansible主机是否可连接：" + ansibleHostCon);
						if (!ansibleHostCon) {
							logger.error("ansible主机无法连接，请确认ansible主机用户名和密码准确");
							for (Long id : hosts.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, "添加节点失败：ansible机器连接失败", "node/add",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 向环境中添加
						// 写入ansible
						Long envId = hosts.getEnvId();
						List<Host> envHosts = hostDao.queryByEnvId(envId);
						if (ListTool.isEmpty(envHosts)) {
							for (Long id : hosts.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, "添加节点失败:节点数为0,无法找到ETCD服务器，无法实现节点添加", "node/add",
									MapTools.simpleMap("userId", userId), userId));
						}
						List<Host> etcdHosts = new ArrayList<>();
						for (Host envHost : envHosts) {
							if ("0".equals(envHost.getEtcd())) {
								etcdHosts.add(envHost);
							}
						}
						if(ListTool.isEmpty(etcdHosts)){
							for (Long id : hosts.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, "添加节点失败:未找到ETCD服务器，无法实现添加", "node/add",
									MapTools.simpleMap("userId", userId), userId));
						}
						// 该环境下的主节点
						String master = "";
						if (!StringUtils.isEmpty(environment.getMaster())) {
							master = environment.getMaster();
						}
						//
						List<Host> hostList = new ArrayList<>();
						for (Long id : hosts.getIds()) {
							Host host = hostDao.queryById(id);
							hostList.add(host);
						}
						
						boolean result = writeAddNodeAnsibleFile(hostList, master, environment.getProxy(), etcdHosts);
						if (!result) {
							// 更新主机状态：脚本执行失败，恢复主机状态为"正常"
							for (Long id : hosts.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, "添加节点失败：写入脚本变量失败", "node/add",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}

						boolean addNodeResult = ssh.execute(
								"cd /root/ansible_install_k8s/kubernetes_expand && ./trigger_expand.sh /etc/ansible/addNode");
						logger.info("添加节点脚本执行结果： " + addNodeResult);
						if (!addNodeResult) {
							// 更新主机状态：脚本执行失败，恢复主机状态为"正常"
							for (Long id : hosts.getIds()) {
								updateHostStatus(id, HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, "添加节点失败：脚本执行失败", "node/add",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 获取kubernetes客户端
						KubernetesClient client = new ApplicationClient(environment.getProxy(),
								CLUSTER_DEFAULT_PORT.toString()).getKubeClient();
						List<Node> nodes = client.nodes().list().getItems();
						String failIp = "";// 添加失败的主机IP
						List<Host> failHosts = new ArrayList<>();// 添加失败的主机
						for (Host host : hostList) {
							int i = 0;
							for (Node node : nodes) {
								List<NodeAddress> addresses = node.getStatus().getAddresses();
								for (NodeAddress address : addresses) {
									if ("InternalIP".equals(address.getType())) {
										if (host.getIp().equals(address.getAddress())) {
											// 执行成功之后，更新标签和状态以及环境ID
											// 主机状态
											List<NodeCondition> conditions = node.getStatus().getConditions();
											for (NodeCondition condition : conditions) {
												if (NODE_CONDITION_TYPE.equals(condition.getType())) {
													if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
														// 节点状态，1--正常；2--异常
														// 如果是正常的话，判断主机是否可调度
														// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
														if (node.getSpec().getUnschedulable() != null
																&& node.getSpec().getUnschedulable()) {
															host.setStatus(HostEnum.UNSCHEDUABLE.getCode());
														} else {
															host.setStatus(HostEnum.SCHEDUABLE.getCode());
														}
													} else {
														host.setStatus(HostEnum.ABNORMAL.getCode());
													}
												}
											}
											JSONArray array = new JSONArray();
											// 主机标签
											if (!MapUtils.isEmpty(node.getMetadata().getLabels())) {
												Map<String, String> labels = node.getMetadata().getLabels();
												for (String key : labels.keySet()) {
													JSONObject obj = new JSONObject();
													obj.put("keys", key);
													obj.put("values", labels.get(key));
													array.add(obj);
												}
											}
											// 判断要添加的节点是否原先存在用户编辑的标签信息，如果有，同步更新到集群中的实际节点上
											if (!StringUtils.isEmpty(host.getLabels())) {
												List<JSONObject> labs = JSON.parseArray(host.getLabels(),
														JSONObject.class);
												if (!ListTool.isEmpty(labs)) {
													for (JSONObject jsonObject : labs) {
														if (!"".equals(jsonObject.get("keys").toString())
																&& null != jsonObject.get("keys").toString()) {
															array.add(jsonObject);
														}
													}
												}
												List<Host> updateNode = new ArrayList<>();
												host.setLabels(array.toString());
												updateNode.add(host);
												// 更新节点标签信息
												addNode(environment.getProxy(), environment.getPort().toString(),
														updateNode);
											}

											host.setLabels(array.toString());
											host.setEnvId(envId);
											i++;
											// 更新数据库数据
											// 判断
											hostDao.update(host);
										}
									}
								}
							}
							if (i == 0) {
								failIp += " " + host.getIp();
								failHosts.add(host);
							}
						}
						// 执行完成之后，同步环境
						List<String> notReadyHost = getNotReadyNode(environment.getProxy(),
								environment.getPort().toString());
						if (!ListTool.isEmpty(notReadyHost)) {
							environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
							environmentDao.update(environment);
						}
						// 判断是否有执行失败的主机
						if (StringUtils.isEmpty(failIp)) {
							resourceEventPublisher.send(new OperateResult(true, "添加节点成功", "node/add",
									MapTools.simpleMap("userId", userId), userId));
						} else {
							for (Host failHost : failHosts) {
								updateHostStatus(failHost.getId(), HostEnum.NORMAL.getCode());
							}
							resourceEventPublisher.send(new OperateResult(false, failIp + "节点添加失败", "node/add",
									MapTools.simpleMap("userId", userId), userId));
						}
					} catch (Exception e) {
						try {
							for (Long id : hosts.getIds()) {
								Host host = hostDao.queryById(id);
								host.setEnvId(null);
								host.setStatus(HostEnum.NORMAL.getCode());
								hostDao.update(host);
							}
						} catch (Exception e1) {
							logger.error("添加节点数据回归时出现异常", e);
						}
						logger.error("添加失败：添加节点异常", e);
						resourceEventPublisher.send(new OperateResult(false, "添加节点异常" + e, "node/add",
								MapTools.simpleMap("userId", userId), userId));
					}
				}
			});
			return new BsmResult(true, "添加任务已下发，正在执行...");
		} catch (Exception e) {
			try {
				List<Long> ids = hosts.getIds();
				for (Long id : ids) {
					Host host = hostDao.queryById(id);
					host.setStatus(HostEnum.NORMAL.getCode());
					host.setEnvId(null);
					hostDao.update(host);
				}
			} catch (Exception e1) {
				logger.error("添加主机时，出现异常做数据回归出错");
			}
			logger.error("添加节点异常", e);
			return new BsmResult(false, "添加节点失败：添加节点出现异常");
		}
	}

	@Override
	public synchronized BsmResult deleteNode(Host host, Long userId) {
		try {
			if (null == host) {
				return new BsmResult(false, "删除节点失败： 传递参数为空");
			}
			Host ho = hostDao.queryById(host.getIds().get(0));
			host.setEnvId(ho.getEnvId());
			Long envId = host.getEnvId();
			if (null == envId) {
				return new BsmResult(false, "环境ID为空，无法删除主机");
			}
			Environment environment = environmentDao.query(envId);
			if (null == environment) {
				return new BsmResult(false, "要删除主机的环境不存在，无法删除节点");
			}
			// if
			// (!EnvironmentEnum.ACTIVE.getCode().equals(environment.getStatus()))
			// {
			// return new BsmResult(false, "只有当环境处于激活状态时才能删除节点");
			// }
			// 判断环境代理主机ip是否可达
			boolean conn = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
			if (!conn) {
				return new BsmResult(false, "环境代理IP不可达，无法删除节点");
			}
			// 判断端口是否可达
			boolean connect = new AddressConUtil().connect(environment.getProxy(), environment.getPort());
			if (!connect) {
				return new BsmResult(false, "环境端口不可达，无法删除节点");
			}

			List<Host> enableDelHosts = hostDao.queryByEnvId(envId);
			if (ListTool.isEmpty(enableDelHosts)) {
				return new BsmResult(false, "节点数为0，无法删除");
			}

			List<Long> ids = host.getIds();
			// 判断是否删除全部节点，不允许删除全部节点
			if (enableDelHosts.size() == ids.size()) {
				return new BsmResult(false, "不允许删除全部节点");
			}

			// 如果该集群只剩下一个子节点，不允许删除节点
			if (enableDelHosts.size() == 1) {
				return new BsmResult(false, "环境下只剩下一个节点，不允许删除");
			}

			KubernetesClient client = new ApplicationClient(environment.getProxy(), environment.getPort().toString())
					.getKubeClient();
			List<Node> nodes = client.nodes().list().getItems();

			// 删除的主机ID
			List<Host> delNodeList = new ArrayList<>();
			Map<String, Host> hostMap = new HashMap<>();

			// 获取要删除主机，判断主机是否可达
			for (Long id : ids) {
				Host host2 = hostDao.queryById(id);
				if (null == host2) {
					return new BsmResult(false, "要删除节点不存在，无法删除");
				}
				boolean con = InetAddress.getByName(host2.getIp()).isReachable(TIMEOUT);
				if (!con) {
					return new BsmResult(false, "ip为" + host2.getIp() + "的主机不可达，无法删除节点");
				}
				// 判断主机是否是etcd服务器
				if("0".equals(host2.getEtcd())){
					return new BsmResult(false, "该节点作为ETCD服务器，无法删除");
				}
				
				int i = 0;
				// 判断该主机是否在集群中
				for (Node node : nodes) {
					List<NodeAddress> addresses = node.getStatus().getAddresses();
					for (NodeAddress address : addresses) {
						if (NODE_ADDRESS_TYPE.equals(address.getType())) {
							if (host2.getIp().equals(address.getAddress())) {
								i++;
							}
						}
					}
				}
				// 如果i==0证明该主机不在集群中，不进行删除
				if (0 == i) {
					return new BsmResult(false, "IP为" + host2.getIp() + "的主机不在集群中，无法删除");
				}
				delNodeList.add(host2);
			}

			if (ListTool.isEmpty(nodes)) {
				return new BsmResult(false, "环境下实际节点数为0，无法删除节点");
			}

			// 更新主机状态为移出中
			for (Long id : ids) {
				updateHostStatus(id, HostEnum.OUTING.getCode());
			}

			delExec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
								config.getAnsibleHostPassword());
						boolean con = ssh.connect();
						logger.info("ansible主机是否可连接：" + con);
						if (!con) {
							// ansible主机连接失败，主机恢复为原来状态
							for (Host delNode : delNodeList) {
								updateHostStatus(delNode.getId(), delNode.getStatus());
							}
							resourceEventPublisher.send(new OperateResult(false, "ansible主机连接失败，无法删除主机", "node/delete",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}

						// 写入ansbile变量文件（删除节点时需要）
						boolean writeDelNodeAnsibleFileResult = writeDeleteNodeAnsibleFile(delNodeList,
								environment.getMaster());
						if (!writeDelNodeAnsibleFileResult) {
							for (Host delNode : delNodeList) {
								updateHostStatus(delNode.getId(), delNode.getStatus());
							}
							resourceEventPublisher.send(new OperateResult(false, "删除节点失败：写入删除节点变量时失败", "node/delete",
									MapTools.simpleMap("userId", userId), userId));
							return;
						}

						// ansible执行删除脚本
						boolean delNodeResult = ssh.execute(
								"cd /root/ansible_install_k8s/kubernetes_shrink && ./trigger_shrink.sh /etc/ansible/delNode");
						logger.info("删除节点脚本执行结果：" + delNodeResult);
						if (!delNodeResult) {
							for (Host delNode : delNodeList) {
								updateHostStatus(delNode.getId(), delNode.getStatus());
							}
							resourceEventPublisher.send(new OperateResult(false, "删除节点失败：执行删除节点脚本失败，请确认该节点上是否存在不可删除服务",
									"node/delete", MapTools.simpleMap("userId", userId), userId));
							return;
						}
						// 删除成功之后更新数据库
						List<Node> nodes = client.nodes().list().getItems();
						for (Node node : nodes) {
							List<NodeAddress> addresses = node.getStatus().getAddresses();
							for (NodeAddress address : addresses) {
								if (NODE_ADDRESS_TYPE.equals(address.getType())) {
									for (Host host : delNodeList) {
										if (host.getIp().equals(address.getAddress())) {
											hostMap.put(host.getIp(), host);
										}
									}
								}
							}
						}

						for (Host host : delNodeList) {
							if (null == hostMap.get(host.getIp())) {
								host.setLabels(null);
								host.setStatus(HostEnum.NORMAL.getCode());
								host.setEnvId(null);
								host.setMenderId(userId);
								host.setGmtModify(new Date());
								hostDao.update(host);
							}
						}

						// 更新环境状态
						List<String> notReadyHost = getNotReadyNode(environment.getProxy(),
								environment.getPort().toString());
						if (!ListTool.isEmpty(notReadyHost)) {
							environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
							environmentDao.update(environment);
						}

						// 删除失败的主机
						String failIp = "";
						if (hostMap.isEmpty()) {
							resourceEventPublisher.send(new OperateResult(true, "删除节点成功", "node/delete",
									MapTools.simpleMap("userId", userId), userId));
						} else {
							for (String key : hostMap.keySet()) {
								failIp = failIp + " " + key;
								// 恢复删除失败主机的状态，变更为删除之前的状态
								for (Host delFailNode : delNodeList) {
									if (key.equals(delFailNode.getIp())) {
										updateHostStatus(delFailNode.getId(), delFailNode.getStatus());
									}
								}
							}
							resourceEventPublisher.send(new OperateResult(false, "删除节点异常: " + failIp + "节点删除失败",
									"node/delete", MapTools.simpleMap("userId", userId), userId));
						}
					} catch (Exception e) {
						// 更新主机状态
						for (Host delNode : delNodeList) {
							updateHostStatus(delNode.getId(), delNode.getStatus());
						}
						logger.error("删除节点异常", e);
						resourceEventPublisher.send(new OperateResult(false, "删除节点异常", "node/delete",
								MapTools.simpleMap("userId", userId), userId));
					}
				}
			});
			return new BsmResult(true, "删除任务已下发，正在执行...");
		} catch (Exception e) {
			// 更新主机状态为移出中
			for (Long id : host.getIds()) {
				updateHostStatus(id, HostEnum.NORMAL.getCode());
			}
			logger.error("删除节点异常", e);
			return new BsmResult(false, "删除节点异常");
		}
	}

	/**
	 * 部署k8s集群时，写入ansible变量文件以及ansible的hosts文件
	 * 
	 * @param hosts
	 * @param num
	 * @param virtualIp
	 * @throws Exception
	 */
	private boolean writeAnsibleFile(List<Host> hosts, int num, String virtualIp, String networkSegment) {
		try {
			String masterGroup = "";
			for (int i = 0; i < num; i++) {
				if (i == 0) {
					masterGroup = "[master]\n" + hosts.get(i).getIp() + "\n";
					continue;
				}
				masterGroup += hosts.get(i).getIp() + "\n";
			}

			SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
					config.getAnsibleHostPassword());
			ssh.connect();

			String command = "echo '" + masterGroup + "' > /etc/ansible/hosts";
			boolean result = ssh.execute(command);
			if (!result) {
				return false;
			}
			// 写入etcd主机IP
			ssh.connect();
			String etcdGroup = "";
			for (int i = 0; i < num; i++) {
				if (i == 0) {
					etcdGroup = "[etcd]\n" + hosts.get(i).getIp() + "\n";
					continue;
				}
				etcdGroup += hosts.get(i).getIp() + "\n";
			}

			String etcdCom = "echo '" + etcdGroup + "' >> /etc/ansible/hosts";
			boolean etcdResult = ssh.execute(etcdCom);
			// 判断etcd主机组是否写入成功
			if (!etcdResult) {
				return false;
			}

			// 写入node
			String nodeGroup = "";
			for (int i = 0; i < hosts.size(); i++) {
				if (i == 0) {
					nodeGroup = "[node]\n" + hosts.get(i).getIp() + "\n";
					continue;
				}

				nodeGroup += hosts.get(i).getIp() + "\n";
			}
			ssh.connect();
			String nodeCom = "echo '" + nodeGroup + "' >> /etc/ansible/hosts";
			boolean nodeResult = ssh.execute(nodeCom);
			if (!nodeResult) {
				return false;
			}

			// 写入keepalived-master
			String keepalivedMaster = "[keepalived_master]\n" + hosts.get(0).getIp() + "\n";
			ssh.connect();
			String keepalivedMasterCom = "echo '" + keepalivedMaster + "' >> /etc/ansible/hosts";
			boolean kresult = ssh.execute(keepalivedMasterCom);
			if (!kresult) {
				return false;
			}

			// 写入keepalived-node
			String keepalivedNode = "";
			if (1 == num) {
				keepalivedNode = "[keepalived_node]\n";
			} else {
				for (int i = 1; i < num; i++) {
					if (i == 1) {
						keepalivedNode = "[keepalived_node]\n" + hosts.get(i).getIp() + "\n";
						continue;
					}
					keepalivedNode += hosts.get(i).getIp() + "\n";
				}
			}
			ssh.connect();
			String keepalivedNodeCom = "echo '" + keepalivedNode + "' >> /etc/ansible/hosts";
			boolean keepalivepedNodeResult = ssh.execute(keepalivedNodeCom);
			// 判断keepalived Node主机组是否写入成功
			if (!keepalivepedNodeResult) {
				return false;
			}

			// 写入[all_host]
			String allHost = "";
			for (int i = 0; i < hosts.size(); i++) {
				if (i == 0) {
					allHost = "[all_host]\n" + hosts.get(i).getIp() + " ansible_ssh_host=" + hosts.get(i).getIp()
							+ "  ansible_ssh_user=" + hosts.get(i).getUsername() + " ansible_ssh_pass="
							+ hosts.get(i).getPassword() + "\n";
					continue;
				}
				allHost += hosts.get(i).getIp() + " ansible_ssh_host=" + hosts.get(i).getIp() + "  ansible_ssh_user="
						+ hosts.get(i).getUsername() + " ansible_ssh_pass=" + hosts.get(i).getPassword() + "\n";
			}
			String allHostCom = "echo '" + allHost + "' >> /etc/ansible/hosts";
			ssh.connect();
			boolean allHostResult = ssh.execute(allHostCom);
			// 判断[all_host]主机组是否写入成功
			if (!allHostResult) {
				return false;
			}

			// 写入[node_role]：任意一个主节点的ip
			String nodeRole = "[node_role]\n" + hosts.get(0).getIp() + "\n";
			String nodeRoleCom = "echo '" + nodeRole + "' >> /etc/ansible/hosts";
			ssh.connect();
			boolean nodeRoleResult = ssh.execute(nodeRoleCom);
			// 判断[node_role]主机组是否写入成功
			if (!nodeRoleResult) {
				return false;
			}

			// 写入[ansible_host]
			String ansibleHost = "[ansible_host]\n" + config.getAnsibleHostIp() + " ansible_ssh_host="
					+ config.getAnsibleHostIp() + "  ansible_ssh_user=" + config.getAnsibleHostUsername()
					+ " ansible_ssh_pass=" + config.getAnsibleHostPassword() + "\n";
			String ansibleHostCom = "echo '" + ansibleHost + "' >> /etc/ansible/hosts";
			ssh.connect();
			boolean ansibleHostResult = ssh.execute(ansibleHostCom);
			// 判断[ansible_host]主机组是否写入成功
			if (!ansibleHostResult) {
				return false;
			}
			// 向virtual_ip中写入虚拟ip
			String virtualIP = "virtual_ip: " + virtualIp;
			ssh.connect();
			String virtualIpCom = "echo '" + virtualIP + "' > /root/ansible_install_k8s/virtual_ip.yaml";
			boolean virtualIpResult = ssh.execute(virtualIpCom);
			// 判断virtualIP是否写入成功
			if (!virtualIpResult) {
				return false;
			}

			// 写入variable.yaml
			String variableIps = "";
			for (int i = 0; i < num; i++) {
				if (i == 0) {
					variableIps = "ips:\n" + " - " + hosts.get(i).getIp() + "\n";
					if (1 == num) {
						variableIps += " - " + virtualIp + "\n";
					}
					continue;
				}
				variableIps += " - " + hosts.get(i).getIp() + "\n";
				if (i == (num - 1)) {
					variableIps += " - " + virtualIp + "\n";
				}
			}
			ssh.connect();
			String variableIpsCom = "echo '" + variableIps + "' > /root/ansible_install_k8s/variable.yaml";
			boolean variableIpResult = ssh.execute(variableIpsCom);
			// 判断variableIp变量是否写入成功
			if (!variableIpResult) {
				return false;
			}

			// 写入haproxy_ip.yaml
			String haproxyIp = "";
			for (int i = 0; i < num; i++) {
				if (i == 0) {
					haproxyIp = "haproxyips:\n" + "  - " + hosts.get(i).getIp() + "\n";
					continue;
				}
				haproxyIp += "  - " + hosts.get(i).getIp() + "\n";
			}
			String haproxyIpCom = "echo '" + haproxyIp + "' > /root/ansible_install_k8s/haproxy_ip.yaml";
			ssh.connect();
			boolean haproxyIpResult = ssh.execute(haproxyIpCom);
			// 判断haproxyIP变量是否写入成功
			if (!haproxyIpResult) {
				return false;
			}

			String etcdHost = "";
			for (int i = 0; i < num; i++) {
				SSHUtil ssh2 = new SSHUtil(hosts.get(i).getIp(), hosts.get(i).getUsername(),
						hosts.get(i).getPassword());
				ssh2.connect();
				String name = ssh2.executeWithResult("hostname");
				if (name.contains("\n")) {
					name = name.substring(0, name.length() - 1);
				}
				if (i == 0) {
					etcdHost = "hosts:\n" + "  - {name: " + name + "," + "ip: " + hosts.get(i).getIp() + "}\n";
					continue;
				}

				etcdHost += "  - {name: " + name + "," + "ip: " + hosts.get(i).getIp() + "}\n";
			}
			String etcdHostCom = "echo '" + etcdHost + "' > /root/ansible_install_k8s/etcd_hosts.yaml";
			ssh.connect();
			boolean etcdIpVariableResult = ssh.execute(etcdHostCom);
			// 判断etcdIp变量是否写入成功
			if (!etcdIpVariableResult) {
				return false;
			}

			// 写入flanneld的网段
			String networkSeg = "NETWORK= " + networkSegment;
			String networkSegCommand = "echo '" + networkSeg + "' > /root/ansible_install_k8s/network.conf";
			ssh.connect();
			boolean networkExecResult = ssh.execute(networkSegCommand);
			if (!networkExecResult) {
				return false;
			}

			if (null != ssh) {
				ssh.close();
			}

			return true;
		} catch (Exception e) {
			logger.error("写入ansible脚本所需变量失败", e);
			return false;
		}
	}

	private boolean writeAddNodeAnsibleFile(List<Host> hosts, String master, String virtaulIp, List<Host> etcdHosts) {
		try {
			// 写入主节点[]：从主节点上拿证书为了添加添加节点使用
			SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
					config.getAnsibleHostPassword());
			ssh.connect();
			boolean createAnsibleFile = ssh.execute("touch /etc/ansible/addNode");
			if (!createAnsibleFile) {
				return false;
			}

			ssh.connect();
			String masterGroup = "";
			masterGroup = "[master]\n" + master + "\n";
			String command = "echo '" + masterGroup + "' > /etc/ansible/addNode";
			boolean masterResult = ssh.execute(command);
			if (!masterResult) {
				return false;
			}
			// 写入要添加节点
			if (ListTool.isEmpty(hosts)) {
				return false;
			}
			String newHost = "";
			for (int i = 0; i < hosts.size(); i++) {
				if (i == 0) {
					newHost = "[new_host]\n" + hosts.get(i).getIp() + " ansible_ssh_host=" + hosts.get(i).getIp()
							+ "  ansible_ssh_user=" + hosts.get(i).getUsername() + " ansible_ssh_pass="
							+ hosts.get(i).getPassword() + "\n";
					continue;
				}
				newHost += hosts.get(i).getIp() + " ansible_ssh_host=" + hosts.get(i).getIp() + "  ansible_ssh_user="
						+ hosts.get(i).getUsername() + " ansible_ssh_pass=" + hosts.get(i).getPassword() + "\n";
			}
			String newHostCommand = "echo '" + newHost + "' >> /etc/ansible/addNode";
			ssh.connect();
			ssh.executeWithResult(newHostCommand);

			// 写入ansible的[ansible_host]
			String ansibleHost = "[ansible_host]\n" + config.getAnsibleHostIp() + " ansible_ssh_host="
					+ config.getAnsibleHostIp() + "  ansible_ssh_user=" + config.getAnsibleHostUsername()
					+ " ansible_ssh_pass=" + config.getAnsibleHostPassword() + "\n";
			String ansibleHostCom = "echo '" + ansibleHost + "' >> /etc/ansible/addNode";
			ssh.connect();
			boolean ansibleResult = ssh.execute(ansibleHostCom);
			if (!ansibleResult) {
				return false;
			}

			// 写入虚拟ip（添加节点是用到的虚拟IP）
			ssh.connect();
			String virtualIpCommand = "echo 'virtual_ip: " + virtaulIp
					+ "' > /root/ansible_install_k8s/kubernetes_expand/virtual_ip.yaml";
			boolean virtaulIpResult = ssh.execute(virtualIpCommand);
			if (!virtaulIpResult) {
				return false;
			}

			// 写入etcd主机
			String etcdHost = "";
			for (int i = 0; i < etcdHosts.size(); i++) {
				SSHUtil ssh2 = new SSHUtil(etcdHosts.get(i).getIp(), etcdHosts.get(i).getUsername(),
						etcdHosts.get(i).getPassword());
				ssh2.connect();
				String name = ssh2.executeWithResult("hostname");
				if (name.contains("\n")) {
					name = name.substring(0, name.length() - 1);
				}
				if (i == 0) {
					etcdHost = "hosts:\n" + "  - {name: " + name + "," + "ip: " + etcdHosts.get(i).getIp() + "}\n";
					continue;
				}

				etcdHost += "  - {name: " + name + "," + "ip: " + etcdHosts.get(i).getIp() + "}\n";
			}

			ssh.connect();
			String etcdHostCom = "echo '" + etcdHost
					+ "' > /root/ansible_install_k8s/kubernetes_expand/etcd_hosts.yaml";
			boolean etcdResult = ssh.execute(etcdHostCom);
			if (!etcdResult) {
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error("添加节点时，写入ansible文件出错", e);
			return false;
		}
	}

	private boolean writeDeleteNodeAnsibleFile(List<Host> delNodes, String master) {
		try {
			SSHUtil ssh = new SSHUtil(config.getAnsibleHostIp(), config.getAnsibleHostUsername(),
					config.getAnsibleHostPassword());
			boolean ansibleHostCon = ssh.connect();
			if (!ansibleHostCon) {
				return false;
			}
			boolean createAnsibleHostFile = ssh.execute("touch /etc/ansible/delNode");
			if (!createAnsibleHostFile) {
				return false;
			}

			// 写入master主机组
			ssh.connect();
			String masterGroup = "";
			masterGroup = "[master]\n" + master + "\n";
			String command = "echo '" + masterGroup + "' > /etc/ansible/delNode";
			boolean masterResult = ssh.execute(command);
			if (!masterResult) {
				return false;
			}

			// 写入ansible主机组
			String ansibleHost = "[ansible_host]\n" + config.getAnsibleHostIp() + " ansible_ssh_host="
					+ config.getAnsibleHostIp() + "  ansible_ssh_user=" + config.getAnsibleHostUsername()
					+ " ansible_ssh_pass=" + config.getAnsibleHostPassword() + "\n";
			String ansibleHostCom = "echo '" + ansibleHost + "' >> /etc/ansible/delNode";
			ssh.connect();
			boolean ansibleResult = ssh.execute(ansibleHostCom);
			if (!ansibleResult) {
				return false;
			}

			// 写入[shrink_host]主机组
			String shrinkHost = "";
			for (int i = 0; i < delNodes.size(); i++) {
				if (i == 0) {
					shrinkHost = "[shrink_host]\n" + delNodes.get(i).getIp() + " ansible_ssh_host="
							+ delNodes.get(i).getIp() + "  ansible_ssh_user=" + delNodes.get(i).getUsername()
							+ " ansible_ssh_pass=" + delNodes.get(i).getPassword() + "\n";
					continue;
				}
				shrinkHost += delNodes.get(i).getIp() + " ansible_ssh_host=" + delNodes.get(i).getIp()
						+ "  ansible_ssh_user=" + delNodes.get(i).getUsername() + " ansible_ssh_pass="
						+ delNodes.get(i).getPassword() + "\n";
			}
			String shrinkHostCommand = "echo '" + shrinkHost + "' >> /etc/ansible/delNode";
			ssh.connect();
			boolean writeDelNodeResult = ssh.execute(shrinkHostCommand);
			if (!writeDelNodeResult) {
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error("删除节点时，写入ansible文件异常", e);
			return false;
		}
	}

	@Override
	public BsmResult nameSpace(Long id, Long userId) {
		KubernetesClient client = null;
		try {
			Environment env = environmentDao.query(id);
			if (null == env) {
				return new BsmResult(false, "查询命名空间失败：不存在该环境");
			}
			// 只有当集群处于“激活”状态时，即已接管集群是才会有命名空间
			if (!EnvironmentEnum.ACTIVE.getCode().equals(env.getStatus())) {
				return new BsmResult(false, "只有当环境处于激活状态时才能获取到命名空间");
			}
			// 判断该主机是否可达
			boolean con = InetAddress.getByName(env.getProxy()).isReachable(TIMEOUT);
			if (!con) {
				return new BsmResult(false, "环境主机不可达，无法获取命名空间");
			}
			// 判断端口是否可达
			boolean connect = new AddressConUtil().connect(env.getProxy(), env.getPort());
			if (!connect) {
				return new BsmResult(false, "环境端口不可达，无法获取命名空间");
			}
			// 获取k8s客户端
			client = new ApplicationClient(env.getProxy(), env.getPort().toString()).getKubeClient();
			List<Namespace> namespaces = client.namespaces().list().getItems();
			return new BsmResult(true, namespaces, "获取命名空间成功");
		} catch (Exception e) {
			logger.error("查询命名空间失败", e);
			return new BsmResult(false, "查询命名空间失败");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	@Override
	public BsmResult monitorUrl(Long id, Long userId) {
		KubernetesClient client = null;
		try {
			if (null == id) {
				return new BsmResult(false, "获取监控地址失败：环境ID为空，请确认主机是否已经存在于环境中");
			}
			Environment env = environmentDao.query(id);
			if (null == env) {
				return new BsmResult(false, "环境不存在");
			}
			// 判断是否存在proxy
			if (null == env.getProxy()) {
				return new BsmResult(false, "环境不可用无法获取监控地址");
			}
			// 判断该主机是否可达
			boolean con = InetAddress.getByName(env.getProxy()).isReachable(TIMEOUT);
			if (!con) {
				return new BsmResult(false, "环境主机不可达，无法获取监控url");
			}
			//
			boolean connect = new AddressConUtil().connect(env.getProxy(), env.getPort());
			if (!connect) {
				return new BsmResult(false, "环境端口不可达，无法获取监控url");
			}
			// 获取k8s客户端
			client = new ApplicationClient(env.getProxy(), env.getPort().toString()).getKubeClient();
			JSONObject object = new JSONObject();
			object.put("hostIp", env.getProxy());
			object.put("port", env.getPort());
			object.put("dataSource", "k8s");
			object.put("url", "http://" + env.getProxy() + ":" + env.getPort()
					+ "/api/v1/namespaces/kube-system/services/monitoring-grafana/proxy");
			return new BsmResult(true, object, "获取监控地址成功");
		} catch (Exception e) {
			logger.error("获取监控地址异常", e);
			return new BsmResult(false, "获取监控地址异常");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	/**
	 * 向集群中添加节点
	 * 
	 * @param masterIP
	 * @param nodes
	 */
	private void addNode(String masterIP, String port, List<Host> nodes) {
		KubernetesClient kubernetesClient = new ApplicationClient(masterIP, port).getKubeClient();
		try {
			for (Host host : nodes) {
				// 添加节点
				Node newNode = new Node();
				// 设置元数据
				ObjectMeta metadata = new ObjectMeta();
				Map<String, String> labelMap = new HashMap<String, String>();
				// 设置节点的标签
				List<JSONObject> labels = JSON.parseArray(host.getLabels(), JSONObject.class);
				if (!ListTool.isEmpty(labels)) {
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("keys").toString())
								&& null != jsonObject.get("keys").toString()) {
							labelMap.put(jsonObject.get("keys").toString(), jsonObject.get("values").toString());
						}
					}
				}
				metadata.setLabels(labelMap);
				metadata.setName(host.getIp());
				newNode.setMetadata(metadata);

				// 设置List<NodeAddress>
				List<NodeAddress> addresseList = new ArrayList<NodeAddress>();
				NodeAddress address = new NodeAddress();
				address.setAddress(host.getIp());
				addresseList.add(address);
				NodeStatus nodeStatus = new NodeStatus();
				nodeStatus.setAddresses(addresseList);
				newNode.setStatus(nodeStatus);

				// 设置nodeSpec
				NodeSpec nodeSpec = new NodeSpec();
				nodeSpec.setExternalID(host.getIp());
				newNode.setSpec(nodeSpec);

				kubernetesClient.nodes().createOrReplace(newNode);
			}
		} catch (Exception e) {
			logger.error("添加节点失败", e);
		} finally {
			if (null != kubernetesClient) {
				// 关闭client
				kubernetesClient.close();
			}
		}
	}

	/**
	 * 判断节点是否正常：返回不正常节点的ip
	 * 
	 * @param masterUrl
	 * @return
	 */
	public List<String> getNotReadyNode(String masterUrl, String port) {
		KubernetesClient kubernetesClient = new ApplicationClient(masterUrl, port).getKubeClient();
		try {
			List<Node> nodes = kubernetesClient.nodes().list().getItems();
			if (ListTool.isEmpty(nodes)) {
				return null;
			}
			List<String> ipList = Lists.newArrayList();
			for (Node node : nodes) {
				List<NodeCondition> conditions = node.getStatus().getConditions();
				for (NodeCondition condition : conditions) {
					if (NODE_CONDITION_TYPE.equals(condition.getType())) {
						if (!NODE_CONDITION_STATUS.equals(condition.getStatus())) {
							List<NodeAddress> nodeAddresses = node.getStatus().getAddresses();
							for (NodeAddress nodeAddress : nodeAddresses) {
								if (null != nodeAddress.getAddress()) {
									ipList.add(nodeAddress.getAddress());
									break;
								}
							}
						}
					}
				}
			}
			return ipList;
		} catch (Exception e) {
			logger.error("获取不正常节点失败", e);
			return null;
		} finally {
			if (null != kubernetesClient) {
				kubernetesClient.close();
			}
		}
	}

	@Override
	public void envMonitor() {
		KubernetesClient client = null;
		try {
			List<Environment> environments = environmentDao.queryAll("");
			if (ListTool.isEmpty(environments)) {
				return;
			}
			if (ListTool.isEmpty(environments)) {
				return;
			}
			// 遍历所有环境
			for (Environment environment : environments) {
				// 判断环境代理是否为空
				if (StringUtils.isEmpty(environment.getProxy())) {
					continue;
				}

				if (StringUtils.isEmpty(environment.getPort())) {
					continue;
				}

				// 判断IP+port是否可用
				boolean conn = new AddressConUtil().connect(environment.getProxy(), environment.getPort());
				if (!conn) {
					environment.setStatus(EnvironmentEnum.DEAD.getCode());
					environmentDao.update(environment);
					List<Host> hosts = hostDao.queryByEnvId(environment.getId());
					if (!ListTool.isEmpty(hosts)) {
						for (Host host : hosts) {
							host.setStatus(HostEnum.ABNORMAL.getCode());
							hostDao.update(host);
						}
					}
					continue;
				}
				// 获取环境下所有
				client = new ApplicationClient(environment.getProxy(), environment.getPort().toString())
						.getKubeClient();
				// 获取集群中所有节点
				List<Node> nodes = client.nodes().list().getItems();
				List<Host> hosts = hostDao.queryByEnvId(environment.getId());

				if (ListTool.isEmpty(nodes) && ListTool.isEmpty(hosts)) {
					updateEnvStatus(environment, EnvironmentEnum.DEAD.getCode());
					continue;
				}

				if (ListTool.isEmpty(nodes) && !ListTool.isEmpty(hosts)) {
					for (Host host : hosts) {
						hostDao.delete(Host.class, host.getId());
					}
					updateEnvStatus(environment, EnvironmentEnum.DEAD.getCode());
					continue;
				}

				Map<String, Host> hostMap = new HashMap<>();

				for (Node node : nodes) {
					int i = 0;
					for (Host host : hosts) {
						List<NodeAddress> addresses = node.getStatus().getAddresses();
						for (NodeAddress address : addresses) {
							if (NODE_ADDRESS_TYPE.equals(address.getType())) {
								if (address.getAddress().equals(host.getIp())) {
									// 如果主机状态为移出中，该主机不做更新
									if (HostEnum.OUTING.getCode().equals(host.getStatus())) {
										continue;
									}
									Host newHost = new Host();
									BeanUtils.copyProperties(host, newHost);
									newHost.setHostName(node.getMetadata().getName());
									// 主机状态
									List<NodeCondition> conditions = node.getStatus().getConditions();
									for (NodeCondition condition : conditions) {
										if (NODE_CONDITION_TYPE.equals(condition.getType())) {
											if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
												// 节点状态，1--正常；2--异常
												// 如果是正常的话，判断主机是否可调度
												// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
												if (node.getSpec().getUnschedulable() != null
														&& node.getSpec().getUnschedulable()) {
													newHost.setStatus(HostEnum.UNSCHEDUABLE.getCode());
												} else {
													newHost.setStatus(HostEnum.SCHEDUABLE.getCode());
												}
											} else {
												newHost.setStatus(HostEnum.ABNORMAL.getCode());
											}
										}
									}

									// 主机标签
									// 判断标签是否更改
									Map<String, String> labelMap = node.getMetadata().getLabels();
									JSONArray array = new JSONArray();
									if (!MapUtils.isEmpty(labelMap)) {
										for (String key : labelMap.keySet()) {
											JSONObject obj = new JSONObject();
											obj.put("keys", key);
											obj.put("values", labelMap.get(key));
											array.add(obj);
										}
									}
									// 判断标签是否更改
									if (null != newHost.getLabels() && null == labelMap) {
										newHost.setLabels(null);
									} else if (null == newHost.getLabels() && null != labelMap) {
										newHost.setLabels(array.toString());
									} else if (null != newHost.getLabels() && null != labelMap) {
										if (!newHost.getLabels().equals(labelMap)) {
											if (!array.isEmpty()) {
												newHost.setLabels(array.toString());
											}
										}
									}

									// 如果不相等证明节点更改了，更新数据库
									if (!newHost.equals(host)) {
										hostDao.update(newHost);
									}
									i++;
								}
							}
						}
						hostMap.put(host.getIp(), host);
					}

					if (i == 0) {// 如果i等于0,证明数据库中不存在位于这个环境下的节点，把节点信息更新的数据库
						Host host = new Host();
						// 节点名称
						host.setName(node.getMetadata().getName());
						// hostname
						host.setHostName(node.getMetadata().getName());
						// 节点ip
						List<NodeAddress> addresses = node.getStatus().getAddresses();
						for (NodeAddress address : addresses) {
							if (NODE_ADDRESS_TYPE.equals(address.getType())) {
								if (!StringUtils.isEmpty(address.getAddress())) {
									host.setIp(address.getAddress());
									// 判断数据库是否存在该IP的主机且不存在与环境中
									List<Host> hosts2 = hostDao.queryByIp(address.getAddress());
									if (!ListTool.isEmpty(hosts2)) {
										if (StringUtils.isEmpty(hosts2.get(0).getEnvId())) {
											host.setName(hosts2.get(0).getName());
											host.setSource(hosts2.get(0).getSource());
											host.setCreaterId(hosts2.get(0).getCreaterId());
											host.setId(hosts2.get(0).getId());
											host.setUsername(hosts2.get(0).getUsername());
											host.setPassword(hosts2.get(0).getPassword());
										}
									}
								}
							}
						}

						if (null == host.getIp()) {
							if (!ListTool.isEmpty(addresses)) {
								host.setIp(addresses.get(0).getAddress());
							}
							if (StringUtils.isEmpty(host.getIp())) {
								host.setIp("");
							}
						}

						// 节点状态
						List<NodeCondition> conditions = node.getStatus().getConditions();
						for (NodeCondition condition : conditions) {
							if (NODE_CONDITION_TYPE.equals(condition.getType())) {
								if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
									// 节点状态，1--正常；2--异常
									// 如果是正常的话，判断主机是否可调度
									// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
									if (node.getSpec().getUnschedulable() != null
											&& node.getSpec().getUnschedulable()) {
										host.setStatus(HostEnum.UNSCHEDUABLE.getCode());
									} else {
										host.setStatus(HostEnum.SCHEDUABLE.getCode());
									}
								} else {
									host.setStatus(HostEnum.ABNORMAL.getCode());
								}
							}
						}

						// 创建时间
						host.setGmtCreate(DateTools.stringToDate(DateTools.formatTime(
								RfcDateTimeParser.parseDateString(node.getMetadata().getCreationTimestamp()))));
						// 主机所处环境
						host.setEnvId(environment.getId());

						// 标签
						if (null != node.getMetadata().getLabels()) {
							Map<String, String> labels = node.getMetadata().getLabels();
							JSONArray array = new JSONArray();
							for (String key : labels.keySet()) {
								JSONObject obj = new JSONObject();
								obj.put("keys", key);
								obj.put("values", labels.get(key));
								array.add(obj);
							}
							host.setLabels(array.toString());
						}
						if (StringUtils.isEmpty(host.getSource())) {
							host.setSource("receive");
						}
						if (null == host.getId()) {
							hostDao.save(host);
						} else {
							hostDao.update(host);
						}

					}
				}

				for (Node node : nodes) {
					List<NodeAddress> addresses = node.getStatus().getAddresses();
					for (NodeAddress address : addresses) {
						if (NODE_ADDRESS_TYPE.equals(address.getType())) {
							hostMap.remove(address.getAddress());
						}
					}
				}

				// 删除不存在于集群但存在于数据库的节点:若是人为创建的更新，同步过来的删除
				for (String hostIp : hostMap.keySet()) {
					List<Host> hosts2 = hostDao.queryByIp(hostIp);
					for (Host host2 : hosts2) {
						// 如果该节点是创建的只更新该机器属性，而不从数据库中删除
						if ("create".equals(host2.getSource())) {
							host2.setEnvId(null);
							host2.setLabels(null);
							host2.setStatus(HostEnum.NORMAL.getCode());
							hostDao.update(host2);
							continue;
						}
						hostDao.delete(Host.class, hostMap.get(hostIp).getId());
					}
				}

				// 更新完成之后，判断状态，更新环境状态
				List<String> notReadyList = getNotReadyNode(environment.getProxy(), environment.getPort().toString());
				if (notReadyList.size() > 0) {
					// 如果原先环境状态为激活状态，更新为异常状态
					environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
					environmentDao.update(environment);
				}
				if (notReadyList.size() == 0) {
					// 如果原先为异常状态更新为激活状态
					if (!EnvironmentEnum.ACTIVE.getCode().equals(environment.getStatus())) {
						environment.setStatus(EnvironmentEnum.ACTIVE.getCode());
						environmentDao.update(environment);
					}
				}

				// 同步完成之后,检查数据库，如果数据库中存在两个相同IP的主机在同一个环境下，删除同步过来的主机：造成这种情况的原因是添加节点的时候，由于时间差导致
				List<Host> hosts2 = hostDao.queryByEnvId(environment.getId());
				if (!ListTool.isEmpty(hosts2)) {
					for (Host host : hosts2) {
						List<Host> ipHosts = hostDao.queryByIp(host.getIp());
						if (!ListTool.isEmpty(ipHosts) && ipHosts.size() > 1) {
							for (Host ipHost : ipHosts) {
								if (StringUtils.isEmpty(ipHost.getCreaterId())) {
									hostDao.remove(Host.class, ipHost.getId());
								}
							}
						}
					}
				}
				// 判断该环境下所有的节点是否挂掉，如果全部挂掉则该环境不可用
				List<Host> hosts3 = hostDao.queryByEnvId(environment.getId());
				if (!ListTool.isEmpty(hosts3)) {
					int i = 0;
					for (Host host : hosts3) {
						if (HostEnum.ABNORMAL.getCode().equals(host.getStatus())) {
							i++;
						}
					}
					// 判断环境下不正常（集群中节点状态为NotReady）节点的个数
					if (i == hosts3.size()) {
						updateEnvStatus(environment, EnvironmentEnum.DEAD.getCode());
					}
				} else {
					updateEnvStatus(environment, EnvironmentEnum.DEAD.getCode());
				}
			}
		} catch (Exception e) {
			logger.error("监控环境异常时出现异常", e);
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	/**
	 * 更新主机状态
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	private boolean updateHostStatus(Long id, String status) {
		try {
			Host host = hostDao.queryById(id);
			host.setStatus(status);
			return hostDao.update(host);
		} catch (Exception e) {
			logger.error("更新主机状态出错", e);
			return false;
		}
	}

	private boolean updateEnvStatus(Environment env, String status) {
		try {
			env.setStatus(status);
			return environmentDao.update(env);
		} catch (Exception e) {
			logger.error("更新环境状态出错", e);
			return false;
		}
	}

	@Override
	public BsmResult envTopology(Long envId) {
		BsmResult bsmResult = new BsmResult();
		JSONArray envArray = new JSONArray(); // 存放多个环境数组
		JSONObject envObject = new JSONObject(); // 存放一个环境对象信息
		JSONArray nodeArray = new JSONArray(); // 存放多个节点数组
		Environment environment = null;
		try {
			/**
			 * 1、环境层处理
			 */
			environment = environmentDao.query(envId);
			if (null == environment || 
					!Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				bsmResult.setMessage("未获取到该环境信息， 或环境状态["+environment.getStatus()+"]不可用");
				return bsmResult;
			}

			// 采集环境信息
			envObject.put("name", environment.getName());
			envObject.put("targetCategory", "service");

			/**
			 * 2、节点层处理
			 */
			ApplicationClient client = new ApplicationClient(environment.getProxy(),
					String.valueOf(environment.getPort()));
			NodeList nodeList = (NodeList) client.list(ApplicationEnum.RESOURCE.NODE);
			if (null == nodeList) {
				logger.warn("未获取到该环境下的所有节点信息");
				envObject.put("tagetJson", nodeArray);
				envArray.add(envObject);

				bsmResult.setSuccess(true);
				bsmResult.setData(envArray);
				return bsmResult;
			}

			List<Node> nodes = nodeList.getItems();
			if (ListTool.isEmpty(nodes)) {
				logger.warn("该环境没有节点");
				envObject.put("tagetJson", nodeArray);
				envArray.add(envObject);

				bsmResult.setSuccess(true);
				bsmResult.setData(envArray);
				return bsmResult;
			}
			
			PodList podList = (PodList) client.list(ApplicationEnum.RESOURCE.POD);
			
			for (Node node : nodes) {
				JSONArray podArray = new JSONArray();
				JSONObject nodeObject = new JSONObject();
				nodeObject.put("targetName", node.getMetadata().getName());
				nodeObject.put("targetCategory", "host");

				if (null == podList) {
					logger.warn("未获取到该主机中的运行实例信息");
					nodeObject.put("target", podArray);
					nodeArray.add(nodeObject);
					continue;
				}

				List<Pod> pods = podList.getItems();
				if (ListTool.isEmpty(pods)) {
					logger.warn("该节点下没有任何运行实例");
					nodeObject.put("target", podArray);
					nodeArray.add(nodeObject);
					continue;
				}

				/**
				 * 3、pod层处理
				 */
				for (Pod pod : pods) {
					String podNodeIp = pod.getStatus().getHostIP();
					if (StringUtils.isEmpty(podNodeIp)) {
						continue;
					}
					if (podNodeIp.equals(node.getMetadata().getName())) {
						JSONObject podObject = new JSONObject();
						podObject.put("targetName", pod.getMetadata().getName());
						podObject.put("targetCategory", "apply");
						podArray.add(podObject);
					}
					
				}
				nodeObject.put("target", podArray);
				nodeArray.add(nodeObject);
			}

			envObject.put("tagetJson", nodeArray);
			envArray.add(envObject);
			bsmResult.setSuccess(true);
			bsmResult.setData(envArray);
			return bsmResult;

		} catch (Exception e) {
			logger.error("获取该环境信息异常", e);
			bsmResult.setMessage("获取该环境信息异常");
			return bsmResult;
		}
	}
}
