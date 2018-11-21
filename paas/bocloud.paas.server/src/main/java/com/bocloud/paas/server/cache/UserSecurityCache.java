package com.bocloud.paas.server.cache;

import java.util.LinkedHashMap;

import com.bocloud.paas.entity.UserSecurity;

/**
 * 用户认证信息缓存
 * 
 * @author dmw
 *
 */
public class UserSecurityCache extends LinkedHashMap<String, UserSecurity> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int capacity = 16;

	public UserSecurityCache(int capacity) {
		super(16, 0.75f, true);
		if (capacity > 0) {
			this.capacity = capacity;
		}
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String, UserSecurity> eldest) {
		return super.size() > capacity;
	}

	public synchronized void putCache(String key, UserSecurity userSecurity) {
		this.put(key, userSecurity);
	}

	public synchronized UserSecurity getCache(String key) {
		UserSecurity userSecurity = this.get(key);
		if (null != userSecurity) {
			this.put(key, userSecurity);
		}
		return userSecurity;
	}
}
