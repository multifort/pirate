package com.bocloud.paas.service.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.model.UserBean;

/**
 * 用户抽象Service接口
 * 
 * @author dongkai
 *
 */
public interface UserService {

	/**
	 * 用户登录
	 * 
	 * @param username
	 * @param password
	 * @param sessionId
	 * @return
	 */
	public BsmResult login(String username, String password, String sessionId);

	/**
	 * 用户退出
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult logout(Long id);

	/**
	 * 创建用户
	 * 
	 * @param user
	 * @return
	 */
	public BsmResult create(User user);

	/**
	 * 更新用户
	 * 
	 * @param user
	 * @return
	 */
	public BsmResult modify(UserBean bean, Long userId);

	/**
	 * 删除用户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 冻结用户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult lock(Long id, Long userId);

	/**
	 * 解冻用户
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult active(Long id, Long userId);

	/**
	 * 查看用户详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 根据用户姓名查询用户
	 * 
	 * @param username
	 * @return
	 */
	public BsmResult getByName(String username);

	/**
	 * 重置密码
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult reset(Long id, Long userId);

	/**
	 * 用户授权
	 * 
	 * @param id
	 * @param roles
	 * @param userId
	 * @return
	 */
	public BsmResult accredit(Long id, String roles, Long userId);

	/**
	 * 查询用户列表
	 * 
	 * @param page
	 * @param rows
	 * @param parseArray
	 * @param parseObject
	 * @param simple
	 * @return
	 */
	public BsmResult list(Integer page, Integer rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser user);

	/**
	 * 获取安全密钥
	 * 
	 * @param apiKey
	 * @return
	 */
	public BsmResult secKey(String apiKey);

	/**
	 * 校验密码
	 * 
	 * @param id
	 * @param password
	 * @return
	 */
	public BsmResult check(Long id, String password);

	/**
	 * 根据角色ID查询
	 * 
	 * @param roleId
	 * @return
	 */
	public BsmResult getByRid(Long roleId);

	/**
	 * 修改密码
	 * 
	 * @param id
	 * @param password
	 * @return
	 */
	public BsmResult change(Long id, String password);

	/**
	 * 检查用户是否异地登录
	 * 
	 * @param id
	 * @param sessionId
	 * @return
	 */
	public BsmResult checkUser(Long id, String sessionId);

	/**
	 * 校验用户名
	 * 
	 * @param username
	 * @return
	 */
	public BsmResult checkUsername(String username);

	/**
	 * 查询用户的角色
	 * 
	 * @param id
	 * @param tenantId
	 * @return
	 */
	public BsmResult listRoles(Long id, Long tenantId);

	/**
	 * 根据组织查询用户列表,不分页
	 * 
	 * @param departId
	 * @return
	 */
	public BsmResult listByDid(Long departId);

	public User getUser(String username, String password);

	/**
	 * 获取某个租户下面的全部用户
	 * 
	 * @author sxp
	 * @since V1.0 Dec 30, 2016
	 */
	public List<User> getUsersByTenantId(Long tenantId);

	/**
	 * 查询用户所在机构以及子机构的ID
	 * 
	 * @param id
	 * @return
	 */
	public String listDept(Long id);
	

	/**
	 * 校验用户工号
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult checkUseId(String userId);

}
