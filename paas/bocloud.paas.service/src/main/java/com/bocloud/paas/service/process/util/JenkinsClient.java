package com.bocloud.paas.service.process.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.Result;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.util.ExtendHttpClient;
import com.bocloud.paas.common.util.FileUtil;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;

public class JenkinsClient {

	private static final Logger LOGGER = Logger.getLogger(JenkinsClient.class);

	private JenkinsServer jenkinsServer = null;
	private final String url;
	private final String username;
	private final String password;

	public JenkinsClient(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		try {
			URI uri = new URI(url);
			jenkinsServer = new JenkinsServer(uri, username, password);
		} catch (URISyntaxException e) {
			LOGGER.error("init URI exception", e);
		}
	}

	/**
	 * 按照job名称获取job
	 * 
	 * @param jobName
	 * @return
	 */
	public Job getJob(String name) {
		try {
			return jenkinsServer.getJob(name);
		} catch (IOException e) {
			LOGGER.error("get jenkins job exception", e);
			return null;
		}
	}

	public boolean existed(String name) {
		return null != getJob(name) ? true : false;
	}

	/**
	 * 获取所有job
	 * 
	 * @return
	 */
	public Map<String, Job> listJobs() {
		try {
			return jenkinsServer.getJobs();
		} catch (Exception e) {
			LOGGER.error("get jenkins Jobs exception", e);
			return null;
		}
	}

	/**
	 * 创建job
	 * 
	 * @param filePath
	 *            模版文件地址
	 * @param fileName
	 *            模版文件名称
	 * @param jobName
	 *            job名称
	 * @param params
	 *            模版参数
	 * @return
	 */
	public Boolean createJob(String filePath, String fileName, String jobName, Map<String, Object> params) {
		Job job = getJob(jobName);
		if (null != job) {
			LOGGER.warn("job is existed, faile to create");
			return false;
		}
		try {
			Object object = JSONArray.toJSON(params);
			File file = FileUtil.templateToExecute(filePath, fileName, JSONObject.parseObject(object.toString()),
					jobName + ".xml");
			String fileContent = FileUtil.readFile(file.getPath());
			if (StringUtils.hasText(fileContent)) {
				jenkinsServer.createJob(jobName, fileContent);
				return true;
			}

		} catch (IOException e) {
			LOGGER.error("create job exception", e);
		}
		return false;
	}
	
	/**
	 * 修改job
	 * 
	 * @param filePath
	 *            模版文件地址
	 * @param fileName
	 *            模版文件名称
	 * @param jobName
	 *            job名称
	 * @param params
	 *            模版参数
	 * @return
	 */
	public Boolean updateJob(String filePath, String fileName, String jobName, Map<String, Object> params) {
		Job job = getJob(jobName);
		if (null == job) {
			LOGGER.warn("job not existed, faile to update");
			return false;
		}
		try {
			Object object = JSONArray.toJSON(params);
			File file = FileUtil.templateToExecute(filePath, fileName, JSONObject.parseObject(object.toString()),
					jobName + ".xml");
			String fileContent = FileUtil.readFile(file.getPath());
			if (StringUtils.hasText(fileContent)) {
				jenkinsServer.updateJob(jobName, fileContent);
				return true;
			}

		} catch (IOException e) {
			LOGGER.error("update job exception", e);
		}
		return false;
	}

	/**
	 * 按照job名称删除job
	 * 
	 * @param jobName
	 */
	public Boolean deleteJob(String name) {
		try {
			jenkinsServer.deleteJob(name);
			return true;
		} catch (IOException e) {
			LOGGER.error("delete job exception", e);
			return false;
		}
	}

	/**
	 * 按照job名称构建job
	 * 
	 * @param jobName
	 * @return
	 */
	public boolean buildJob(String name, Map<String, String> params) {
		try {
			if (null == params) {
				getJob(name).build();
			} else {
				getJob(name).build(params);
			}
			return getJobLastBuildOutput(name).isSuccess();
		} catch (Exception e) {
			LOGGER.error("build job exception", e);
			return false;
		}
	}

	/**
	 * 按照job名称获取job最后一次构建日志
	 * 
	 * @param jobName
	 * @return
	 */
	public Result getJobLastBuildOutput(String jobName) {
		Result result = new Result(false, "");
		try {
			Job job = getJob(jobName);
			if (null == job) {
				result.setMessage("job [" + jobName + "] not find... ");
				LOGGER.error(result.getMessage());
				return result;
			}
			// 是否等待
			boolean isInQueue = job.details().isInQueue();
			if (job.details().getAllBuilds().isEmpty() && !isInQueue) {
				result.setMessage("job [" + jobName + "] not build record...");
				LOGGER.warn(result.getMessage());
				return result;
			}
			long currentTime = System.currentTimeMillis();
			while (isInQueue) {
				isInQueue = job.details().isInQueue();
				if (isInQueue) {
					Thread.sleep(2 * 1000);
				}
			}
			BuildWithDetails buildDetails = job.details().getLastBuild().details();
			// 是否build完成
			boolean isBuildComplete = null != buildDetails.getResult();
			while (!isBuildComplete) {
				buildDetails = job.details().getLastBuild().details();
				isBuildComplete = null != buildDetails.getResult();
				if (!isBuildComplete) {
					Thread.sleep(2 * 1000);
				}
			}
			// 上面的判断有可能没有执行完，再次判断构建是否结束
			boolean isBuildEnd = buildDetails.getConsoleOutputText()
					.substring(buildDetails.getConsoleOutputText().indexOf("\n")).contains("Finished:");
			while (!isBuildEnd) {
				isBuildEnd = buildDetails.getConsoleOutputText()
						.substring(buildDetails.getConsoleOutputText().indexOf("\n")).contains("Finished:");
				if (!isBuildEnd) {
					Thread.sleep(2 * 1000);
				}
			}
			LOGGER.info(
					"job [" + jobName + "] build time for " + (System.currentTimeMillis() - currentTime) / 1000 + "s.");
			result.setMessage(buildDetails.getConsoleOutputText());
			result.setSuccess((buildDetails.getResult().toString()).equals("SUCCESS"));
			LOGGER.info(result.getMessage());
		} catch (Exception e) {
			result.setMessage("get job [" + jobName + "] log error: \n" + e);
			LOGGER.error(result.getMessage());
		}
		return result;
	}

	/**
	 * 根据job名称获取所有构建记录
	 * 
	 * @param jobName
	 * @return
	 */
	public List<Build> getAllBuilds(String name) {
		try {
			JobWithDetails jobDetails = jenkinsServer.getJob(name);
			if (null == jobDetails) {
				LOGGER.warn("get job with details failed");
				return null;
			}
			List<Build> builds = jobDetails.getAllBuilds();
			return builds;
		} catch (Exception e) {
			LOGGER.error("get all job builds exception", e);
			return null;
		}
	}

	/**
	 * 根据job名称和构建的第几次获取构建的输出
	 * 
	 * @param jobName
	 * @param buildNum
	 * @return
	 */
	public String getBuildOutput(String name, int version) {
		try {
			JobWithDetails job = jenkinsServer.getJob(name);
			String output = job.getBuildByNumber(version).details().getConsoleOutputText();
			return output;
		} catch (Exception e) {
			LOGGER.error("get build version output exception", e);
			return "";
		}
	}

	/**
	 * 添加jenkins认证
	 * @param id
	 * @param username
	 * @param password
	 * @param description
	 * @return
	 */
	public Result createCredentials(String scope, String id, String username, String password, String description) {
		Result result = new Result(); 
		try {
			URI uri = new URI(this.url + "/credentials/store/system/domain/_/createCredentials");
			ExtendHttpClient extendHttpClient = new ExtendHttpClient();
			Map<String, Object> headers = MapTools.simpleMap("Content-Type", "application/x-www-form-urlencoded");

			Map<String, Object> jsonParams = MapTools.simpleMap("", "0");
			Map<String, Object> credentials = MapTools.simpleMap("scope", scope);
			credentials.put("id", id);
			credentials.put("username", username);
			credentials.put("password", password);
			credentials.put("description", description);
			credentials.put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
			jsonParams.put("credentials", JSONArray.toJSON(credentials));
			
			Map<String, Object> params = MapTools.simpleMap("json", JSONArray.toJSON(jsonParams));
			result = extendHttpClient.post(headers, params, uri, this.username, this.password);
		} catch (URISyntaxException e) {
			LOGGER.error("jenkins url format fail: \n", e); 
			result.setMessage("jenkins地址格式错误！");
		}
		return result;
	}
	

}
