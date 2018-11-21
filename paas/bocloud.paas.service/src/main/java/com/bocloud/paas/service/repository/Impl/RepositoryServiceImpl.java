package com.bocloud.paas.service.repository.Impl;

import com.bocloud.common.model.*;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.enums.CommonEnum.Status;
import com.bocloud.paas.common.enums.RepositoryAuthMode;
import com.bocloud.paas.common.enums.RepositoryType;
import com.bocloud.paas.common.enums.Requestprotocol;
import com.bocloud.paas.common.harbor.HarborClient;
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.entity.*;
import com.bocloud.paas.service.app.resource.CommonServiceImpl;
import com.bocloud.paas.service.repository.RepositoryService;
import com.bocloud.paas.service.repository.util.RegistryClient;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 仓库实现类
 *
 * @author Zaney
 */
@Service("registryService")
public class RepositoryServiceImpl extends CommonServiceImpl implements RepositoryService {

    private static Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private UserService userService;

    @Override
    public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
                          RequestUser requestUser) {
        BsmResult bsmResult = new BsmResult(false, "查询仓库信息异常！");
        List<Repository> registrys = null;
        List<SimpleBean> beans = null;
        int total = 0;
        GridBean gridBean = null;
        try {
            if (ListTool.isEmpty(params)) {
                params = new ArrayList<Param>();
            }
            // 2、list列表查询
            if (null == sorter) {
                sorter = Maps.newHashMap();
            }
            sorter.put("gmtCreate", Common.ONE);
            String deptId = userService.listDept(requestUser.getId());
            total = registryDao.count(params, deptId);
            if (simple) {
                beans = registryDao.list(params, sorter, deptId);
                gridBean = new GridBean(1, 1, total, beans);
            } else {
                registrys = registryDao.list(page, rows, params, sorter, deptId);
                if (!ListTool.isEmpty(registrys)) {
                    for (Repository registry : registrys) {
                        // 获取用户信息
                        User creator = userDao.query(registry.getCreaterId());
                        registry.setCreatorName(creator.getName());
                        // 获取修改者信息
                        User mender = userDao.query(registry.getMenderId());
                        registry.setMenderName(mender.getName());
                    }
                }
                gridBean = GridHelper.getBean(page, rows, total, registrys);
            }
            bsmResult.setMessage("查询仓库信息成功！");
            bsmResult.setData(gridBean);
            bsmResult.setSuccess(true);
        } catch (Exception e) {
            logger.error("list registry exception:", e);
        }
        saveLog("registry/operation", "仓库列表展示", bsmResult.getMessage(), requestUser.getId());
        return bsmResult;
    }
    
	@Override
    public BsmResult listAddress(List<Param> params, Map<String, String> sorter, RequestUser requestUser) {
        BsmResult bsmResult = new BsmResult(false, "查询仓库地址列表异常！");
        List<Repository> repositories = null;
        List<SimpleBean> beans = new ArrayList<>();
        int total = 0;
        GridBean gridBean = null;
        try {
            if (ListTool.isEmpty(params)) {
                params = new ArrayList<Param>();
            }
            // 2、list列表查询
            if (null == sorter) {
                sorter = Maps.newHashMap();
            }
            sorter.put("gmtCreate", Common.ONE);
            String deptId = userService.listDept(requestUser.getId());
            total = registryDao.count(params, deptId);
            repositories = registryDao.select(params, sorter, deptId);
            if (!ListTool.isEmpty(repositories)) {
            	for (com.bocloud.paas.entity.Repository repository : repositories) {
        			String url = "";
        			String http = null;
        			Requestprotocol requestprotocol = Requestprotocol.values()[repository.getProtocol()];
        			switch (requestprotocol) {
    				case HTTP:
    					http = "http://";
    					break;
    					
                    case HTTPS:
                    	http = "https://";
    					break;

    				default:
    					break;
    				}
        			if (repository.getType() == 1) {
        				url = http + repository.getAddress();
        			} else {
        				url = http + repository.getAddress() + ":" + repository.getPort();
        			}
        			beans.add(new SimpleBean("", repository.getName(), url));
        		}
			}
            
            gridBean = new GridBean(1, 1, total, beans);
            bsmResult.setMessage("查询仓库地址列表成功！");
            bsmResult.setData(gridBean);
            bsmResult.setSuccess(true);
        } catch (Exception e) {
            logger.error("list registry exception:", e);
        }
        saveLog("registry/operation", "仓库地址列表展示", bsmResult.getMessage(), requestUser.getId());
        return bsmResult;
    }

    @Override
    public BsmResult create(RequestUser requestUser, Repository repository) {
        BsmResult bsmResult = new BsmResult(false, "");
        User user = null;
		if (null == (user = getUser(requestUser.getId()))) {
			bsmResult.setMessage("未获取到当前用户信息");
			return bsmResult;
		}
        //校验仓库信息
        boolean isExisted = isExisted(repository);
        if (isExisted){
            bsmResult.setMessage("该仓库已经存在，请重新输入");
            return bsmResult;
        }
        boolean isConnected = connect(repository);
        if (!isConnected) {
            bsmResult.setMessage("未获取到仓库的链接，请检查输入参数是否正确");
            return bsmResult;
        }
        //添加仓库
        repository.setStatus(String.valueOf(Status.NORMAL.ordinal()));// 默认状态正常
        repository.setType(repository.getType());
        repository.setDeleted(false);
        repository.setGmtCreate(new Date());
        repository.setGmtModify(new Date());
        repository.setCreaterId(user.getId());
        repository.setMenderId(user.getId());
        repository.setDeptId(user.getDepartId());
        try {
            registryDao.save(repository);
        } catch (Exception e) {
            logger.error("create registry error:", e);
            bsmResult.setMessage("创建仓库异常!");
        } finally {
            saveLog("registry/operation", "创建仓库", bsmResult.getMessage(), user.getId());
        }
        bsmResult.setSuccess(true);
        bsmResult.setMessage("创建仓库成功!");
        return bsmResult;

    }
    
    /**
	 * 获取用户信息
	 * @param id
	 * @return
	 */
	private User getUser(Long id) {
		User user = null;
		try {
			user = userDao.query(id);
			if (null == user) {
				logger.warn("该用户不存在");
			}
		} catch (Exception e) {
			logger.error("获取该用户信息异常", e);
		}
		return user;
	}

    @Override
    public BsmResult detail(Long id, Long userId) {
        List<Param> params = new ArrayList<Param>();
        BsmResult bsmResult = new BsmResult();
        try {
            // 1、获取该用户的组织机构以及Children组织机构ID
            String deptId = userService.listDept(userId);
            // 判断当前用户是否拥有查看“容器平台”的权限，如果有则可以查看所有的镜像
            User user = null;
            if (null == (user = queryUser(userId, bsmResult))) {
                return bsmResult;
            }
            // 2、获取镜像总数
            int imageSum = imageDao.count(params, deptId, id);
            // 3、获取仓库详情
            Repository registry = registryDao.query(id);
            if (null == registry) {
                bsmResult.setSuccess(false);
                logger.error("仓库id = [" + id + "]不存在");
                bsmResult.setMessage("仓库id = [" + id + "]不存在");
                return bsmResult;
            }
            // 获取用户信息
            User creator = userDao.query(registry.getCreaterId());
            // 获取修改者信息
            User mender = userDao.query(registry.getMenderId());
            registry.setCreatorName(creator.getName());
            registry.setMenderName(mender.getName());
            registry.setImageSum(imageSum);
            bsmResult.setSuccess(true);
            bsmResult.setData(registry);
            bsmResult.setMessage("获取仓库详细信息成功");
        } catch (Exception e) {
            bsmResult.setSuccess(false);
            logger.error("Get registry error：", e);
            bsmResult.setMessage("获取仓库详细信息异常");
        } finally {
            saveLog("registry/operation", "仓库详情信息", bsmResult.getMessage(), userId);
        }
        return bsmResult;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
    @Override
    public BsmResult remove(Long id, Long userId) {
        BsmResult bsmResult = new BsmResult(false, "");
        Repository registry = null;
        try {
            if (null == (registry = queryRegistry(id))) {
                bsmResult.setMessage("未获取到仓库信息");
                return bsmResult;
            }
            if (registry.getStatus().equals(String.valueOf(Status.LOCK.ordinal()))) {
                return new BsmResult(false, "仓库锁定中不允许操作！");
            }
            if (!registryDao.deleteById(id, userId)) {
                logger.error("删除仓库id = [" + id + "]失败");
                bsmResult.setMessage("删除仓库失败");
                return bsmResult;
            }
        } catch (Exception e) {
            logger.error("Remove registry error:", e);
            bsmResult.setMessage("删除仓库异常");
            return bsmResult;
        } finally {
            saveLog("registry/operation", "删除仓库", bsmResult.getMessage(), userId);
        }
        return removeRegistryInfo(id);
    }

    @Override
    public BsmResult modify(Repository registry, Long userId) {
        BsmResult bsmResult = new BsmResult();
        // 重名验证，如果重复则不允许添加
        try {
            Repository repository = registryDao.query(registry.getName());
            if (repository != null && (!repository.getId().equals(registry.getId()))) {
                bsmResult.setMessage("仓库[" + registry.getName() + "]已存在不允许添加！");
                return bsmResult;
            }
        } catch (Exception e) {
            logger.error("get repository[" + registry.getName() + "]info fail!", e);
            bsmResult.setMessage("获取仓库[" + registry.getName() + "]信息失败");
            return bsmResult;
        }
        //校验仓库信息
        if (!connect(registry)) {
            bsmResult.setMessage("未获取到仓库的链接，请检查网络以及仓库情况是否正常");
            return bsmResult;
        }
        try {
            Repository regResult = registryDao.query(registry.getId());
            regResult.setName(registry.getName());
            regResult.setMenderId(userId);
            regResult.setRemark(registry.getRemark());
            regResult.setUsername(registry.getUsername());
            regResult.setPassword(registry.getPassword());
            regResult.setAddress(registry.getAddress());
            regResult.setPort(registry.getPort());
            regResult.setType(registry.getType());
            if (registryDao.update(regResult)) {
                bsmResult.setSuccess(true);
                bsmResult.setMessage("修改仓库信息成功");
            } else {
                bsmResult.setMessage("修改仓库信息失败");
            }
        } catch (Exception e) {
            logger.error("Modify registry error:", e);
            bsmResult.setMessage("修改仓库信息异常");
        } finally {
            saveLog("registry/operation", "修改仓库信息", bsmResult.getMessage(), userId);
        }
        return bsmResult;
    }

    @Override
    public BsmResult countInfo(Long userId) {
        BsmResult bsmResult = new BsmResult(false, null, "统计仓库信息异常");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String deptId = userService.listDept(userId);
            int total = registryDao.count(null, deptId);
            List<Map<String, Object>> countInfo = registryDao.countInfo(userId);
            map.put("public", countInfo.get(0).get("public"));
            map.put("private", countInfo.get(0).get("private"));
            map.put("openstackNum", total);
            bsmResult.setData(map);
            bsmResult.setSuccess(true);
            bsmResult.setMessage("统计仓库信息成功");
            return bsmResult;
        } catch (Exception e) {
            logger.error("get registry total fail:", e);
        } finally {
            saveLog("registry/operation", "统计仓库数量", bsmResult.getMessage(), userId);
        }
        return bsmResult;
    }

    @Override
    public BsmResult getImagesInRegistry(int page, int rows, List<Param> params, Long userId) {
        BsmResult bsmResult = new BsmResult(false, "");
        try {
            // 1、获取该用户的组织机构以及Children组织机构ID
            if (ListTool.isEmpty(params)) {
                params = new ArrayList<Param>();
            }
            // 判断当前用户是否拥有查看“容器平台”的权限，如果有则可以查看所有的镜像
            /*User user = new User();
            if (null == (user = queryUser(userId, bsmResult))) {
				return bsmResult;
			}*/
            String deptId = userService.listDept(userId);
            /*List<Role> roles = roleDao.listByUid(user.getId());
            for (Role role : roles) {
				List<Authority> authorities = authDao.listByRid(role.getId());
				for (Authority authority : authorities) {
					// TODO 和威哥少龙沟通后暂时写死判断“容器平台”权限的id
					if (authority.getId().equals(new Long(132))) {
						deptId = null;
						break;
					}
				}
			}*/
            // 2、查询镜像数
            int count = registryImageInfoDao.count(params, deptId);
            List<RepositoryImageInfo> repositoryImages = registryImageInfoDao.selectRepositoryImage(page, rows,
                    params, deptId);
            if (!ListTool.isEmpty(repositoryImages)) {
                for (RepositoryImageInfo imageInfo : repositoryImages) {
                    User mender = userDao.query(imageInfo.getMenderId());
                    imageInfo.setMenderName(mender.getName());
                }
            }
            GridBean gridBean = new GridBean(page, rows, count, repositoryImages);
            bsmResult.setSuccess(true);
            bsmResult.setData(gridBean);
            bsmResult.setMessage("获取仓库镜像成功");
        } catch (Exception e) {
            logger.error("get image info in registry Exception", e);
            bsmResult.setMessage("获取仓库镜像失败");
        } finally {
            saveLog("registry/operation", "查询仓库镜像", bsmResult.getMessage(), userId);
        }
        return bsmResult;
    }

    @Override
    public List<Repository> listRepository() {
        try {
            return registryDao.selectRepository();
        } catch (Exception e) {
            logger.error("list all repository error", e);
            return null;
        }
    }

    @Override
    public void updateWithField(Repository registry, String[] fields) {
        // 如果仓库是锁定状态不允许操作
        if (registry.getStatus().equals(String.valueOf(Status.LOCK.ordinal()))) {
            logger.error("update registry With Field error: registry status lock...");
            return;
        }
        try {
            registryDao.update(registry, fields);
        } catch (Exception e) {
            logger.error("update registry With Field error", e);
        }
    }

    private BsmResult removeRegistryInfo(Long id) {
        BsmResult bsmResult = new BsmResult(false, "");
        try {
            List<RepositoryImageInfo> registryImageInfo = registryImageInfoDao.selectRepositoryImage(id);
            for (RepositoryImageInfo info : registryImageInfo) {
                // 删除镜像
                imageDao.remove(Image.class, info.getId());
                // 获取仓库镜像关联对象信息
                List<RepositoryImage> registryImage = registryImageDao.getRepositoryImageById(info.getRegId());
                // 删除仓库镜像关联表信息
                registryImageDao.deleteRepositoryImage(registryImage.get(0));
            }
            bsmResult.setSuccess(true);
            bsmResult.setMessage("删除仓库信息成功");
        } catch (Exception e) {
            logger.error("delete registry info error", e);
            bsmResult.setMessage("删除仓库信息异常");
        }
        return bsmResult;
    }

    /**
     * @param repository
     * @Author: langzi
     * @Description: 仓库存在返回true；否则返回false
     * @Date: 12:03 2017/10/27
     */
    private boolean isExisted(Repository repository) {
        String address = repository.getAddress(), username = repository.getUsername(),
                password = repository.getPassword();
        Integer port = repository.getPort();
        try {
            Repository exsitRepository = registryDao.selectRepository(address, port, username, password);
            if (null != exsitRepository) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Get repository is Exception", e);
            return true;
        }
    }

    /**
     * @param repository
     * @Author: langzi
     * @Description: 测试仓库的链接连通性
     * @Date: 11:57 2017/10/27
     */
    private boolean connect(Repository repository) {
        String address = repository.getAddress(), username = repository.getUsername(),
                password = repository.getPassword();
        int port = repository.getPort();
        boolean connected = false;
        //认证类型
        RepositoryAuthMode authMode = RepositoryAuthMode.values()[repository.getAuthMode()];
        //请求协议类型
        Requestprotocol protocol = Requestprotocol.values()[repository.getProtocol()];
        //构建镜像仓库的url
        StringBuilder builder = new StringBuilder().append(protocol.name())
                .append("://").append(address).append(":").append(port);
        String url = builder.toString();
        RepositoryType repositoryType = RepositoryType.values()[repository.getType()];
        switch (repositoryType) {
            case DOCKER_REGISTRY:
                RegistryClient client = new RegistryClient(url);
                connected = client.isConnected();
                break;
            case HARBOR:
                HarborClient harborClient = new HarborClient(url);
                connected = harborClient.isConnected(repository.getUsername(), repository.getPassword());
                break;
        }
        return connected;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public BsmResult sycn(Long id) {
        return new BsmResult(false, "仓库同步失败");
    }

    /**
     * 服务端镜像与本地端镜像信息比对
     *
     * @param serverImages
     * @param localImages
     * @param id
     * @return
     */
    public BsmResult imageInfoCompare(List<Image> serverImages, List<RepositoryImageInfo> localImages, Long id) {
        StringBuffer serverName = null;
        StringBuffer localName = null;
        try {
            // 收集服务端 镜像:版本号
            List<String> serverImageList = new ArrayList<String>();
            // 收集本地端 镜像:版本号
            List<String> localImageList = new ArrayList<String>();
            for (Image serverImage : serverImages) {
                serverName = new StringBuffer();
                serverName.append(serverImage.getName()).append(":").append(serverImage.getTag());
                serverImageList.add(serverName.toString());
            }
            for (RepositoryImageInfo localImage : localImages) {
                localName = new StringBuffer();
                localName.append(localImage.getNamespace()).append("/").append(localImage.getName()).append(":")
                        .append(localImage.getTag());
                localImageList.add(localName.toString());
            }
            // 1、服务器端仓库 与 数据库中仓库 的镜像信息进行比对 添加操作
            for (Image serverImage : serverImages) {
                serverName = new StringBuffer();
                serverName.append(serverImage.getName()).append(":").append(serverImage.getTag());
                if (!localImageList.contains(serverName.toString())) {
                    String namespace = serverImage.getName().split("/")[0];
                    // 根据namespace获取组织id
                    List<Department> list = departmentDao.list(Department.class,
                            "select * from department where is_deleted = 0 and name = '" + namespace + "'");
                    if (!ListTool.isEmpty(list)) {
                        Long deptId = list.get(0).getId();
                        serverImage.setDeptId(deptId);
                    }
                    // 镜像名测拆分和重组
                    String name = serverImage.getName().substring(serverImage.getName().indexOf("/") + 1);
                    serverImage.setName(name);
                    serverImage.setDeleted(false);
                    serverImage.setProperty(0);
                    serverImage.setType(0);
                    serverImage.setUsage_count(0);
                    serverImage.setGmtCreate(new Date());
                    serverImage.setMenderId(1L);
                    serverImage.setCreaterId(1L);
                    serverImage.setStatus(String.valueOf(Status.NORMAL.ordinal()));
                    imageDao.save(serverImage);
                    // 维护仓库镜像关联表
                    RepositoryImage registryImage = new RepositoryImage();
                    registryImage.setNamespace(namespace);
                    registryImage.setRepositoryId(id);
                    registryImage.setImageId(serverImage.getId());
                    registryImageDao.saveRepositoryImage(registryImage);
                }
            }

            // 2、数据库中仓库 与 服务器端仓库 的镜像信息进行比对 删除操作
            for (RepositoryImageInfo localImage : localImages) {
                localName = new StringBuffer();
                localName.append(localImage.getNamespace()).append("/").append(localImage.getName()).append(":")
                        .append(localImage.getTag());
                if (!serverImageList.contains(localName.toString())) {
                    // 删除镜像
                    imageDao.remove(Image.class, localImage.getId());
                    // 删除仓库镜像表关系
                    List<RepositoryImage> registryImage = registryImageDao
                            .getRepositoryImageById(localImage.getRegId());
                    if (ListTool.isEmpty(registryImage)) {
                        logger.debug("仓库镜像关联表信息存储异常，获取的信息失败:" + registryImage);
                        return new BsmResult(false, "仓库镜像关联表信息存储异常，获取的信息失败:" + registryImage);
                    }
                    registryImageDao.deleteRepositoryImage(registryImage.get(0));
                }
            }
            return new BsmResult(true, "同步成功");
        } catch (Exception e) {
            logger.error("同步信息出现异常", e);
            return new BsmResult(false, "同步异常");
        }
    }

}
