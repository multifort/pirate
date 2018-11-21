package com.bocloud.paas.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@RestController
@RequestMapping("/image")
public class ImageController {
	private final String BASE_SERVICE = "/image";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;

	@RequestMapping(value = "/listSelectName", method = { RequestMethod.GET })
	public BsmResult listSelectName(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/listSelectName";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/listSelectTag", method = { RequestMethod.GET })
	public BsmResult listSelectTag(@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/listSelectTag";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 创建
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, ImageController.class.getSimpleName());
	}

	/**
	 * 更新
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, ImageController.class.getSimpleName());
	}

	/**
	 * 列表展示
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROW, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				ImageController.class.getSimpleName());
	}

	/**
	 * 删除
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/remove";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, ImageController.class.getSimpleName());
	}

	/**
	 * 导入镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/load", method = { RequestMethod.POST })
	public BsmResult load(@RequestParam(value = "registryId", required = true) Long registryId,
			@RequestParam(value = "filePath", required = true) String filePath,
			@RequestParam(value = "project", required = false) String project, HttpServletRequest request) {
		String url = BASE_SERVICE + "/load";
		Map<String, Object> paramMap = MapTools.simpleMap("registryId", registryId);
		paramMap.put("filePath", filePath);
		if (!StringUtils.isEmpty(project)) {
			paramMap.put("project", project);
		}
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 获取镜像信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inspectImage", method = { RequestMethod.POST })
	public BsmResult inspect(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/inspectImage";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			paramMap.put("imageId", jsonObject.get("imageId"));
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			BsmResult result = service.invoke();
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 统计镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/countImage", method = { RequestMethod.POST })
	public BsmResult countImage(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/countImage";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 镜像部署
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deployImage", method = { RequestMethod.POST })
	public BsmResult deployImage(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/deployImage";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/getByappId", method = { RequestMethod.POST })
	public BsmResult getByappId(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/getByappId";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/authorize", method = { RequestMethod.POST })
	public BsmResult authorize(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/authorize";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/space", method = { RequestMethod.POST })
	public BsmResult nameWithSpace(@RequestParam(value = Common.SORTER, required = false) String sorter,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/space";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, null, request);
		return service.invoke();
	}

	/**
	 * 源码构建
	 * 
	 * @param baseImage
	 *            基础镜像
	 * @param repositoryUrl
	 *            源码地址
	 * @param repositoryBranch
	 *            源码分支
	 * @param repositoryUsername
	 *            源码账户
	 * @param repositoryPassword
	 *            源码密码
	 * @param warName
	 *            源码包名
	 * @param newImage
	 *            新镜像名
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/buildBySource", method = { RequestMethod.GET })
	public BsmResult buildBySource(@RequestParam(required = true) String baseImage,
			@RequestParam(required = true) String pomPath, @RequestParam(required = true) String repositoryUrl,
			@RequestParam(required = true) String repositoryBranch,
			@RequestParam(required = false) String repositoryUsername,
			@RequestParam(required = false) String repositoryPassword, @RequestParam(required = true) String warName,
			@RequestParam(required = true) String newImageName, @RequestParam(required = true) Integer registryId,
			@RequestParam(required = false) String project,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/buildBySource";
		Map<String, Object> paramMap = MapTools.simpleMap("baseImage", baseImage);
		paramMap.put("pomPath", pomPath);
		paramMap.put("repositoryUrl", repositoryUrl);
		paramMap.put("repositoryBranch", repositoryBranch);
		paramMap.put("repositoryUsername", repositoryUsername);
		paramMap.put("repositoryPassword", repositoryPassword);
		paramMap.put("warName", warName);
		paramMap.put("newImageName", newImageName);
		paramMap.put("registryId", registryId);
		paramMap.put("project", project);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}

	@RequestMapping(value = "/getProjects", method = { RequestMethod.GET })
	public BsmResult getProjects(@RequestParam(required = true) Integer registryId, HttpServletRequest request) {
		String url = BASE_SERVICE + "/getProjects";
		Map<String, Object> paramMap = MapTools.simpleMap("registryId", registryId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	
}
