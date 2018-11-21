package com.bocloud.paas.service.repository.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bocloud.common.model.Result;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.util.ExtendHttpClient;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RemovedImage;

/**
 * docker镜像操作类
 * 
 * @author zjm
 * @date 2017年3月17日
 */
public class ImageUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);
	private static final String DOCKER_PORT = "2375";

	/**
	 * 获取docker客户端
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param ip
	 *            docker服务器ip
	 * @param port
	 *            docker服务器端口（默认2375）
	 * @return
	 */
	private static DockerClient getDockerClient(String ip, String port) {
		return DefaultDockerClient.builder().uri("http://" + ip + ":" + port).build();
	}

	/**
	 * load镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param file
	 *            镜像文件
	 * @param registryIp
	 *            本地服务器地址
	 * @return
	 */
	public static Result load(File file, String registryIp) {
		ExtendHttpClient httpClient = new ExtendHttpClient();
		httpClient.setTimeout(10 * 60 * 1000);//大镜像得导入，设置超时时间为10分钟
		Map<String, Object> headers = MapTools.simpleMap("Content-Type","application/x-tar");
		headers.put("Keep-Alive", "10");
		String url = "http://" + registryIp + ":" + DOCKER_PORT + "/v1.24/images/load";
		FileEntity entity = new FileEntity(file, ContentType.DEFAULT_BINARY);
		return httpClient.post(headers, null, url, entity);
	}
	
	/**
	 * 打标镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param imageName
	 *            镜像名称
	 * @param imageNewName
	 *            镜像新名称
	 * @param registryIp
	 *            本地服务器地址
	 * @return
	 */
	public static Boolean tag(String imageName, String imageNewName, String registryIp) {
		DockerClient client = getDockerClient(registryIp, DOCKER_PORT);
		try {
			client.tag(imageName.trim(), imageNewName.trim());
		} catch (DockerException e) {
			logger.error("[" + imageName + "]镜像打标为[" + imageNewName + "]异常！", e);
			return false;
		} catch (InterruptedException e) {
			logger.error("[" + imageName + "]镜像打标为[" + imageNewName + "]异常！", e);
			return false;
		}
		logger.info("[" + imageName + "]镜像打标为[" + imageNewName + "]成功！");
		return true;
	}

	/**
	 * 推送镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param imageName
	 *            镜像名称
	 * @param registryIp
	 *            本地服务器地址
	 * @return
	 */
	public static Boolean push(String imageName, String registryIp) {
		DockerClient client = getDockerClient(registryIp, DOCKER_PORT);
		try {
			client.push(imageName);
		} catch (DockerException e) {
			logger.error("镜像[" + imageName + "]推送异常！", e);
			return false;
		} catch (InterruptedException e) {
			logger.error("镜像[" + imageName + "]推送异常！", e);
			return false;
		}
		logger.info("镜像[" + imageName + "]推送成功！");
		return true;
	}
	
	public static Boolean push(String image, String registryIp, String username, String password) {
		DockerClient client = getDockerClient(registryIp, DOCKER_PORT);
		RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(registryIp).username(username).password(password).build();
		try {
			client.push(image, registryAuth);
		} catch (DockerException e) {
			logger.error("镜像[" + image + "]推送异常！", e);
			return false;
		} catch (InterruptedException e) {
			logger.error("镜像[" + image + "]推送异常！", e);
			return false;
		}
		logger.info("镜像[" + image + "]推送成功！");
		return true;
	}

	/**
	 * 删除镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param imageName
	 *            镜像名称
	 * @param registryIp
	 *            仓库ip
	 * @return
	 */
	public static boolean remove(String imageName, String registryIp) {
		DockerClient client = getDockerClient(registryIp, DOCKER_PORT);
		List<RemovedImage> images = null;
		try {
			images = client.removeImage(imageName);
		} catch (DockerException e) {
			logger.error("删除镜像[" + imageName + "]信息异常！", e);
			return false;
		} catch (InterruptedException e) {
			logger.error("删除镜像[" + imageName + "]信息异常！", e);
			return false;
		}
		if (null == images || images.size() < 0) {
			logger.error("删除镜像[" + imageName + "]信息异常！");
			return false;
		}
		logger.info("删除镜像[" + imageName + "]镜像成功！");
		return true;
	}
	
}
