package com.bocloud.paas.dao.repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.paas.dao.repository.RepositoryImageInfoDao;
import com.bocloud.paas.entity.RepositoryImageInfo;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
/**
 * 
 * @author Zaney
 * @data:2017年3月14日
 * @describe:
 */
@Repository("repositoryImageInfoDao")
public class RepositoryImageInfoDaoImpl extends JdbcGenericDao<RepositoryImageInfo, Long> implements RepositoryImageInfoDao {
	@Override
	public List<RepositoryImageInfo> selectRepositoryImage(int page, int rows,List<Param> params, String deptId ) throws Exception {
		String sql = "select repository_image_info.repository_id,repository_image_info.namespace,repository_image_info.image_id,image.* "
				+ "from repository_image_info  LEFT JOIN image  on repository_image_info.image_id = image.id "
				+ "WHERE image.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, null, null);
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", deptId);
		return this.list(RepositoryImageInfo.class, sql, paramMap);
	}
	
	@Override
	public List<RepositoryImageInfo> selectRepositoryImage(Long repositoryId) throws Exception {
		String sql = "select r.id reg_id,r.namespace,r.image_id,r.repository_id,s.* from repository_image_info r LEFT JOIN image s on r.image_id = s.id "
				+ "WHERE s.is_deleted = 0 and r.repository_id = :repositoryId";
		Map<String, Object> params = MapTools.simpleMap("repositoryId", repositoryId);
		return this.list(RepositoryImageInfo.class, sql, params);
	}
	@Override
	public int count(List<Param> params, String deptId) throws Exception {
		String sql = "select count(1) from repository_image_info LEFT JOIN image  "
				+ "on repository_image_info.image_id = image.id "
				+ "WHERE image.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, params, null, null);
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", deptId);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public RepositoryImageInfo getByImageId(Long imageId) throws Exception {
		String sql = "select rs.namespace,rs.image_id,rs.repository_id,r.name as repository_name,s.* from repository_image_info rs "
				+ "LEFT JOIN image s on rs.image_id = s.id "
				+ "LEFT JOIN repository r on rs.repository_id = r.id  "
				+ "WHERE s.is_deleted = 0 and rs.image_id = :imageId";
		Map<String, Object> params = MapTools.simpleMap("imageId", imageId);
		List<RepositoryImageInfo> repositoryImageInfos = this.list(RepositoryImageInfo.class, sql, params);
		if (!ListTool.isEmpty(repositoryImageInfos)) {
			return repositoryImageInfos.get(0);
		}
		return null;
	}
	
	@Override
	public List<RepositoryImageInfo> list(List<Param> params, Map<String, String> sorter, String deptId) throws Exception {
		String sql = "select repository_image_info.repository_id,repository_image_info.namespace,repository_image_info.image_id,"
				+ "image.name,image.tag "
				+ "from repository_image_info  LEFT JOIN image  on repository_image_info.image_id = image.id "
				+ "WHERE image.is_deleted = 0 ";
		if (null != deptId) {
			sql += "and (image.dept_id in (:deptId) or repository_image_info.namespace = 'libaray')";
		}
		sql = SQLHelper.buildRawSql(sql, params, null, null);
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", deptId);
		return this.list(RepositoryImageInfo.class, sql, paramMap);
	}
	
}
