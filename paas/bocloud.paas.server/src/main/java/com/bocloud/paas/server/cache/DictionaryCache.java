package com.bocloud.paas.server.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典缓存
 * 
 * @author wewei
 *
 */
public class DictionaryCache {

	private static Map<String, String> dictCache = new ConcurrentHashMap<>(100, 0.9f);
	private static boolean isInit = true;

	public static Map<String, String> getCache() {
		return dictCache;
	}

	public static String readCache(String key) {
		if (null == key) {
			return null;
		}
		return dictCache.get(key);
	}

	public static void writeCache(Map<String, String> map) {
		if (null != map) {
			for (String key : map.keySet()) {
				dictCache.put(key, map.get(key));
			}
		}
	}

	public static boolean isInit() {
		return isInit;
	}

	public static void setInit(boolean isInit) {
		DictionaryCache.isInit = isInit;
	}
	
}
