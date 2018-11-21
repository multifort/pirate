package com.bocloud.paas.dao.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.paas.dao.repository.RepositoryImageDao;
import com.bocloud.paas.entity.RepositoryImage;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
/**
 * 仓库镜像Dao层实现类
 * @author Zaney
 * @data:2017年3月15日
 * @describe:
 */
@Repository
public class RepositoryImageDaoImpl extends BasicDao implements RepositoryImageDao{

	@Override
	public boolean saveRepositoryImage(RepositoryImage repositoryImage) throws Exception {
		return this.baseSaveEntity(repositoryImage);
	}

	@Override
	public boolean deleteRepositoryImage(RepositoryImage repositoryImage) throws Exception {
		return this.baseDelete(repositoryImage);
	}

	@Override
	public List<RepositoryImage> getRepositoryImageById(Long id) throws Exception {
		String sql = "select * from repository_image_info where  id = :id";
		Map<String, Object> paramMap = MapTools.simpleMap("id", id);
		List<Object> list = this.queryForList(sql, paramMap, RepositoryImage.class);
		List<RepositoryImage> result = new ArrayList<>();
		for (Object object : list) {
			result.add((RepositoryImage) object);
		}
		return result;
	}

	@Override
	public boolean deleteRepositoryImage(Long imageId) throws Exception {
		String sql = "delete from repository_image_info where image_id = :imageId";
		Map<String, Object> params = MapTools.simpleMap("imageId", imageId);
		return this.update(sql, params) > 0;
	}
}
