package com.bocloud.paas.service.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.Role;
import com.bocloud.paas.model.RoleBean;

/**
 * 角色抽象Service接口
 * 
 * @author dongkai
 *
 */
public interface RoleService {

	/**
	 * 创建角色
	 * 
	 * @param role
	 * @return
	 */
	public BsmResult create(Role role);

	/**
	 * 修改角色信息
	 * 
	 * @param role
	 * @return
	 */
	public BsmResult modify(RoleBean bean, Long userId);

	/**
	 * 删除角色(逻辑删除更新状态)
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 查询角色详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 授权角色权限
	 * 
	 * @param id
	 * @param authoritys
	 * @param userId
	 * @return
	 */
	public BsmResult accredit(Long id, String authoritys, Long userId);

	/**
	 * 查询角色列表
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser);

	/**
	 * 查询角色权限
	 * 
	 * @param id
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
	public BsmResult listAuths(Long id, Long tenantId, Long parentId);

}
