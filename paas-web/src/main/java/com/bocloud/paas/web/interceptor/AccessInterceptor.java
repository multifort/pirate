package com.bocloud.paas.web.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.http.CommonResultBuilder;
import com.bocloud.common.http.ResultBuilder;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ResultTools;

public class AccessInterceptor implements HandlerInterceptor {

	private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);
	ResultBuilder resultBuilder = new CommonResultBuilder();
	private List<String> ignores;
	private List<String> ignoreUrls;

	/**
	 * @param ignores
	 *            the ignores to set
	 */
	public void setIgnores(List<String> ignores) {
		this.ignores = ignores;
	}

	/**
	 * @param ignoreUrls
	 *            the ignoreUrls to set
	 */
	public void setIgnoreUrls(List<String> ignoreUrls) {
		this.ignoreUrls = ignoreUrls;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestPath = request.getServletPath();
		if (!StringUtils.hasText(requestPath) || requestPath.equals("/")) {
			return true;
		}
		if (null != ignores) {
			for (String url : ignores) {
				if (requestPath.contains(url)) {
					return true;
				}
			}
		}
		HttpSession session = request.getSession();
		Long userId = (Long) session.getAttribute(Common.USERID);
		if (null == userId) {
			if (null != ignoreUrls) {
				for (String url : ignoreUrls) {
					if (requestPath.contains(url)) {
						return true;
					}
				}
			}
			if (StringUtils.hasText(request.getHeader(Common.ANGULARJS_AJAX_TOKEN))
					|| StringUtils.hasText(request.getHeader(Common.JQUERY_AJAX_TOKEN))) {
				logger.error("Ajax Session timeout!");
				response.setHeader("Content-type", "application/json;charset=UTF-8");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(JSONObject.toJSONString(ResultTools.sessionTimeoutResult()));
				response.getWriter().close();
				return false;
			} else {
				logger.error("Common Session timeout!");
				response.sendRedirect("/");
				return false;
			}

		}
//		// 接口过滤
//		String authApi = (String) session.getAttribute("authApi");
//		String[] authApis = authApi.substring(1, authApi.length() - 1).split(",");
//		boolean flag = false;
//		for (String str : authApis) {
//			if (("\"" + requestPath + "\"").equals(str)) {
//				flag = true;
//			}
//		}
//		if (!flag) {
//			logger.error("No Permission!");
//			response.setHeader("Content-type", "application/json;charset=UTF-8");
//			response.setCharacterEncoding("UTF-8");
//			response.getWriter().println(JSONObject.toJSONString(ResultTools.noPermissionResult()));
//			response.getWriter().close();
//			return false;
//		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
}
