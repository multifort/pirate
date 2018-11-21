package com.bocloud.paas.server.scheduler;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.ListTool;
import com.bocloud.coordinator.cache.LeaderShip;
import com.bocloud.paas.common.enums.CommonEnum.Status;
import com.bocloud.paas.common.enums.RepositoryType;
import com.bocloud.paas.common.enums.Requestprotocol;
import com.bocloud.paas.common.harbor.HarborClient;
import com.bocloud.paas.common.harbor.model.Project;
import com.bocloud.paas.common.harbor.model.Tag;
import com.bocloud.paas.dao.repository.ImageDao;
import com.bocloud.paas.dao.repository.RepositoryDao;
import com.bocloud.paas.dao.repository.RepositoryImageDao;
import com.bocloud.paas.dao.repository.RepositoryImageInfoDao;
import com.bocloud.paas.entity.Image;
import com.bocloud.paas.entity.Repository;
import com.bocloud.paas.entity.RepositoryImage;
import com.bocloud.paas.entity.RepositoryImageInfo;
import com.bocloud.paas.service.repository.util.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 本地仓库与服务端仓库镜像信息同步
 *
 * @author Zaney
 * @data:2017年3月14日
 * @describe:
 */
@Component("system.properties")
public class ImageScheduler {

    private static Logger logger = LoggerFactory.getLogger(ImageScheduler.class);

    @Autowired
    private RepositoryDao repositoryDao;
    @Autowired
    private RepositoryImageInfoDao repositoryImageInfoDao;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private RepositoryImageDao repositoryImageDao;

    @Scheduled(cron = "${sync.schedule}")
    public void sync() {
        if (!LeaderShip.isLeader()) {
            return;
        }
        try {
            // 1、获取所有的仓库
            List<Repository> repositorys = repositoryDao.selectRepository();
            for (Repository repository : repositorys) {
                // 如果是锁定状态不允许操作
                if (Integer.parseInt(repository.getStatus()) == Status.LOCK.ordinal()) {
                    continue;
                }
                String address = repository.getAddress();
                int port = repository.getPort();
                //请求协议类型
                Requestprotocol protocol = Requestprotocol.values()[repository.getProtocol()];
                //构建镜像仓库的url
                StringBuilder builder = new StringBuilder().append(protocol.name())
                        .append("://").append(address).append(":").append(port);
                String url = builder.toString();
                List<Image> images = new ArrayList<>();
                // 2、获取服务器端仓库的镜像
                RepositoryType repositoryType = RepositoryType.values()[repository.getType()];
                switch (repositoryType) {
                    //DOCKER_REGISTRY
                    case DOCKER_REGISTRY:
                        images = getRepositoryImages(url);
                        break;
                    //HARBOR
                    case HARBOR:
                        String username = repository.getUsername();
                        String password = repository.getPassword();
                        images = getRepositoryImages(url, username, password);
                        break;
                    default:
                        break;
                }
                if (ListTool.isEmpty(images)) {
                    logger.info("服务器仓库不存在任何镜像信息");
                    continue;
                }
                // 3、获取数据库中仓库镜像表信息
                List<RepositoryImageInfo> repositoryImages = repositoryImageInfoDao
                        .selectRepositoryImage(repository.getId());
                if (repositoryImages.isEmpty()) {
                    logger.info("数据库仓库不存在任何镜像信息");
                }
                // 4、比对服务端与本地端镜像信息
                BsmResult imageInfoCompare = imageInfoCompare(images, repositoryImages,
                        repository.getId());
                if (imageInfoCompare.isSuccess()) {
                    logger.info("信息同步成功");
                }
            }
        } catch (Exception e) {
            logger.error("信息同步异常", e);
        }
    }

    /**
     * 服务端镜像与本地端镜像信息比对
     *
     * @param images
     * @param localImages
     * @param id
     * @return
     */
    public BsmResult imageInfoCompare(List<Image> images, List<RepositoryImageInfo> localImages, Long id) {
        StringBuilder serverName = null;
        StringBuilder localName = null;
        List<String> serverImageList = new ArrayList<String>();
        // 收集本地端 镜像:版本号
        List<String> localImageList = new ArrayList<String>();
        for (Image image : images) {
            serverName = new StringBuilder();
            serverName.append(image.getName()).append(":").append(image.getTag());
            serverImageList.add(serverName.toString());
        }
        if (localImages.size() != 0) {
            for (RepositoryImageInfo localImage : localImages) {
                localName = new StringBuilder();
                localName.append(localImage.getNamespace()).append("/").append(localImage.getName()).append(":")
                        .append(localImage.getTag());
                localImageList.add(localName.toString());
            }
        }
        try {
            // 收集服务端 镜像:版本号
            // 1、服务器端仓库 与 数据库中仓库 的镜像信息进行比对 添加操作
            for (Image image : images) {
                serverName = new StringBuilder();
                serverName.append(image.getName()).append(":").append(image.getTag());
                String namespace = image.getName().split("/")[0];
                if (!localImageList.contains(serverName.toString())) {// 不包含，进行镜像添加操作
                    // 镜像名测拆分和重组
                    String name = image.getName().substring(image.getName().indexOf("/") + 1);
                    image.setName(name);
                    image.setDeleted(false);
                    image.setProperty(0);
                    image.setType(0);
                    image.setUsage_count(0);
                    image.setGmtCreate(new Date());
                    image.setMenderId(1L);
                    image.setCreaterId(1L);
                    image.setStatus(String.valueOf(Status.NORMAL.ordinal()));
                    imageDao.save(image);
                    // 维护仓库镜像关联表
                    RepositoryImage repositoryImage = new RepositoryImage();
                    repositoryImage.setNamespace(namespace);
                    repositoryImage.setRepositoryId(id);
                    repositoryImage.setImageId(image.getId());
                    repositoryImageDao.saveRepositoryImage(repositoryImage);
                }
            }
            if (localImages.size() != 0) {
                // 2、数据库中仓库 与 服务器端仓库 的镜像信息进行比对 删除操作
                for (RepositoryImageInfo localImage : localImages) {
                    localName = new StringBuilder();
                    localName.append(localImage.getNamespace()).append("/").append(localImage.getName()).append(":")
                            .append(localImage.getTag());
                    if (!serverImageList.contains(localName.toString())) {
                        imageDao.remove(Image.class, localImage.getId());
                        repositoryImageDao.deleteRepositoryImage(localImage.getId());
                    }
                }
            }
            return new BsmResult(true, "同步成功");
        } catch (Exception e) {
            logger.error("同步信息出现异常", e);
            return new BsmResult(false, "同步异常");
        }
    }

    /**
     * @param url
     * @Author: langzi
     * @Description: 获取镜像仓库的镜像信息
     * @Date: 16:06 2017/10/30
     */
    private List<Image> getRepositoryImages(String url) {
        List<Image> images = new ArrayList<Image>();
        RegistryClient client = new RegistryClient(url);
        // 获取仓库中所有镜像的名称
        String[] imageNames = client.listImageNames();
        if (StringUtils.isEmpty(imageNames)) {
            return null;
        }
        for (String name : imageNames) {
            client = new RegistryClient(url);
            //获取一个镜像下的所有tag
            String[] tags = client.listImageTags(name);
            if (StringUtils.isEmpty(tags)) {
                continue;
            }
            for (String tag : tags) {
                // 获取镜像digest
                client = new RegistryClient(url);
                String digest = client.getImageDigest(name, tag);
                Image image = new Image();
                image.setName(name);
                image.setTag(tag);
                image.setUuid(digest);
                images.add(image);
            }
        }
        return images;
    }


    public List<Image> getRepositoryImages(String url, String username, String password) {
        List<Image> images = new ArrayList<Image>();
        HarborClient client = new HarborClient(url);
        List<Project> projects = new ArrayList<>();
        client.login(username, password);
        client.setUrl(url);
        projects = client.getProject();
        if (!ListTool.isEmpty(projects)) {
            for (Project project : projects) {
                int projectId = project.getProjectId();
                client.setUrl(url);
                List<com.bocloud.paas.common.harbor.model.Repository> repositories = client.getRepositories(String.valueOf(projectId));
                if (!ListTool.isEmpty(repositories)) {
                    for (com.bocloud.paas.common.harbor.model.Repository repository : repositories) {
                        String repositoryName = repository.getName();
                        client.setUrl(url);
                        List<Tag> tags = client.getRepositorieTags(repositoryName);
                        if (!ListTool.isEmpty(tags)) {
                            for (Tag tag : tags) {
                                String tagName = tag.getName();
                                Image image = new Image();
                                image.setName(repositoryName);
                                image.setTag(tagName);
                                client.setUrl(url);
                                String digest = client.getDigest(repositoryName, tagName);
                                image.setUuid(digest);
                                images.add(image);
                            }
                        }
                    }
                }
            }
        }
        return images;
    }

}
