package com.bocloud.paas.service.user.impl;

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
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.entity.Department;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.model.DepartmentBean;
import com.bocloud.paas.service.user.DepartmentService;

/**
 * 组织机构Service接口实现
 * 
 * @author dongkai
 *
 */
@Service("departmentService")
public class DepartmentServiceImpl implements DepartmentService {
	private Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
	@Autowired
	private DepartmentDao departmentDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private LockFactory lockFactory;

	@Override
	public BsmResult create(Department department) {
		try {
			department.setStatus(BaseStatus.NORMAL.name());
			BsmResult result = exists(department.getName(), department.getParentId());
			if (result.isSuccess()) {
				return new BsmResult(false, "该组织机构名称已存在!");
			}
			departmentDao.save(department);
			return new BsmResult(true, department, "添加组织机构成功!");
		} catch (Exception e) {
			logger.error("create department fail:", e);
			return new BsmResult(false, "添加组织机构失败", "", "");
		}
	}

	@Override
	public BsmResult modify(DepartmentBean department, Long userId) {
		String path = Authority.class.getSimpleName() + "_" + department.getId();
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Department depart = departmentDao.query(department.getId());
			if (null == depart) {
				logger.warn("depart data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			BsmResult result = exists(department.getName(), depart.getTenantId());
			if (result.isSuccess()) {
				return new BsmResult(false, "该组织机构名称已存在!");
				/*Department dept = (Department) result.getData();
				if (!dept.getId().equals(depart.getId())) {
					return new BsmResult(false, "此名称已存在!");
				}*/
			}
			depart.setMenderId(userId);
			BeanUtils.copyProperties(department, depart);
			Boolean flag = departmentDao.update(depart);
			return flag ? new BsmResult(true, "修改组织机构成功") : new BsmResult(false, "修改组织机构失败", "", "");
		} catch (Exception e) {
			logger.error("modify department fail:", e);
			return new BsmResult(false, "修改组织机构失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = Department.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		int result = 0;
		try {
			List<Department> children = departmentDao.list(id);
			List<User> userList = userDao.listByDid(id);
			if (!children.isEmpty()) {
				return new BsmResult(false, "此组织机构存在子机构，删除失败", "", "");
			}
			if (!userList.isEmpty()) {
				return new BsmResult(false, "此组织机构关联用户，删除失败", "", "");
			}
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时", "", "");
			}
			Department depart = departmentDao.query(id);
			if (null == depart) {
				logger.warn("depart data not exist");
				return new BsmResult(false, "数据不存在", "", "");
			}
			result = departmentDao.delete(id, userId);
		} catch (Exception e) {
			logger.error("remove department fail:", e);
			return new BsmResult(false, "删除组织机构失败", "", "");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
		return result > 0 ? new BsmResult(true, "删除组织机构成功") : new BsmResult(false, "删除组织机构失败", "", "");
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			Department department = departmentDao.query(id);
			return new BsmResult(true, department, "查询组织机构详情成功");
		} catch (Exception e) {
			logger.error("get department detail fail:", e);
			return new BsmResult(false, "查询组织机构详情失败", "", "");
		}
	}

	@Override
	public BsmResult list(Long parentId, Long tenantId) {
		try {
			List<Department> departments = departmentDao.list(parentId, tenantId);
			for (Department department : departments) {
				List<Department> children = departmentDao.list(department.getId());
				if (!children.isEmpty()) {
					department.setChildren("[]");
				}
			}
			return new BsmResult(true, departments, "查询组织机构成功");
		} catch (Exception e) {
			logger.error("list department fail:", e);
			return new BsmResult(false, "查询组织机构失败", "", "");
		}
	}

	@Override
	public BsmResult exists(String name, Long parentId) {
		try {
			Department department = departmentDao.exists(name, parentId);
			if (null == department) {
				return new BsmResult(false, "该组织机构不存在!");
			} else {
				return new BsmResult(true, department, "该组织机构名称已存在!");
			}
		} catch (Exception e) {
			logger.error("get department failure:", e);
			return new BsmResult(false, "该组织机构不存在!");
		}
	}

}
