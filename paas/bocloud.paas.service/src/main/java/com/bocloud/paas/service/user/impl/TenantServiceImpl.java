package com.bocloud.paas.service.user.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

import com.bocloud.common.encrypt.SHAEncryptor;
import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.dao.user.RoleAuthDao;
import com.bocloud.paas.dao.user.RoleDao;
import com.bocloud.paas.dao.user.TenantAuthDao;
import com.bocloud.paas.dao.user.TenantDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.dao.user.UserRoleDao;
import com.bocloud.paas.dao.user.UserSecurityDao;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.entity.Department;
import com.bocloud.paas.entity.Role;
import com.bocloud.paas.entity.RoleAuthority;
import com.bocloud.paas.entity.Tenant;
import com.bocloud.paas.entity.TenantAuthority;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.entity.UserRole;
import com.bocloud.paas.entity.UserSecurity;
import com.bocloud.paas.model.TenantBean;
import com.bocloud.paas.service.user.TenantService;
import com.bocloud.paas.service.user.UserService;
import com.bocloud.paas.service.user.util.AuthorityComparator;

/**
 * 租户抽象Service接口实现类
 * 
 * @author dongkai
 *
 */
@Service("tenantService")
public class TenantServiceImpl implements TenantService {

	private Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);
	@Autowired
	private TenantDao tenantDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private AuthDao authDao;
	@Autowired
	private UserRoleDao userRoleDao;
	@Autowired
	private TenantAuthDao tenantAuthDao;
	@Autowired
	private RoleAuthDao roleAuthDao;
	@Autowired
	private LockFactory lockFactory;
	@Autowired
	private UserService userService;
	@Autowired
	private UserSecurityDao userSecurityDao;
	@Autowired
	private DepartmentDao departmentDao;

	@Override
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple) {
		List<Tenant> list = new ArrayList<>();
		int total = 0;
		GridBean gridBean = null;
		if (sorter.isEmpty()) {
			sorter = new HashMap<>();
		}
		sorter.put("gmtCreate", Common.ONE);
		try {
			list = tenantDao.list(page, rows, params, sorter);
			total = tenantDao.count(params);
			gridBean = GridHelper.getBean(page, rows, total, list);
		} catch (Exception e) {
			logger.error("list tenant fail:", e);
			return new BsmResult(false, "查询租户失败", "", "");
		}
		return new BsmResult(true, gridBean, "查询租户成功");
	}

	@Transactional
	@Override
	public BsmResult create(Tenant tenant, String auths) {
		try {
			tenant.setStatus(BaseStatus.NORMAL.name());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.YEAR, 2);
			tenant.setExpiredTime(calendar.getTime());
			tenantDao.save(tenant);
		} catch (Exception e) {
			logger.error("create tenant fail:", e);
			return new BsmResult(false, "添加租户失败", "", "");
		}
		return oprateAuth(tenant, auths);
	}

	@Override
	public BsmResult modify(TenantBean tenant, Long userId) {
		Boolean result = false;
		String path = Tenant.class.getSimpleName() + "_" + tenant.getId();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant existTenant = tenantDao.query(tenant.getId());
			if (null == existTenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			BeanUtils.copyProperties(tenant, existTenant);
			existTenant.setMenderId(userId);
			result = tenantDao.update(existTenant);
		} catch (Exception e) {
			logger.error("modify tenant fail:", e);
			return new BsmResult(false, "修改租户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "修改租户成功") : new BsmResult(false, "修改租户失败", "", "");
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = Tenant.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant tenant = tenantDao.query(id);
			if (null == tenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 判断是否存在用户
			int count = this.userDao.tenantUserCount(tenant.getId());
			if (count > 0) {
				// 删除租户下的角色及其权限
				List<Role> roles = this.roleDao.listByTid(id);
				for (Role role : roles) {
					this.roleAuthDao.deleteByRid(role.getId());
				}
				this.roleDao.deleteByTid(id, userId);
				// 删除租户下的权限
				this.tenantAuthDao.deleteByTid(id);
				this.userDao.deleteByTid(id, userId);
				this.userSecurityDao.deleteByTid(id);
			}
			// 删除租户的组织机构
			List<Department> departments = departmentDao.listByTid(id);
			for (Department depart : departments) {
				departmentDao.delete(depart.getId(), userId);
			}
			result = tenantDao.delete(id, userId);
		} catch (Exception e) {
			logger.error("remove tenant fail:", e);
			return new BsmResult(false, "删除租户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "删除租户成功") : new BsmResult(false, "删除租户失败", "", "");
	}

	@Override
	public BsmResult lock(Long id, Long tenantId) {
		String path = Tenant.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant tenant = tenantDao.query(id);
			if (null == tenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			result = tenantDao.lock(id, tenantId);
		} catch (Exception e) {
			logger.error("freeze tenant fail:", e);
			return new BsmResult(false, "冻结租户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "冻结租户成功") : new BsmResult(false, "冻结租户失败", "", "");
	}

	@Override
	public BsmResult active(Long id, Long tenantId) {
		String path = Tenant.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant tenant = tenantDao.query(id);
			if (null == tenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			result = tenantDao.active(id, tenantId);
		} catch (Exception e) {
			logger.error("release tenant fail:", e);
			return new BsmResult(false, "解冻租户失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "解冻租户成功") : new BsmResult(false, "解冻租户失败", "", "");
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			Tenant tenant = tenantDao.query(id);
			return new BsmResult(true, tenant, "查询租户详情成功");
		} catch (Exception e) {
			logger.error("query tenant detail fail:", e);
			return new BsmResult(false, "查询租户详情失败", "", "");
		}
	}

	/**
	 * 
	 * 创建租户的细节处理方法
	 * 
	 * @author DZG
	 * @since V1.0 2016年8月12日
	 */
	@Transactional(rollbackFor = Exception.class)
	private BsmResult oprateAuth(Tenant tenant, String authIds) {
		// 添加租户成功完成创建租户管理员角色并授予租户的全部权限，创建管理员用户，将管理员角色授予管理员用户(都是属于该租户的)
		try {
			if (authIds.equals("")) {
				return new BsmResult(false, "请选择需要授权的权限!");
			}
			// 1.先将租户分配到的权限写入 租户-权限 关联表
			String[] authority = authIds.split(",");
			List<Long> ids = new ArrayList<>();
			// 查找子节点
			List<Long> childrens = new ArrayList<>();
			for (String str : authority) {
				ids.add(Long.valueOf(str));
				List<Authority> child = authDao.list(Long.valueOf(str));
				this.childrenIds(childrens, child);
			}
			if (!childrens.isEmpty()) {
				for (Long children : childrens) {
					if (!ids.contains(children)) {
						ids.add(children);
					}
				}

				for (Long auth : ids) {
					TenantAuthority ta = new TenantAuthority(tenant.getId(), Long.valueOf(auth));
					tenantAuthDao.save(ta);
				}
			}
			Long userId = tenant.getCreaterId();
			// 2.创建租户管理员角色，并赋予该角色所拥有的权限
			Role role = new Role("管理员", tenant.getId(), userId, userId, BaseStatus.NORMAL.name());
			roleDao.save(role);
			for (Long auth : ids) {
				RoleAuthority ra = new RoleAuthority(role.getId(), Long.valueOf(auth));
				roleAuthDao.save(ra);
			}

			// 3.创建租户的管理员用户，并将租户管理员角色绑定至管理员用户,使用email作为用户名
			User user = new User(tenant.getName() + "管理员", tenant.getContactEmail(), tenant.getContactEmail(),
					tenant.getCompany(), tenant.getTenantPhone(), tenant.getContactPhone(), userId, userId, true,
					tenant.getId());
			BsmResult result = userService.create(user);
			if (result.isSuccess() == true) {
				// 分配角色给该用户
				UserSecurity userSecurity = (UserSecurity) result.getData();
				UserRole ur = new UserRole(userSecurity.getUserId(), role.getId());
				userRoleDao.save(ur);
			} else {
				logger.error("create tenant-user fail:");
				return new BsmResult(false, "添加租户管理员用户失败", "", "");
			}
		} catch (Exception e) {
			logger.error("create tenant-auth fail:", e);
			return new BsmResult(false, "添加租户-权限绑定失败", "", "");
		}
		return new BsmResult(true, tenant, "添加租户成功");
	}

	@Override
	public BsmResult accredit(Long id, String auths, Long userId) {
		String path = Tenant.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant tenant = tenantDao.query(id);
			if (null == tenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 删除管理角色下权限
			List<Role> roles = this.roleDao.listByTid(id);
			Long roleId = null;
			for (Role role : roles) {
				User user = this.userDao.query(role.getCreaterId());
				if (!user.getIsTenant()) {
					roleId = role.getId();
					break;
				}
			}
			if (null == roleId) {
				logger.warn("role noe exist");
				return new BsmResult(false, "授权租户角色失败", "", "");
			}
			this.roleAuthDao.deleteByRid(roleId);
			// 删除租户下权限
			this.tenantAuthDao.deleteByTid(id);
			if (null == auths) {
				return new BsmResult(true, "授权租户角色成功");
			}
			// 添加该租户下所有权限
			String[] authority = auths.split(",");
			List<Long> ids = new ArrayList<>();
			// 查找子节点
			List<Long> children = new ArrayList<>();
			for (String str : authority) {
				ids.add(Long.valueOf(str));
				List<Authority> child = authDao.list(Long.valueOf(str));
				this.childrenIds(children, child);
			}
			if (!children.isEmpty()) {
				for (Long child : children) {
					if (!ids.contains(child)) {
						ids.add(child);
					}
				}
				// 批量授权租户和角色权限
				for (Long authId : ids) {
					// 添加该角色下所有权限
					TenantAuthority tauth = new TenantAuthority(id, authId);
					tenantAuthDao.save(tauth);
					RoleAuthority rauth = new RoleAuthority(roleId, authId);
					this.roleAuthDao.save(rauth);
				}
				// 更新租户操作
				tenant.setMenderId(userId);
				tenantDao.update(tenant);
			}
			return new BsmResult(true, "授权租户角色成功");
		} catch (Exception e) {
			logger.error("release tenant fail:", e);
			return new BsmResult(false, "授权租户角色失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	// 递归查询子节点ID
	private List<Long> childrenIds(List<Long> childrenId, List<Authority> authoritys) {
		try {
			if (null == authoritys || authoritys.isEmpty()) {
				return childrenId;
			}
			List<Authority> children = new ArrayList<>();
			for (Authority auth : authoritys) {
				childrenId.add(auth.getId());
				List<Authority> child = authDao.list(auth.getId());
				children.addAll(child);
			}
			if (children.isEmpty()) {
				return childrenId;
			}
			return this.childrenIds(childrenId, children);
		} catch (Exception e) {
			logger.error("get auth childrenIds fail:", e);
			return childrenId;
		}
	}

	@Override
	public BsmResult listUsers(int page, int rows, List<Param> params, Map<String, String> sorter) {
		List<User> list = null;
		int total = 0;
		GridBean gridBean = null;
		try {
			list = this.userDao.list(page, rows, params, sorter, null, null);
			total = this.userDao.count(params, null);
			gridBean = GridHelper.getBean(page, rows, total, list);
		} catch (Exception e) {
			logger.error("list user by tenant id:", e);
			return new BsmResult(false, "查询失败", null, null);
		}
		return new BsmResult(true, gridBean, "查询成功");
	}

	@Override
	public BsmResult listRoles(int page, int rows, List<Param> params, Map<String, String> sorter) {
		List<Role> list = null;
		int total = 0;
		GridBean gridBean = null;
		try {
			list = this.roleDao.list(page, rows, params, sorter, null);
			total = this.roleDao.count(params, null);
			gridBean = GridHelper.getBean(page, rows, total, list);
		} catch (Exception e) {
			logger.error("list role by tenant id:", e);
			return new BsmResult(false, "查询失败", null, null);
		}
		return new BsmResult(true, gridBean, "查询成功");
	}

	@Override
	public BsmResult listAuths(Long id, Long parentId) {
		try {
			List<Authority> list = this.authDao.listByTid(id);
			List<Authority> authoritys = authDao.list(parentId);
			if (null == authoritys || authoritys.isEmpty()) {
				return new BsmResult(false, "查询所有角色失败!");
			}
			for (Authority authority : authoritys) {
				List<Authority> children = authDao.list(authority.getId());
				if (null != children && !children.isEmpty()) {
					authority.setChildren("[]");
				}
				if (null != list && !list.isEmpty()) {
					for (Authority auth : list) {
						if (authority.getId().equals(auth.getId())) {
							authority.setChecked(true);
						}
					}
				}
			}
			Collections.sort(authoritys, new AuthorityComparator());
			return new BsmResult(true, authoritys, "查询成功");
		} catch (Exception e) {
			logger.error("list authority by tenant id:", e);
			return new BsmResult(false, "查询失败", null, null);
		}
	}

	@Override
	public BsmResult checkTenant(String email) {
		try {
			Tenant tenant = tenantDao.getByEmail(email);
			User user = userDao.getByEmail(email);
			if (null != tenant || null != user) {
				return new BsmResult(false, "此邮箱已经使用！");
			}
		} catch (Exception e) {
			logger.error("check tenant fail", e);
		}
		return new BsmResult(true, "");
	}

	@Override
	public BsmResult reset(Long id, Long userId) {
		String path = Tenant.class.getSimpleName() + "_" + id;
		String salt = UUID.randomUUID().toString();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Tenant tenant = tenantDao.query(id);
			if (null == tenant) {
				logger.warn("tenant data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			User user = userDao.getByName(tenant.getContactEmail());
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
}
