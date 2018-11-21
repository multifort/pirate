package com.bocloud.paas.dao.process;

import java.util.List;

import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Task;

/**
 * @Describe: 编排任务Dao层接口
 * @author Zaney
 * @2017年6月14日
 */
public interface TaskDao extends GenericDao<Task, Long> {
	/**
	 * 查询
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Task query(Long id) throws Exception;
	/**
	 * 检测名称
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Task selectTask(String name) throws Exception;
	/**
	 * 删除任务
	 * @param name
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean delete(String name, Long userId) throws Exception;
	/**
	 * 获取所有插件信息
	 * @return
	 * @throws Exception
	 */
	public List<Task> select() throws Exception;
}
