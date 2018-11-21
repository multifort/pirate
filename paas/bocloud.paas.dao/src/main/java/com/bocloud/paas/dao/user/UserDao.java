package com.bocloud.paas.dao.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.User;

/**
 * 用户抽象DAO接口
 * 
 * @author dongkai
 *
 */
public interface UserDao extends GenericDao<User, Long> {

	/**
	 * @param username（账号）
	 * @return
	 * @throws Exception
	 */
	public User getByName(String username) throws Exception;

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
	public List<User> list(int page, int rows, List<Param> params, Map<String, String> sorter, Long tenantId, String deptIds) throws Exception;

	/**
	 * 简易查询
	 * 
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 根据租户ID查询
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public List<User> list(Long tenantId) throws Exception;

	/**
	 * 根据组织机构ID查询
	 * 
	 * @param departId
	 * @return
	 * @throws Exception
	 */
	public List<User> listByDid(Long departId) throws Exception;

	/**
	 * 根据租户ID查询
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public List<User> listByTid(Long tenantId) throws Exception;

	/**
	 * 根据角色ID查询
	 * 
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public List<User> listByRid(Long roleId) throws Exception;

	/**
	 * 删除用户
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean delete(Long id, Long userId) throws Exception;

	/**
	 * 删除租户
	 * 
	 * @param tenantId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByTid(Long tenantId, Long userId) throws Exception;

	/**
	 * 锁定用户
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean lock(Long id, Long userId) throws Exception;

	/**
	 * 激活用户
	 * 
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public boolean active(Long id, Long userId) throws Exception;

	/**
	 * 判断用户登录
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public User login(String username, String password) throws Exception;

	/**
	 * 查询用户数量
	 * 
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;

	/**
	 * 
	 * 获取用户信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public User query(Long id) throws Exception;

	/**
	 * 根据email查询
	 * 
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public User getByEmail(String email) throws Exception;

	/**
	 * 
	 * 获取当前租户的用户个数
	 * 
	 * @return
	 * @throws Exception
	 */
	public int tenantUserCount(Long tenantId) throws Exception;
	
	/**
	 * 根据userId查询
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public User getByUserId(String userId) throws Exception;
}
