package com.bocloud.paas.server.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.entity.UserSecurity;
import com.bocloud.paas.server.cache.UserSecurityCache;
import com.bocloud.paas.service.user.UserService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.Result;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.NicTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.log.entity.AccessLog;
import com.bocloud.registry.http.core.SecurityWrapper;
import com.bocloud.registry.utils.IpTool;

/**
 * 访问拦截器
 * 
 * @author dmw
 *
 */
public class AccessInterceptor implements HandlerInterceptor {

	private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);
	private static final String MODULE = "PAAS.API";
	private static final String ROOT = "/";
	private static final String LOG_QUEUE = "bocloud.log.queue";// 审计日志队列
	// 安全协议参数
	private static String[] securityParams = { Common.API_KEY, Common.ENDPOINT, Common.TIMESTAMP, Common.SIGN };
	private static UserSecurityCache userCache = new UserSecurityCache(128);

	@Autowired
	private UserService userService;
	@Autowired
	private SecurityWrapper securityWrapper;
	@Autowired
	private AmqpTemplate amqpTemplate;
	private List<String> ignores;
	private List<String> backdoor;

	/**
	 * @param ignores
	 *            the ignores to set
	 */
	public void setIgnores(List<String> ignores) {
		this.ignores = ignores;
	}

	/**
	 * @param backdoor
	 *            the backdoor to set
	 */
	public void setBackdoor(List<String> backdoor) {
		this.backdoor = backdoor;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		response.setCharacterEncoding("UTF-8");
		String requestPath = request.getServletPath();
		if (StringUtils.isEmpty(requestPath) || ROOT.equals(requestPath)) {
			return true;
		}
		if (null != ignores) {
			for (String url : ignores) {
				if (requestPath.contains(url)) {
					return true;
				}
			}
		}
		/**
		 * app 平台日志 
		 */
		HandlerMethod handlerMethod = (HandlerMethod)handler;  
		Log appLog = handlerMethod.getMethod().getAnnotation(Log.class);
		
		Enumeration<String> paramNames = request.getParameterNames();
		List<String> paramNameList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		// 整理当前请求参数列表和请求Map
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			paramNameList.add(paramName);
			params.put(paramName, request.getParameter(paramName));
		}
		String action = requestPath.split("/")[1];
		if (null != appLog) {
			requestPath = appLog.name();
		}
		String reqeustParam = JSONObject.toJSONString(params);
		String requestIp = IpTool.getIP(request);
		String responseIp = NicTools.getIp();
		AccessLog log = new AccessLog(null, action, requestPath, null, null, responseIp, requestIp, new Date(),
				reqeustParam, MODULE, 0L);
		if (null != backdoor) {
			for (String url : backdoor) {
				if (requestPath.contains(url)) {
					this.processLog("请求处理正常！", log);
					return true;
				}
			}
		}
		if (!safeParams(paramNameList)) {// 判断当前请求是否包括所有的安全协议参数
			logger.warn("[{}]非法请求!请求参数里不包括协议参数！", IpTool.getIP(request) + request.getServletPath());
			this.processLog("非法请求!请求参数里不包括协议参数！", log);
			this.processError(response, ResultTools.noPermissionResult());
			return false;
		}
		// 获取安全协议参数，并从map中移除
		String sign = (String) params.get(Common.SIGN);
		params.remove(Common.SIGN);

		String timestamp = (String) params.get(Common.TIMESTAMP);
		params.remove(Common.TIMESTAMP);

		String endpoint = (String) params.get(Common.ENDPOINT);
		params.remove(Common.ENDPOINT);
		log.setResponseIp(endpoint);
		String apiKey = (String) params.get(Common.API_KEY);
		params.remove(Common.API_KEY);
		if (params.keySet().isEmpty()) {
			params = null;
		}
		String param = MapTools.mapToString(params);
		try {
			Long now = Calendar.getInstance().getTimeInMillis();
			Long reqTime = Long.valueOf(timestamp);
			if (now - reqTime > 1000 * 300) {// 判断当前请求是否超时
				logger.warn("[{}]请求超时！", IpTool.getIP(request) + request.getServletPath());
				this.processLog("请求超时！", log);
				this.processError(response, ResultTools.timeoutResult());
				return false;
			}
			UserSecurity security = userCache.getCache(apiKey);
			if (null == security) {// 用户信息不存在与缓存文件中，需要从数据库中获取
				logger.warn("user info is not in cache ");
				BsmResult result = this.userService.secKey(apiKey);
				if (!result.isSuccess()) {// 请求失败，直接返回到前台
					logger.warn("[{}]非法请求!用户鉴权失败！", IpTool.getIP(request) + request.getServletPath());
					this.processLog("非法请求!用户鉴权失败！", log);
					this.processError(response, ResultTools.noPermissionResult());
					return false;
				} else {// 请求成功，类型转换后把用户鉴权信息放到缓存中
					security = (UserSecurity) result.getData();
					userCache.putCache(security.getApiKey(), security);
				}
			}
			log.setUserId(security.getUserId());
			RequestUser requestUser = new RequestUser(security.getUserId());
			request.setAttribute(Common.USER, requestUser);
			String secKey = security.getSecKey();
			Map<String, Object> raw = MapTools.simpleMap(Common.API_KEY, apiKey);
			raw.put(Common.SEC_KEY, secKey);
			raw.put(Common.PARAMS, param);
			raw.put(Common.ENDPOINT, endpoint);
			raw.put(Common.TIMESTAMP, timestamp);
			// 对请求参数进行反向加密
			Result wrapResult = this.securityWrapper.wrap(raw);
			if (!wrapResult.isSuccess()) {
				logger.warn("[{}]非法请求!请求数据被篡改！", IpTool.getIP(request) + request.getServletPath());
				this.processLog("非法请求!请求数据被篡改！", log);
				this.processError(response, ResultTools.noPermissionResult());
				return false;
			}
			if (sign.equals(wrapResult.getMessage())) {// 判断反向加密后sign值是否一致
				this.processLog("请求处理正常！", log);
				return true;
			} else {
				logger.warn("[{}]非法请求!请求数据被篡改！", IpTool.getIP(request) + request.getServletPath());
				this.processLog("非法请求!请求数据被篡改！", log);
				this.processError(response, ResultTools.noPermissionResult());
				return false;
			}
		} catch (Exception e) {
			logger.warn("[{}]请求处理异常！{}", IpTool.getIP(request) + request.getServletPath(), e);
			this.processLog("请求处理异常！", log);
			this.processError(response, ResultTools.noPermissionResult());
			return false;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	/**
	 * 判断请求参数里是否包含安全协议参数
	 * 
	 * @param params
	 *            当前请求的参数集合
	 * @return
	 */
	private boolean safeParams(List<String> params) {
		if (null == params || params.isEmpty()) {
			return false;
		}
		for (String secParam : securityParams) {
			if (!params.contains(secParam)) {
				return false;
			}
		}
		return true;
	}

	private void processLog(String content, AccessLog log) {
		if (null != log) {
			if (log.getObject().equalsIgnoreCase("error")) {
				log.setResult("非法请求！请求路径未找到");
			} else {
				log.setResult(content);
			}
			if (log.getAction().contains("/log/list")) {
				return;
			}
			this.amqpTemplate.convertAndSend(LOG_QUEUE, log);
		}
	}

	private void processError(HttpServletResponse response, BsmResult result) throws IOException {
		response.getWriter().println(JSONObject.toJSON(ResultTools.noPermissionResult()));
		response.flushBuffer();
	}
}
