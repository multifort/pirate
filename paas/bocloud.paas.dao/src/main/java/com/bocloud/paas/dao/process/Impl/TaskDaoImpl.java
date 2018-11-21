package com.bocloud.paas.dao.process.Impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.paas.dao.process.TaskDao;
import com.bocloud.paas.entity.Task;
/**
 * @Describe: 流程编排Dao层接口实现类
 * @author Zaney
 * @2017年6月15日
 */
@Repository("taskDao")
public class TaskDaoImpl extends JdbcGenericDao<Task, Long> implements TaskDao {

	@Override
	public Task query(Long id) throws Exception {
		String sql = "select * from task where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Task> list = this.list(Task.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return this.get(Task.class, id);
	}
	@Override
	public Task selectTask(String name) throws Exception {
		String sql = "select * from task where is_deleted = 0 and name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Task> list = this.list(Task.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	@Override
	public boolean delete(String name, Long userId) throws Exception {
		String sql = "update task set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}
	
	@Override
	public List<Task> select() throws Exception {
		String sql = "select * from task where is_deleted = 0 ";
		return this.list(Task.class, sql, null);
	}
}
