package com.bocloud.paas.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
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
@RequestMapping("/git")
public class GitController {
	private final String BASE_SERVICE = "/git";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;

	@RequestMapping(value = "/branches", method = { RequestMethod.GET })
	public BsmResult getBranches(@RequestParam(required = false) String repositoryUrl,
			@RequestParam(required = true) String username, @RequestParam(required = false) String password,
			HttpServletRequest request) {
		if (StringUtils.isBlank(repositoryUrl) || repositoryUrl.length() < 8) {
			return ResultTools.formatErrResult();
		}
		String requestUrl = BASE_SERVICE + "/branches";
		Map<String, Object> paramMap = MapTools.simpleMap("repositoryUrl", repositoryUrl);
		paramMap.put("username", username);
		paramMap.put("password", password);
		RemoteService service = serviceFactory.safeBuild(SERVICE, requestUrl, BoCloudMethod.BASIC, null, paramMap,
				request);
		return service.invoke();
	}

}
