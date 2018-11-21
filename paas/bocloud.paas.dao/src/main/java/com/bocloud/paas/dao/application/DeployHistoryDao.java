package com.bocloud.paas.dao.application;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.DeployHistory;

/**
 * 
 * @author zjm
 * @date 2017年4月4日
 */
public interface DeployHistoryDao {
	/**
	 * 添加
	 * @param deployHistory
	 * @return
	 * @throws Exception
	 */
	public boolean insert(DeployHistory deployHistory) throws Exception;
	
	/**
	 * 删除
	 * @param deployHistory
	 * @return
	 * @throws Exception
	 */
	public boolean delete(String[] fileds) throws Exception;

	/**
	 * 分页查询部署历史列表
	 * 
	 * @author zjm
	 * @date 2017年4月4日
	 *
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<DeployHistory> list(int page, int rows, List<Param> params, Map<String, String> sorter)
			throws Exception;
	/**
	 * 不分页查询部署历史列表
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<DeployHistory> list(List<Param> params, Map<String, String> sorter)
			throws Exception;
	/**
	 * 统计
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params) throws Exception;

}
