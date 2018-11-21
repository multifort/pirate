package com.bocloud.paas.dao.repository;

import java.util.List;

import com.bocloud.paas.entity.RepositoryImage;
/**
 * 仓库镜像Dao层
 * @author Zaney
 * @data:2017年3月15日
 * @describe:
 */
public interface RepositoryImageDao {
	
	/**
	 * 保存
	 * @param repositoryImage
	 * @return
	 * @throws Exception
	 */
	public boolean saveRepositoryImage(RepositoryImage repositoryImage) throws Exception;
	/**
	 * 删除
	 * @param repositoryImage
	 * @return
	 * @throws Exception
	 */
	public boolean deleteRepositoryImage(RepositoryImage repositoryImage) throws Exception;
	/**
	 * 按照关联表id查询关联表信息
	 * @param imageId
	 * @return
	 * @throws Exception
	 */
	public List<RepositoryImage> getRepositoryImageById(Long imageId) throws Exception;

	/**
	 * @Author: langzi
	 * @param imageId
	 * @Description: 根据镜像的id删除仓库镜像关联表的信息
	 * @Date: 16:37 2017/10/30
	 */
	public boolean deleteRepositoryImage(Long imageId) throws Exception;
	
}
