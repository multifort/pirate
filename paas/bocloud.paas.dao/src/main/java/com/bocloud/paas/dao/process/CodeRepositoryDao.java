package com.bocloud.paas.dao.process;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.CodeRepository;

/**
 * describe: 代码仓库DAO层接口
 * @author Zaney
 * @data 2017年10月27日
 */
public interface CodeRepositoryDao extends GenericDao<CodeRepository, Long> {
	/**
	 * 校验名称
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public CodeRepository existed(String name) throws Exception;
	/**
	 * 查找详情
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public CodeRepository detail(Long id) throws Exception;
	/**
	 * 查询
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<CodeRepository> list(int page, int rows, List<Param> params, Map<String, String> sorter, 
			String deptIds) throws Exception;
	/**
	 * 查询
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<CodeRepository> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;
	/**
	 * 统计
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;

}
