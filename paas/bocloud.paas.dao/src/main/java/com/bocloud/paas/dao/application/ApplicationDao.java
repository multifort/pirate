package com.bocloud.paas.dao.application;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Application;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
public interface ApplicationDao extends GenericDao<Application, Long> {

	/**
	 * 查询应用详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Application query(Long id) throws Exception;

	/**
	 * 按照名称查询应用
	 * 
	 * @author zjm
	 * @date 2017年4月14日
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Application query(String name) throws Exception;
	/**
	 * 查找应用
	 * @param name
	 * @param envId
	 * @return
	 * @throws Exception
	 */
	public Application query(String name, Long envId) throws Exception;

	/**
	 * 查询应用列表
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Application> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds)
			throws Exception;

	/**
	 * 
	 * 
	 * @author zjm
	 * @date 2017年4月14日
	 *
	 * @param page
	 * @param rows
	 * @param layoutId
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Application> listByLayout(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;

	/**
	 * 查询应用简易列表
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	
	public List<Application> select(String deptIds) throws Exception;

	/**
	 * 查询符合条件的数据数量
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;

	/**
	 * 按照编排id查询数量
	 * 
	 * @author zjm
	 * @date 2017年4月14日
	 *
	 * @param layoutId
	 * @return
	 * @throws Exception
	 */
	public int countByLayout(List<Param> params) throws Exception;

	/**
	 * 删除应用信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean delete(Long id, Long userId) throws Exception;

	/**
	 * 根据应用是否部署统计数量
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> countApp(String deptId) throws Exception;

	/**
	 * 删除应用和文件的关联信息
	 * 
	 * @param appId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteAppLayoutInfo(Long appId) throws Exception;

	/**
	 * 删除应用和镜像的关联信息
	 * 
	 * @param appId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteAppImageInfo(Long appId) throws Exception;

	/**
	 * 删除应用和openshift集群的关联信息
	 * 
	 * @param appId
	 * @return
	 * @throws Exception
	 */
	public boolean delateAppClusterInfo(Long appId) throws Exception;

	/**
	 * 查询所有的应用
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Application> queryAll(String deptIds) throws Exception;

	/**
	 * 根据环境Id查询所有的应用
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Application> queryByEnvId(Long envId) throws Exception;
	/**
	 * 查找应用对象信息
	 * @param envId
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public Application detail(Long envId, String namespace) throws Exception;

}
