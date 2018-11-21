package com.bocloud.paas.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPTool {

	public static boolean isIp(String ip) {
		if (StringUtils.isEmpty(ip) || ip.length() < 7 || ip.length() > 15) {
			return false;
		}
		/**
		 * 判断IP格式和范围
		 */
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pattern = Pattern.compile(rexp);
		Matcher matcher = pattern.matcher(ip);
		return matcher.find();
	}
}
