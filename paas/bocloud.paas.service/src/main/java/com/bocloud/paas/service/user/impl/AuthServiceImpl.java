package com.bocloud.paas.service.user.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.BsmResult;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.dao.user.RoleAuthDao;
import com.bocloud.paas.dao.user.TenantAuthDao;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.model.AuthIcon;
import com.bocloud.paas.model.AuthParentsBean;
import com.bocloud.paas.model.AuthorityBean;
import com.bocloud.paas.service.user.AuthService;
import com.bocloud.paas.service.user.util.AuthParentsComparator;
import com.bocloud.paas.service.user.util.AuthorityComparator;

/**
 * 权限抽象Service接口实现类
 * 
 * @author dongkai
 *
 */
@Service("authService")
public class AuthServiceImpl implements AuthService {

	private Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
	@Autowired
	private AuthDao authDao;
	@Autowired
	private RoleAuthDao roleAuthDao;
	@Autowired
	private TenantAuthDao tenantAuthDao;
	@Autowired
	private LockFactory lockFacroty;

	@Override
	public BsmResult list(Long parentId) {
		try {
			List<Authority> authoritys = authDao.list(parentId);
			for (Authority auth : authoritys) {
				List<Authority> children = authDao.list(auth.getId());
				if (null != children && !children.isEmpty()) {
					auth.setChildren("[]");
				}
			}
			Collections.sort(authoritys, new AuthorityComparator());
			return new BsmResult(true, authoritys, "查询权限成功");
		} catch (Exception e) {
			logger.error("list authority fail:", e);
			return new BsmResult(false, "查询权限失败", "", "");
		}
	}

	@Override
	public BsmResult create(Authority authority) {
		try {
			authority.setStatus(BaseStatus.NORMAL.name());
			authDao.save(authority);
			return new BsmResult(true, authority, "添加权限成功");
		} catch (Exception e) {
			logger.error("create authority fail:", e);
			return new BsmResult(false, "添加权限失败", "", "");
		}
	}

	@Override
	public BsmResult modify(AuthorityBean authority, Long userId) {
		String path = Authority.class.getSimpleName() + "_" + authority.getId();
		HarmonyLock lock = null;
		try {
			lock = lockFacroty.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Authority existAuthority = authDao.query(authority.getId());
			if (null == existAuthority) {
				logger.warn("existAuthority data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			existAuthority.setMenderId(userId);
			BeanUtils.copyProperties(authority, existAuthority);

			Boolean flag = authDao.update(existAuthority);
			return flag ? new BsmResult(true, "修改权限成功") : new BsmResult(false, "修改权限失败", "", "");
		} catch (Exception e) {
			logger.error("modify authority fail:", e);
			return new BsmResult(false, "修改权限失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}

		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = Authority.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		int result = 0;
		try {
			// 判断该权限下是否有子权限
			List<Authority> list = authDao.list(id);
			if (!list.isEmpty()) {
				return new BsmResult(false, "此权限存在子权限，删除失败", "", "");
			}
			lock = lockFacroty.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Authority existAuthority = authDao.query(id);
			if (null == existAuthority) {
				logger.warn("existAuthority data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			// 删除权限和角色、租户的关联关系
			roleAuthDao.deleteByAuthId(id);
			tenantAuthDao.deleteByAuthId(id);
			result = authDao.delete(id, userId);
		} catch (Exception e) {
			logger.error("remove auth fail:", e);
			return new BsmResult(false, "删除权限失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result > 0 ? new BsmResult(true, "删除权限成功") : new BsmResult(false, "删除权限失败", "", "");
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			Authority authority = authDao.query(id);
			return new BsmResult(true, authority, "查询权限详情成功");
		} catch (Exception e) {
			logger.error("get authority detail fail:", e);
			return new BsmResult(false, "查询权限详情失败", "", "");
		}
	}

	@Override
	public BsmResult listParents() {
		try {
			List<Authority> list = authDao.listParents();
			List<AuthParentsBean> parents = new ArrayList<>();
			for (Authority auth : list) {
				AuthParentsBean parent = new AuthParentsBean(auth.getId(), auth.getName(), auth.getPriority());
				parents.add(parent);
			}
			Collections.sort(parents, new AuthParentsComparator());
			return new BsmResult(true, parents, "查询父权限成功");
		} catch (Exception e) {
			logger.error("list authority parents fail:", e);
			return new BsmResult(false, "查询父权限失败", "", "");
		}
	}

	@Override
	public BsmResult listIcon() {
		try {
			List<Authority> list = authDao.listParents();
			List<AuthIcon> icons = new ArrayList<>();
			for (Authority auth : list) {
				AuthIcon icon = new AuthIcon(auth.getId(), auth.getIcon());
				icons.add(icon);
			}
			return new BsmResult(true, icons, "查询成功!");
		} catch (Exception e) {
			logger.error("list icon failure:", e);
			return new BsmResult(false, "查询失败！");
		}
	}

}
