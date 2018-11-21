package com.bocloud.paas.service.user;

import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.entity.Department;
import com.bocloud.paas.model.DepartmentBean;

/**
 * 组织机构抽象Service接口
 * 
 * @author dongkai
 *
 */
public interface DepartmentService {

	/**
	 * 创建组织机构
	 * 
	 * @param authority
	 * @return
	 */
	public BsmResult create(Department department);

	/**
	 * 修改组织机构
	 * 
	 * @param authority
	 * @return
	 */
	public BsmResult modify(DepartmentBean department, Long userId);

	/**
	 * 删除组织机构
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 组织机构详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 查询组织机构
	 * 
	 * @param parentId
	 * @return
	 */
	public BsmResult list(Long parentId, Long tenantId);

	/**
	 * 是否存在
	 * 
	 * @param name
	 * @return
	 */
	public BsmResult exists(String name, Long tenantId);
}
