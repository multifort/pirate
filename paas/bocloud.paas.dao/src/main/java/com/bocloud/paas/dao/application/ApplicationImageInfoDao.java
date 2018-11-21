package com.bocloud.paas.dao.application;

import java.util.List;

import com.bocloud.paas.entity.ApplicationImageInfo;

/**
 * 
 * @author zjm
 * @date 2017年3月18日
 */
public interface ApplicationImageInfoDao {

	/**
	 * 保存
	 * 
	 * @param applicationImageInfo
	 * @return
	 * @throws Exception
	 */
	public boolean insert(ApplicationImageInfo applicationImageInfo) throws Exception;

	/**
	 * 删除
	 * 
	 * @param applicationImageInfo
	 * @return
	 * @throws Exception
	 */
	public boolean delete(ApplicationImageInfo applicationImageInfo) throws Exception;

	/**
	 * 修改
	 * 
	 * @author zjm
	 * @date 2017年5月12日
	 *
	 * @param applicationImageInfo
	 * @return
	 * @throws Exception
	 */
	public boolean update(ApplicationImageInfo applicationImageInfo) throws Exception;

	/**
	 * 根据应用和镜像获取应用镜像中间表
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 *
	 * @param appId
	 * @param imageId
	 * @return
	 * @throws Exception
	 */
	public ApplicationImageInfo detail(Long appId, Long imageId) throws Exception;

	/**
	 * 根据应用id获取镜像id信息
	 * 
	 * @param appId
	 * @return
	 * @throws Exception
	 */
	public List<ApplicationImageInfo> select(Long appId) throws Exception;

}
