package com.bocloud.paas.dao.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Tenant;

/**
 * 租户抽象DAO接口
 * 
 * @author dongkai
 *
 */
public interface TenantDao extends GenericDao<Tenant, Long> {

	/**
	 * 分页查询
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Tenant> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;

	/**
	 * 查询所有租户，不分页
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Tenant> listAll() throws Exception;

	/**
	 * 删除租户
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean delete(Long id, Long userId) throws Exception;

	/**
	 * 锁定租户
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean lock(Long id, Long userId) throws Exception;

	/**
	 * 激活租户
	 * 
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public boolean active(Long id, Long userId) throws Exception;

	/**
	 * 查询租户数量
	 * 
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params) throws Exception;

	/**
	 * 根据邮箱查询
	 * 
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public Tenant getByEmail(String email) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Tenant query(Long id) throws Exception;
}
