package com.bocloud.paas.dao.system;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Dictionary;

/**
 * 字典表数据操作接口
 * 
 * @author luogan
 *
 */
public interface DictionaryDao extends GenericDao<Dictionary, Long> {

	public List<Dictionary> list() throws Exception;

	public Integer remove(Long id, Long userId) throws Exception;

	public Integer count(List<Param> params) throws Exception;

	public List<Dictionary> list(List<Param> params, Map<String, String> sorter)
			throws Exception;

	public List<Dictionary> list(Integer page, Integer rows,
			List<Param> params, Map<String, String> sorter) throws Exception;

	public Dictionary queryByKey(String dictKey) throws Exception;

	public List<Dictionary> queryById(Long id) throws Exception;
	/**
	 * 名称校验
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Dictionary query(String name) throws Exception;
	
}
