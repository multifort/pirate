package com.bocloud.paas.service.application.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.*;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.dao.application.LayoutTemplateVersionDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Layout;
import com.bocloud.paas.entity.LayoutTemplateVersion;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.app.resource.CommonServiceImpl;
import com.bocloud.paas.service.application.LayoutTemplateVersionService;
import com.bocloud.paas.service.exception.BoCloudServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service("layoutTemplateVersionService")
public class LayoutTemplateVersionServiceImpl extends CommonServiceImpl implements LayoutTemplateVersionService {

	private static final Logger logger = LoggerFactory.getLogger(LayoutTemplateVersionServiceImpl.class);

	@Autowired
	private LayoutTemplateVersionDao layoutTemplateVersionDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private LockFactory lockFactory;

	@Value("${local.storage.path}")
	private String localStoragePath;


	public String checkVersion(Long templateId, String version) {
	    String message = "";
        try {
            if (null == version || "".equals(version)) {
                message = "版本号不能为空";
                return message;
            }
            LayoutTemplateVersion templateVersion = layoutTemplateVersionDao.getTemplateVersion(version,templateId);
            if (templateVersion != null) {
                message = "编排模板版本名称已经存在";
            }
            //TODO 校验版本号规则，规则确定
        } catch (Exception e) {
           logger.error("check TemplateVersion service",e);
           message = "版本号查询异常";
        }
        return message;
    }

    /**
     * 构建文件流对象，为后续提取参数及内容准备
     * @return
     */
    private File buildFile(String version, String content) {
        File file = null;

        String fileDir = localStoragePath + File.separatorChar
                + DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS") + "-" +version;
        logger.debug("buildFile fileDir ["+ fileDir +"]");
        String filePath = fileDir + File.separatorChar + version + ".yaml";
        logger.debug("buildFile filePath ["+ filePath +"]");
        BsmResult result = FileUtil.createFile(filePath, content);
        if (result.isSuccess()) {
            file = new File(filePath);
        } else {
            // 创建失败需要回滚，将垃圾文件全部删除
            rollBack4Create(fileDir);
        }
        return file;
    }

    private void rollBack4Create(String fileDir) {
        //删除生成的文件目录，校验不能等于BasePath
        if (localStoragePath.equals(fileDir)){
            return;
        }
        logger.debug("rollBack4Creat fileDir [" + fileDir + "]");
        File file = new File(fileDir);
        FileUtil.deleteDirectory(file);
    }

	/**
	 * 根据传输文件流解析参数列表
	 * @param file
	 * @return
	 */
	private List buildParameters(File file) throws BoCloudServiceException {
	    List<String> parametersList = new ArrayList<>();
		String fileDescContent = FileUtil.readParameters(file);
		if(!"".equals(fileDescContent)){
			JSONObject jsonObject = JSONObject.parseObject(fileDescContent);
			String parameters = jsonObject.getString("parameters");
			JSONArray jsonArray = JSONObject.parseArray(parameters);
			if(jsonArray.size()>0){
				for(int i=0;i<jsonArray.size();i++){
					JSONObject job = jsonArray.getJSONObject(i);
					String name = String.valueOf(job.get("name"));
					if (parametersList.contains(name)) {
					    throw new BoCloudServiceException("应用模板中不允许出现相同参数名，请检查！");
	                }
	                parametersList.add(name);
				}
			}
			return parametersList;
		}else{
			throw new BoCloudServiceException("应用模板中参数与提供参数列表不匹配，请检查！");
		}
		
	}

    /**
     * 从模板内容中获取到使用的参数列表
     * @return
     */
	private List getParametersByContent(File file) throws BoCloudServiceException{

	    List<String> analysisParameters = new ArrayList<>();
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(new FileInputStream(file), "UTF-8");
            bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if(lineTxt.contains("${")){
                    String key = lineTxt.substring(lineTxt.indexOf("${")+2,lineTxt.indexOf("}"));
                    //考虑key重复的问题，需要校验key是否已经存在，如果存在则抛出异常
                    //警告用户模板文件中不能出现重复key
                   /* if (analysisParameters.contains(key)) {
                        throw new BoCloudServiceException("应用模板中不允许出现相同参数名，请检查！");
                    }
                    // 排除for循环中子项的存在
                    if (!key.contains(".")) {
                        analysisParameters.add(key);
                    }*/
                    if (analysisParameters.contains(key)) {
                        // nothing to do
                    }else {
                    	if(!key.contains(".")) {
                        // 排除for循环中子项的存在
                        analysisParameters.add(key);
                     }
                   }
                }
                if(lineTxt.contains("for") && lineTxt.contains("in")) {
                    String key = lineTxt.substring(lineTxt.indexOf("in")+3,lineTxt.indexOf(")"));
                    analysisParameters.add(key.trim());
                }
            }
        } catch (IOException e) {
            logger.error("getParametersByContent", e);
            throw new BoCloudServiceException("解析应用模板中参数异常！");
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
                if (null != read) {
                    read.close();
                }
            } catch (IOException e) {
                logger.error("getParametersByContent", e);
                throw new BoCloudServiceException("解析应用模板中参数异常！");
            }

        }
        return analysisParameters;
    }
	@Override
	public BsmResult create(LayoutTemplateVersion layoutTemplateVersion, RequestUser user) {
		
		BsmResult bsmResult = new BsmResult(false,"");
        File file = null;
        try {
            //校验版本号
            String checkResult = checkVersion(layoutTemplateVersion.getLayoutTemplateId(),layoutTemplateVersion.getVersion());
			if (!"".equals(checkResult)) {
			    throw new BoCloudServiceException(checkResult);
			}
			//生成文件
			file = buildFile(layoutTemplateVersion.getVersion(),layoutTemplateVersion.getFileContent());
			if (file == null) {
			    throw new BoCloudServiceException("新建应用模板版本中生成临时文件异常！");
            }
			// 构建参数列表并校验参数
			List<String> parameters = buildParameters(file);
			List<String> analysisParameters = getParametersByContent(file);
			analysisParameters.removeAll(parameters);

			if (analysisParameters.size() > 0 ) {
			    throw new BoCloudServiceException("应用模板中参数与提供参数列表不匹配，请检查！");
            }
            // 插入操作
            layoutTemplateVersion.setStatus(BaseStatus.NORMAL.name());
            layoutTemplateVersion.setTemplateFilePath(file.getPath());
            layoutTemplateVersion.setProps(String.valueOf(1));
            layoutTemplateVersionDao.save(layoutTemplateVersion);
            bsmResult.setSuccess(true);
            bsmResult.setData(layoutTemplateVersion);
            bsmResult.setMessage("添加编排模板版本成功！");

		} catch (BoCloudServiceException e) {
            logger.error("create new LayoutTeamplateVersion exception", e);
            if (null != file) {
                rollBack4Create(file.getAbsolutePath());
            }
            bsmResult.setMessage(e.getMessage());
        } catch (Exception e) {
			logger.error("create new LayoutTeamplateVersion exception", e);
            if (null != file) {
                rollBack4Create(file.getAbsolutePath());
            }
			bsmResult.setMessage("新增应用版本版本失败");
		}
        return bsmResult;
    }

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple) {
		
		GridBean gridBean = null;
		if (null == params) {
			params = Lists.newArrayList();
		}
		if (null == sorter) {
			sorter = Maps.newHashMap();
		}
		sorter.put("gmtCreate", Common.ONE);
		try {
			int total = layoutTemplateVersionDao.count(params);
			if (simple) {
				List<SimpleBean> beans = layoutTemplateVersionDao.list(params, sorter);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<LayoutTemplateVersion> layoutTemplateVersions = layoutTemplateVersionDao.list(page, rows, params, sorter);
				if (layoutTemplateVersions != null && !layoutTemplateVersions.isEmpty()) {
					for (LayoutTemplateVersion layoutTemplateVersion : layoutTemplateVersions) {
						// 获取用户信息
						User creator = userDao.query(layoutTemplateVersion.getCreaterId());
						layoutTemplateVersion.setCreatorName(creator.getName());
						// 获取修改者信息
						User mender = userDao.query(layoutTemplateVersion.getMenderId());
						layoutTemplateVersion.setMenderName(mender.getName());
						// 读取版本的内容信息
						String path = layoutTemplateVersion.getTemplateFilePath();
						String fileContent = FileUtil.readFile(path);
						layoutTemplateVersion.setFileContent(fileContent);
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, layoutTemplateVersions);
			}
			return new BsmResult(true, gridBean, "获取编排模板版本成功");
		} catch (Exception e) {
			logger.error("Query layoutTemplateVersion list Exception:", e);
			return new BsmResult(false, "获取编排模板版本异常");
		}
	}

	@Override
	public BsmResult modify(LayoutTemplateVersion bean, Long userId) {
		
		String path = LayoutTemplateVersion.class.getSimpleName() + "_" + bean.getId();
		HarmonyLock lock = null;
		lock = lockFactory.getLock(path);
		if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
			logger.warn("Get harmonyLock time out!");
			return new BsmResult(false, "请求超时");
		}
		
		try {
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(bean.getId());
			if (null == layoutTemplateVersion) {
				logger.warn("layoutTemplateVersion does not exist!");
				return new BsmResult(false, "编排模板版本不存在！");
			}
			BsmResult bsmResult = new BsmResult();
			/*if (bean.getFileContent() != null) {
				bsmResult = FileUtil.createFile(layoutTemplateVersion.getFilePath() + File.separatorChar + layout.getFileName(),
						bean.getFileContent());
			}*/
			if (!bsmResult.isSuccess()) {
				/*layoutTemplateVersion.setVersion(bean.getVersion());
				layoutTemplateVersion.setRemark(bean.getRemark());
				layoutTemplateVersion.setProps(bean.getProps());
				layoutTemplateVersion.setMenderId(userId);*/
				bean.setMenderId(userId);
				bean.setGmtModify(new Date());
				this.layoutTemplateVersionDao.update(bean);
			}
			return new BsmResult(true, "编排模板版本修改成功！");
		} catch (Exception e) {
			logger.error("Modify layoutTemplateVersion exception:", e);
			return new BsmResult(false, "编排模板版本修改异常！");
		} finally {
			lock.release();
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		
		String path = Layout.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时");
			}
			LayoutTemplateVersion version = layoutTemplateVersionDao.query(id);
			if (null == version) {
				logger.warn("layoutTemplateVersion does not exist!");
				return new BsmResult(false, "编排模板版本不存在！");
			}
			if (null != version.getTemplateFilePath()) {
                String fileDir = version.getTemplateFilePath().substring(0, version.getTemplateFilePath().lastIndexOf(File.separatorChar));
                rollBack4Create(fileDir);
            }
			// 判断是否可以删除
			boolean result = layoutTemplateVersionDao.remove(id, userId);
			if(result){
				return new BsmResult(true, "编排模板删除成功！");
			}else{
				return new BsmResult(false, "编排模板删除失败！");
			}
		} catch (Exception e) {
			logger.error("Remove layoutTemplateVersion exception:", e);
			return new BsmResult(false, "编排模板版本删除异常！");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult detail(Long id) {
		
		try {
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(id);
			if (null == layoutTemplateVersion) {
				return new BsmResult(false, "编排模板版本不存在！");
			}
			// 获取用户信息
			User creator = userDao.query(layoutTemplateVersion.getCreaterId());
			layoutTemplateVersion.setCreatorName(creator.getName());
			// 获取修改者信息
			User mender = userDao.query(layoutTemplateVersion.getMenderId());
			layoutTemplateVersion.setMenderName(mender.getName());
			// 更新模板版本的path读取文件内容
			/*String fileContent = FileUtil.readFile(layoutTemplateVersion.getTemplateFilePath());
			if (StringUtils.hasText(fileContent)) {
				layoutTemplateVersion.setFileContent(fileContent);
			}*/
			// 获取文件里面的内容
			File file = new File(layoutTemplateVersion.getTemplateFilePath());
			String fileTemplateContent = FileUtil.readTemplate(file);
			// 模板内容
			if (StringUtils.hasText(fileTemplateContent)) {
				layoutTemplateVersion.setFileTemplateContent(fileTemplateContent);
			}
			// 描述内容
			String fileDescContent = FileUtil.readParameters(file);
			if (StringUtils.hasText(fileDescContent)) {
				layoutTemplateVersion.setFileDescContent(fileDescContent);
			}
			return new BsmResult(true, layoutTemplateVersion, "获取编排模板版本详情成功");
		} catch (Exception e) {
			logger.error("Get layoutTemplateVersion exception：", e);
			return new BsmResult(false, "获取编排模板版本详情异常！");
		}
	}

	@Override
	public BsmResult getVarsById(Long id) {

		try {
			// 查询编排模板版本
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(id);
			String path = layoutTemplateVersion.getTemplateFilePath();
			if (null == path || "".equals(path)) {
				return new BsmResult(false, "获取编排模板版本路径出错");
			}
			String template = null;
			File templateFile = null;
			// 根据path读取文件内容
			templateFile = new File(path);
			template = FileUtil.readParameters(templateFile);
				
			if (null != template) {
				return new BsmResult(true, JSONObject.parseObject(template), "获取编排模板版本参数成功");
			}
			return new BsmResult(false, "获取编排模板版本参数失败");
		} catch (Exception e) {
			logger.error("获取编排模板版本参数失败", e);
			return new BsmResult(false, "获取失败：获取编排模板版本参数时发生异常");
		}
	}
	@Override
	public BsmResult getTemplate(Long id) {

		try {
			// 查询编排模板版本
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(id);
			String path = layoutTemplateVersion.getTemplateFilePath();
			if (null == path || "".equals(path)) {
				return new BsmResult(false, "获取编排模板版本路径出错");
			}
			// 根据path读取文件内容
			String template = null;
			template = FileUtil.readFile(path);
				
			if (null != template) {
				return new BsmResult(true, JSONObject.parseObject(template), "获取编排模板版本内容成功");
			}
			return new BsmResult(false, "获取编排模板版本内容失败");
		} catch (Exception e) {
			logger.error("获取编排模板版本内容失败", e);
			return new BsmResult(false, "获取失败：获取编排模板版本内容时发生异常");
		}
	}

	@Override
	public BsmResult upgrade(Long id,Long userId) {
		
		try {
			// 查询编排模板版本
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(id);
			String path = layoutTemplateVersion.getTemplateFilePath();
			if (null == path || "".equals(path)) {
				return new BsmResult(false, "获取编排模板版本路径出错");
			}
			// 根据path读取文件内容
			String template = null;
			template = FileUtil.readFile(path);
				
			if (null != template) {
				return new BsmResult(true, template, "获取编排模板版本内容成功");
			}
			return new BsmResult(false, "获取编排模板版本内容失败");
		} catch (Exception e) {
			logger.error("获取编排模板版本内容失败", e);
			return new BsmResult(false, "获取失败：获取编排模板版本内容时发生异常");
		}
	}


	@Override
	public BsmResult instantiation(Long id,String params,Long userId) {
		
		File tempPath = null;
		String tempContext = null;
				
		try {
			// 获取编排模板版本
			LayoutTemplateVersion layoutTemplateVersion = layoutTemplateVersionDao.query(id);
			String path = layoutTemplateVersion.getTemplateFilePath();
			if (null == path || "".equals(path)) {
				return new BsmResult(false, "获取编排模板版本路径出错");
			}
			// 截取模板,获取页面传入的参数值，生成可执行文件
			File file = new File(path);
			String filePath = file.getParentFile().getPath();
			String fileName = file.getName();
			tempPath = FileUtil.templateToExecuteFile(filePath, fileName, JSONTools.isJSONObj(params), "temp.yaml");
			tempContext = FileUtil.readFile(tempPath.toString());
			
			return new BsmResult(true,tempContext,"文件内容实例化成功");
		} catch (Exception e) {
			logger.error("文件内容实例化失败", e);
			return new BsmResult(false, "获取失败：文件内容实例化时发生异常");
		}/*finally{
			// 删除临时文件
			if(null != tempContext && "" != tempContext){
				FileUtil.deleteDirectory(tempPath);
			}
		}*/
	}

	/*@Override
	public BsmResult templatable(Long id, Long userId) {
		
		try {
			//1、根据应用实例的id查询出对应的编排文件（json格式）
			// 先写死代替
			String url = "http://192.168.1.75:8080";
			Config config = new ConfigBuilder().withMasterUrl(url).build();
			DefaultKubernetesClient kubeClient = new DefaultKubernetesClient(config);
			io.fabric8.kubernetes.api.model.Service service = kubeClient.services().inNamespace("application-yu").withName("tr").get();
			String jsonString = JSONObject.toJSONString(service);
			FileWriter writer = new FileWriter("C:\\Temp\\testTemplate.txt");
			writer.write(jsonString);
			writer.close();
			
			String path = "C:\\Temp\\testTemplate.txt";
	    	String jsonString2 = FileUtil.readFile(path);
			//2、jsonToYaml
			JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString2);
			String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
			//3、返回给前端
			return new BsmResult(true,jsonAsYaml,"应用实例模板化查询文件内容成功");
		} catch (Exception e) {
			logger.error("应用实例模板化查询文件内容失败", e);
			return new BsmResult(false, "获取失败：应用实例模板化查询文件内容时发生异常");
		}
	}*/

}
