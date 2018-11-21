package com.bocloud.paas.dao.user;

import java.util.List;

import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Authority;

/**
 * 权限抽象DAO接口
 * 
 * @author dongkai
 *
 */
public interface AuthDao extends GenericDao<Authority, Long> {

	/**
	 * 根据父类ID查询
	 * 
	 * @param parentId
	 * @return
	 * @throws Exception
	 */

	public List<Authority> list(Long parentId) throws Exception;

	/**
	 * 查询父节点
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Authority> listParents() throws Exception;

	/**
	 * 根据租户ID和父ID查询
	 * 
	 * @param parentId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public List<Authority> list(Long parentId, Long tenantId) throws Exception;

	/**
	 * 查询所有
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Authority> list() throws Exception;

	/**
	 * 根据角色ID查询权限
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public List<Authority> listByRid(Long roleId) throws Exception;

	/**
	 * 根据角色ID查询权限
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public List<Authority> listByTid(Long tenantId) throws Exception;

	/**
	 * 删除权限
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public int delete(Long id, Long menderId) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Authority query(Long id) throws Exception;

}
