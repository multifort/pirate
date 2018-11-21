package com.bocloud.paas.dao.application;

import java.util.List;
import com.bocloud.paas.entity.ServiceRelyInfo;
/**
 * describe: 服务依赖dao层
 * @author Zaney
 * @data 2017年8月1日
 */
public interface ServiceRelyInfoDao {
	/**
	 * 保存服务之间的依赖关系
	 * @param workflowJobInfo
	 * @return
	 * @throws Exception
	 */
	public boolean insert(ServiceRelyInfo serviceRelyInfo) throws Exception;
	/**
	 * 删除工作流与job信息
	 * @param workflowJobInfo
	 * @return
	 * @throws Exception
	 */
	public boolean delete(ServiceRelyInfo serviceRelyInfo) throws Exception;
	/**
	 * 获取符合条件的所有对象
	 * @return
	 * @throws Exception
	 */
	public List<ServiceRelyInfo> select(String currentName, String currentNamespace) throws Exception;
	/**
	 * 获取符合条件的所有对象
	 * @return
	 * @throws Exception
	 */
	public List<ServiceRelyInfo> list(String relyName, String relyNamespace) throws Exception;
}
