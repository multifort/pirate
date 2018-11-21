package com.bocloud.paas.dao.repository;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Repository;
/**
 * 仓库Dao层
 * @author Zaney
 * @data:2017年3月15日
 * @describe:
 */
public interface RepositoryDao extends GenericDao<Repository, Long> {
	/**
	 * 查询仓库详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Repository query(Long id) throws Exception;
	/**
	 * 查询仓库列表
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Repository> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 查询仓库简易列表
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 查询仓库访问地址列表
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<com.bocloud.paas.entity.Repository> select(List<Param> params, Map<String, String> sorter, String deptId) throws Exception;
	/**
	 * 查询符合条件的数据数量
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;
	/**
	 * 删除仓库信息
	 * @param id
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteById(Long id, Long userId) throws Exception;
	/**
	 * 统计
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> countInfo(Long userId) throws Exception;
	
	/**
	 * @param address
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Repository selectRepository(String address, Integer port, String username, String password) throws Exception;
	
	/**
	 * 获取本地所有仓库
	 * @return
	 * @throws Exception
	 */
	public List<Repository> selectRepository() throws Exception;
	/**
	 * 名称和组织机构校验
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Boolean checkName(String name) throws Exception;
	
	/**
	 * 安装名称查询仓库
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Repository query(String name) throws Exception;

	/**
	 * @Author: langzi
	 * @param address
	 * @param port
	 * @param username
	 * @param password
	 * @Description:
	 * @Date: 12:05 2017/11/10
	 */
	public Repository query(String address, Integer port, String username, String password) throws Exception;
}
