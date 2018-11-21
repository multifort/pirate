package com.bocloud.paas.service.repository.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.http.HttpClient;
import com.bocloud.common.model.*;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.enums.*;
import com.bocloud.paas.common.enums.CommonEnum.Property;
import com.bocloud.paas.common.enums.CommonEnum.Status;
import com.bocloud.paas.common.harbor.HarborClient;
import com.bocloud.paas.common.harbor.model.Project;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ApplicationImageInfoDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.repository.ImageDao;
import com.bocloud.paas.dao.repository.RepositoryDao;
import com.bocloud.paas.dao.repository.RepositoryImageDao;
import com.bocloud.paas.dao.repository.RepositoryImageInfoDao;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.dao.user.RoleDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.*;
import com.bocloud.paas.service.application.config.STIConfig;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.event.model.OperateResult;
import com.bocloud.paas.service.repository.ImageService;
import com.bocloud.paas.service.repository.model.ImageInfo;
import com.bocloud.paas.service.repository.util.ImageUtil;
import com.bocloud.paas.service.repository.util.RegistryClient;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * @author zjm
 * @date 2017年3月17日
 */
@Service("imageService")
public class ImageServiceImpl implements ImageService {

    private static Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    @Autowired
    private STIConfig stiConfig;

    @Autowired
    private ImageDao imageDao;
    @Autowired
    private RepositoryDao registryDao;
    @Autowired
    private RepositoryImageInfoDao registryImageInfoDao;
    @Autowired
    private ApplicationDao applicationDao;
    @Autowired
    private RepositoryImageDao registryImageDao;
    @Autowired
    private ApplicationImageInfoDao applicationImageInfoDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private AuthDao authDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    DepartmentDao departmentDao;
    @Autowired
    EnvironmentDao environmentDao;

    @Override
    public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
                          RequestUser requestUser, String choice) {
        GridBean gridBean;
        if (ListTool.isEmpty(params)) {
            params = new ArrayList<Param>();
        }
        if (null == sorter) {
            sorter = Maps.newHashMap();
        }
        sorter.put("gmtCreate", Common.ONE);
        try {
            String deptId = userService.listDept(requestUser.getId());
            // 判断当前用户是否拥有查看“容器平台”的权限，如果有则可以查看所有的镜像
            try {
                BsmResult bsmResult = new BsmResult();
                User user = null;
                if (null == (user = queryUser(requestUser.getId(), bsmResult))) {
                    return bsmResult;
                }
                List<Role> roles = roleDao.listByUid(user.getId());
                for (Role role : roles) {
                    List<Authority> authorities = authDao.listByRid(role.getId());
                    for (Authority authority : authorities) {
                        // TODO 暂时写死判断“容器平台”权限的id
                        if (authority.getId().equals(new Long(132))) {
                            deptId = null;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("list image failure:", e);
                return new BsmResult(false, "镜像查询失败！获取登陆用户信息失败！");
            }
            // 如果是simple则是下拉列表形式的返回值，可以选择显示类型为full：镜像id:镜像名+标签；
            // 显示类型为name：镜像id:镜像名；
            // 显示类型为tag：镜像id:镜像标签；
            int total = imageDao.count(params, deptId, null);
            if (simple) {
                List<Image> list = imageDao.list(params, sorter, deptId, choice);
                gridBean = new GridBean(1, 1, total, list);
            } else {
                List<Image> list = imageDao.list(page, rows, params, sorter, deptId);
                gridBean = GridHelper.getBean(page, rows, total, list);
            }
            return new BsmResult(true, gridBean, "镜像查询成功！");
        } catch (Exception e) {
            logger.error("list image failure:", e);
            return new BsmResult(false, "镜像查询失败！获取登陆用户信息失败！");
        }
    }

    @Override
    public BsmResult create(RequestUser requestUser, Image image) {

        image.setCreaterId(requestUser.getId());
        image.setGmtCreate(new Date());
        image.setMenderId(requestUser.getId());
        image.setGmtModify(new Date());
        BsmResult result = new BsmResult(false, "");
        try {
            User user = null;
            if (null == (user = queryUser(requestUser.getId(), result))) {
                return result;
            }
            image.setDeptId(user.getDepartId());
            imageDao.save(image);
            result.setSuccess(true);
            result.setMessage("创建镜像成功");
        } catch (Exception e) {
            logger.error("create image exception:", e);
            result.setMessage("创建镜像失败");
        }
        return result;
    }

    @Override
    public BsmResult detail(Long id) {
        BsmResult result = new BsmResult(false, "");
        try {
            Image image = imageDao.query(id);
            if (null == image) {
                result.setMessage("仓库不存在");
            } else {
                result.setSuccess(true);
                result.setData(image);
                result.setMessage("获取镜像详情成功");
            }
        } catch (Exception e) {
            logger.error("query image error：", e);
            result.setMessage("获取仓库详情失败");
        }
        return result;
    }

    @Override
    public BsmResult remove(List<Long> ids, Long userId) {
        BsmResult bsmResult = new BsmResult();
        bsmResult.setSuccess(false);
        for (Long id : ids) {
            Image image;
            if (null == (image = queryImg(id))) {
                return bsmResult;
            }
            RepositoryImageInfo registryImageInfo;
            if (null == (registryImageInfo = queryRegistryImageInfo(id, bsmResult))) {
                return bsmResult;
            }
            Repository registry;
            if (null == (registry = queryRegistry(registryImageInfo.getRepositoryId(), bsmResult))) {
                return bsmResult;
            }
            if (registry.getStatus().equals(String.valueOf(CommonEnum.Status.LOCK.ordinal()))) {
                return new BsmResult(false, "仓库锁定中不允许操作！");
            }
            String imageName = registryImageInfo.getNamespace() + "/" + image.getName();
            String tag = image.getTag();
            String address = registry.getAddress();
            Integer port = registry.getPort();
            Requestprotocol protocol = Requestprotocol.values()[registry.getProtocol()];
            //构建镜像仓库的url
            RepositoryType repositoryType = RepositoryType.values()[registry.getType()];
            StringBuilder builder = new StringBuilder().append(protocol.name())
                    .append("://").append(address).append(":").append(port);
            String url = builder.toString();
            //分别harbor还是registry
            switch (repositoryType) {
                case DOCKER_REGISTRY:
                    RegistryClient client = new RegistryClient(url);
                    String digest = client.getImageDigest(imageName, tag);
                    if (!StringUtils.isEmpty(digest)) {
                        client = new RegistryClient(url);
                        if (!client.deleteImage(imageName, digest)) {
                            bsmResult.setMessage("删除镜像失败");
                            return bsmResult;
                        }
                    }
                    break;
                case HARBOR:
                    HarborClient harborClient = new HarborClient(url);
                    harborClient.login(registry.getUsername(), registry.getPassword());
                    harborClient.setUrl(url);
                    if (!harborClient.deleteTag(imageName, tag)) {
                        bsmResult.setMessage("删除镜像失败");
                        return bsmResult;
                    }
                    break;
                default:
                    break;
            }
            if (!removeImage(id, bsmResult)) {
                return bsmResult;
            }
        }
        bsmResult.setSuccess(true);
        bsmResult.setMessage("镜像删除成功！");
        return bsmResult;
    }

    @Override
    public BsmResult modify(Image image, Long userId) {
        image.setMenderId(userId);
        image.setGmtModify(new Date());
        BsmResult result = new BsmResult(false, "");
        try {
            if (imageDao.update(image)) {
                result.setSuccess(true);
                result.setMessage("修改镜像信息成功！");
            } else {
                result.setMessage("修改镜像信息失败！");
            }
        } catch (Exception e) {
            logger.error("modify image fail:", e);
            result.setMessage("修改镜像信息失败！");
        }
        return result;
    }

    @Override
    public BsmResult load(RequestUser requestUser, Long registryId, String file, String project) {
        BsmResult result = new BsmResult(false, "");
        File imageFile = new File(file);
        String fileName = imageFile.getName();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!(fileType.equals("tar")) && !(fileType.equals("img"))) {
            logger.warn("不支持上传的文件格式！目前只支持tar或者img类型的镜像包");
            result.setMessage("不支持上传的文件格式！目前只支持tar或者img类型的镜像包");
            FileUtil.deleteDirectory(imageFile);
            return result;
        }
        // 获取仓库信息
        Repository registry = queryRegistry(registryId, result);
        if (null == registry) {
            FileUtil.deleteDirectory(imageFile);
            return result;
        }
        if (registry.getStatus().equals(String.valueOf(CommonEnum.Status.LOCK.ordinal()))) {
            FileUtil.deleteDirectory(imageFile);
            return new BsmResult(false, "仓库锁定中不允许操作！");
        }
        String address = registry.getAddress();
        int port = registry.getPort();
        //请求协议类型
        Requestprotocol protocol = Requestprotocol.values()[registry.getProtocol()];
        //构建镜像仓库的url
        StringBuilder builder = new StringBuilder().append(protocol.name())
                .append("://").append(address).append(":").append(port);
        String url = builder.toString();
        try {
            // 启动一个线程从MQ上获取数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BsmResult bsmResult = new BsmResult(false, "");
                    String registryUrl = StringUtils.isEmpty(port) ? registryUrl = address
                            : address + ":" + port;
                    Result result = ImageUtil.load(imageFile, address);
                    FileUtil.deleteDirectory(imageFile);
                    if (!result.isSuccess() || StringUtils.isEmpty(result.getMessage())) {
                        bsmResult.setMessage("导入镜像失败");
                        logger.error(bsmResult.getMessage());
                        eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                "image/load", requestUser.getId()));
                        return;
                    }
                    String[] imageNames = result.getMessage().split("}");
                    for (int i = 0; i < imageNames.length; i++) {
                        String[] names = imageNames[i].split(":");
                        String name = "";
                        for (int j = 0; j < names.length; j++) {
                            if (names[j].contains("Loaded image")) {
                                for (int k = j; k < names.length; k++) {
                                    if (k < names.length - 2) {
                                        name += names[k + 1] + ":";
                                    }
                                }
                                // 区别镜像包中是否包含多个镜像
                                if (names[names.length - 1].contains("\\n")) {
                                    name += names[names.length - 1].substring(0, names[names.length - 1].indexOf("\\n"));
                                } else {
                                    name += names[names.length - 1];
                                }
                                name = name.trim();
                                break;
                            }
                        }
                        if (StringUtils.isEmpty(name)) {
                            continue;
                        }
                        String imageName = name.substring(name.lastIndexOf("/") + 1);
                        String imageNewName = "";
                        String namespace = "library";
                        RepositoryType repositoryType = RepositoryType.values()[registry.getType()];
                        // 获取新的镜像名称
                        switch (repositoryType) {
                            case DOCKER_REGISTRY:
                                if (name.indexOf("/") > 0) {
                                    namespace = name.substring(0, name.lastIndexOf("/"));
                                    if (namespace.indexOf("/") > 0) {
                                        namespace = namespace.substring(namespace.lastIndexOf("/") + 1);
                                    }
                                }
                                if (StringUtils.isEmpty(port)) {
                                    imageNewName = "localhost" + "/"+namespace+"/" + imageName;
                                } else {
                                    imageNewName = "localhost:" + port + "/"+namespace+"/" + imageName;
                                }
                                if (!ImageUtil.tag(name, imageNewName, address)) {
                                    bsmResult.setMessage("镜像打标失败");
                                    logger.error(bsmResult.getMessage());
                                    eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                            "image/load", requestUser.getId()));
                                    return;
                                }
                                if (!ImageUtil.push(imageNewName, address)) {
                                    bsmResult.setMessage("推送镜像失败");
                                    logger.error(bsmResult.getMessage());
                                    eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                            "image/load", requestUser.getId()));
                                    return;
                                }
                                break;
                            case HARBOR:
                                if (!StringUtils.isEmpty(project)) {
                                    namespace = project;
                                }
                                imageNewName = registryUrl + "/" + namespace + "/" + imageName;
                                if (!ImageUtil.tag(name, imageNewName, address)) {
                                    bsmResult.setMessage("镜像打标失败，请检查仓库是否已存在同名镜像！");
                                    logger.error(bsmResult.getMessage());
                                    eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                            "image/load", requestUser.getId()));
                                    return;
                                }
                                if (!ImageUtil.push(imageNewName, address, registry.getUsername(),
                                        registry.getPassword())) {
                                    bsmResult.setMessage("推送镜像失败");
                                    logger.error(bsmResult.getMessage());
                                    eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                            "image/load", requestUser.getId()));
                                    return;
                                }
                                break;
                            default:
                                break;
                        }
                        // 重新获取镜像信息
                        String imageOldName = imageName.substring(0, imageName.indexOf(":"));
                        String imageOldTag = imageName.substring(imageName.indexOf(":") + 1);
                        String digest = "";
                        // 获取新的镜像名称
                        switch (repositoryType) {
                            case DOCKER_REGISTRY:
                                RegistryClient registryClient = new RegistryClient(url);
                                digest = registryClient.getImageDigest(imageOldName, imageOldTag);
                                break;
                            case HARBOR:
                                HarborClient harborClient = new HarborClient(url);
                                harborClient.login(registry.getUsername(), registry.getPassword());
                                harborClient.setUrl(url);
                                digest = harborClient.getDigest(namespace + "/" + imageOldName, imageOldTag);
                                break;
                            default:
                                break;
                        }
                        // 判断镜像是否存在，如果不存在则新增
                        Image image = null;
                        try {
                            image = imageDao.query(registryId, namespace, imageOldName, imageOldTag);
                        } catch (Exception e) {
                            ImageUtil.remove(imageName, address);
                            bsmResult.setMessage("查询[" + imageOldName + ":" + imageOldTag + "]镜像信息失败！");
                            logger.error("query [" + imageOldName + ":" + imageOldTag + "] image info fail!:\n", e);
                            eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                    "image/load", requestUser.getId()));
                            return;
                        }
                        if (image == null) {
                            image = new Image();
                            image.setUuid(digest);
                            image.setName(imageOldName);
                            image.setTag(imageOldTag);
                            // TODO 暂时都是公有镜像
                            image.setProperty(Property.PUBLIC.ordinal());
                            image.setType(ImageEnum.Type.CONTAINER.ordinal());
                            image.setStatus(Integer.toString(Status.NORMAL.ordinal()));
                            image.setUsage_count(0);
                            image.setDeleted(false);
                            image.setGmtCreate(new Date());
                            image.setMenderId(requestUser.getId());
                            image.setGmtModify(new Date());
                            image.setCreaterId(requestUser.getId());

                            try {
                                imageDao.save(image);
                            } catch (Exception e) {
                                ImageUtil.remove(imageName, address);
                                bsmResult.setMessage("保存镜像[" + imageNewName + "]失败！");
                                logger.error(bsmResult.getMessage(), e);
                                eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                        "image/load", requestUser.getId()));
                                return;
                            }
                            RepositoryImage registryImage = new RepositoryImage();
                            registryImage.setNamespace(namespace);
                            registryImage.setRepositoryId(registryId);
                            registryImage.setImageId(image.getId());
                            try {
                                registryImageDao.saveRepositoryImage(registryImage);
                            } catch (Exception e) {
                                ImageUtil.remove(imageName, address);
                                bsmResult.setMessage("保存仓库镜像[" + imageNewName + "]信息失败！");
                                logger.error(bsmResult.getMessage(), e);
                                eventPublisher.send(new OperateResult(bsmResult.isSuccess(), bsmResult.getMessage(),
                                        "image/load", requestUser.getId()));
                                return;
                            }
                        }
                        bsmResult.setSuccess(true);
                        bsmResult.setMessage("导入镜像成功");
                        logger.info(bsmResult.getMessage());
                        eventPublisher.send(new OperateResult(true, "导入镜像成功！",
                                "image/load", null, requestUser.getId()));
                    }
                    return;
                }
            }).start();
            return new BsmResult(true, "镜像导入任务已经下发，正在执行……");
        } catch (Exception e) {
            logger.error("image import failure:", e);
            return new BsmResult(false, "镜像导入失败!");
        }
    }

    @Override
    public BsmResult inspect(Long imageId) {
        BsmResult bsmResult = new BsmResult(false, "");
        Image image = queryImg(imageId);
        if (null == image) {
            bsmResult.setMessage("未获取到镜像的信息，检查镜像是否存在");
            return bsmResult;
        }
        // 获取仓库镜像中间表信息
        RepositoryImageInfo registryImageInfo = queryRegistryImageInfo(imageId, bsmResult);
        if (null == registryImageInfo) {
            return bsmResult;
        }
        // 获取仓库信息
        Repository repository = queryRegistry(registryImageInfo.getRepositoryId(), bsmResult);
        if (null == repository) {
            return bsmResult;
        }
        String address = repository.getAddress();
        Integer port = repository.getPort();

        image.setRepositoryAddress(address);
        image.setRepositoryPort(port);
        image.setRepositoryType(repository.getType());

        //请求协议类型
        Requestprotocol protocol = Requestprotocol.values()[repository.getProtocol()];
        //构建镜像仓库的url
        StringBuilder builder = new StringBuilder().append(protocol.name())
                .append("://").append(address).append(":").append(port);
        String url = builder.toString();
        String imageName = registryImageInfo.getNamespace() + "/" + image.getName();

        ImageInfo imageInfo = new ImageInfo();
        RepositoryType repositoryType = RepositoryType.values()[repository.getType()];
        switch (repositoryType) {
            case DOCKER_REGISTRY:
                RegistryClient client = new RegistryClient(url);
                JSONObject imageObject = client.getImageDetail(imageName, image.getTag());
                JSONObject configObject = JSONObject
                        .parseObject(imageObject.getJSONArray("history").getJSONObject(0).get("v1Compatibility").toString())
                        .getJSONObject("config");
                JSONArray env = configObject.getJSONArray("Env");
                if (null != env) {
                    imageInfo.setEnv(env.toJSONString());
                }
                JSONObject exposedPorts = configObject.getJSONObject("ExposedPorts");
                if (null != exposedPorts) {
                    imageInfo.setExposedPort(exposedPorts.toJSONString());
                }
                break;
            case HARBOR:
                HarborClient harborClient = new HarborClient(url);
                harborClient.login(repository.getUsername(), repository.getPassword());
                harborClient.setUrl(url);
                String config = harborClient.getConfig(imageName, image.getTag());
                JSONObject configJson = JSONObject.parseObject(config);
                JSONArray enviroment = configJson.getJSONArray("Env");
                if (enviroment != null) {
                    imageInfo.setEnv(enviroment.toJSONString());
                }
                JSONObject ports = configJson.getJSONObject("ExposedPorts");
                if (null != ports) {
                    imageInfo.setExposedPort(ports.toJSONString());
                }
                break;
            default:
                break;
        }
        BeanUtils.copyProperties(image, imageInfo);
        // TODO 需要获取镜像层
        bsmResult.setData(imageInfo);
        bsmResult.setSuccess(true);
        return bsmResult;
    }

    @Override
    public BsmResult remove(Long registryId, Long imageId) {
        BsmResult bsmResult = new BsmResult();
        bsmResult.setSuccess(false);
        Image image;
        if ((image = queryImg(imageId)) == null) {
            bsmResult.setMessage("未获取到镜像的信息");
            return bsmResult;
        }
        // 获取仓库信息
        Repository registry;
        if ((registry = queryRegistry(registryId, bsmResult)) == null) {
            return bsmResult;
        }
        if (registry.getStatus().equals(String.valueOf(Status.LOCK.ordinal()))) {
            return new BsmResult(false, "仓库锁定中不允许操作！");
        }
        String registryIp = registry.getAddress();
        if (ImageUtil.remove(image.getName(), registryIp) && !removeImage(imageId, bsmResult)) {
            return bsmResult;
        }
        bsmResult.setSuccess(true);
        bsmResult.setMessage("镜像删除成功！");
        return bsmResult;
    }

    @Override
    public BsmResult count(RequestUser requestUser) {
        BsmResult bsmResult = new BsmResult();
        bsmResult.setSuccess(false);
        int count;
        try {
            String deptId = userService.listDept(requestUser.getId());
            // 判断当前用户是否拥有查看“容器平台”的权限，如果有则可以查看所有的镜像
            try {
                User user = null;
                if (null == (user = queryUser(requestUser.getId(), bsmResult))) {
                    return bsmResult;
                }
                List<Role> roles = roleDao.listByUid(user.getId());
                for (Role role : roles) {
                    List<Authority> authorities = authDao.listByRid(role.getId());
                    for (Authority authority : authorities) {
                        if (authority.getId().equals(new Long(132))) {
                            deptId = null;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("统计镜像失败！未获取到获取用户信息！", e);
                bsmResult.setMessage("统计镜像失败！未获取到获取用户信息！");
                return bsmResult;
            }
            count = imageDao.count(new ArrayList<>(), deptId, null);
        } catch (Exception e) {
            logger.error("统计镜像失败！", e);
            bsmResult.setMessage("统计镜像失败！");
            return bsmResult;
        }
        bsmResult.setSuccess(true);
        bsmResult.setMessage("镜像总数为：" + count);
        return bsmResult;
    }

    private boolean modifyApplication(Application application, BsmResult bsmResult) {
        boolean result = false;
        try {
            result = applicationDao.update(application);
        } catch (Exception e) {
            logger.error("更新应用数据失败！", e);
            bsmResult.setMessage("更新应用数据失败！");
        }
        if (!result) {
            bsmResult.setMessage("更新应用数据失败！");
        }
        return result;
    }

    private boolean saveApplicationImageInfo(ApplicationImageInfo applicationImageInfo, BsmResult bsmResult) {
        boolean result = false;
        try {
            result = applicationImageInfoDao.insert(applicationImageInfo);
        } catch (Exception e) {
            logger.error("添加应用镜像关联数据失败！", e);
            bsmResult.setMessage("添加应用镜像关联数据失败！");
        }
        if (!result) {
            bsmResult.setMessage("添加应用镜像关联数据失败！");
        }
        return result;
    }

    private Image queryImg(Long imageId) {
        Image image = null;
        try {
            image = imageDao.query(imageId);
        } catch (Exception e) {
            logger.error("获取镜像信息失败！", e);
            return null;
        }
        return image;
    }

    private User queryUser(Long userId, BsmResult bsmResult) {
        User user = null;
        try {
            user = userDao.query(userId);
        } catch (Exception e) {
            logger.error("获取用户信息失败！", e);
            bsmResult.setMessage("获取用户信息失败！");
        }
        if (null == user) {
            bsmResult.setMessage("获取用户信息为空！");
        }
        return user;
    }

    private RepositoryImageInfo queryRegistryImageInfo(Long registryImageInfoId, BsmResult bsmResult) {
        // 获取仓库镜像中间表信息
        RepositoryImageInfo registryImageInfo = null;
        try {
            registryImageInfo = registryImageInfoDao.getByImageId(registryImageInfoId);
        } catch (Exception e) {
            logger.error("获取仓库镜像信息失败！", e);
            bsmResult.setMessage("获取仓库镜像信息失败！");
        }
        if (null == registryImageInfo) {
            logger.error("获取仓库镜像信息失败！");
            bsmResult.setMessage("获取仓库镜像信息失败！");
        }
        return registryImageInfo;
    }

    private Repository queryRegistry(Long registryId, BsmResult bsmResult) {
        // 获取仓库信息
        Repository registry = null;
        try {
            registry = registryDao.query(registryId);
        } catch (Exception e) {
            logger.error("获取仓库信息失败！", e);
            bsmResult.setMessage("获取仓库信息失败！");
        }
        if (null == registry) {
            logger.warn("获取仓库信息失败！");
            bsmResult.setMessage("获取仓库信息失败！");
        }
        return registry;
    }

    private boolean removeImage(Long id, BsmResult bsmResult) {
        boolean result = false;
        try {
            result = imageDao.remove(Image.class, id) && imageDao.deleteRepositoryImageInfo(id)
                    && imageDao.deleteImageAppInfo(id);
        } catch (Exception e) {
            logger.error("服务器端镜像删除成功，数据库镜像删除失败！", e);
            bsmResult.setMessage("服务器端镜像删除成功，数据库镜像删除失败！");
        }
        if (!result) {
            bsmResult.setMessage("服务器端镜像删除成功，数据库镜像删除失败！");
        }
        return result;
    }

    @Override
    public BsmResult authorize(Long imageId, Long userId, Long deptId) {
        BsmResult result = new BsmResult(false, "");
        try {
            if (imageDao.authorize(imageId, userId, deptId)) {
                result.setSuccess(true);
                result.setMessage("镜像授权成功！");
            } else {
                result.setMessage("镜像授权失败！");
            }
            return result;
        } catch (Exception e) {
            logger.error("Authorize image exception: ", e);
            result.setMessage("镜像授权失败！");
            return result;
        }
    }

    /**
     * 添加镜像中间表
     *
     * @param imageId
     * @param application
     * @return
     * @author zjm
     * @date 2017年4月21日
     */
    private BsmResult insertImageMiddleTable(Long imageId, Application application) {
        BsmResult bsmResult = new BsmResult();
        bsmResult.setSuccess(false);
        // 判断应用镜像中间表是否包含这个应用和镜像，如果不包含添加应用镜像中间表数据
        ApplicationImageInfo applicationImageInfo = null;
        try {
            applicationImageInfo = applicationImageInfoDao.detail(application.getId(), imageId);
        } catch (Exception e) {
            logger.error("获取应用镜像中间表失败！", e);
            bsmResult.setMessage("获取应用镜像中间表失败！");
            return bsmResult;
        }
        if (null == applicationImageInfo) {
            applicationImageInfo = new ApplicationImageInfo();
            applicationImageInfo.setImageId(imageId);
            applicationImageInfo.setApplicationId(application.getId());
            applicationImageInfo.setUseCount(1);
            if (!saveApplicationImageInfo(applicationImageInfo, bsmResult)) {
                return bsmResult;
            }
        } else {
            applicationImageInfo.setUseCount(applicationImageInfo.getUseCount() + 1);
            try {
                applicationImageInfoDao.update(applicationImageInfo);
            } catch (Exception e) {
                bsmResult.setSuccess(false);
                bsmResult.setMessage("添加应用镜像关联数据失败！" + e);
                logger.error(bsmResult.getMessage());
                return bsmResult;
            }
        }
        // 修改应用状态
        application.setStatus(ApplicationEnum.Status.DEPLOY.toString());
        if (!modifyApplication(application, bsmResult)) {
            logger.error(bsmResult.getMessage());
        }
        bsmResult.setSuccess(true);
        return bsmResult;
    }

    @Override
    public BsmResult list(JSONArray array) {
        JSONArray images = new JSONArray();
        for (Object object : array) {
            JSONObject image = null;
            String imageName = object.toString();
            int length = imageName.split("/").length;
            switch (length) {
                case 3: // 如果镜像名称符合条件，则会认为是本平台仓库镜像
                    // 192.168.1.111:5000/default/tomcat:8.5
                    image = listImage(imageName);
                    images.add(image);
                    break;
                case 2: // 没有仓库地址 default/tomcat:8.5 或没有namespace
                    // 192.168.1.111:5000/tomcat:8.5
                    image = splitImageName(imageName);
                    images.add(image);
                    break;
                default:// 1 只有名称和版本号 tomcat:8.5 或 tomcat
                    image = new JSONObject();
                    if (imageName.contains(":")) {
                        image.put("name", imageName.split(":")[0]);
                        image.put("tag", imageName.split(":")[1]);
                        images.add(image);
                    } else {
                        image.put("name", imageName);
                        images.add(image);
                    }
                    break;
            }
        }
        return new BsmResult(true, images, "获取成功");
    }

    /**
     * 拆没有地址或没有namespace的镜像名称
     *
     * @param imageName
     * @return
     */
    private JSONObject splitImageName(String imageName) {
        JSONObject image = new JSONObject();
        if (imageName.split("/")[0].contains(".")) {// 说明有仓库地址，没有namespace
            String address = "";
            if (imageName.split("/")[0].contains(":")) {// 说明有端口的地址名
                // 192.168.1.151:5000
                address = imageName.split("/")[0].split(":")[0];
            } else {
                address = imageName.split("/")[0];
            }
            // 获取仓库名称
            String regName = null;
            try {
                Repository repository = registryDao.query(address);
                if (null != repository) {
                    regName = repository.getName();
                }
            } catch (Exception e) {
                logger.error("Get Repository info exception", e);
            }
            image.put("repositoryName", regName);
            // image.put("namespace", imageName.split("/")[0]);
            image.put("name", imageName.split("/")[1].split(":")[0]);
            image.put("tag", imageName.split("/")[1].split(":")[1]);
        } else {
            image.put("namespace", imageName.split("/")[0]);
            image.put("name", imageName.split("/")[1].split(":")[0]);
            image.put("tag", imageName.split("/")[1].split(":")[1]);
        }
        return image;
    }

    /**
     * 获取镜像信息
     *
     * @param imageName
     * @return
     */
    private JSONObject listImage(String imageName) {
        JSONObject object = new JSONObject();
        // 数据整合
        List<Param> params = param(imageName);
        try {
            List<Image> list = imageDao.list(1, 1, params, null, null);
            if (!ListTool.isEmpty(list)) {// 有可能命名规则和平台一样，但不属于平台仓库镜像
                object.put("id", list.get(0).getId());
                object.put("repositoryName", list.get(0).getRepositoryName());
            } else {
                object.put("repositoryName", null);
            }

            object.put("namespace", imageName.split("/")[1]);
            object.put("name", imageName.split("/")[2].split(":")[0]);
            object.put("tag", imageName.split("/")[2].split(":")[1]);

            return object;
        } catch (Exception e) {
            logger.error("list image failure:", e);
            return null;
        }
    }

    /**
     * 数据库查找镜像，参数整合
     *
     * @param imageName
     * @return
     */
    private List<Param> param(String imageName) {
        List<Param> params = new ArrayList<Param>();
        Param param = new Param();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        String address = imageName.split("/")[0];
        if (address.contains(":")) {
            paramMap.put("repository.address", address.split(":")[0]);
        }
        paramMap.put("repository.address", address);

        paramMap.put("repository_image_info.namespace", imageName.split("/")[1]);

        String nameTag = imageName.split("/")[2];
        paramMap.put("image.name", nameTag.split(":")[0]);
        paramMap.put("image.tag", nameTag.split(":")[1]);

        param.setParam(paramMap);
        param.setSign(Sign.EQ);

        params.add(param);
        return params;
    }

    @Override
    public BsmResult buildBySource(String baseImage, String pomPath, String repositoryUrl, String project,
                                   String repositoryBranch, String repositoryUsername, String repositoryPassword, String warName,
                                   String newImageName, Long registryId, RequestUser requestUser) {
        BsmResult bsmResult = new BsmResult();
        // 获取仓库信息
        Repository registry = queryRegistry(registryId, bsmResult);
        if (null == registry) {
            bsmResult.setMessage("获取仓库信息异常！");
            return bsmResult;
        }
        if (registry.getStatus().equals(String.valueOf(CommonEnum.Status.LOCK.ordinal()))) {
            bsmResult.setMessage("仓库锁定中不允许操作！");
            return bsmResult;
        }
        HttpClient httpClient = new HttpClient(1000 * 60 * 30);
        String address = registry.getAddress();
        int port = registry.getPort();
        String registryUrl = address + ":" + port;
        //请求协议类型
        Requestprotocol protocol = Requestprotocol.values()[registry.getProtocol()];
        //构建镜像仓库的url
        StringBuilder builder = new StringBuilder().append(protocol.name())
                .append("://").append(address).append(":").append(port);
        String url = builder.toString();
        try {
            // 启动一个线程从MQ上获取数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> paramMap = MapTools.simpleMap("baseImage", baseImage);
                    paramMap.put("repositoryUrl", repositoryUrl);
                    paramMap.put("pomPath", pomPath);
                    paramMap.put("repositoryBranch", repositoryBranch);
                    paramMap.put("repositoryUsername", repositoryUsername);
                    paramMap.put("repositoryPassword", repositoryPassword);
                    paramMap.put("warName", warName);
                    paramMap.put("registryUrl", registryUrl);
                    paramMap.put("registryUsername", registry.getUsername());
                    paramMap.put("registryPassword", registry.getPassword());
                    String newImage = newImageName;
                    if (!StringUtils.isEmpty(project)) {
                        newImage = project + "/" + newImage;
                    }
                    paramMap.put("newImage", registryUrl + "/" + newImage);
                    Result result = httpClient.get(null, paramMap, stiConfig.getStiUrl() + "/api/build");
                    boolean success = !result.getMessage().contains("false");
                    if (success) {
                        logger.info(result.getMessage());
                        String imageTag = "latest";
                        // namespace+name
                        String imageFullName = newImage;
                        if (newImage.contains(":")) {
                            imageTag = newImage.substring(newImage.lastIndexOf(":") + 1);
                            imageFullName = newImage.substring(0, newImage.lastIndexOf(":"));
                        }
                        String imageName = imageFullName;
                        String namespace = "";
                        if (imageFullName.contains("/")) {
                            imageName = imageFullName.substring(imageFullName.indexOf("/") + 1);
                            namespace = imageFullName.substring(0, imageFullName.indexOf("/"));
                        }
                        String uuid = "";
                        RepositoryType repositoryType = RepositoryType.values()[registry.getType()];
                        switch (repositoryType) {
                            case DOCKER_REGISTRY:
                                RegistryClient client = new RegistryClient(url);
                                uuid = client.getImageDigest(imageFullName, imageTag);
                                break;
                            case HARBOR:
                                HarborClient harborClient = new HarborClient(url);
                                harborClient.login(registry.getUsername(), registry.getPassword());
                                harborClient.setUrl(url);
                                uuid = harborClient.getDigest(imageFullName, imageTag);
                                break;
                            default:
                                break;
                        }
                        if (StringUtils.isEmpty(uuid)) {
                            eventPublisher.send(new OperateResult(success, result.getMessage(), "image/buildBySource"));
                            return;
                        }
                        Image image = new Image();
                        image.setUuid(uuid);
                        image.setName(imageName);
                        image.setTag(imageTag);
                        // TODO 暂时都是公有镜像
                        image.setProperty(Property.PUBLIC.ordinal());
                        image.setType(ImageEnum.Type.CONTAINER.ordinal());
                        image.setStatus(Integer.toString(Status.NORMAL.ordinal()));
                        image.setUsage_count(0);
                        BsmResult bsmResult = create(requestUser, image);
                        if (bsmResult.isSuccess()) {
                            RepositoryImage registryImage = new RepositoryImage();
                            registryImage.setNamespace(namespace);
                            registryImage.setRepositoryId(registryId);
                            registryImage.setImageId(image.getId());
                            try {
                                registryImageDao.saveRepositoryImage(registryImage);
                            } catch (Exception e) {
                                result.setMessage("构建镜像成功，但是保存镜像信息失败！");
                                logger.error("build image success, but save image fail: " + bsmResult.getMessage(), e);
                                eventPublisher
                                        .send(new OperateResult(success, result.getMessage(), "image/buildBySource"));
                                return;
                            }
                            logger.info("build image success!");
                            result.setMessage("构建镜像成功!");
                            eventPublisher.send(new OperateResult(success, result.getMessage(), "image/buildBySource"));
                            return;
                        }
                        result.setMessage("镜像构建失败，请检查配置信息是否正确！");
                        logger.error(result.getMessage());
                        eventPublisher.send(new OperateResult(success, result.getMessage(), "image/buildBySource"));
                        return;
                    }
                }
            }).start();
            logger.info("image is being deploy...");
            return new BsmResult(true, "源码构建任务已经下发，正在执行……");
        } catch (Exception e) {
            logger.error("image deploy failure:", e);
            return new BsmResult(false, "源码构建失败!");
        }
    }

    @Override
    public BsmResult getProjects(Long registryId) {
        BsmResult bsmResult = new BsmResult();
        // 获取仓库信息
        Repository registry = queryRegistry(registryId, bsmResult);
        if (null == registry) {
            bsmResult.setMessage("获取仓库信息异常！");
            return bsmResult;
        }
        if (registry.getStatus().equals(String.valueOf(Status.LOCK.ordinal()))) {
            bsmResult.setMessage("仓库锁定中不允许操作！");
            return bsmResult;
        }
        if (registry.getType() != 1) {
            bsmResult.setMessage("目前仅支持获取harbor类型的仓库项目！");
            return bsmResult;
        }

        String address = registry.getAddress();
        int port = registry.getPort();
        //请求协议类型
        Requestprotocol protocol = Requestprotocol.values()[registry.getProtocol()];
        //构建镜像仓库的url
        StringBuilder builder = new StringBuilder().append(protocol.name())
                .append("://").append(address).append(":").append(port);
        String url = builder.toString();
        HarborClient client = new HarborClient(url);
        client.login(registry.getUsername(), registry.getPassword());
        client.setUrl(url);
        List<Project> projects = client.getProject();
        bsmResult.setSuccess(true);
        bsmResult.setData(projects);
        bsmResult.setMessage("获取[" + registry.getName() + "]仓库项目成功！");
        return bsmResult;
    }

}
