package com.bocloud.paas.dao.system.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.system.DictionaryDao;
import com.bocloud.paas.entity.Dictionary;

/**
 * 字典操作数据层接口实现类
 * 
 * @author luogan
 *
 */
@Component("dictionaryDao")
public class DictionaryDaoImpl extends JdbcGenericDao<Dictionary, Long>
		implements DictionaryDao {

	@Override
	public List<Dictionary> list() throws Exception {

		String sql = "select * from dictionary where is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", "");
		List<Dictionary> dictionarys = this.list(Dictionary.class, sql, params);
		List<Dictionary> dictionaries = new ArrayList<>();
		if (null != dictionarys) {
			for (Dictionary dict : dictionarys) {
				dictionaries.add(dict);
			}
		}
		return dictionaries;
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {

		String sql = "update dictionary set is_deleted = true , gmt_modify = :gmtModify where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);

	}

	@Override
	public Integer count(List<Param> params) throws Exception {

		String sql = "select count(1) from dictionary a where is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();

	}

	@Override
	public List<Dictionary> list(List<Param> params, Map<String, String> sorter)
			throws Exception {

		String sql = "select a.* from dictionary a where a.is_deleted = 0";
		sql = SQLHelper
				.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(Dictionary.class, sql, paramMap);
	}

	@Override
	public List<Dictionary> list(Integer page, Integer rows,
			List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select * from dictionary a  where is_deleted = 0";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(Dictionary.class, sql, paramMap);
	}

	@Override
	public Dictionary queryByKey(String dictKey) throws Exception {
		String sql = "select * from dictionary a where a.dict_key = :dict_key and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("dict_key", dictKey);
		List<Dictionary> dictionarys = this.list(Dictionary.class, sql, params);
		if (ListTool.isEmpty(dictionarys)) {
			return null;
		}
		return dictionarys.get(0);
	}

	@Override
	public List<Dictionary> queryById(Long id) throws Exception {
		String sql = "select * from dictionary a where a.id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Dictionary> dictionarys = this.list(Dictionary.class, sql, params);
		return dictionarys;
	}

	@Override
	public Dictionary query(String name) throws Exception {
		String sql = "select * from dictionary a where a.name = :name and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Dictionary> dictionarys = this.list(Dictionary.class, sql, params);
		if (ListTool.isEmpty(dictionarys)) {
			return null;
		}
		return dictionarys.get(0);
	}

}
