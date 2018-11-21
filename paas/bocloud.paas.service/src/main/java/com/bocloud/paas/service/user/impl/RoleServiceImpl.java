package com.bocloud.paas.service.user.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.Sign;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.dao.user.RoleAuthDao;
import com.bocloud.paas.dao.user.RoleDao;
import com.bocloud.paas.dao.user.UserRoleDao;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.entity.Role;
import com.bocloud.paas.entity.RoleAuthority;
import com.bocloud.paas.entity.UserRole;
import com.bocloud.paas.model.RoleBean;
import com.bocloud.paas.service.user.RoleService;
import com.bocloud.paas.service.user.UserService;
import com.bocloud.paas.service.user.util.AuthorityComparator;
import com.google.common.collect.Maps;

/**
 * 角色抽象Service接口实现类
 * 
 * @author dongkai
 *
 */
@Service("roleService")
public class RoleServiceImpl implements RoleService {

	private Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private AuthDao authDao;
	@Autowired
	private RoleAuthDao roleAuthDao;
	@Autowired
	private UserRoleDao userRoleDao;
	@Autowired
	private LockFactory lockFactory;
	@Autowired
	private UserService userService;

	@Override
	public BsmResult create(Role role) {
		try {
			role.setStatus(BaseStatus.NORMAL.name());
			roleDao.save(role);
			return new BsmResult(true, "添加角色成功");
		} catch (Exception e) {
			logger.error("create role fail:", e);
			return new BsmResult(false, "添加角色失败", "", "");
		}
	}

	@Override
	public BsmResult modify(RoleBean bean, Long userId) {
		String path = Role.class.getSimpleName() + "_" + bean.getId();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Role role = roleDao.query(bean.getId());
			if (null == role) {
				logger.warn("role data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			BeanUtils.copyProperties(bean, role);
			role.setMenderId(userId);
			Boolean result = roleDao.update(role);
			return result ? new BsmResult(true, "修改角色成功") : new BsmResult(false, "修改角色失败", "", "");
		} catch (Exception e) {
			logger.error("modify role fail:", e);
			return new BsmResult(false, "修改角色失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = Role.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		boolean result = false;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			List<UserRole> list = userRoleDao.queryByRid(id);
			if (!list.isEmpty()) {
				return new BsmResult(false, "该角色关联用户，删除失败");
			}
			Role role = roleDao.query(id);
			if (null == role) {
				logger.warn("role data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			if (id.equals(1l) || id.equals(2l)) {
				return new BsmResult(false, "系统初始化角色不能删除!");
			}
			
			// 删除角色和权限、用户的对应关系
			this.roleAuthDao.deleteByRid(id);
			this.userRoleDao.deleteByRid(id);

			result = roleDao.delete(id, userId);
		} catch (Exception e) {
			logger.error("remove role fail:", e);
			return new BsmResult(false, "删除角色失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result ? new BsmResult(true, "删除角色成功") : new BsmResult(false, "删除角色失败", "", "");
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			Role role = roleDao.query(id);
			return new BsmResult(true, role, "查询角色详情成功");
		} catch (Exception e) {
			logger.error("query user detail fail:", e);
			return new BsmResult(false, "查询角色详情失败", "", "");
		}
	}

	@Override
	public BsmResult accredit(Long id, String authoritys, Long userId) {
		String path = Role.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Role role = roleDao.query(id);
			if (null == role) {
				logger.warn("role data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 删除角色下的所有权限
			this.roleAuthDao.deleteByRid(id);
			if (!("").equals(authoritys)) {
				// 添加该角色下所有权限
				String[] authority = authoritys.split(",");
				List<Long> ids = new ArrayList<>();
				// 查找子节点
				List<Long> children = new ArrayList<>();
				//String str = null;
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
					// 批量授权角色权限
					for (Long authId : ids) {
						// 添加该角色下所有权限
						RoleAuthority roleAuthority = new RoleAuthority(id, authId);
						roleAuthDao.save(roleAuthority);
					}
					// 更新用户操作
					role.setMenderId(userId);
					roleDao.update(role);
				}else{
					//添加没有子节点（child）的情况 by luogan
					// 批量授权角色权限
					for (Long authId : ids) {
						// 添加该角色下所有权限
						RoleAuthority roleAuthority = new RoleAuthority(id, authId);
						roleAuthDao.save(roleAuthority);
					}
					// 更新用户操作
					role.setMenderId(userId);
					roleDao.update(role);
				}
			}
			return new BsmResult(true, "授权角色权限成功");
		} catch (Exception e) {
			logger.error("delete role authority fail:", e);
			return new BsmResult(false, "授权角色权限失败", "", "");
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
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser) {
		List<Role> list = null;
		int total = 0;
		GridBean gridBean = null;
		List<SimpleBean> beans = null;
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(requestUser.getId());
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
			total = roleDao.count(params, deptIds);
			if (simple) {
				beans = roleDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				list = roleDao.list(page, rows, params, sorter, deptIds);
				gridBean = GridHelper.getBean(page, rows, total, list);
			}
		} catch (Exception e) {
			logger.error("list role fail:", e);
			return new BsmResult(false, "查询角色失败", "", "");
		}
		return new BsmResult(true, gridBean, "查询角色成功");
	}

	@Override
	public BsmResult listAuths(Long id, Long tenantId, Long parentId) {
		try {
			Role role = roleDao.query(id);
			if (null != role.getTenantId() && role.getTenantId().equals(0l)) {
				List<Authority> list = authDao.listByRid(id);
				List<Authority> authoritys = authDao.list(parentId);
				if (null == authoritys || authoritys.isEmpty()) {
					return new BsmResult(false, "查询所有权限失败!");
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
			} else {
				// 角色拥有的所有权限
				List<Authority> list = authDao.listByRid(id);
				List<Authority> authoritys = null;
				List<Authority> auths = null;
				if (parentId.equals(0l)) {
					authoritys = authDao.list(parentId, tenantId);
					// 和租户相关暂时注释掉 by luogan
					/*if (null == authoritys || authoritys.isEmpty()) {
						authoritys = authDao.listByTid(tenantId);
					}*/
					auths = new ArrayList<>();
					for (Authority authority : authoritys) {
						this.getAll(auths, authority);
					}
				} else {
					auths = authDao.list(parentId, tenantId);
				}
				if (!auths.isEmpty()) {
					for (Authority authority : auths) {
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
				}
				Collections.sort(auths, new AuthorityComparator());
				return new BsmResult(true, auths, "查询成功");
			}
		} catch (Exception e) {
			logger.error("list by userid faile", e);
			return new BsmResult(false, "查询角色权限失败!", "", "");
		}
	}

	// 递归查找所有节点
	private List<Authority> getAll(List<Authority> auths, Authority authority) {
		Long parentId = authority.getParentId();
		boolean flag = true;
		if (parentId.equals(0l)) {
			for (Authority auth : auths) {
				if (auth.getId().equals(authority.getId())) {
					flag = false;
				}
			}
			if (flag) {
				auths.add(authority);
			}
			return auths;
		}
		try {
			Authority auth = authDao.query(parentId);
			return this.getAll(auths, auth);
		} catch (Exception e) {
			logger.error("get auth fail", e);
			return auths;
		}
	}

}
