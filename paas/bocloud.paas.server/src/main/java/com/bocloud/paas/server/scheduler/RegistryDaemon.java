package com.bocloud.paas.server.scheduler;

import com.bocloud.common.utils.ListTool;
import com.bocloud.coordinator.cache.LeaderShip;
import com.bocloud.paas.common.enums.CommonEnum.Status;
import com.bocloud.paas.common.enums.RepositoryType;
import com.bocloud.paas.common.enums.Requestprotocol;
import com.bocloud.paas.common.harbor.HarborClient;
import com.bocloud.paas.entity.Repository;
import com.bocloud.paas.service.repository.RepositoryService;
import com.bocloud.paas.service.repository.util.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 定期检查仓库的状态是否正常
 *
 * @author Zaney
 * @data:2017年3月24日
 * @describe:
 */
public class RegistryDaemon extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(RegistryDaemon.class);
    private boolean stop = false;
    private RepositoryService repositoryService;
    private final static Long SLEEP_TIME = 1000l * 10l;

    public RegistryDaemon(RepositoryService repositoryService) {
        super();
        this.repositoryService = repositoryService;
    }

    public void run() {
        while (!stop) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error("Registry daemon is　intercepted", e);
                continue;
            }
            if (!LeaderShip.isLeader()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("I am not leader! skip this loop!");
                }
                continue;
            }
            // 1、查询所有非删除的仓库信息
            List<Repository> repositories = repositoryService.listRepository();
            if (ListTool.isEmpty(repositories)) {
                if (logger.isDebugEnabled()) {
                    logger.warn("check registry status, but no registries exist!");
                }
                continue;
            }
            healthCheck(repositories);
        }
    }

    public void stopDeamon() {
        this.stop = true;
    }

    public void startDeamon() {
        this.stop = false;
    }

    public void healthCheck(List<Repository> repositories) {
        for (Repository repository : repositories) {
            // 2、获取仓库的数据库中的状态
            String status = repository.getStatus();
            // 如果是锁定状态不允许操作
            if (Integer.valueOf(status) == Status.LOCK.ordinal()) {
                continue;
            }
            // 3、获取仓库在服务器端的状态
            String address = repository.getAddress();
            int port = repository.getPort();
            //请求协议类型
            Requestprotocol protocol = Requestprotocol.values()[repository.getProtocol()];
            //构建镜像仓库的url
            StringBuilder builder = new StringBuilder().append(protocol.name())
                    .append("://").append(address).append(":").append(port);
            String url = builder.toString();

            RepositoryType repositoryType = RepositoryType.values()[repository.getType()];
            boolean connected = false;
            switch (repositoryType) {
                // TODO 后续添加更多种仓库类型
                case DOCKER_REGISTRY:
                    RegistryClient client = new RegistryClient(url);
                    connected = client.isConnected();
                    break;
                case HARBOR:
                    HarborClient harborClient = new HarborClient(url);
                    connected = harborClient.isConnected(repository.getUsername(), repository.getPassword());
                    break;
                default:
                    break;
            }
            String[] fields = {"status"};
            if (connected) {
                repository.setStatus(String.valueOf(Status.NORMAL.ordinal()));
            } else {
                repository.setStatus(String.valueOf(Status.ABNORMAL.ordinal()));
            }
            repositoryService.updateWithField(repository, fields);
        }
    }

}
