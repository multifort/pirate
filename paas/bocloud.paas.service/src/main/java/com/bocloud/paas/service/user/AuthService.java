package com.bocloud.paas.service.user;

import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.model.AuthorityBean;

/**
 * 权限抽象Service接口
 * 
 * @author dongkai
 *
 */
public interface AuthService {

	/**
	 * 创建权限
	 * 
	 * @param authority
	 * @return
	 */
	public BsmResult create(Authority authority);

	/**
	 * 修改权限
	 * 
	 * @param authority
	 * @return
	 */
	public BsmResult modify(AuthorityBean authority, Long userId);

	/**
	 * 删除权限
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 权限详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 查询权限
	 * 
	 * @param parentId
	 * @return
	 */
	public BsmResult list(Long parentId);

	/**
	 * 查询父节点
	 * 
	 * @return
	 */
	public BsmResult listParents();

	/**
	 * 查询图标
	 * 
	 * @return
	 */
	public BsmResult listIcon();

}
