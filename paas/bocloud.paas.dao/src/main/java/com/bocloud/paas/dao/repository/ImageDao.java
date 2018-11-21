package com.bocloud.paas.dao.repository;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Image;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
public interface ImageDao extends GenericDao<Image, Long> {

	/**
	 * 查询镜像详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Image query(Long id) throws Exception;

	/**
	 * 查询镜像列表
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Image> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptId)
			throws Exception;

	/**
	 * 查询镜像简易列表
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param sorter
	 * @param select image为查询镜像名称下拉框，tag为查询镜像标签下拉框, full为查询镜像名称和标签的全称
	 * @return
	 * @throws Exception
	 */
	public List<Image> list(List<Param> params, Map<String, String> sorter, String deptId, String select) throws Exception;
	
	/**
	 * 查询符合条件的数据数量
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptId, Long repositoryId) throws Exception;

	/**
	 * 删除镜像信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteById(Long id, Long userId) throws Exception;

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean deleteRepositoryImageInfo(Long id) throws Exception;

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean deleteImageAppInfo(Long id) throws Exception;

	/**
	 * @param id
	 * @param deptId
	 * @return
	 * @throws Exception
	 */
	public boolean authorize(Long id, Long userId, Long deptId) throws Exception;

	/**
	 * 获取所有的镜像
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Image> queryAll() throws Exception;
	
	/**
	 * 根据镜像uuid查询镜像
	 * @param uuid
	 * @return
	 * @throws Exception
	 */
	public Image query(String uuid) throws Exception;
	
	/**
	 * 根据镜像仓库、命名空间、名称和标签查询镜像
	 * @param repositoryId
	 * @param namespace
	 * @param name
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	public Image query(Long repositoryId, String namespace, String name, String tag) throws Exception;
	/**
	 * 根据镜像IP、命名空间、名称和标签查询镜像
	 * @param ip
	 * @param namespace
	 * @param name
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	public Image query(String address, String namespace, String name, String tag) throws Exception;
}
