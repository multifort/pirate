package com.bocloud.paas.dao.application;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.ConfigManage;

/**
 * describe: 配置管理与数据库交互层接口层
 * @author Zaney
 * @data 2017年10月17日
 */
public interface ConfigManageDao extends GenericDao<ConfigManage, Long> {
	/**
	 * 校验名称，需要的条件：同一环境，同一命名空间，不允许重复
	 * @param appId
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ConfigManage existed(Long appId, String name) throws Exception;
	/**
	 * 查找详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public ConfigManage detail(Long id) throws Exception;
	/**
	 * 查询
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<ConfigManage> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 查询
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 统计
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;
	/**
	 * 根据名称获取信息
	 * @param name 配置实例名称
	 * @param appName  应用名称
	 * @param envName  环境名称
	 * @return
	 * @throws Exception
	 */
	public ConfigManage detail(String name, String appName, String envName) throws Exception;
	/**
	 * 获取某应用下的所有配置管理项
	 * @param applicationId
	 * @return
	 * @throws Exception
	 */
	public List<ConfigManage> list(Long applicationId) throws Exception;
}
