package com.bocloud.paas.service.resource.Impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.enums.EnvironmentEnum;
import com.bocloud.paas.common.enums.HostEnum;
import com.bocloud.paas.common.util.SSHUtil;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.environment.GpuMonitorDao;
import com.bocloud.paas.dao.environment.HostDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.GpuMonitor;
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.application.util.ApplicationUtil;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.event.model.OperateResult;
import com.bocloud.paas.service.resource.HostService;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.NodeSpec;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service("/hostService")
public class HostServiceImpl implements HostService {

	@Autowired
	private ApplicationUtil openshiftUtil;
	@Autowired
	private HostDao hostDao;
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private EventPublisher resourceEventPublisher;
	@Autowired
	private UserService userService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private GpuMonitorDao gpuMonitorDao;

	private String NODE_CONDITION_TYPE = "Ready";
	private String NODE_CONDITION_STATUS = "True";

	private static Logger logger = LoggerFactory.getLogger(HostServiceImpl.class);

	@Override
	public BsmResult addHost(Host host, Long userId) {
		try {
			// 判断是否存在该环境
			Environment env = environmentDao.query(host.getEnvId());
			if (null == env) {
				return new BsmResult(false, "要添加的环境不存在");
			}
			// 判断要添加的环境是否可用
			if (!EnvironmentEnum.ACTIVE.getCode().equals(env.getStatus())) {
				return new BsmResult(false, "集群状态不正常，无法添加主机");
			}
			// 获取主节点的IP,默认使用8080端口进行通信
			String masterIP = env.getProxy();
			// 判断主节点是否可达
			boolean conMaster = InetAddress.getByName(masterIP).isReachable(3000);
			if (!conMaster) {
				return new BsmResult(false, "添加失败，主节点不可达");
			}
			// 要添加到环境中的主机的id
			List<Long> ids = host.getIds();
			// 要添加到环境的主机
			List<Host> nodeHosts = Lists.newArrayList();
			for (Long id : ids) {
				Host nodeHost = hostDao.queryById(id);
				if (null == nodeHost) {
					return new BsmResult(false, "添加失败： 主机不存在");
				}
				// 判断要添加的主机是否已存在于环境中:如果envId不为null时证明该主机已存在于环境中（如果存在于当前要添加的环境中时无所谓）
				if (null != nodeHost.getEnvId() && nodeHost.getEnvId() != host.getEnvId()) {
					return new BsmResult(false, nodeHost.getIp() + "已存在于别的集群中，不允许添加");
				}
				// 判断主机是否可达
				boolean con = InetAddress.getByName(nodeHost.getIp()).isReachable(3000);
				if (!con) {
					return new BsmResult(false, "添加失败：" + nodeHost.getName() + "不可达");
				}
				nodeHosts.add(nodeHost);
			}
			// 节点添加完毕
			addNode(masterIP, nodeHosts);
			// 线程睡眠3秒：节点添加完毕之后，节点信息不回立即完善，等待3秒
			new Thread(new Runnable() {
				@Override
				public void run() {
					KubernetesClient client = null;
					try {
						Thread.sleep(10000);
						// 获取k8s客户端
						client = openshiftUtil.getKubernetesClient(masterIP);
						// 节点添加完成更新数据库：节点添加完成后，不管节点是否正常都要更新数据库
						for (Host updateHost : nodeHosts) {
							List<Node> nodes = client.nodes().list().getItems();
							for (Node node : nodes) {
								updateHost.setHostName(node.getMetadata().getName());
								if (updateHost.getIp().equals(node.getStatus().getAddresses().get(0).getAddress())) {
									if (null != node.getMetadata().getLabels()) {
										Map<String, String> labels = node.getMetadata().getLabels();
										JSONArray array = new JSONArray();
										for (String key : labels.keySet()) {
											JSONObject obj = new JSONObject();
											obj.put("keys", labels.get(key));
											obj.put("values", labels.get(key));
											array.add(obj);
										}
										updateHost.setLabels(array.toString());
									}
									List<NodeCondition> conditions = node.getStatus().getConditions();
									for (NodeCondition condition : conditions) {
										if (NODE_CONDITION_TYPE.equals(condition.getType())) {
											if (NODE_CONDITION_STATUS.equals(condition.getStatus())) {
												// 节点状态，1--正常；2--异常
												// 如果是正常的话，判断主机是否可调度
												// 主机是否可调度：3--可调度；4--不可调度，备注：如果是正常的话，判断主机是否可调度
												if (node.getSpec().getUnschedulable() != null
														&& node.getSpec().getUnschedulable()) {
													updateHost.setStatus(HostEnum.UNSCHEDUABLE.getCode());
												} else {
													updateHost.setStatus(HostEnum.SCHEDUABLE.getCode());
												}
											} else {
												updateHost.setStatus(HostEnum.ABNORMAL.getCode());
											}
										} else {
											// 如果刚添加到环境中的节点未取到信息，主机默认设置为可调度
											updateHost.setStatus(HostEnum.SCHEDUABLE.getCode());
										}
									}
									// 主机所在环境id
									updateHost.setEnvId(host.getEnvId());
									break;
								}
							}
							hostDao.update(updateHost);
						}
						client.close();
						// 判断添加的节点是否正常:返回不正常Node
						List<String> notReadyNode = getNotReadyNode(masterIP);
						if (notReadyNode.isEmpty()) {
							resourceEventPublisher.send(new OperateResult(true, "添加节点成功", "host/add",
									MapTools.simpleMap("userId", userId), userId));
						} else {
							// 返回状态不正常的节点
							String notReadyNodeIp = " ";
							for (String nodeIp : notReadyNode) {
								notReadyNodeIp += nodeIp;
							}
							// 如果添加到环境中的节点存在不正常状态，则环境异常
							Environment environment = new Environment();
							environment.setId(host.getEnvId());
							environment.setStatus(EnvironmentEnum.ABNORMAL.getCode()); // 4--表示环境状态异常
							environmentDao.update(environment);
							resourceEventPublisher.send(new OperateResult(false, notReadyNodeIp + "节点不正常", "host/add",
									MapTools.simpleMap("userId", userId), userId));
						}
					} catch (Exception e) {
						logger.error("添加节点异常", e);
						resourceEventPublisher.send(new OperateResult(false, "节点添加失败", "host/add",
								MapTools.simpleMap("userId", userId), userId));
					} finally {
						if (null != client) {
							client.close();
						}
					}
				}
			}).start();
			return new BsmResult(true, "添加任务已下发，正在执行...");
		} catch (Exception e) {
			logger.error("添加节点失败", e);
			return new BsmResult(false, "添加节点失败");
		}
	}

	@Override
	public BsmResult removeHost(List<Long> ids, Long userId) {
		try {
			for (Long id : ids) {
				Host host = hostDao.queryById(id);
				// 判断数据库中是否存在该主机
				if (null == host) {
					return new BsmResult(false, "数据库中不存在ID为" + id + "的主机,请刷新页面重新操作");
				}
				Long envId = host.getEnvId();
				// 判断该主机是否存在于环境中
				if (null == envId) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机不存在于环境中");
				}

				// 查询主机所在的环境
				Environment environment = environmentDao.query(envId);
				if (null == environment) {
					return new BsmResult(false, "IP为" + host.getIp() + "的主机所在环境不存在于数据库中");
				}
				// 判断ip是否可达
				boolean con = InetAddress.getByName(environment.getProxy()).isReachable(3000);
				if (!con) {
					return new BsmResult(false, "删除失败：IP为" + host.getIp() + "主机所在环境不可达");
				}
				// 获取环境中的节点
				KubernetesClient kubernetesClient = openshiftUtil.getKubernetesClient(environment.getProxy());
				List<Node> nodes = kubernetesClient.nodes().list().getItems();
				if (ListTool.isEmpty(nodes)) {// 如果不存在节点，更新数据库
					for (Long hostId : ids) {
						Host hostt = hostDao.queryById(hostId);
						hostt.setEnvId(null);
						hostt.setLabels(null);
						hostDao.update(hostt);
					}
					return new BsmResult(true, "主机删除完成");
				}
				// 删除节点，异步操作，时间可能会过长
				new Thread(new Runnable() {
					@Override
					public void run() {
						// 要从集群中删除的主机
						// Host host = hosts.get(0);
						try {
							// 要删除的节点名称
							String deleteNodeName = "";
							for (Node node : nodes) {
								List<NodeAddress> nodeAddresses = node.getStatus().getAddresses();
								for (NodeAddress nodeAddress : nodeAddresses) {
									if (null != nodeAddress.getAddress()) {
										if (host.getIp().equals(nodeAddress.getAddress())) {
											deleteNodeName = node.getMetadata().getName();
											/*
											 * Map<String, String> labels =
											 * node.getMetadata().getLabels();
											 * for (String key :
											 * labels.keySet()) { JSONObject obj
											 * = new JSONObject();
											 * obj.put("keys", labels.get(key));
											 * obj.put("values",
											 * labels.get(key)); array.add(obj);
											 * }
											 */
											break;
										}
									}
								}
								// 如果deleteNodeName不为"",证明已找到给节点，跳出循环
								if (!"".equals(deleteNodeName)) {
									break;
								}
							}
							if ("".equals(deleteNodeName)) {
								resourceEventPublisher.send(new OperateResult(false,
										"删除主机失败，集群中不存在IP为" + host.getIp() + "的主机", "host/delete", null, userId));
							}
							// 从集群中删除节点
							boolean delResult = kubernetesClient.nodes().withName(deleteNodeName).delete();
							if (delResult) {
								boolean con = InetAddress.getByName(host.getIp()).isReachable(3000);
								if (!con) {
									host.setStatus("2");
								} else {
									host.setStatus("1");
								}
								host.setEnvId(null);
								// host.setLabels(array.toString());
								hostDao.update(host);
								resourceEventPublisher.send(new OperateResult(true, "删除节点成功", "host/delete",
										MapTools.simpleMap("userId", userId), userId));
							} else {
								resourceEventPublisher.send(new OperateResult(false, host.getIp() + "主机删除失败",
										"host/delete", MapTools.simpleMap("userId", userId), userId));
							}
							List<Node> nodes = kubernetesClient.nodes().list().getItems();
							if (ListTool.isEmpty(nodes)) {
								environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
							} else {
								List<String> notReadyNode = getNotReadyNode(environment.getProxy());
								if (ListTool.isEmpty(notReadyNode)) {
									environment.setStatus(EnvironmentEnum.ACTIVE.getCode());
								} else {
									environment.setStatus(EnvironmentEnum.ABNORMAL.getCode());
								}
							}
							environmentDao.update(environment);
						} catch (Exception e) {
							logger.error("删除节点失败", e);
							resourceEventPublisher.send(new OperateResult(false, host.getIp() + "主机删除失败", "host/delete",
									MapTools.simpleMap("userId", userId), userId));
						} finally {
							if (null != kubernetesClient) {
								kubernetesClient.close();
							}
						}
					}
				}).start();

			}
			return new BsmResult(true, "删除任务已下发，正在执行...");
		} catch (Exception e) {
			logger.error("删除集群中主机失败", e);
			return new BsmResult(false, "删除集群中主机失败");
		}
	}
	/**
	 * 获取用户信息
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
	public BsmResult create(Host host, Long userId) {
		try {
			User user = null;
			if (null == (user = getUser(userId))) {
				return new BsmResult(false, "未获取到当前用户信息");
			}
			// ----------对要添加的主机进行校验：1）主机是否可达，2）用户名和密码是否准确；3）IP是否已存在于数据库----------
			if(!StringUtils.isEmpty(host.getIpRange())){
				String hostIp = host.getIp();
				String[] ipSplitArr = hostIp.split("\\.");
				int ipBegin = Integer.parseInt(ipSplitArr[3]);
				int ipLast = Integer.parseInt(host.getIpRange());
				// 判断IP范围是否准确
				if(ipBegin > ipLast){
					return new BsmResult(false, "批量添加时，起始IP必须小于末尾IP");
				}
				// 判断IP用户名和密码是否准确
				String newIp = ipSplitArr[0] + "." + ipSplitArr[1] + "." + ipSplitArr[2] + ".";
				for (int i = 0; i <= (ipLast - ipBegin); i++) {
					String newCompleteIp = newIp + (ipBegin + i);
					// 查询主机名称是否已存在
					List<Host> hostList = hostDao.queryByName(host.getName() + "-" + newCompleteIp);
					if (!ListTool.isEmpty(hostList)) {
						return new BsmResult(false, "主机名称已存在，无法添加");
					}
					// 校验主机是否已添加：不允许重复添加
					List<Host> hosts = hostDao.queryByIp(newCompleteIp);
					if (!ListTool.isEmpty(hosts)) {
						return new BsmResult(false, "ip为" +  newCompleteIp + "已存在数据库，不允许重复添加");
					}
					// 根据用户名和密码判断主机是否可达
					SSHUtil ssh = new SSHUtil(newCompleteIp, host.getUsername(), host.getPassword());
					boolean con = ssh.connect();
					if (!con) {
						return new BsmResult(false, "IP为" + newCompleteIp + "的主机不可达，请确认用户名和密码是否准确");
					}
				}
			}else{
				// 校验主机名称是否已存在
				// 查询主机名称是否已存在
				List<Host> hostList = hostDao.queryByName(host.getName());
				if (!ListTool.isEmpty(hostList)) {
					return new BsmResult(false, "主机名称已存在，无法添加");
				}
				// 校验主机是否已添加：不允许重复添加
				List<Host> hosts = hostDao.queryByIp(host.getIp());
				if (!ListTool.isEmpty(hosts)) {
					return new BsmResult(false, "该主机ip已存在数据库，不允许重复添加");
				}
				// 根据用户名和密码判断主机是否可达
				SSHUtil ssh = new SSHUtil(host.getIp(), host.getUsername(), host.getPassword());
				boolean con = ssh.connect();
				if (!con) {
					return new BsmResult(false, "主机不可达，请确认用户名和密码是否准确");
				}
			}
			
			// -----------向数据库中写入数据--------------
			if(!StringUtils.isEmpty(host.getIpRange())){
				String hostIp = host.getIp();
				String[] ipSplitArr = hostIp.split("\\.");
				int ipBegin = Integer.parseInt(ipSplitArr[3]);
				int ipLast = Integer.parseInt(host.getIpRange());
				
				String newIp = ipSplitArr[0] + "." + ipSplitArr[1] + "." + ipSplitArr[2] + ".";
				for (int i = 0; i <= (ipLast - ipBegin); i++) {
					Host newHost =  new Host();
					String newCompleteIp = newIp + (ipBegin + i);
					newHost.setIp(newCompleteIp);
					newHost.setUsername(host.getUsername());
					newHost.setPassword(host.getPassword());
					newHost.setName(newCompleteIp);
					newHost.setDeptId(user.getDepartId());
					// 主机状态为“正常”状态
					newHost.setStatus(HostEnum.NORMAL.getCode());
					// 创建者
					newHost.setCreaterId(userId);
					// 创建时间
					newHost.setGmtCreate(new Date());
					newHost.setGmtModify(new Date());
					newHost.setMenderId(userId);
					newHost.setSource("create");
					newHost.setLabels(host.getLabels());
					newHost.setProps(host.getProps());
					hostDao.save(newHost);
				}
			}else{
				Host newHost =  new Host();
				newHost.setIp(host.getIp());
				newHost.setUsername(host.getUsername());
				newHost.setPassword(host.getPassword());
				newHost.setName(host.getIp());
				newHost.setDeptId(user.getDepartId());
				// 主机状态为“正常”状态
				newHost.setStatus(HostEnum.NORMAL.getCode());
				// 创建者
				newHost.setCreaterId(userId);
				// 创建时间
				newHost.setGmtCreate(new Date());
				newHost.setGmtModify(new Date());
				newHost.setMenderId(userId);
				newHost.setLabels(host.getLabels());
				newHost.setSource("create");
				newHost.setProps(host.getProps());
				hostDao.save(newHost);
			}
			return new BsmResult(true, "添加主机成功");
		} catch (Exception e) {
			logger.error("添加主机到数据库失败", e);
			return new BsmResult(false, "添加主机到数据库失败");
		}
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {
		try {
			// 存在于环境中，无法删除的主机
			String notBeDeleted = "";
			// 删除失败的主机
			String failResult = "";

			for (Long id : ids) {
				Host host = hostDao.queryById(id);
				if (null != host) {
					Long envId = host.getEnvId();
					if (null != envId) {// 主机存在于某一环境中，无法删除
						notBeDeleted += host.getName();
						continue;
					}
					int i = hostDao.remove(id, userId);
					if (i <= 0) {
						failResult += host.getName();
					}
				}
			}

			if ("".equals(notBeDeleted) && !"".equals(failResult)) {
				return new BsmResult(false, failResult + "删除失败");
			} else if (!"".equals(notBeDeleted) && "".equals(failResult)) {
				return new BsmResult(false, notBeDeleted + "存在于环境中无法被删除");
			} else if (!"".equals(notBeDeleted) && !"".equals(failResult)) {
				return new BsmResult(false, notBeDeleted + "存在于环境中无法被删除，" + failResult + "删除失败");
			}
			return new BsmResult(true, "删除主机成功");
		} catch (Exception e) {
			logger.error("删除主机失败", e);
			return new BsmResult(false, "删除主机失败");
		}
	}

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, Long userId) {
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
			int total = hostDao.count(params, deptId);
			if (simple) {
				List<SimpleBean> beans = hostDao.list(params, sorter, deptId);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<Host> hosts = hostDao.list(page, rows, params, sorter, deptId);
				if (!ListTool.isEmpty(hosts)) {
					for (Host host : hosts) {
						// 查询主机创建者
						if (null != host.getCreaterId()) {
							User creater = userDao.query(host.getCreaterId());
							if (null != creater) {
								host.setCreater(creater.getName());
							}
						}
						// 查询主机修改者
						if (null != host.getMenderId()) {
							User mendor = userDao.query(host.getMenderId());
							if (null != mendor) {
								host.setMendor(mendor.getName());
							}
						}
						// 查询主机所处环境
						if (null != host.getEnvId()) {
							Environment env = environmentDao.query(host.getEnvId());
							if (null != env) {
								host.setEnvName(env.getName());
							}
						}
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, hosts);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "查询失败");
		}
	}

	@Override
	public BsmResult modify(Host host, Long userId) {
		try {
			// 查询要更该的主机，判断该主机是否存在
			Host oldHost = hostDao.queryById(host.getId());
			if (null == oldHost) {
				return new BsmResult(false, "该主机不存在");
			}
			// 判断主机是否可达
			boolean conn = InetAddress.getByName(host.getIp()).isReachable(3000);
			if (!conn) {
				return new BsmResult(false, "机器不可达，无法更改");
			}
			// 由于主机的ip、用户名和密码都是可以更改的，确认更改后的ip用户名和密码都是正确的
			// 根据用户名和密码判断主机是否可达
			if(!StringUtils.isEmpty(host.getUsername()) || !StringUtils.isEmpty(host.getPassword())){
				SSHUtil ssh = new SSHUtil(host.getIp(), host.getUsername(), host.getPassword());
				boolean con = ssh.connect();
				if (!con) {
					return new BsmResult(false, "主机不可达，请确认用户名和密码是否准确");
				}
			}

			if (!host.getLabels().equals(oldHost.getLabels())) {
				Environment environment = environmentDao.query(oldHost.getEnvId());
				// 判断主机所在环境是否为空
				if (null != environment) {
					// return new BsmResult(false, "无法获取主机所在环境，无法更新标签");
					List<Host> updateHost = new ArrayList<Host>();
					oldHost.setLabels(host.getLabels());
					updateHost.add(oldHost);
					addNode(environment.getProxy(), updateHost);
				}
			}

			// 更改者id
			host.setMenderId(userId);
			// 更改时间
			host.setGmtModify(new Date());
			Boolean updateResult = hostDao.update(host);
			if (!updateResult) {
				return new BsmResult(false, "更新失败");
			}
			return new BsmResult(true, "更新成功");
		} catch (Exception e) {
			logger.error("更改主机失败", e);
			return new BsmResult(false, "更改主机失败，请确认主机用户名和密码是否准确");
		}
	}

	@Override
	public BsmResult detail(Long hostId) {
		try {
			Host host = hostDao.queryById(hostId);
			if (null == host) {
				return new BsmResult(false, "要查询主机不存在");
			}
			// 查询主机所在的环境
			if (null != host.getEnvId()) {
				// 主机所在环境
				Environment environment = environmentDao.query(host.getEnvId());
				if (null != environment) {
					host.setEnvName(environment.getName());
				}
			}
			// 主机创建者
			if (null != host.getCreaterId()) {
				User creater = userDao.query(host.getCreaterId());
				if (null != creater) {
					host.setCreater(creater.getName());
				}
			}
			// 主机修改者
			if (null != host.getMenderId()) {
				User mendor = userDao.query(host.getCreaterId());
				if (null != mendor) {
					host.setMendor(mendor.getName());
				}
			}
			return new BsmResult(true, host, "查询成功");
		} catch (Exception e) {
			logger.error("查询详情失败", e);
			return new BsmResult(false, "查询详情失败");
		}
	}

	/**
	 * 向集群中添加节点
	 * 
	 * @param masterIP
	 * @param nodes
	 */
	public void addNode(String masterIP, List<Host> nodes) {
		KubernetesClient kubernetesClient = openshiftUtil.getKubernetesClient(masterIP);
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
	public List<String> getNotReadyNode(String masterUrl) {
		KubernetesClient kubernetesClient = openshiftUtil.getKubernetesClient(masterUrl);
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
	public BsmResult queryNormalHost(Host host, Long userId) {
		try {
			// 查询状态正常且不在环境中的主机
			List<Host> normalHosts = hostDao.queryNormalHost(userId);
			if (ListTool.isEmpty(normalHosts)) {
				return new BsmResult(false, "不存在独立主机，请添加主机之后再操作");
			}
			return new BsmResult(true, normalHosts, "查询状态正常主机成功");
		} catch (Exception e) {
			logger.error("查询状态正常的独立主机失败", e);
			return new BsmResult(false, "查询状态正常的独立主机失败");
		}
	}

	@Override
	public BsmResult scheduleNode(Long hostId, Long userId) {
		KubernetesClient kubernetesClient = null;
		try {
			// 查询该主机
			Host host = hostDao.queryById(hostId);
			// 判断主机是否存在
			if (null == host) {
				return new BsmResult(false, "数据库中不存在该主机");
			}
			// 判断主机是否存在于环境中
			if (null == host.getEnvId()) {
				return new BsmResult(false, "该主机不存在于环境中，无法实现调度");
			}
			// 判断主机的状态:只有当主机处于3--可调度或者是4--不可调度状态 时才能实现状态的转换
			if (!HostEnum.SCHEDUABLE.getCode().equals(host.getStatus())
					&& !HostEnum.UNSCHEDUABLE.getCode().equals(host.getStatus())) {
				return new BsmResult(false, "主机不处于可调度或不可调度状态，无法实现状态转换");
			}
			// 获取与集群通信IP
			Environment environment = environmentDao.query(host.getEnvId());
			if (null == environment) {
				return new BsmResult(false, "获取与集群通信IP失败");
			}
			kubernetesClient = openshiftUtil.getKubernetesClient(environment.getProxy());
			// 获取该环境下的所有节点
			List<Node> nodes = kubernetesClient.nodes().list().getItems();
			int i = 0;
			for (Node node : nodes) {
				// 获取节点地址
				List<NodeAddress> nodeAddresses = node.getStatus().getAddresses();
				for (NodeAddress nodeAddress : nodeAddresses) {
					if (null != nodeAddress.getAddress()) {
						if (host.getIp().equals(nodeAddress.getAddress())) {
							boolean unschedulable = false;
							if (HostEnum.SCHEDUABLE.getCode().equals(host.getStatus())) {// 如果主机的状态是‘3’,证明该节点当前状态是可调度的，由当前的‘可调度’变为‘不可调度’
								unschedulable = true;
								// 由可调度变为不可调度
								host.setStatus(HostEnum.UNSCHEDUABLE.getCode());
							} else if (HostEnum.UNSCHEDUABLE.getCode().equals(host.getStatus())) {
								// 由不可调度变为可调度
								host.setStatus(HostEnum.SCHEDUABLE.getCode());
							}
							// 更新节点的调度状态
							kubernetesClient.nodes().withName(node.getMetadata().getName()).edit().editSpec()
									.withUnschedulable(unschedulable).endSpec().done();
							// 更新数据库数据
							boolean result = hostDao.update(host);
							if (!result) {
								return new BsmResult(false, "数据库更新失败！");
							}
							i++;
							break;
						}
					}
				}
				if (i == 1) {// 如果i==1,证明节点调度成功，跳出循环
					break;
				}
			}
			if (i == 0) {// 如果i==0，证明节点调度失败
				return new BsmResult(false, "调度失败");
			}

			return new BsmResult(true, "调度成功");
		} catch (Exception e) {
			logger.error("节点调度出错", e);
			return new BsmResult(false, "节点调度出错");
		} finally {
			if (null != kubernetesClient) {
				kubernetesClient.close();
			}
		}
	}

	@Override
	public BsmResult queryHostInEnv(Long envId, Long userId) {
		try {
			// 查询处于该环境下的主机
			List<Host> hosts = hostDao.queryByEnvId(envId);
			if (ListTool.isEmpty(hosts)) {
				return new BsmResult(false, "该环境下不存在主机");
			}
			return new BsmResult(true, hosts, "查询成功");
		} catch (Exception e) {
			logger.error("查询主机失败", e);
			return new BsmResult(false, "查询主机失败");
		}
	}

	@Override
	public BsmResult queryHostNotInEnv(Long envId, Long userId) {
		try {
			if (null == envId) {
				List<Host> hosts = hostDao.queryHostNotInEnv();
				return new BsmResult(true, hosts, "查询成功");
			}
			return new BsmResult(false, "查询失败");
		} catch (Exception e) {
			logger.error("查询不在环境中主机失败", e);
			return new BsmResult(false, "查询失败");
		}
	}

	@Override
	public void monitor() {
		try {
			List<Host> hosts = hostDao.queryAll();
			// 如果主机为空，不进行监控
			if (ListTool.isEmpty(hosts)) {
				return;
			}
			for (Host host : hosts) {
				// 如果
				if(HostEnum.ADDING.getCode().equals(host.getStatus())){
					continue;
				}
				// 判断主机是否可达
				boolean isCon = InetAddress.getByName(host.getIp()).isReachable(3000);
				if (!isCon) {
					// 主机不可达发送通知邮件
					host.setStatus(HostEnum.ABNORMAL.getCode());
					hostDao.update(host);
				} else {
					if (null == host.getEnvId()) {
						// 判断主机是否可达:只对不在环境中的主机进行监控，存在于环境中的主机在环境监控中进行监控
						host.setStatus(HostEnum.NORMAL.getCode());
						hostDao.update(host);
					}
				}
				if(StringUtils.isEmpty(host.getLabels())){
					System.out.println("标签为空");
				}
			}
		} catch (Exception e) {
			logger.error("主机监控出现异常", e);
			return;
		}
	}

	@Override
	public BsmResult hostTopology(Long hostId) {
		BsmResult bsmResult = new BsmResult();
		JSONArray hostArray = new JSONArray(); // 存放多个主机数组
		JSONObject hostObject = new JSONObject(); // 存放一个主机对象信息
		JSONArray podArray = new JSONArray(); // 存放多个运行实例数组
		Host host = null;
		try {
			/**
			 * 1、主机层处理
			 */
			host = hostDao.queryById(hostId);
			if (null == host) {
				bsmResult.setMessage("未获取到该主机信息");
				return bsmResult;
			}

			// 采集环境信息
			hostObject.put("name", host.getName());
			hostObject.put("targetCategory", "host");

			/**
			 * 2、运行实例层处理
			 */
			//获取环境信息
			Environment environment = environmentDao.queryByHostId(hostId);
			if (null == environment || 
					!Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				logger.warn("未获取到该应用所在的环境信息， 或环境状态["+environment.getStatus()+"]不可用");
				hostObject.put("tagetJson", podArray);
				hostArray.add(hostObject);
				bsmResult.setSuccess(true);
				bsmResult.setData(hostArray);
				return bsmResult;
			}
			
			ApplicationClient client = new ApplicationClient(environment.getProxy(),
					String.valueOf(environment.getPort()));
			
			PodList podList = (PodList) client.list(ApplicationEnum.RESOURCE.POD);
			if (null == podList) {
				logger.warn("未获取到该主机中的运行实例信息");
				hostObject.put("tagetJson", podArray);
				hostArray.add(hostObject);
				bsmResult.setSuccess(true);
				bsmResult.setData(hostArray);
				return bsmResult;
			}

			List<Pod> pods = podList.getItems();
			if (ListTool.isEmpty(pods)) {
				logger.warn("该节点下没有任何运行实例");
				hostObject.put("tagetJson", podArray);
				hostArray.add(hostObject);
				bsmResult.setSuccess(true);
				bsmResult.setData(hostArray);
				return bsmResult;
			}

			/**
			 * 3、pod层处理
			 */
			for (Pod pod : pods) {
				JSONObject podObject = new JSONObject();
				if (null != pod.getSpec().getNodeName() && 
						pod.getSpec().getNodeName().equals(host.getIp())) {
					podObject.put("targetName", pod.getSpec().getNodeName());
					podObject.put("targetCategory", "apply");
					podArray.add(podObject);
				}
			}

			hostObject.put("tagetJson", podArray);
			hostArray.add(hostObject);
			bsmResult.setSuccess(true);
			bsmResult.setData(hostArray);
			return bsmResult;

		} catch (Exception e) {
			logger.error("获取该主机信息异常", e);
			bsmResult.setMessage("获取该主机信息异常");
			return bsmResult;
		}
	
	}

	@Override
	public BsmResult monitorGpu(Long id, String num, String timeUnit) {
		try {
			Host host = hostDao.queryById(id);
			logger.info("------host:-----"+JSONObject.toJSONString(host));
			if (null == host) {
				return new BsmResult(false, "要查询主机不存在");
			}
			//
			List<GpuMonitor> gpuMonitors = gpuMonitorDao.list(host.getHostName(), num, timeUnit);
			logger.info("------gpuMonitors:------"+JSONObject.toJSONString(gpuMonitors));
			//数据格式处理
			JSONObject monitorData = gpuDataHandle(gpuMonitors);
			return new BsmResult(true, monitorData, "查询gpu监控数据信息成功");
		} catch (Exception e) {
			logger.error("查询gpu监控信息异常", e);
			return new BsmResult(false, "查询异常");
		}
	}
	
	private JSONObject gpuDataHandle(List<GpuMonitor> gpuMonitors){
		JSONObject monitorResult = new JSONObject();
		
		//时间数据
		JSONArray keys = new JSONArray();
		
		//data数据
		JSONArray tempDatas = new JSONArray();
		JSONArray gpuUsageDatas = new JSONArray();
		JSONArray memUsageDatas = new JSONArray();
		
		//Object
		JSONObject tempObject = new JSONObject();
		JSONObject gpuUsageObject = new JSONObject();
		JSONObject memUsageObject = new JSONObject();
		tempObject.put("name", "temp");//温度
		tempObject.put("data", tempDatas);//温度数据
		gpuUsageObject.put("name", "gpuUsage");//gpu使用率
		gpuUsageObject.put("data", gpuUsageDatas);//gpu使用率数据
		memUsageObject.put("name", "memUsage");//内存使用量
		memUsageObject.put("data", memUsageDatas);//内存使用量数据
		
		//values数组
		JSONArray tempValues = new JSONArray();
		JSONArray gpuUsageValues = new JSONArray();
		JSONArray memUsageValues = new JSONArray();
		tempValues.add(tempObject);
		gpuUsageValues.add(gpuUsageObject);
		memUsageValues.add(memUsageObject);
		
		JSONObject temp = new JSONObject();//温度监控数据
		JSONObject gpuUsage = new JSONObject();//gpu使用率监控数据
		JSONObject memUsage = new JSONObject();//内存使用量数据
		temp.put("keys", keys);
		temp.put("values", tempValues);
		gpuUsage.put("keys", keys);
		gpuUsage.put("values", gpuUsageValues);
		memUsage.put("keys", keys);
		memUsage.put("values", memUsageValues);
		
		monitorResult.put("Temp", temp);
		monitorResult.put("GpuUsage", gpuUsage);
		monitorResult.put("MemUsage", memUsage);
		
		for (GpuMonitor gpuMonitor : gpuMonitors) {
			
			keys.add(gpuMonitor.getTime());//时间轴
			
			tempDatas.add(gpuMonitor.getGpuTemp()); //温度
			gpuUsageDatas.add(gpuMonitor.getGpuUsage()); //gpu使用率
			memUsageDatas.add(gpuMonitor.getMemoryUsage()); //内存使用量
		}
		logger.info("-------result:----"+monitorResult.toJSONString());
		return monitorResult;
	}

}
