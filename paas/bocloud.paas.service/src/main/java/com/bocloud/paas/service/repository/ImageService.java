package com.bocloud.paas.service.repository;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.Image;

/**
 * 应用镜像service接口
 * 
 * @author zjm
 * @date 2017年3月17日
 * @describe
 */
public interface ImageService {

	/**
	 * 列表查询
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param requestUser
	 * @param choice
	 *            选择显示类型为full：镜像id:镜像名+标签，显示类型为name：镜像id:镜像名, 显示类型为tag：镜像id:镜像标.
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser, String choice);

	/**
	 * 添加信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param requestUser
	 * @param image
	 * @return
	 */
	public BsmResult create(RequestUser requestUser, Image image);

	/**
	 * 获取详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 删除信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param ids
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 修改信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param image
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Image image, Long userId);

	/**
	 * 导入镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param user
	 * @param registryId
	 * @param fileName
	 * @param project 项目（命名空间）
	 * @return
	 */
	public BsmResult load(RequestUser requestUser, Long registryId, String file, String project);

	/**
	 * 获取镜像信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param imageId
	 * @return
	 */
	public BsmResult inspect(Long imageId);

	/**
	 * 删除镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param registryId
	 * @param imageId
	 * @return
	 */
	public BsmResult remove(Long registryId, Long imageId);

	/**
	 * 统计镜像
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @return
	 */
	public BsmResult count(RequestUser requestUser);


	/**
	 * 镜像授权
	 * 
	 * @param imageId
	 * @param deptId
	 * @return
	 */
	public BsmResult authorize(Long imageId, Long userId, Long deptId);

	/**
	 * 服务依赖镜像列表查询
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 */
	public BsmResult list(JSONArray array);

	/**
	 * 源码构建
	 * 
	 * @param baseImage
	 *            基础镜像
	 * @param pomPath
	 *            pom文件位置
	 * @param repositoryUrl
	 *            源码地址
	 * @param project
	 *            项目名称（命名空间）
	 * @param repositoryBranch
	 *            源码分支
	 * @param repositoryUsername
	 *            源码账户
	 * @param repositoryPassword
	 *            源码密码
	 * @param warName
	 *            源码包名
	 * @param newImageName
	 *            新镜像名（包含命名空间，镜像名和版本号）
	 * @param registryId
	 *            仓库id
	 * @return
	 */
	public BsmResult buildBySource(String baseImage, String pomPath, String repositoryUrl, String project, String repositoryBranch,
			String repositoryUsername, String repositoryPassword, String warName, String newImageName, Long registryId,
			RequestUser requestUser);

	/**
	 * 获取镜像的所有项目名（命名空间）
	 * @param registryId
	 * @return
	 */
	public BsmResult getProjects(Long registryId);
}
