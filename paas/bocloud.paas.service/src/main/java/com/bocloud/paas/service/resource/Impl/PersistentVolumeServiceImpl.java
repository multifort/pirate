package com.bocloud.paas.service.resource.Impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
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
import com.bocloud.paas.common.enums.EnvironmentEnum;
import com.bocloud.paas.common.util.AddressConUtil;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.common.util.RfcDateTimeParser;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.environment.PersistentVolumeDao;
import com.bocloud.paas.dao.system.DictionaryDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Dictionary;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.entity.Volume;
import com.bocloud.paas.model.Labels;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.event.model.OperateResult;
import com.bocloud.paas.service.resource.PersistentVolumeService;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.RBDVolumeSource;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service("/persistentVolumeService")
public class PersistentVolumeServiceImpl implements PersistentVolumeService {

	private static Logger logger = LoggerFactory.getLogger(PersistentVolumeServiceImpl.class);
	@Autowired
	private PersistentVolumeDao persistentVolumeDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private DictionaryDao dictionaryDao;
	@Autowired
	private UserService userService;
	@Autowired
	private EventPublisher resourceEventPublisher;
	private static final Integer TIMEOUT = 3000;
	private String PV_TYPE_HOSTPATH = "hostPath";
	private String PV_TYPE_NFS = "NFS";
	private String PV_TYPE_CEPH = "ceph";

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, Long userId) {
		try {
			GridBean gridBean = null;
			if (null == params) {
				params = Lists.newArrayList();
			}
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			String deptId = userService.listDept(userId);
			int total = persistentVolumeDao.count(params, deptId);
			if (simple) {
				List<SimpleBean> beans = persistentVolumeDao.list(params, sorter, deptId);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<Volume> pvs = persistentVolumeDao.list(page, rows, params, sorter, deptId);
				if (!ListTool.isEmpty(pvs)) {
					for (Volume pv : pvs) {
						// 查询pv创建者
						if (null != pv.getCreaterId()) {
							User creater = userDao.query(pv.getCreaterId());
							if (null != creater) {
								pv.setCreater(creater.getName());
							}
						}
						// 查询pv修改者
						if (null != pv.getMenderId()) {
							User mendor = userDao.query(pv.getMenderId());
							if (null != mendor) {
								pv.setMendor(mendor.getName());
							}
						}
						// pv所在环境（环境id不会为空）
						Long envId = pv.getEnvId();
						if (null != envId) {
							Environment environment = environmentDao.query(envId);
							if (null != environment) {
								pv.setEnvName(environment.getName());
							}
						}
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, pvs);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询存储出错", e);
			return new BsmResult(false, "查询出错");
		}
	}

	public BsmResult create(Volume pv, Long userId) {
		KubernetesClient client = null;
		try {
			User user = userDao.query(userId);
			if (null == user) {
				return new BsmResult(false, "未获取到当前用户信息");
			}
			// 判断环境是否为空
			Environment environment = environmentDao.query(pv.getEnvId());
			if (null == environment) {
				return new BsmResult(false, "所选环境不存在，无法创建存储卷");
			}

			// 判断主机是否可达
			boolean conMaster = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
			if (!conMaster) {
				return new BsmResult(false, "该环境下代理主机不可达，无法创建存储");
			}
			// 判断环境的代理IP是否为空
			if(StringUtils.isEmpty(environment.getProxy())){
				return new BsmResult(false, "该环境下代理IP为空，无法创建存储卷");
			}
			//判断环境的端口是否为空
			if(StringUtils.isEmpty(environment.getPort())){
				return new BsmResult(false, "该环境下端口为空，无法创建存储卷");
			}
			client = new ApplicationClient(environment.getProxy(), environment.getPort().toString()).getKubeClient();
			// 判断该名称的存储卷是否存在
			PersistentVolume volume = client.persistentVolumes().withName(pv.getName()).get();
			if (null != volume) {
				return new BsmResult(false, "相同名称的存储卷在集群中已存在，请重新填写");
			}
			// 判断环境所处状态，只有当环境处于激活状态时，才能创建存储卷
			if (!EnvironmentEnum.ACTIVE.getCode().equals(environment.getStatus())) {
				return new BsmResult(false, "创建失败：只有当环境处于已激活状态时才能创建存储卷");
			}

			// 创建hostPath存储
			if (PV_TYPE_HOSTPATH.equals(pv.getType())) {
				BsmResult result = readTemplateFile(FileUtil.filePath("resource_template") + "hostpath_pv.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}

				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File hostpath_pv_temporary = new File(
						FileUtil.filePath("resource_template") + "hostpath_pv_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!hostpath_pv_temporary.exists()) {
					boolean createResult = hostpath_pv_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}
				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File templateFile = writeFile(hostpath_pv_temporary, result.getData().toString());
				if (null == templateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				String root = FileUtil.filePath("resource_template");
				FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
				Configuration configuration = Configuration.defaultConfiguration();
				GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
				Template template = groupTemplate.getTemplate("hostpath_pv_temporary.yaml");
				template.binding("name", pv.getName());
				if (null == pv.getAnnotations()) {
					template.binding("annotations", "");
				} else {
					List<Labels> annotationList = new ArrayList<Labels>();
					List<JSONObject> annotations = JSON.parseArray(pv.getAnnotations(), JSONObject.class);
					for (JSONObject jsonObject : annotations) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							annotationList.add(label);
						}
					}
					// 把创建者作为一个默认的注解写入到pv中
					Labels userLabel = new Labels();
					userLabel.setKey("creater");
					userLabel.setValue("creater" + ": " + user.getUsername());
					annotationList.add(userLabel);
					JSONObject obj = new JSONObject();
					obj.put("key", "creator");
					obj.put("value", user.getUsername());
					annotations.add(obj);
					JSONArray jsonArray = new JSONArray();
					for (JSONObject obj2 : annotations) {
						JSONObject object = new JSONObject();
						object.put("key", obj2.get("key"));
						object.put("value", obj2.get("value"));
						jsonArray.add(object);
					pv.setAnnotations(jsonArray.toJSONString());
					}
					template.binding("annotations", annotationList);
				}
				if (null == pv.getLabels()) {
					template.binding("labels", "");
				} else {
					List<Labels> labelList = new ArrayList<Labels>();
					List<JSONObject> labels = JSON.parseArray(pv.getLabels(), JSONObject.class);
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							labelList.add(label);
						}
					}
					template.binding("labels", labelList);
				}
				template.binding("access", pv.getAccess());
				template.binding("capacity", pv.getCapacity());
				template.binding("policy", pv.getPolicy());
				template.binding("path", pv.getPath());
				String templateContent = template.render();
				if ("".equals(templateContent) || null == templateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}
				// 向文件中写入数据，组装成创建存储的模板
				FileWriter writer = new FileWriter(root + "hostpath_pv_temporary.yaml");
				writer.write(templateContent);
				writer.close();

				client.load(new FileInputStream(new File(root + "hostpath_pv_temporary.yaml"))).createOrReplace();

				// 创建完成之后删除文件
				if (hostpath_pv_temporary.isFile() && hostpath_pv_temporary.exists()) {
					hostpath_pv_temporary.delete();
				}

				Thread.sleep(5000);
				// 查询集群下的所有存储，遍历，看是否创建成功
				PersistentVolume pvss = client.persistentVolumes().withName(pv.getName()).get();
				if (null == pvss) {
					return new BsmResult(false, "创建存储失败,请更换名称重新创建");
				}

				// 从集群中查询存储，如果创建成功保存至数据库
				int i = 0;
				if (pv.getName().equals(pvss.getMetadata().getName())) {
					pv.setStatus(pvss.getStatus().getPhase());
					i++;
				}
				if (i == 0) {// 如果i==0,证明查询出来的存储不包括刚才创建的存储，创建存储失败
					return new BsmResult(false, "创建存储失败");
				}

			}

			// 创建NFS存储
			if (PV_TYPE_NFS.equals(pv.getType())) {
				BsmResult result = readTemplateFile(FileUtil.filePath("resource_template") + "nfs_pv.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}
				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File nfs_pv_temporary = new File(FileUtil.filePath("resource_template") + "nfs_pv_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!nfs_pv_temporary.exists()) {
					boolean createResult = nfs_pv_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}

				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File nfsTemplateFile = writeFile(nfs_pv_temporary, result.getData().toString());
				if (null == nfsTemplateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				String root = FileUtil.filePath("resource_template");
				FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
				Configuration configuration = Configuration.defaultConfiguration();
				GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
				Template template = groupTemplate.getTemplate("nfs_pv_temporary.yaml");
				template.binding("name", pv.getName());
				if (null == pv.getAnnotations()) {
					template.binding("annotations", "");
				} else {
					List<Labels> annotationList = new ArrayList<Labels>();
					List<JSONObject> annotations = JSON.parseArray(pv.getAnnotations(), JSONObject.class);
					for (JSONObject jsonObject : annotations) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							annotationList.add(label);
						}
					}

					// 把创建者作为一个默认的注解写入到pv中
					Labels userLabel = new Labels();
					userLabel.setKey("creater");
					userLabel.setValue("creater" + ": " + user.getUsername());
					annotationList.add(userLabel);
					JSONObject obj = new JSONObject();
					obj.put("key", "creator");
					obj.put("value", user.getUsername());
					annotations.add(obj);
					JSONArray jsonArray = new JSONArray();
					for (JSONObject obj2 : annotations) {
						JSONObject object = new JSONObject();
						object.put("key", obj2.get("key"));
						object.put("value", obj2.get("value"));
						jsonArray.add(object);
					}
					pv.setAnnotations(jsonArray.toJSONString());
					template.binding("annotations", annotationList);
				}
				if (null == pv.getLabels()) {
					template.binding("labels", "");
				} else {
					List<Labels> labelList = new ArrayList<Labels>();
					List<JSONObject> labels = JSON.parseArray(pv.getLabels(), JSONObject.class);
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							labelList.add(label);
						}
					}
					template.binding("labels", labelList);
				}
				template.binding("access", pv.getAccess());
				template.binding("capacity", pv.getCapacity());
				template.binding("policy", pv.getPolicy());
				template.binding("path", pv.getPath());
				template.binding("ip", pv.getIp());
				String nfsTemplateContent = template.render();
				if ("".equals(nfsTemplateContent) || null == nfsTemplateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}

				// 向文件中写入数据，组装成创建存储的模板
				FileWriter writer = new FileWriter(root + "nfs_pv_temporary.yaml");
				writer.write(nfsTemplateContent);
				writer.close();

				client.load(new FileInputStream(new File(root + "nfs_pv_temporary.yaml"))).createOrReplaceAnd();

				// 创建完成之后删除文件
				if (nfs_pv_temporary.isFile() && nfs_pv_temporary.exists()) {
					nfs_pv_temporary.delete();
				}

				Thread.sleep(5000);

				PersistentVolume pvss = client.persistentVolumes().withName(pv.getName()).get();
				if (null == pvss) {
					return new BsmResult(false, "创建存储失败,请更换名称重新创建");
				}
				int j = 0;
				if (pv.getName().equals(pvss.getMetadata().getName())) {
					// 存储的状态
					pv.setStatus(pvss.getStatus().getPhase());
					j++;
				}
				if (j == 0) { // 如果j==0证明集群中不存在刚创建的存储，创建失败
					return new BsmResult(false, "创建存储失败");
				}
			}

			// 创建ceph存储
			if (PV_TYPE_CEPH.equals(pv.getType())) {
				//先创建secret
				BsmResult result = readTemplateFile(FileUtil.filePath("resource_template") + "ceph_secret.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}

				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File ceph_secret_temporary = new File(FileUtil.filePath("resource_template") + "ceph_secret_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!ceph_secret_temporary.exists()) {
					boolean createResult = ceph_secret_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}
				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File secretTemplateFile = writeFile(ceph_secret_temporary, result.getData().toString());
				if (null == secretTemplateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				String root = FileUtil.filePath("resource_template");
				FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
				Configuration configuration = Configuration.defaultConfiguration();
				GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
				Template secretTemplate = groupTemplate.getTemplate("ceph_secret_temporary.yaml");
				Dictionary dictionary = dictionaryDao.query("ceph-secret");
				if (null != dictionary) {
					secretTemplate.binding("key", dictionary.getDictValue());
				} else {
					return new BsmResult(false, "获取ceph 获取管理 key 失败！");
				}
				String secretTemplateContent = secretTemplate.render();
				if ("".equals(secretTemplateContent) || null == secretTemplateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}
				// 向文件中写入数据，组装成创建存储的模板
				FileWriter secretWriter = new FileWriter(root + "ceph_secret_temporary.yaml");
				secretWriter.write(secretTemplateContent);
				secretWriter.close();

				client.load(new FileInputStream(new File(root + "ceph_secret_temporary.yaml"))).createOrReplace();

				// 创建完成之后删除文件
				if (ceph_secret_temporary.isFile() && ceph_secret_temporary.exists()) {
					ceph_secret_temporary.delete();
				}
				
				result = readTemplateFile(FileUtil.filePath("resource_template") + "ceph_pv.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}

				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File ceph_pv_temporary = new File(FileUtil.filePath("resource_template") + "ceph_pv_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!ceph_pv_temporary.exists()) {
					boolean createResult = ceph_pv_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}
				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File templateFile = writeFile(ceph_pv_temporary, result.getData().toString());
				if (null == templateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				Template template = groupTemplate.getTemplate("ceph_pv_temporary.yaml");
				template.binding("name", pv.getName());
				if (null == pv.getAnnotations()) {
					template.binding("annotations", "");
				} else {
					List<Labels> annotationList = new ArrayList<Labels>();
					List<JSONObject> annotations = JSON.parseArray(pv.getAnnotations(), JSONObject.class);
					for (JSONObject jsonObject : annotations) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							annotationList.add(label);
						}
					}
					// 把创建者作为一个默认的注解写入到pv中
					Labels userLabel = new Labels();
					userLabel.setKey("creater");
					userLabel.setValue("creater" + ": " + user.getUsername());
					annotationList.add(userLabel);
					JSONObject obj = new JSONObject();
					obj.put("key", "creator");
					obj.put("value", user.getUsername());
					annotations.add(obj);
					JSONArray jsonArray = new JSONArray();
					for (JSONObject obj2 : annotations) {
						JSONObject object = new JSONObject();
						object.put("key", obj2.get("key"));
						object.put("value", obj2.get("value"));
						jsonArray.add(object);
					}
					pv.setAnnotations(jsonArray.toJSONString());
					template.binding("annotations", annotationList);
				}
				if (null == pv.getLabels()) {
					template.binding("labels", "");
				} else {
					List<Labels> labelList = new ArrayList<Labels>();
					List<JSONObject> labels = JSON.parseArray(pv.getLabels(), JSONObject.class);
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							labelList.add(label);
						}
					}
					template.binding("labels", labelList);
				}
				template.binding("access", pv.getAccess());
				template.binding("capacity", pv.getCapacity());
				template.binding("policy", pv.getPolicy());
				// ceph特有属性值
				Dictionary monitors = dictionaryDao.query("ceph-monitors");
				if (monitors != null) {
					template.binding("monitors", Arrays.asList(monitors.getDictValue().split(",")));
				} else {
					return new BsmResult(false, "获取ceph monitors失败！");
				}
				Dictionary image = dictionaryDao.query("ceph-image");
				if (image != null) {
					template.binding("image", image.getDictValue());
				} else {
					return new BsmResult(false, "获取ceph image失败！");
				}

				String templateContent = template.render();
				if ("".equals(templateContent) || null == templateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}
				// 向文件中写入数据，组装成创建存储的模板
				FileWriter writer = new FileWriter(root + "ceph_pv_temporary.yaml");
				writer.write(templateContent);
				writer.close();

				client.load(new FileInputStream(new File(root + "ceph_pv_temporary.yaml"))).createOrReplace();

				// 创建完成之后删除文件
				if (ceph_pv_temporary.isFile() && ceph_pv_temporary.exists()) {
					ceph_pv_temporary.delete();
				}

				Thread.sleep(5000);
				// 查询集群下的所有存储，遍历，看是否创建成功
				PersistentVolume pvss = client.persistentVolumes().withName(pv.getName()).get();
				if (null == pvss) {
					return new BsmResult(false, "创建存储失败,请更换名称重新创建");
				}

				// 从集群中查询存储，如果创建成功保存至数据库
				int i = 0;
				if (pv.getName().equals(pvss.getMetadata().getName())) {
					pv.setStatus(pvss.getStatus().getPhase());
					i++;
				}
				if (i == 0) {// 如果i==0,证明查询出来的存储不包括刚才创建的存储，创建存储失败
					return new BsmResult(false, "创建存储失败");
				}

			}

			pv.setCreaterId(userId); // 创建者ID
			pv.setGmtCreate(new Date()); // 创建时间
			pv.setOwnerId(userId); // 所属者ID
			pv.setDeptId(user.getDepartId()); //组织机构ID
			pv.setMenderId(userId); // 修改者ID
			pv.setGmtModify(new Date()); // 修改时间：第一次默认是创建时间
			persistentVolumeDao.save(pv);// 保存至数据库
			client.close();
			return new BsmResult(true, "创建存储成功");
		} catch (Exception e) {
			logger.error("创建存储卷失败", e);
			return new BsmResult(false, "创建失败：创建存储卷发生异常");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	@Override
	public BsmResult queryPVTemplate(Volume volume, Long userId) {
		try {
			String path = FileUtil.filePath("resource_template");
			if (null == path || "".equals(path)) {
				return new BsmResult(false, "获取模板文件路径出错");
			}
			String template = null;
			File templateFile = null;
			switch (volume.getType()) {
			case "hostPath":
				templateFile = new File(path + "hostpath_pv.yaml");
				template = FileUtil.readParameters(templateFile);
				break;
			case "NFS":
				templateFile = new File(path + "nfs_pv.yaml");
				template = FileUtil.readParameters(templateFile);
				break;
			case "ceph":
				templateFile = new File(path + "ceph_pv.yaml");
				template = FileUtil.readParameters(templateFile);
				break;
			default:
				break;
			}
			if (null != template) {
				return new BsmResult(true, JSONObject.parseObject(template), "获取模板成功");
			}
			return new BsmResult(false, "读取模板失败");
		} catch (Exception e) {
			logger.error("获取模板失败", e);
			return new BsmResult(false, "获取模板失败：获取模板时发生异常");
		}
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {
		try {
			// 根据id查询
			for (Long id : ids) {
				Volume volume = persistentVolumeDao.queryById(id);
				// 如果数据库中不存在该存储，跳过本次循环，执行下一次
				if (null == volume) {
					continue;
				}
				// 异步执行删除存储
				new Thread(new Runnable() {
					@Override
					public void run() {
						KubernetesClient client = null;
						try {
							if (null == volume.getEnvId()) {
								resourceEventPublisher
										.send(new OperateResult(false, volume.getName() + "删除失败：获取存储所在环境失败",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
								return;
							}
							// 判断存储所在环境是否存在
							Environment env = environmentDao.query(volume.getEnvId());
							if (null == env) {
								resourceEventPublisher
										.send(new OperateResult(false, volume.getName() + "删除失败：获取存储所在环境失败",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
								return;
							}
							// 判断主机是否可达
							boolean conMaster = InetAddress.getByName(env.getProxy()).isReachable(3000);
							if (!conMaster) {
								resourceEventPublisher
										.send(new OperateResult(false, volume.getName() + "删除失败：存储所在环境主机不可达",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
								return;
							}

							client = new ApplicationClient(env.getProxy(), env.getPort().toString()).getKubeClient();
							PersistentVolume pv = client.persistentVolumes().withName(volume.getName()).get();
							if (null == pv) {
								volume.setDeleted(true);
								persistentVolumeDao.remove(volume.getId(), userId);
								resourceEventPublisher
										.send(new OperateResult(true, volume.getName() + volume.getName() + "存储删除成功",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
								return;
							}

							// 删除虚拟机上的存储
							boolean deleteResult = client.persistentVolumes().withName(volume.getName()).delete();
							if (deleteResult) {// 根据结果更新数据库并返回消息
								volume.setDeleted(true);
								persistentVolumeDao.remove(volume.getId(), userId);
								resourceEventPublisher
										.send(new OperateResult(true, volume.getName() + volume.getName() + "存储删除成功",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
							} else {
								resourceEventPublisher
										.send(new OperateResult(false, volume.getName() + "删除失败：删除虚拟机上存储失败",
												"volume/delete", MapTools.simpleMap("userId", userId), userId));
							}
						} catch (Exception e) {
							logger.error(volume.getName() + "删除失败", e);
							resourceEventPublisher.send(new OperateResult(false, volume.getName() + "删除失败：删除时发生异常",
									"volume/delete", MapTools.simpleMap("userId", userId), userId));
						} finally {
							if (null != client) {
								client.close();
							}
						}
					}
				}).start();
			}
			return new BsmResult(true, "删除任务已下发，正在执行...");
		} catch (Exception e) {
			logger.error("删除存储失败", e);
			return new BsmResult(false, "删除失败：删除时出现异常");
		}
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			if (null != id) {
				Volume volume = persistentVolumeDao.queryById(id);
				if (null == volume) {
					return new BsmResult(false, "数据库中不存在该存储");
				}
				// 查询存储创建者
				if (null != volume.getCreaterId()) {
					User creater = userDao.query(volume.getCreaterId());
					if (null != creater) {
						volume.setCreater(creater.getName());
					}
				}
				// 查询存储修改者
				if (null != volume.getMenderId()) {
					User mendor = userDao.query(volume.getMenderId());
					if (null != mendor) {
						volume.setMendor(mendor.getName());
					}
				}
				return new BsmResult(true, volume, "查询成功");
			}
			return new BsmResult(false, "查询失败");
		} catch (Exception e) {
			logger.error("查询存储详情失败", e);
			return new BsmResult(false, "查询存储详情失败：查询出现异常");
		}
	}

	@Override
	public BsmResult modify(Volume pv, Long userId) {
		KubernetesClient client = null;
		try {
			// 判断环境是否为空
			Environment environment = environmentDao.query(pv.getEnvId());
			if (null == environment) {
				return new BsmResult(false, "所选环境不存在，无法编辑存储卷");
			}
			client = new ApplicationClient(environment.getProxy(), environment.getPort().toString()).getKubeClient();
			// 判断该名称的存储卷是否存在
			PersistentVolume volume = client.persistentVolumes().withName(pv.getName()).get();
			if (null == volume) {
				return new BsmResult(false, "该环境下不存在该存储卷，无法编辑");
			}

			// 判断环境所处状态，只有当环境处于激活状态时，才能创建存储卷
			if (!EnvironmentEnum.ACTIVE.getCode().equals(environment.getStatus())) {
				return new BsmResult(false, "创建失败：只有当环境处于已激活状态时才能编辑存储卷");
			}
			// 判断主机是否可达
			boolean con = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
			if (!con) {
				return new BsmResult(false, "该环境下代理主机不可达，无法编辑存储");
			}
			// 创建hostPath存储
			if (PV_TYPE_HOSTPATH.equals(pv.getType())) {
				BsmResult result = readTemplateFile(FileUtil.filePath("resource_template") + "hostpath_pv.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}

				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File hostpath_pv_temporary = new File(
						FileUtil.filePath("resource_template") + "hostpath_pv_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!hostpath_pv_temporary.exists()) {
					boolean createResult = hostpath_pv_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}
				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File templateFile = writeFile(hostpath_pv_temporary, result.getData().toString());
				if (null == templateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				String root = FileUtil.filePath("resource_template");
				FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
				Configuration configuration = Configuration.defaultConfiguration();
				GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
				Template template = groupTemplate.getTemplate("hostpath_pv_temporary.yaml");
				if (null == pv.getAnnotations()) {
					template.binding("annotations", "");
				} else {
					List<Labels> annotationList = new ArrayList<Labels>();
					List<JSONObject> annotations = JSON.parseArray(pv.getAnnotations(), JSONObject.class);
					for (JSONObject jsonObject : annotations) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							annotationList.add(label);
						}
					}
					template.binding("annotations", annotationList);
				}

				if (null == pv.getLabels()) {
					template.binding("labels", "");
				} else {
					List<Labels> labelList = new ArrayList<Labels>();
					List<JSONObject> labels = JSON.parseArray(pv.getLabels(), JSONObject.class);
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							labelList.add(label);
						}
					}
					template.binding("labels", labelList);
				}
				template.binding("name", pv.getName());
				template.binding("access", pv.getAccess());
				template.binding("capacity", pv.getCapacity());
				template.binding("policy", pv.getPolicy());
				template.binding("path", pv.getPath());
				String templateContent = template.render();
				if ("".equals(templateContent) || null == templateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}
				// 向文件中写入数据，组装成创建存储的模板
				FileWriter writer = new FileWriter(root + "hostpath_pv_temporary.yaml");
				writer.write(templateContent);
				writer.close();

				client.load(new FileInputStream(root + "hostpath_pv_temporary.yaml")).createOrReplaceAnd();

				// 创建完成之后删除文件
				if (hostpath_pv_temporary.isFile() && hostpath_pv_temporary.exists()) {
					hostpath_pv_temporary.delete();
				}
				// 查询集群下的所有存储，遍历，看是否创建成功
				List<PersistentVolume> pvss = client.persistentVolumes().list().getItems();
				if (ListTool.isEmpty(pvss)) {
					return new BsmResult(false, "编辑存储失败");
				}

				// 从集群中查询存储，如果创建成功保存至数据库
				int i = 0;
				for (PersistentVolume persistentVolume : pvss) {
					if (pv.getName().equals(persistentVolume.getMetadata().getName())) {
						pv.setStatus(persistentVolume.getStatus().getPhase());
						i++;
					}
				}
				if (i == 0) {// 如果i==0,证明查询出来的存储不包括刚才创建的存储，创建存储失败
					return new BsmResult(false, "编辑存储失败");
				}
			}

			// 创建NFS存储
			if (PV_TYPE_NFS.equals(pv.getType())) {
				BsmResult result = readTemplateFile(FileUtil.filePath("resource_template") + "nfs_pv.yaml");
				if (!result.isSuccess()) {
					return new BsmResult(false, "读取模板文件失败");
				}
				// 创建一个临时模板文件，然后把需要创建的存储的值存入临时模板文件中，然后根据临时模板文件来创建存储卷
				File nfs_pv_temporary = new File(FileUtil.filePath("resource_template") + "nfs_pv_temporary.yaml");
				// 判断临时文件是否存在，如果不存在则创建
				if (!nfs_pv_temporary.exists()) {
					boolean createResult = nfs_pv_temporary.createNewFile();
					if (!createResult) {
						return new BsmResult(false, "创建临时模板文件失败！");
					}
				}

				// 向存储文件中写入创建存储的内容,如：apiVersion: v1等
				File nfsTemplateFile = writeFile(nfs_pv_temporary, result.getData().toString());
				if (null == nfsTemplateFile) {
					return new BsmResult(false, "生成存储模板文件失败");
				}

				// 临时文件生成之后，把临时文件中需要填值得地方替代为前段传过来的属性值
				String root = FileUtil.filePath("resource_template");
				FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
				Configuration configuration = Configuration.defaultConfiguration();
				GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
				Template template = groupTemplate.getTemplate("nfs_pv_temporary.yaml");

				if (null == pv.getAnnotations()) {
					template.binding("annotations", "");
				} else {
					List<Labels> annotationList = new ArrayList<Labels>();
					List<JSONObject> annotations = JSON.parseArray(pv.getAnnotations(), JSONObject.class);
					for (JSONObject jsonObject : annotations) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							annotationList.add(label);
						}
					}
					template.binding("annotations", annotationList);
				}

				if (null == pv.getLabels()) {
					template.binding("labels", "");
				} else {
					List<Labels> labelList = new ArrayList<Labels>();
					List<JSONObject> labels = JSON.parseArray(pv.getLabels(), JSONObject.class);
					for (JSONObject jsonObject : labels) {
						if (!"".equals(jsonObject.get("key").toString()) && null != jsonObject.get("key").toString()) {
							Labels label = new Labels();
							label.setKey(jsonObject.get("key").toString());
							label.setValue(
									jsonObject.get("key").toString() + ": " + jsonObject.get("value").toString());
							labelList.add(label);
						}
					}
					template.binding("labels", labelList);
				}
				template.binding("name", pv.getName());
				template.binding("access", pv.getAccess());
				template.binding("capacity", pv.getCapacity());
				template.binding("policy", pv.getPolicy());
				template.binding("path", pv.getPath());
				template.binding("ip", pv.getIp());
				String nfsTemplateContent = template.render();
				if ("".equals(nfsTemplateContent) || null == nfsTemplateContent) {
					return new BsmResult(false, "组装模板内容出错");
				}

				// 向文件中写入数据，组装成创建存储的模板
				FileWriter writer = new FileWriter(root + "nfs_pv_temporary.yaml");
				writer.write(nfsTemplateContent);
				writer.close();

				client.load(new FileInputStream(root + "nfs_pv_temporary.yaml")).createOrReplaceAnd();

				// 创建完成之后删除文件
				if (nfs_pv_temporary.isFile() && nfs_pv_temporary.exists()) {
					nfs_pv_temporary.delete();
				}

				List<PersistentVolume> pvss = client.persistentVolumes().list().getItems();
				if (ListTool.isEmpty(pvss)) {
					return new BsmResult(false, "编辑存储失败");
				}
				int j = 0;
				for (PersistentVolume nfsVolume : pvss) {
					if (pv.getName().equals(nfsVolume.getMetadata().getName())) {
						pv.setStatus(nfsVolume.getStatus().getPhase());
						j++;
					}
				}
				if (j == 0) { // 如果j==0证明集群中不存在刚创建的存储，创建失败
					return new BsmResult(false, "编辑存储失败");
				}
			}
			pv.setMenderId(userId); // 修改者ID
			pv.setGmtModify(new Date()); // 修改时间：第一次默认是创建时间
			persistentVolumeDao.update(pv);// 保存至数据库
			client.close();
			return new BsmResult(true, "编辑存储成功");
		} catch (Exception e) {
			logger.error("编辑存储卷失败", e);
			return new BsmResult(false, "编辑失败：编辑存储卷发生异常");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	/**
	 * 读取模板文件
	 * 
	 * @param filePath
	 * @return
	 */
	private static BsmResult readTemplateFile(String filePath) {
		StringBuffer content = new StringBuffer();
		String encoding = "UTF-8";
		File file = new File(filePath);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					break;
				}
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return new BsmResult(false, "读取文件失败");
		}
		return new BsmResult(true, content, "读取文件成功。");
	}

	/**
	 * 向文件中写入数据
	 * 
	 * @param file
	 * @param data
	 * @return
	 */
	private static File writeFile(File file, String data) {
		try {
			FileWriter fileWritter = new FileWriter(file);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(data);
			bufferWritter.close();
			return file;
		} catch (IOException e) {
			logger.error("写文件异常", e);
			return null;
		}
	}

	@Override
	public BsmResult queryPVData(Volume pv, Long userId) {
		KubernetesClient client = null;
		try {
			Environment environment = environmentDao.query(pv.getEnvId());
			if (null == environment) {
				return new BsmResult(false, "获取存储卷所在环境异常");
			}
			// 判断主机是否可达
			boolean conMaster = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
			if (!conMaster) {
				return new BsmResult(false, "存储卷所处环境下代理主机不可达，无法获取存储卷数据");
			}

			client = new ApplicationClient(environment.getProxy(), environment.getPort().toString()).getKubeClient();
			// 判断该名称的存储卷是否存在
			PersistentVolume volume = client.persistentVolumes().withName(pv.getName()).get();
			if (null == volume) {
				return new BsmResult(false, "该环境下不存在该存储卷，无法编辑");
			}
			client.close();
			return new BsmResult(true, JSONObject.toJSON(volume), "获取数据成功");
		} catch (Exception e) {
			logger.error("获取存储卷数据异常", e);
			return new BsmResult(false, "获取存储卷数据异常");
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

	@Override
	public void pvMonitor() {
		KubernetesClient client = null;
		try {
			List<Environment> environments = environmentDao.queryAll(null);
			if (ListTool.isEmpty(environments)) {
				return;
			}
			// 遍历环境获取所有pv更新到数据库
			for (Environment environment : environments) {
				if (EnvironmentEnum.UNAVAILABLE.getCode().equals(environment.getStatus()) || EnvironmentEnum.ABNORMAL.getCode().equals(environment.getStatus())
						|| EnvironmentEnum.FREEZE.getCode().equals(environment.getStatus())) {
					continue;
				}
				// 判断环境的代理ip是否为空
				if (StringUtils.isEmpty(environment.getProxy())) {
					continue;
				}
				//判断环境的代理端口是否为空
				if(StringUtils.isEmpty(environment.getPort())){
					continue;
				}
				boolean con = InetAddress.getByName(environment.getProxy()).isReachable(TIMEOUT);
				if (!con) {
					continue;
				}
				// 判断环境IP+port是否可用
				boolean conn = new AddressConUtil().connect(environment.getProxy(), environment.getPort());
				if (!conn) {
					continue;
				} 
				// 获取环境下所有persistentvolume
				client = new ApplicationClient(environment.getProxy(), environment.getPort().toString()).getKubeClient();
				// 查看数据库中在该环境下否存在pv
				List<Volume> pvs = persistentVolumeDao.queryByEnvId(environment.getId());
				List<PersistentVolume> persistentVolumes = client.persistentVolumes().list().getItems();
				// 判断环境中是否存在persistentVolume
				if (ListTool.isEmpty(persistentVolumes)) {// 如果环境中不存在volume,删除数据库中存在于当前环境中存储卷
					if (!ListTool.isEmpty(pvs)) {
						for (Volume volume : pvs) {
							persistentVolumeDao.delete(Volume.class, volume.getId());
						}
					}
					continue;
				}

				// 存放数据库中的
				Map<String, Volume> volumeMap = new HashMap<>();

				// 遍历从环境中获取到的实际存在的存储卷，遍历，更新数据库
				for (PersistentVolume pv : persistentVolumes) {
					int i = 0;
					for (Volume volume : pvs) {
						if (pv.getMetadata().getName().equals(volume.getName())) {
							// 判断环境下的pv的个属性是否与数据库中一致
							Volume newVolume = new Volume();
							BeanUtils.copyProperties(volume, newVolume);
							// 判断存储卷的状态是否变更
							if (!pv.getStatus().getPhase().equals(volume.getStatus())) {
								newVolume.setStatus(pv.getStatus().getPhase());
							}
							// 判断回收策略是否更改
							if (!pv.getSpec().getPersistentVolumeReclaimPolicy().equals(newVolume.getPolicy())) {
								newVolume.setPolicy(pv.getSpec().getPersistentVolumeReclaimPolicy());
							}
							// 判断读写模式是否更改
							if (!pv.getSpec().getAccessModes().get(0).equals(newVolume.getAccess())) {
								newVolume.setAccess(pv.getSpec().getAccessModes().get(0));
							}
							// 判断容量是否更改
							String amount = pv.getSpec().getCapacity().get("storage").getAmount();
//							if (!amount.substring(0, amount.length() - 2).equals(newVolume.getCapacity())) {
//								newVolume.setCapacity(amount.substring(0, amount.length() - 2));
//							}
							if(amount.contains("Gi")){
								volume.setCapacity(amount.substring(0, amount.length() - 2));
							}else{
								volume.setCapacity(amount);
							}
							// 判断类型是否更改
							HostPathVolumeSource hostPathVolumeSouce = pv.getSpec().getHostPath();
							if (null != hostPathVolumeSouce && newVolume.getType().equals(PV_TYPE_HOSTPATH)) {
								if (!hostPathVolumeSouce.getPath().equals(newVolume.getPath())) {
									newVolume.setPath(hostPathVolumeSouce.getPath());
									newVolume.setIp(null);
								}
							}

							NFSVolumeSource nfsVolume = pv.getSpec().getNfs();
							if (null != nfsVolume && newVolume.getType().equals(PV_TYPE_NFS)) {
								if (!nfsVolume.getPath().equals(newVolume.getPath())) {
									newVolume.setPath(nfsVolume.getPath());
								}

								if (!nfsVolume.getServer().equals(newVolume.getIp())) {
									newVolume.setIp(nfsVolume.getServer());
								}
							}

							// 判断注解是否更改
							Map<String, String> annotationMap = pv.getMetadata().getAnnotations();
							JSONArray annoArray = new JSONArray();
							if (null != annotationMap) {
								for (String key : annotationMap.keySet()) {
									JSONObject obj = new JSONObject();
									obj.put("key", key);
									obj.put("value", annotationMap.get(key));
									annoArray.add(obj);
								}
							}
							// 判断注解是否相等
							if (null != newVolume.getAnnotations() && null == annotationMap) {
								newVolume.setAnnotations(null);
							} else if (null == newVolume.getAnnotations() && null != annotationMap) {
								newVolume.setAnnotations(annoArray.toString());
							} else if (null != newVolume.getAnnotations() && null != annotationMap) {
								if (!newVolume.getAnnotations().equals(annoArray.toString())) {
									if (!annoArray.isEmpty()) {
										newVolume.setAnnotations(annoArray.toString());
									}
								}
							}

							// 判断标签是否更改
							Map<String, String> labelMap = pv.getMetadata().getLabels();
							JSONArray array = new JSONArray();
							if (null != labelMap) {
								for (String key : labelMap.keySet()) {
									JSONObject obj = new JSONObject();
									obj.put("key", key);
									obj.put("value", labelMap.get(key));
									array.add(obj);
								}
							}
							// 判断标签是否更改
							if (null != newVolume.getLabels() && null == labelMap) {
								newVolume.setLabels(null);
							} else if (null == newVolume.getLabels() && null != labelMap) {
								newVolume.setLabels(array.toString());
							} else if (null != newVolume.getLabels() && null != labelMap) {
								if (!newVolume.getLabels().equals(labelMap)) {
									if (!array.isEmpty()) {
										newVolume.setLabels(array.toString());
									}
								}
							}

							// 如果不相等证明Volume更改了，更新数据库数据
							if (!newVolume.equals(volume)) {
								persistentVolumeDao.update(newVolume);
							}
							i++;
						}
						// 存放数据库中已经存在的存储卷
						volumeMap.put(volume.getName(), volume);
					}

					if (i == 0) {// 如果i等于0，证明数据库中不存在该存储卷，把新的存储卷更新到数据库
						Volume volume = new Volume();
						volume.setName(pv.getMetadata().getName());
						HostPathVolumeSource hostPathVolumeSouce = pv.getSpec().getHostPath();
						NFSVolumeSource nfsVolume = pv.getSpec().getNfs();
						RBDVolumeSource rbdVolumeSource = pv.getSpec().getRbd();
						if (null != hostPathVolumeSouce) {
							// hostPath类型存储卷的存储路径
							volume.setPath(hostPathVolumeSouce.getPath());
							// 类型
							volume.setType("hostPath");
						}

						if (null != nfsVolume) {
							// nfs类型存储卷的IP
							volume.setIp(nfsVolume.getServer());
							// nfs类型存储卷的存储路径
							volume.setPath(nfsVolume.getPath());
							// 类型
							volume.setType("NFS");
						}

						if (null != rbdVolumeSource) {
							// 类型
							volume.setMonitors(rbdVolumeSource.getMonitors().toString());
							volume.setType("ceph");
						}

						// 获取读取模式
						volume.setAccess(pv.getSpec().getAccessModes().get(0));
						// 获取回收策略
						volume.setPolicy(pv.getSpec().getPersistentVolumeReclaimPolicy());
						// 获取存储卷容量大小
						String amount = pv.getSpec().getCapacity().get("storage").getAmount();
						if(amount.contains("Gi")){
							volume.setCapacity(amount.substring(0, amount.length() - 2));
						}else if(amount.contains("G")){
							volume.setCapacity(amount.substring(0, amount.length() - 1));
						}else{
							volume.setCapacity(amount);
						}
						// 存储卷所在环境
						volume.setEnvId(environment.getId());
						// 获取label
						Map<String, String> labelMap = pv.getMetadata().getLabels();
						JSONArray array = new JSONArray();
						if (null != labelMap) {
							for (String key : labelMap.keySet()) {
								JSONObject obj = new JSONObject();
								obj.put("key", key);
								obj.put("value", labelMap.get(key));
								array.add(obj);
							}
						}
						// 存储卷标签
						if (!array.isEmpty()) {
							volume.setLabels(array.toString());
						}
						// 存储卷注解
						Map<String, String> annotationMap = pv.getMetadata().getAnnotations();
						JSONArray annoArray = new JSONArray();
						if (null != annotationMap) {
							for (String key : annotationMap.keySet()) {
								JSONObject obj = new JSONObject();
								obj.put("key", key);
								obj.put("value", annotationMap.get(key));
								annoArray.add(obj);
							}
						}
						// 存储卷注解
						if (!annoArray.isEmpty()) {
							volume.setAnnotations(annoArray.toString());
						}
						// 存储卷创建时间
						volume.setGmtCreate(DateTools.stringToDate(DateTools.formatTime(
								RfcDateTimeParser.parseDateString(pv.getMetadata().getCreationTimestamp()))));
						// 存储卷状态
						volume.setStatus(pv.getStatus().getPhase());
						// 存储卷名称
						volume.setName(pv.getMetadata().getName());
						// 保存到数据库
						persistentVolumeDao.save(volume);
					}
				}

				// 删除map中的存储卷：剩下的是需要删除的存储卷，即实际集群中不存在该存储卷
				for (PersistentVolume persistentVolume : persistentVolumes) {
					volumeMap.remove(persistentVolume.getMetadata().getName());
				}

				// 更新数据库：删除数据库中在实际环境中不存在的存储卷
				for (String volumeName : volumeMap.keySet()) {
					persistentVolumeDao.delete(Volume.class, volumeMap.get(volumeName).getId());
				}
			}

		} catch (Exception e) {
			logger.error("监控persistentVolume出现异常", e);
		} finally {
			if (null != client) {
				client.close();
			}
		}
	}

}
