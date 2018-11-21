package com.bocloud.paas.dao.repository;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.RepositoryImageInfo;

public interface RepositoryImageInfoDao extends GenericDao<RepositoryImageInfo, Long> {
	/**
	 * 根据仓库ID获取该仓库下的数据库镜像信息   分页
	 * @param page
	 * @param rows
	 * @param repositoryId
	 * @return
	 * @throws Exception
	 */
	public List<RepositoryImageInfo> selectRepositoryImage(int page, int rows,List<Param> params, String deptId) throws Exception;
	/**
	 * 根据仓库ID获取该仓库下的数据库镜像信息 
	 * @param repositoryId
	 * @return
	 * @throws Exception
	 */
	public List<RepositoryImageInfo> selectRepositoryImage(Long repositoryId) throws Exception;
	/**
	 * 查询符合条件的数据数量
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptId) throws Exception;
	/**
	 * 根据镜像ID获取该镜像的数据库镜像信息 
	 * @param repositoryId
	 * @return
	 * @throws Exception
	 */
	public RepositoryImageInfo getByImageId(Long imageId) throws Exception;
	/**
	 * 获取镜像名与命名空间
	 * @param params
	 * @param sorter
	 * @param deptId
	 * @return
	 * @throws Exception
	 */
	public List<RepositoryImageInfo> list(List<Param> params, Map<String, String> sorter, String deptId) throws Exception;
	
}
