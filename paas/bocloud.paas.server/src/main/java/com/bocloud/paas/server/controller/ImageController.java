package com.bocloud.paas.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Image;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.repository.ImageService;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@RestController
@RequestMapping("/image")
public class ImageController {
	@Autowired
	private ImageService imageService;

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "镜像列表查询")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return imageService.list(page, rows, paramList, sorterMap, simple, user, "");
	}

	@RequestMapping(value = "/listSelectName", method = { RequestMethod.GET })
	@Log(name = "镜像名称下拉框查询")
	public BsmResult listSelectName(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Param> paramList = new ArrayList<Param>();
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(jsonObject.getString("sorter"), HashMap.class);
		return imageService.list(1, 1, paramList, sorterMap, Boolean.parseBoolean(jsonObject.getString("simple")), user,
				"name");
	}

	@RequestMapping(value = "/listSelectTag", method = { RequestMethod.GET })
	@Log(name = "镜像下标下拉框查询")
	public BsmResult listSelectTag(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Param> paramList = new ArrayList<Param>();
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(jsonObject.getString("sorter"), HashMap.class);
		return imageService.list(1, 1, paramList, sorterMap, Boolean.parseBoolean(jsonObject.getString("simple")), user,
				"tag");
	}

	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建镜像")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			Image image = objectToImage(object);
			return imageService.create(user, image);
		}
		return ResultTools.formatErrResult();
	}

	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "镜像信息修改")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			Image image = objectToImage(object);
			return imageService.modify(image, user.getId());
		}
		return ResultTools.formatErrResult();
	}

	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "本地端镜像详情信息")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return imageService.detail(id);
	}

	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "删除镜像")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = jsonToObject(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("ids").toString(), Long.class);
		return imageService.remove(ids, user.getId());
	}

	@RequestMapping(value = "/load", method = { RequestMethod.POST })
	@Log(name = "镜像导入")
	public BsmResult load(@RequestParam(value = "registryId", required = true) Long registryId,
			@RequestParam(value = "filePath", required = true) String filePath,
			@RequestParam(value = "project", required = false) String project,
			@Value(Common.REQ_USER) RequestUser user) {
		return imageService.load(user, registryId, filePath, project);
	}

	@RequestMapping(value = "/inspectImage", method = { RequestMethod.POST })
	@Log(name = "服务端镜像详情信息")
	public BsmResult inspectImage(@RequestParam(value = Common.PARAMS, required = true) String params,
			@RequestParam(value = "imageId", required = true) Long imageId) {
		return imageService.inspect(imageId);
	}

	@RequestMapping(value = "/countImage", method = { RequestMethod.POST })
	@Log(name = "镜像数据统计")
	public BsmResult countImage(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		return imageService.count(user);
	}

	@RequestMapping(value = "/authorize", method = { RequestMethod.POST })
	@Log(name = "镜像权限")
	public BsmResult authorize(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		return imageService.authorize(object.getLong("imageId"), user.getId(), object.getLong("deptId"));
	}

	/**
	 * 源码构建
	 * 
	 * @param baseImage
	 *            基础镜像
	 * @param pomPath
	 *            pom文件位置
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
	 * @param registryId
	 *            仓库id
	 * @return
	 */
	@RequestMapping(value = "/buildBySource", method = { RequestMethod.GET })
	@Log(name = "源码构建")
	public BsmResult buildBySource(@RequestParam(required = true) String baseImage,
			@RequestParam(required = true) String pomPath, @RequestParam(required = true) String repositoryUrl,
			@RequestParam(required = true) String repositoryBranch,
			@RequestParam(required = false) String repositoryUsername,
			@RequestParam(required = false) String repositoryPassword, @RequestParam(required = true) String warName,
			@RequestParam(required = true) String newImageName, @RequestParam(required = true) Long registryId,
			@RequestParam(required = false) String project,
			@Value(Common.REQ_USER) RequestUser user) {
		return imageService.buildBySource(baseImage, pomPath, repositoryUrl, project, repositoryBranch, repositoryUsername,
				repositoryPassword, warName, newImageName, registryId, user);
	}

	@RequestMapping(value = "/getProjects", method = { RequestMethod.GET })
	@Log(name = "获取仓库项目")
	public BsmResult getProjects(@RequestParam(required = true) Long registryId,
			@Value(Common.REQ_USER) RequestUser user) {
		return imageService.getProjects(registryId);
	}
	
	/**
	 * json字符串转换为json对象
	 * 
	 * @param json
	 * @return
	 */
	private final JSONObject jsonToObject(String json) {
		return JSONTools.isJSONObj(json);
	}

	/**
	 * json对象转换为软件对象
	 * 
	 * @param obj
	 * @return
	 */
	private final Image objectToImage(JSONObject obj) {
		return JSONObject.parseObject(obj.toJSONString(), Image.class);
	}

}
