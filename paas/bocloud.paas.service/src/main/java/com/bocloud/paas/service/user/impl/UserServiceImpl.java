package com.bocloud.paas.service.user.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocloud.common.encrypt.Encryptor;
import com.bocloud.common.encrypt.SHAEncryptor;
import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.Sign;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.dao.user.RoleDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.dao.user.UserRoleDao;
import com.bocloud.paas.dao.user.UserSecurityDao;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.entity.Department;
import com.bocloud.paas.entity.Role;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.entity.UserRole;
import com.bocloud.paas.entity.UserSecurity;
import com.bocloud.paas.model.AuthBean;
import com.bocloud.paas.model.MenuBean;
import com.bocloud.paas.model.UserBean;
import com.bocloud.paas.service.user.UserService;
import com.bocloud.paas.service.user.util.AuthComparator;
import com.bocloud.paas.service.user.util.MenuComparator;
import com.google.common.collect.Maps;

/**
 * 用户抽象Service接口实现类
 * 
 * @author dongkai
 *
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserRoleDao userRoleDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private UserSecurityDao userSecurityDao;
	@Autowired
	private AuthDao authDao;
	@Autowired
	private LockFactory lockFactory;
	@Autowired
	private DepartmentDao departmentDao;
	private String departmentName = "";
	
	@Override
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser) {
		List<User> list = null;
		int total = 0;
		GridBean gridBean = null;
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = listDept(requestUser.getId());
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			if (null == params) {
				params = new ArrayList<>();
			}
			if(null != requestUser.getTenantId()){
				params.add(new Param(MapTools.simpleMap("tenantId", requestUser.getTenantId()), Sign.EQ));
			}
			if (simple) {
				List<SimpleBean> beans = userDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				list = userDao.list(page, rows, params, sorter, requestUser.getTenantId(), deptIds);
				if (!list.isEmpty()) {
					for (User user : list) {
						List<Role> roles = roleDao.listByUid(user.getId());
						if (!roles.isEmpty()) {
							List<String> roleNames = new ArrayList<>();
							for (Role role : roles) {
								roleNames.add(role.getName());
							}
							user.setRoleNames(roleNames);
						}
						// 设置部门
						if (null != user.getDepartId()) {
							Department depart =  departmentDao.query(user.getDepartId());
							if(null != depart && 0 != depart.getParentId()){
								departmentName = depart.getName()+"/" + getDepart(depart);
							}else if (null != depart) {
								departmentName = depart.getName();
							}
							// reverse组织部门
							String[] sArr = departmentName.split("/");
					        List<String> listName = new ArrayList<String>();  
					        listName = Arrays.asList(sArr);  
					        Collections.reverse(listName);  
					        // 遍历
					        String reverseName = "";
					        for(String name:listName){  
					        	reverseName += name+"/";
					        }  
					        user.setDepartName(reverseName.substring(0,reverseName.length() - 1));
						}
						departmentName = "";
					}
				}
				total = userDao.count(params, deptIds);
				gridBean = GridHelper.getBean(page, rows, total, list);
			}
		} catch (Exception e) {
			logger.error("list user fail:", e);
			return new BsmResult(false, "查询用户失败", "", "");
		}
		return new BsmResult(true, gridBean, "查询用户成功");
	}
	
	private String getDepart(Department department) throws Exception{
		
		if(0 != department.getParentId()){
			Department depart = departmentDao.query(department.getParentId());
			if(0 != depart.getParentId()){
				departmentName += depart.getName()+"/";
			}else{
				departmentName += depart.getName();
			}
			getDepart(depart);
		}
		return departmentName;
	}

	@Override
	public BsmResult login(String username, String password, String sessionId) {
		// 判断用户是否存在
		User user = null;
		try {
			user = userDao.getByName(username);
		} catch (Exception e) {
			logger.error("get user fail by username:", e);
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		if (null == user) {
			logger.warn("user is null");
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		if (user.getStatus().equals(BaseStatus.ABNORMAL.name())) {
			return new BsmResult(false, "冻结用户无法登录!");
		}
		// 查询随机数
		UserSecurity security = null;
		try {
			security = userSecurityDao.getByUid(user.getId());
		} catch (Exception e) {
			logger.error("query random fail:", e);
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		if (null == security) {
			logger.warn("security is null");
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		// 加密
		Encryptor encryptor = new SHAEncryptor();
		String result = encryptor.encrypt(password, security.getSalt());
		// 判断是否密码正确
		if (null == result) {
			logger.warn("encryptor is null");
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		if (!result.equals(user.getPassword())) {
			logger.warn("in password error");
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		// 更新用户登录状态
		try {
			user.setLoginStatus(true);
			user.setSessionId(sessionId);
			userDao.update(user);
		} catch (Exception e) {
			logger.error("update user login status fail", e);
			return new BsmResult(false, "更新用户登录状态失败", "", "");
		}
		// 构建返回数据
		Map<String, Object> map = MapTools.simpleMap(Common.USER, user);
		map.put(Common.API_KEY, security.getApiKey());
		map.put(Common.SEC_KEY, security.getSecKey());
		try {
			// 查询某用户所有的角色
			List<Role> roles = roleDao.listByUid(user.getId());
			map.put(Common.ROLES, roles);
			List<Authority> auths = new ArrayList<>();
			for (Role role : roles) {
				// 查询某角色所有的权限
				List<Authority> permissions = authDao.listByRid(role.getId());
				List<Authority> parents = new ArrayList<>();
				for (Authority auth : permissions) {
					this.getParents(parents, auth);
				}
				permissions.addAll(parents);
				auths.addAll(permissions);
			}
			Map<Long, AuthBean> authority = new HashMap<>();
			Map<Long, String> authApi = new HashMap<>();
			for (Authority auth : auths) {
				AuthBean authBean = new AuthBean(auth.getId(), auth.getName(), auth.getActionUrl(), auth.getIcon(),
						auth.getParentId(), auth.getPriority());
				if (auth.getCategory().equals("menu")) {
					authority.put(authBean.getId(), authBean);
				}
				if (auth.getCategory().equals("api")) {
					authApi.put(authBean.getId(), authBean.getActionUrl());
				}
			}
			// 排序
			List<MenuBean> menus = new ArrayList<>();
			List<AuthBean> authBeans = new ArrayList<>();
			for (Long key : authority.keySet()) {
				authBeans.add(authority.get(key));
			}
			// 获取所有的一级菜单
			for (AuthBean authBean : authBeans) {
				if (authBean.getParentId().equals(0l)) {
					MenuBean menuBean = new MenuBean(authBean.getId(), authBean.getName(), authBean.getActionUrl(),
							authBean.getIcon(), authBean.getPriority());
					menus.add(menuBean);
				}
			}
			// 排序父节点
			Collections.sort(menus, new MenuComparator());
			// 组装数据
			for (MenuBean menu : menus) {
				List<AuthBean> child = new ArrayList<>();
				for (AuthBean authBean : authBeans) {
					if (menu.getId().equals(authBean.getParentId())) {
						child.add(authBean);
					}
				}
				// 排序子节点
				Collections.sort(child, new AuthComparator());
				if (null == child || child.isEmpty()) {
					menu.setChildren(null);
				} else {
					menu.setChildren(child);
				}
			}

			map.put(Common.AUTHS, menus);
			map.put("authApi", authApi.values());
		} catch (Exception e) {
			logger.error("get role and permission fail:", e);
			return new BsmResult(false, "用户名或密码错误", "", "");
		}
		return new BsmResult(true, map, "用户登录成功", "200", "");
	}

	// 递归查询父菜单
	private List<Authority> getParents(List<Authority> parents, Authority auth) {
		Long parentId = auth.getParentId();
		if (parentId.equals(0l)) {
			parents.add(auth);
			return parents;
		} else {
			try {
				Authority parent = authDao.query(parentId);
				parents.add(parent);
				return this.getParents(parents, parent);
			} catch (Exception e) {
				logger.error("get Authority failure:", e);
				return null;
			}
		}

	}

	@Transactional
	@Override
	public BsmResult create(User user) {
		// 判断用户名唯一
		try {
			User existUser1 = userDao.getByUserId(user.getUserId());
			User existUser2 = userDao.getByName(user.getUsername());
			if (null != existUser1) {
				return new BsmResult(false, "用户工号已存在");
			}
			if (null != existUser2) {
				return new BsmResult(false, "用户名已存在");
			}
		} catch (Exception e) {
			logger.error("get user fail by username:", e);
			return new BsmResult(false, "添加用户失败", "", "");
		}
		// 初始化密码
		String salt = UUID.randomUUID().toString();
		Encryptor encryptor = new SHAEncryptor();

		// 查询创建者的租户信息并赋值给所创建的用户
		try {
			String password = encryptor.encrypt("123456", salt);
			user.setPassword(password);
			user.setStatus(BaseStatus.NORMAL.name());
			user.setLoginStatus(false);
			if (null != user.getTenantId() && user.getTenantId() != 0) {
				user.setIsTenant(true);
			} else {
				user.setIsTenant(false);
			}
			userDao.save(user);
		} catch (Exception e) {
			logger.error("create user fail:", e);
			return new BsmResult(false, "添加用户失败", "", "");
		}

		// 构建用户随机数对象
		UserSecurity security = new UserSecurity();
		String data = Long.toString(System.currentTimeMillis());
		String apiKey = encryptor.encrypt(data, Common.API_KEY);
		String secKey = encryptor.encrypt(data, Common.SEC_KEY);
		security.setUserId(user.getId());
		security.setSalt(salt);
		security.setApiKey(apiKey);
		security.setSecKey(secKey);
		security.setTenantId(user.getTenantId());
		UserSecurity object = null;
		try {
			object = userSecurityDao.save(security);
		} catch (Exception e) {
			logger.error("user random add fail:", e);
			return new BsmResult(false, "添加用户失败", "", "");
		}
		return null == object ? new BsmResult(false, "添加用户失败", "", "") : new BsmResult(true, object, "添加用户成功");
	}

	@Override
	public BsmResult modify(UserBean bean, Long userId) {
		String path = User.class.getSimpleName() + "_" + bean.getId();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(bean.getId());
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			BeanUtils.copyProperties(bean, user);
			user.setMenderId(userId);
			Boolean result = userDao.update(user);
			return result ? new BsmResult(true, "修改用户成功") : new BsmResult(false, "修改用户失败", "", "");
		} catch (Exception e) {
			logger.error("modify user fail:", e);
			return new BsmResult(false, "修改用户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = User.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			if (id.equals(userId)) {
				logger.warn("user don't delete oneself");
				return new BsmResult(false, "不能删除登录的用户", "", "");
			}
			// 删除用户和角色的对应关系
			this.userRoleDao.deleteByUid(id);
			result = userDao.delete(id, userId);
		} catch (Exception e) {
			logger.error("remove user fail:", e);
			return new BsmResult(false, "删除用户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "删除用户成功") : new BsmResult(false, "删除用户失败", "", "");
	}

	@Override
	public BsmResult lock(Long id, Long userId) {
		String path = User.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			if (id.equals(userId)) {
				logger.warn("user don't lock oneself ");
				return new BsmResult(false, "用户不能冻结当前登录的用户", "", "");
			}
			result = userDao.lock(id, userId);
		} catch (Exception e) {
			logger.error("freeze user fail:", e);
			return new BsmResult(false, "冻结用户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "冻结用户成功") : new BsmResult(false, "冻结用户失败", "", "");
	}

	@Override
	public BsmResult active(Long id, Long userId) {
		String path = User.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			result = userDao.active(id, userId);
		} catch (Exception e) {
			logger.error("release user fail:", e);
			return new BsmResult(false, "解冻用户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "解冻用户成功") : new BsmResult(false, "解冻用户失败", "", "");
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			User user = userDao.query(id);
			// 设置部门
			Department department = departmentDao.query(user.getDepartId());
			if (null != department) {
				user.setDepartName(department.getName());
			}
			// 设置角色
			List<Role> roles = roleDao.listByUid(user.getId());
			if (!roles.isEmpty()) {
				List<String> roleNames = new ArrayList<>();
				for (Role role : roles) {
					roleNames.add(role.getName());
				}
				user.setRoleNames(roleNames);
			}
			return new BsmResult(true, user, "查询用户详情成功");
		} catch (Exception e) {
			logger.error("query user detail fail:", e);
			return new BsmResult(false, "查询用户详情失败", "", "");
		}
	}

	@Override
	public BsmResult reset(Long id, Long userId) {
		String path = User.class.getSimpleName() + "_" + id;
		String salt = UUID.randomUUID().toString();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			List<Role> roles = roleDao.listByUid(userId);
			String username = userDao.query(userId).getUsername();
			StringBuffer roleName = new StringBuffer();
			for (Role role : roles) {
				roleName.append(role.getName());
			}
			if (!"admin".equals(username) || roleName.toString().indexOf("管理员") < 0) {
				logger.warn("others can not reset");
				return new BsmResult(false, "只有admin才能重置密码", "", "");
			}
			// 加密
			SHAEncryptor SHA = new SHAEncryptor();
			String encrypt = SHA.encrypt("123456", salt);
			user.setPassword(encrypt);
			user.setMenderId(userId);
			userDao.update(user);
			// 准备随机数数据
			UserSecurity security = userSecurityDao.getByUid(user.getId());
			security.setSalt(salt);
			// 更新随机数
			userSecurityDao.update(security);
			return new BsmResult(true, "重置密码成功");
		} catch (Exception e) {
			logger.error("reset password fail:", e);
			return new BsmResult(false, "重置密码失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult accredit(Long id, String roles, Long userId) {
		String path = User.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 删除用户下角色
			this.userRoleDao.deleteByUid(id);
			if (!("").equals(roles)) {
				String[] role = roles.split(",");
				UserRole userRole = null;
				for (String str : role) {
					userRole = new UserRole(id, Long.valueOf(str));
					userRoleDao.save(userRole);
				}
				int length = role.length;
				for (int i = 0; i < length; i++) {

				}
				// 更新用户操作
				user.setMenderId(userId);
				userDao.update(user);
			}
			return new BsmResult(true, "授权用户角色成功");
		} catch (Exception e) {
			logger.error("release user fail:", e);
			return new BsmResult(false, "授权用户角色失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult secKey(String apiKey) {
		try {
			UserSecurity security = userSecurityDao.getKey(apiKey);
			return new BsmResult(true, security, "获取安全密钥成功");
		} catch (Exception e) {
			logger.error("get secKey fail:", e);
			return new BsmResult(false, "获取安全密钥失败", "", "");
		}
	}

	@Override
	public BsmResult check(Long id, String password) {
		try {
			User user = userDao.query(id);
			UserSecurity security = userSecurityDao.getByUid(id);
			if (null == user || null == security) {
				logger.warn("old password is not correct");
				return new BsmResult(false, "原密码验证失败", "", "");
			}
			SHAEncryptor SHA = new SHAEncryptor();
			String encrypt = SHA.encrypt(password, security.getSalt());
			if (null == encrypt || !encrypt.equals(user.getPassword())) {
				logger.warn("old password is not correct");
				return new BsmResult(false, "原密码验证失败", "", "");
			}
			return new BsmResult(true, "原密码正确");
		} catch (Exception e) {
			logger.error("old password not correct:", e);
			return new BsmResult(false, "原密码验证失败", "", "");
		}
	}

	@Override
	public BsmResult change(Long id, String password) {
		String path = User.class.getSimpleName() + "_" + id;
		String salt = UUID.randomUUID().toString();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 加密
			SHAEncryptor SHA = new SHAEncryptor();
			String encrypt = SHA.encrypt(password, salt);
			user.setPassword(encrypt);
			userDao.update(user);
			// 准备随机数数据
			UserSecurity security = userSecurityDao.getByUid(user.getId());
			security.setSalt(salt);
			// 更新随机数
			userSecurityDao.update(security);
			return new BsmResult(true, "修改密码成功");
		} catch (Exception e) {
			logger.error("change password fail:", e);
			return new BsmResult(false, "修改密码失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult getByName(String username) {
		try {
			User user = userDao.getByName(username);
			return new BsmResult(true, user, "查询用户成功！");
		} catch (Exception e) {
			logger.error("get user by name fail", e);
			return new BsmResult(false, "查询用户失败!", "", "");
		}
	}

	@Override
	public BsmResult checkUser(Long id, String sessionId) {
		try {
			User user = userDao.query(id);
			if (null != user) {
				String oldSessionId = user.getSessionId();
				if (null != oldSessionId && (!oldSessionId.equals(sessionId))) {
					return new BsmResult(false, "此用户已在别处登录!", "", "");
				} else {
					return new BsmResult(true, "");
				}
			} else {
				return new BsmResult(false, "查询用户失败!", "", "");
			}
		} catch (Exception e) {
			logger.error("check user login fail:", e);
			return new BsmResult(false, "检查用户登录失败!", "", "");
		}
	}

	@Override
	public BsmResult logout(Long id) {
		try {
			User user = userDao.query(id);
			user.setLoginStatus(false);
			user.setSessionId("");
			boolean result = userDao.update(user);
			if (result) {
				return new BsmResult(true, "用户退出成功!", "", "");
			} else {
				return new BsmResult(false, "用户退出更新失败!", "", "");
			}
		} catch (Exception e) {
			logger.error("get user fail:", e);
			return new BsmResult(false, "获取用户失败!", "", "");
		}
	}

	@Override
	public BsmResult listRoles(Long id, Long tenantId) {
		try {
			User user = userDao.query(id);
			if (!user.getIsTenant()) {
				List<Role> list = this.roleDao.listByUid(id);
				List<Role> roles = this.roleDao.list(tenantId);
				if (null == roles || roles.isEmpty()) {
					return new BsmResult(false, "查询所有角色失败！");
				}
				for (Role role : roles) {
					if (null != list && !list.isEmpty()) {
						for (Role r : list) {
							if (role.getId().equals(r.getId())) {
								role.setChecked(true);
							}
						}
					}
				}
				return new BsmResult(true, roles, "查询成功");
			} else {
				List<Role> list = this.roleDao.listByUid(id);
				List<Role> roles = this.roleDao.list(tenantId);
				if (null == roles || roles.isEmpty()) {
					return new BsmResult(false, "查询所有角色失败!");
				}
				for (Role role : roles) {
					if (null != list && !list.isEmpty()) {
						for (Role r : list) {
							if (role.getId().equals(r.getId())) {
								role.setChecked(true);
							}
						}
					}
				}
				return new BsmResult(true, roles, "查询成功");
			}
		} catch (Exception e) {
			logger.error("list authority by tenant id:", e);
			return new BsmResult(false, "查询失败", null, null);
		}
	}

	@Override
	public BsmResult checkUsername(String username) {
		try {
			User user = userDao.getByName(username);
			if (null != user) {
				return new BsmResult(false, "此用户名已存在!");
			}
		} catch (Exception e) {
			logger.error("check username fail", e);
		}
		return new BsmResult(true, "");

	}

	@Override
	public BsmResult listByDid(Long departId) {
		try {
			List<User> users = userDao.listByDid(departId);
			return new BsmResult(true, users, "查询用户成功");
		} catch (Exception e) {
			logger.error("List user failure by departId:", e);
			return new BsmResult(false, "查询组织关联用户失败!");
		}
	}

	@Override
	public User getUser(String username, String password) {
		// 判断用户是否存在
		User user = null;
		try {
			user = userDao.getByName(username);
		} catch (Exception e) {
			logger.error("get user fail by username:", e);
			return null;
		}
		if (null == user) {
			logger.warn("user is null");
			return null;
		}
		// 查询随机数
		UserSecurity security = null;
		try {
			security = userSecurityDao.getByUid(user.getId());
		} catch (Exception e) {
			logger.error("query random fail:", e);
			return null;
		}
		if (null == security) {
			logger.warn("security is null");
			return null;
		}
		// 加密
		Encryptor encryptor = new SHAEncryptor();
		String result = encryptor.encrypt(password, security.getSalt());
		// 判断是否密码正确
		if (null == result || !result.equals(user.getPassword())) {
			return null;
		}
		return user;
	}

	@Override
	public BsmResult getByRid(Long roleId) {
		try {
			List<User> users = userDao.listByRid(roleId);
			return new BsmResult(true, users, "查询成功!");
		} catch (Exception e) {
			logger.error("get users error:", e);
			return new BsmResult(false, "查询失败!");
		}
	}

	@Override
	public List<User> getUsersByTenantId(Long tenantId) {
		try {
			List<User> users = userDao.listByTid(tenantId);
			return users;
		} catch (Exception e) {
			logger.error("UserService getUsersByTenantId fails:", e);
			return null;
		}
	}

	@Override
	public String listDept(Long id) {
		try {
			StringBuffer departIds = new StringBuffer();
			User user = userDao.query(id);
			if (null == user) {
				logger.warn("user not exists");
				return null;
			}
			Department department = departmentDao.query(user.getDepartId());
			if (null == department) {
				logger.warn("department not exists");
				return null;
			}
			Long departId = department.getId();
			departIds.append(departId).append(",");
			List<Department> departments = departmentDao.list(departId);
			return this.getChildren(departIds, departments);
		} catch (Exception e) {
			logger.error("list depart error", e);
			return null;
		}
	}

	/**
	 * 递归查询departIds
	 * 
	 * @param departIds
	 * @param departments
	 * @return
	 */
	private String getChildren(StringBuffer departIds, List<Department> departments) {
		if (!ListTool.isEmpty(departments)) {
			List<Department> children = new ArrayList<>();
			for (Department department : departments) {
				Long departId = department.getId();
				departIds.append(departId).append(",");
				try {
					List<Department> child = departmentDao.list(departId);
					children.addAll(child);
				} catch (Exception e) {
					logger.error("list depart child error", e);
					return null;
				}
			}
			if (!ListTool.isEmpty(children)) {
				return this.getChildren(departIds, children);
			} else {
				return departIds.toString().substring(0, departIds.length() - 1);
			}
		} else {
			return departIds.toString().substring(0, departIds.length() - 1);
		}
	}

	@Override
	public BsmResult checkUseId(String userId) {
		try {
			User user = userDao.getByUserId(userId);
			if (null != user) {
				return new BsmResult(false, "此用户工号存在!");
			}
		} catch (Exception e) {
			logger.error("check username fail", e);
			return new BsmResult(false, "查询用户工号异常");
		}
		return new BsmResult(true, "");
	}

}
