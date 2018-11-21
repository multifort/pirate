package com.bocloud.paas.server.config;

import com.bocloud.common.enums.BoCloudConfig;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.Result;
import com.bocloud.common.utils.NicTools;
import com.bocloud.coordinator.config.client.SimpleZkClient;
import com.bocloud.coordinator.config.client.ZkClientHelper;
import com.bocloud.coordinator.esb.intf.EsbService;
import com.bocloud.paas.dao.system.DictionaryDao;
import com.bocloud.paas.entity.Dictionary;
import com.bocloud.paas.server.cache.DictionaryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Initialization {

    private static Logger logger = LoggerFactory.getLogger(Initialization.class);

    @Autowired
    private EsbService esbService;

    @Autowired
    private ZkClientHelper zkClientHelper;

    @Autowired
    private DictionaryDao dictionaryDao;

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.ip:127.0.0.1}")
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * 服务初始化
     */
    @PostConstruct
    public void init() {
        this.register(); // 注册
        this.electLeader(); // 选主
        this.dictionary(); // 字典
        this.initConfig(); // 初始化配置
    }

    private void dictionary() {
        if (!DictionaryCache.isInit()) {
            return;
        }
        logger.info("Start to init cache dictionary...");
        try {
            List<Dictionary> dictList = dictionaryDao.list();
            Map<String, String> itemMap = new ConcurrentHashMap<String, String>();
            for (Dictionary dict : dictList) {
                itemMap.put(dict.getDictValue(), dict.getName());
            }
            synchronized (DictionaryCache.class) {
                if (DictionaryCache.isInit()) {
                    DictionaryCache.writeCache(itemMap);
                    DictionaryCache.setInit(false);
                    ;
                }
            }
            logger.info("Cache dictionary successful!");
        } catch (Exception e) {
            logger.error("Cache dictionary exception!", e);
            System.exit(-1);
        }
    }

    /**
     * 选主
     */
    private void electLeader() {
        String path = BoCloudConfig.Leadership.getZkPath() + "/" + BoCloudService.Cmp.getZkNode();
        esbService.elect(path, null);
    }

    private void register() {
        String address = this.ip;
        String localhost = "127.0.0.1";
        if (address.equalsIgnoreCase(localhost)) {
            address = NicTools.getIp();
        }
        if (StringUtils.hasText(address)) {
            String servicePath = address + ":" + port;
            Result result = esbService.register(BoCloudService.Cmp.getZkNode(), servicePath, null);
            logger.info(result.toString());
            if (!result.isSuccess()) {
                logger.error("服务启动失败！原因：[{}];[{}]", "服务注册失败！", result.getMessage());
                System.exit(-1);
            } else {
                logger.info("服务启动成功！");
            }
            MDC.put("ip", ip);
            MDC.put("module", BoCloudService.Cmp.name());
        } else {
            logger.error("服务启动失败！原因：[{}]", "获取主机IP失败！");
            System.exit(-1);
        }
    }

    private void initConfig() {
        String path = BoCloudConfig.Butler.getZkPath();
        SimpleZkClient client = new SimpleZkClient(this.zkClientHelper.getClient());
        // 判断是否基础路径是否存在，不存在则创建
        if (!client.isExists(path, false)) {
            client.create(path, "");
        }
    }

    @PreDestroy
    public void destory() {
        logger.info("Service Stopped");
    }

}
