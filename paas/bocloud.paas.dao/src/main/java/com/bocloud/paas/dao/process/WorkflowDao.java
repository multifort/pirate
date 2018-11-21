package com.bocloud.paas.dao.process;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Workflow;

/**
 * @Describe: 流程编排Dao层接口
 * @author Zaney
 * @2017年6月14日
 */
public interface WorkflowDao extends GenericDao<Workflow, Long> {
	/**
	 * 检测名称
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Workflow checkName(String name, Integer version) throws Exception;
	/**
	 * 从数据库获取页面模板排版信息
	 * @param name
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public Workflow selectWorkflow(String name, Integer version) throws Exception;
	/**
	 * 列表查询所有WorkflowDef
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Workflow> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 查询仓库简易列表
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 查询符合条件的数据数量
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;
	/**
	 * 根据ID获取工作流信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Workflow query(Long id) throws Exception;
}
