package com.bocloud.paas.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;

public class StringUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	
	public static String convertPinyin(String str) {
		try {
			str = PinyinHelper.getShortPinyin(str);
		} catch (PinyinException e) {
			logger.error("covert string to pinyin exception", e);
		}
		return str;
	}
}
