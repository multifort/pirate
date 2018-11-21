package com.bocloud.paas.service.application.Impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.common.util.StringUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ConfigManageDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ConfigManage;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.service.application.ConfigManageService;
import com.bocloud.paas.service.application.model.DataMap;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * describe: 配置管理业务逻辑层实现类
 * @author Zaney
 * @data 2017年10月17日
 */
@Service("configManageService")
public class ConfigManageServiceImpl implements ConfigManageService {
	private static Logger logger = LoggerFactory.getLogger(ConfigManageServiceImpl.class);
	
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private ConfigManageDao configManageDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserService userService;

	@Override
	public BsmResult create(ConfigManage configManage, Map<String, String> dataMap, RequestUser requestUser) {
		BsmResult bsmResult = new BsmResult();
		User user = null;
		if (null == (user = getUser(requestUser.getId()))) {
			bsmResult.setMessage("未获取到当前用户信息");
			return bsmResult;
		}
		//名称校验
		boolean existed = existed(configManage.getAppId(), configManage.getName());
		if (existed) {
			bsmResult.setMessage("该名称在该应用下已存在");
			return bsmResult;
		}
		
		//获取应用信息
		Application application = getApplication(configManage.getAppId());
		if (null == application) {
			bsmResult.setMessage("未获取到应用信息");
			return bsmResult;
		}
		
		//获取环境连接
	    ApplicationClient client = this.build(application.getEnvId());
	    if (null == client) {
	    	bsmResult.setMessage("未获取到环境资源连接");
			return bsmResult;
		}
		
		//获取可执行模板内容
		String templateDir = FileUtil.filePath("resource_template/config_manage");
		String content = getExecutableTemplate(templateDir, configManage.getFileDir(), dataMap, configManage.getName(), 
				application.getNamespace(), StringUtil.convertPinyin(application.getName()).toLowerCase());
		
		//创建一个可执行的临时文件,并执行
		File newFile = FileUtil.createTemporaryFile(templateDir, "simple_configmap_template.yaml", content);
		List<HasMetadata> metadatas = load(client, newFile, true);
		client.close();
		if (ListTool.isEmpty(metadatas)) {
			bsmResult.setMessage("创建配置管理实例失败");
			return bsmResult;
		}
		
		//保存数据库信息
		configManage.setCreaterId(user.getId());
		configManage.setMenderId(user.getId());
		configManage.setOwnerId(user.getId());
		configManage.setDeptId(user.getDepartId());
		boolean saved = save(configManage);
		if (!saved) {
			bsmResult.setMessage("创建成功,但保存数据库信息失败");
		} else {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("创建成功");
		}
		
		return bsmResult;
	}
	
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
	public BsmResult detail(Long id) {
		BsmResult bsmResult = new BsmResult();
		JSONObject object = new JSONObject();
		//获取配置实例
		ConfigManage configManage = getConfigManage(id);
		if (null == configManage) {
			bsmResult.setMessage("未获取到该配置项的信息");
			return bsmResult;
		}
		
		//获取应用信息
		Application application = getApplication(configManage.getAppId());
		if (null == application) {
			bsmResult.setMessage("未获取到应用信息");
			return bsmResult;
		}
		
		//构建环境资源链接
		ApplicationClient client = this.build(application.getEnvId());
		if (null == client) {
			bsmResult.setMessage("未获取到环境资源链接");
			return bsmResult;
		}
		
		ConfigMap configMap = (ConfigMap) client.detail(application.getNamespace(), 
				configManage.getName(), ApplicationEnum.RESOURCE.CONFIGMAP);
		
		//获取文件
		if (StringUtils.hasText(configManage.getFileDir())) {
			String fileDir = configManage.getFileDir();
			File file = new File(fileDir);
			String[] fileNames = file.list();
			object.put("files", fileNames);
		}
		
		object.put("configMap", configMap);
		object.put("configManage", configManage);
		bsmResult.setData(object);
		bsmResult.setSuccess(true);
		bsmResult.setMessage("查询详情成功");
		return bsmResult;
	}

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			 RequestUser user) {
		List<ConfigManage> configManages;
		List<SimpleBean> beans;
		GridBean gridBean;
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(user.getId());
			
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = 0;
			total = configManageDao.count(params, deptIds);
			if (simple) {
				beans = configManageDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				configManages = configManageDao.list(page, rows, params, sorter, deptIds);
				gridBean = GridHelper.getBean(page, rows, total, configManages);
			}
			return new BsmResult(true, gridBean, "配置实例查询成功");
		} catch (Exception e) {
			logger.error("list configManages exception:", e);
			return new BsmResult(false, "查询配置实例异常");
		}

	}

	@Override
	public BsmResult remove(List<Long> ids) {
		BsmResult bsmResult = new BsmResult();
		int count = 0;
		
		for (Long id : ids) {
			//获取配置实例信息
			ConfigManage configManage = getConfigManage(id);
			if (null == configManage) {
				bsmResult.setMessage("未获取到该配置项信息");
				return bsmResult;
			}
			
			//获取应用信息
			Application application = getApplication(configManage.getAppId());
			if (null == application) {
				bsmResult.setMessage("未获取到应用信息");
				return bsmResult;
			}
			
			//获取环境资源链接
			ApplicationClient client = this.build(application.getEnvId());
			
			//删除服务端的ConfigManage
			boolean deleted = client.remove(application.getNamespace(), configManage.getName(), ApplicationEnum.RESOURCE.CONFIGMAP);
			if (deleted) {
				//从数据库删除配置实例
				boolean removed = remove(id);
				if (removed) {
					//删除文件
					boolean rmFile = remove(configManage.getFileDir());
					if (rmFile) {
						count ++;
					}
				}
			}
		}
		
		if (ids.size() != count) {
			logger.warn("delete some configMap from kubernetes server failed, please check kubernetes environment");
			bsmResult.setMessage("删除一些配置资源失败");
			return bsmResult;
		}
		
		bsmResult.setSuccess(true);
		bsmResult.setMessage("删除成功");
		return bsmResult;
	}
	
	@Override
	public BsmResult modify(Long id, String remark, Map<String, String> dataMap, RequestUser user) {
		BsmResult bsmResult = new BsmResult();
		//获取配置实例信息
		ConfigManage configManage = getConfigManage(id);
		if (null == configManage) {
			bsmResult.setMessage("未获取到配置实例信息");
			return bsmResult;
		}
		
		//获取应用信息
		Application application = getApplication(configManage.getAppId());
		if (null == application) {
			bsmResult.setMessage("未获取到应用信息");
			return bsmResult;
		}
		
		//获取环境连接
	    ApplicationClient client = this.build(application.getEnvId());
	    if (null == client) {
	    	bsmResult.setMessage("未获取到环境资源连接");
			return bsmResult;
		}
		
		//获取可执行模板内容
		String templateDir = FileUtil.filePath("resource_template/config_manage");
		String content = getExecutableTemplate(templateDir, configManage.getFileDir(), dataMap, configManage.getName(), 
				application.getNamespace(), StringUtil.convertPinyin(application.getName()).toLowerCase());
		
		//创建一个可执行的临时文件,并执行
		File newFile = FileUtil.createTemporaryFile(templateDir, "simple_configmap_template.yaml", content);
		List<HasMetadata> metadatas = load(client, newFile, true);
		client.close();
		if (ListTool.isEmpty(metadatas)) {
			logger.info("服务端配置实例项数据没有被篡改");
		}
		
		//修改数据库信息
		configManage.setMenderId(user.getId());
		configManage.setGmtModify(new Date());
		configManage.setRemark(remark);
		boolean modified = modify(configManage);
		if (!modified) {
			bsmResult.setMessage("修改成功,但修改数据库信息失败");
		} else {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("修改成功");
		}
		
		return bsmResult;
	}
	
	/**
	 * 校验配置实例项的名称在该应用下是否存在
	 * @param appId
	 * @param name
	 * @return
	 */
	private boolean existed(Long appId, String name){
		ConfigManage configManage = null;
		try {
			configManage = configManageDao.existed(appId, name);
			if (null == configManage) {
				return false;
			}
		} catch (Exception e) {
			logger.error("校验配置实例项的名称在该应用下是否存在，出现异常", e);
		}
		return true;
	}
	/**
	 * 删除配置实例的文件
	 * @param fileDir
	 * @return
	 */
	private boolean remove(String fileDir){
		//如果配置实例不为文件方式创建，则return
		if (!StringUtils.hasText(fileDir)) {
			return true;
		}
		
		return FileUtil.deleteDirectory(new File(fileDir));
	}
	
	/**
	 * 修改配置实例
	 * @param configManage
	 * @return
	 */
	private boolean modify(ConfigManage configManage){
		try {
			return configManageDao.update(configManage);
		} catch (Exception e) {
			logger.error("保存配置项异常", e);
			return false;
		}
	}
	/**
	 * 数据库保存配置项
	 * @param configManage
	 * @return
	 */
	private boolean save(ConfigManage configManage){
		try {
			return configManageDao.baseSave(configManage);
		} catch (Exception e) {
			logger.error("保存配置项异常", e);
			return false;
		}
	}
	/**
	 * 从数据库中删除配置项
	 * @param id
	 * @return
	 */
	private boolean remove(Long id){
		try {
			return configManageDao.delete(ConfigManage.class, id);
		} catch (Exception e) {
			logger.error("从数据库中删除配置项异常", e);
			return false;
		}
	}
	
	/**
	 * 获取可执行模板
	 * @param filePath configMap基础模板的路径
	 * @param fileDir  用户上传的文件的目录路径
	 * @param name
	 * @param namespace
	 * @param appName
	 */
	private String getExecutableTemplate(String templateDir, String fileDir, Map<String, String> dataMap,
			String name, String namespace, String appName){
		Template template = template(templateDir, "configMap_basic.yaml");
		template.binding("name", name);
		template.binding("namespace", namespace);
		template.binding("appName", appName);
		
		//获取目录文件列表
		List<DataMap> listFile = listFile(fileDir);
		template.binding("files", listFile);
		
		//键值对转换成对象
		List<DataMap> datas = new ArrayList<>();
		if (!dataMap.isEmpty()) {
			for (Map.Entry<String, String> entry : dataMap.entrySet()) {
				DataMap data = new DataMap();
				data.setKey(entry.getKey());
				data.setValue(entry.getValue());
				datas.add(data);
			}
		}
		template.binding("dataMaps", datas);
		
		return template.render();
	}
	
	/**
	 * 获取文件列表以及内容
	 * @param fileDir
	 * @return
	 */
	private List<DataMap> listFile(String fileDir){
		List<DataMap> datas = new ArrayList<>();
		if (!StringUtils.hasText(fileDir)) {
			return datas;
		}
		logger.info("上传的文件目录路径："+fileDir);
		File file=new File(fileDir);
		String[] files = file.list();
		logger.info("上传的文件列表："+files);
		for (String name : files) {
			//获取子模板的内容
			String childrenContent = getChildrenTemplate(fileDir + File.separatorChar + name);
			DataMap data = new DataMap(name, childrenContent);
			datas.add(data);
		}
		return datas;
	}
	
	private String getChildrenTemplate(String filePath){
		//获取文件内容，以数组形式存储
		List<String> contentList = FileUtil.readContent(filePath);
		
		//获取子模板，并绑定数据
		String fileDir = FileUtil.filePath("resource_template/config_manage");
		Template template = template(fileDir, "children.yaml");
		template.binding("contents", contentList);
		
		return template.render();
		
	}
	
	/**
	 * 根据文件模板部署
	 * @param client 
	 * @param file 文件名称
	 * @param flag  load成功后，是否删除文件   true|删除  、 false|不删除
	 * @return
	 */
	private List<HasMetadata> load(ApplicationClient client, File file, boolean flag){
		List<HasMetadata> metadatas = null;
		try {
			metadatas = client.load(file);
		} catch (Exception e) {
			logger.error("deploy exception", e);
		}
		// 创建完成之后删除文件
		if (flag) { //如果true， 删除文件
			if (file.isFile() && file.exists()) {
				file.delete();
			}
		}
		return metadatas;
	}
	
	
	/**
	 * 构建资源链接的client
	 * 
	 * @param applicationId
	 * @return
	 */
	private ApplicationClient build(Long envId) {
		ApplicationClient client = null;
		//获取环境信息
		Environment environment = getEnvironment(envId);
		if (null == environment) {
			return client;
		}

		//获取资源连接
		client = new ApplicationClient(environment.getProxy(), String.valueOf(environment.getPort()));
		return client;
	}
	
	/**
	 * 获取配置实例信息
	 * @param id
	 * @return
	 */
	private ConfigManage getConfigManage(Long id){
		ConfigManage configManage = null;
		try {
			configManage = configManageDao.detail(id);
		} catch (Exception e) {
			logger.error("Get configManage detail info exception", e);
		}
		return configManage;
	}
	
	/**
	 * 获取环境资源信息
	 * @param envId
	 * @return
	 */
	private Environment getEnvironment(Long envId){
		Environment environment = null;
		try {
			environment = environmentDao.query(envId);
		} catch (Exception e) {
			logger.error("获取环境资源信息异常", e);
		}
		return environment;
	}
	
	/**
	 * 获取应用信息
	 * @param applicationId
	 * @return
	 */
	private Application getApplication(Long applicationId){
		Application application = null;
		try {
			application = applicationDao.query(applicationId);
		} catch (Exception e) {
			logger.error("获取应用信息异常", e);
		}
		return application;
	}
	
	/**
	 * 获取组件部署模板资源
	 * @return
	 */
	private Template template(String fileDir, String fileName){
		FileResourceLoader resourceLoader = new FileResourceLoader(fileDir, "utf-8");
		GroupTemplate groupTemplate = null;
		try {
			groupTemplate = new GroupTemplate(resourceLoader, Configuration.defaultConfiguration());
		} catch (IOException e) {
			logger.error("create GroupTemplate failed.", e);
			return null;
		}
		return groupTemplate.getTemplate(fileName);
	}

}
