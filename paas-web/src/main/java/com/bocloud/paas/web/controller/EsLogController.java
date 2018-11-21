package com.bocloud.paas.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping(value = "/eslog")
public class EsLogController {
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	private static Logger logger = LoggerFactory
			.getLogger(EsLogController.class);

	private final String BASE_SERVICE = "/eslog";
	@Autowired
	private BasicController basicController;

	// private static final BoCloudService SERVICE = BoCloudService.Cmp;

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			HttpServletRequest request) {

		BsmResult result = basicController.list(page, rows, params, sorter,
				false, BASE_SERVICE, request, null);

		return result;

	}

	@RequestMapping(value = "/sysLogList", method = { RequestMethod.POST })
	public BsmResult sysLogList(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			HttpServletRequest request) {

		BsmResult result = null;
		String realPath = request.getSession().getServletContext().getRealPath("");
		logger.info("系统部署真实路径："+realPath);
		
		// 获取realPath路径的最后两个等级目录
		String symbol = null;
		String[] dirs = realPath.replace("\\", "/").split("/"); ;
		StringBuffer buffer = new StringBuffer();
		if (System.getProperty("os.name").toUpperCase().contains("windows".toUpperCase())) {
			symbol = "\\\\";
		} else {
			symbol = "/";
		}
		buffer.append(symbol).append(dirs[dirs.length-2]).append(symbol).append(dirs[dirs.length-1]);
		
		//截取去除realPath路径的最后两个等级目录的上等级目录
		String path = realPath.split(buffer.toString())[0];
		path += symbol + "logs";
		logger.info("系统部署日志路径："+path);
		

		List<JSONObject> dataList = new ArrayList<JSONObject>();
		File file = new File(path);
		
		logger.info("系统部署日志路径是否存在："+file.exists());
		
		File[] tempList = file.listFiles();
		JSONObject object = null;
		
		if (null != tempList) {
			for (int i = 0; i < tempList.length; i++) {
				if (tempList[i].isFile()) {
					object = new JSONObject();
					object.put("logName", tempList[i].getName());
					object.put("logPath", tempList[i].toString());
					object.put("type", "客户端");
				}
				if (tempList[i].isDirectory()) {
					// nothing todo
				}
				dataList.add(object);
			}
		}
		
		if (!dataList.isEmpty()) {
			result = new BsmResult(true, dataList, "查询成功");
		} else {
			logger.info("查询为空,没有获取到系统日志信息列表");
			result = new BsmResult(true, dataList, "查询成功");
		}
		return result;
	}

	@RequestMapping(value = "/sysLogDetail", method = { RequestMethod.POST })
	public BsmResult sysLogDetail(
			@RequestParam(value = Common.PARAMS, required = true) String params) {
		StringBuilder result = new StringBuilder();
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		String filePath = null;

		try {
			if (null != jsonObject) {
				if (null != jsonObject.get("filePath")) {
					filePath = jsonObject.get("filePath").toString();
					BufferedReader br = new BufferedReader(new FileReader(
							filePath));
					String str = null;
					while ((str = br.readLine()) != null) {
						result.append(System.lineSeparator() + str);
					}
					br.close();
				}
			}
			return new BsmResult(true, result.toString(), "读取文件内容成功");
		} catch (Exception e) {
			logger.error("请求方法sysLogDetail参数filePath未找到!", e);
			return new BsmResult(false, "读取文件内容为空");
		}
	}
	
	/**
	 * 获取server端系统日志
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/system", method = { RequestMethod.POST })
	public BsmResult create(HttpServletRequest request) {
			String url = BASE_SERVICE + "/system";
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null,
					request);
			return service.invoke();
	}

}
