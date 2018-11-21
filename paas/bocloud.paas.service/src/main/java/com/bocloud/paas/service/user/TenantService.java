package com.bocloud.paas.service.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Tenant;
import com.bocloud.paas.model.TenantBean;

/**
 * 租户抽象Service接口
 * 
 * @author dongkai
 *
 */
public interface TenantService {

	/**
	 * 创建租户
	 * 
	 * @param tenant
	 * @return
	 */
	public BsmResult create(Tenant tenant, String authIds);

	/**
	 * 更新租户
	 * 
	 * @param tenant
	 * @return
	 */
	public BsmResult modify(TenantBean tenant, Long userId);

	/**
	 * 删除租户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 冻结租户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult lock(Long id, Long userId);

	/**
	 * 解冻租户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult active(Long id, Long userId);

	/**
	 * 重置密码
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult reset(Long id, Long userId);

	/**
	 * 查看租户详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 租户授权
	 * 
	 * @param id
	 * @param roles
	 * @param userId
	 * @return
	 */
	public BsmResult accredit(Long id, String roles, Long userId);

	/**
	 * 查询租户列表
	 * 
	 * @param page
	 * @param rows
	 * @param parseArray
	 * @param parseObject
	 * @param simple
	 * @return
	 */
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple);

	/**
	 * 查询租户下的用户
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult listUsers(int page, int rows, List<Param> params, Map<String, String> sorter);

	/**
	 * 查询租户下的角色
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult listRoles(int page, int rows, List<Param> params, Map<String, String> sorter);

	/**
	 * 查询租户下的权限
	 * 
	 * @param id
	 * @param parentId
	 * @return
	 */
	public BsmResult listAuths(Long id, Long parentId);

	/**
	 * 校验租户
	 * 
	 * @param email
	 * @return
	 */
	public BsmResult checkTenant(String email);
}
