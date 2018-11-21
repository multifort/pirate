package com.bocloud.paas.server.cache;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 用户权限列表信息缓存
 * 
 * @author dmw
 *
 */
public class UserPermissionCache extends LinkedHashMap<String, List<String>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int capacity = 16;

	public UserPermissionCache(int capacity) {
		super(16, 0.75f, true);
		if (capacity > 0) {
			this.capacity = capacity;
		}
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String, List<String>> eldest) {
		return super.size() > capacity;
	}

	public synchronized void putCache(String key, List<String> permissions) {
		this.put(key, permissions);
	}

	public synchronized List<String> getCache(String key) {
		List<String> userPermissions = this.get(key);
		if (null != userPermissions) {
			this.put(key, userPermissions);
		}
		return userPermissions;
	}
}
