package com.bocloud.paas.service.app.resource;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.IDFactory;
import com.bocloud.log.dao.LogDao;
import com.bocloud.log.entity.AccessLog;
import com.bocloud.paas.dao.application.*;
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
import com.bocloud.paas.service.application.util.ApplicationUtil;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 公用service
 *
 * @author zjm
 */
@Service("commonService")
public class CommonServiceImpl {

    private static Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);

    @Autowired
    protected LogDao logDao;
    @Autowired
    protected RepositoryDao registryDao;
    @Autowired
    protected UserDao userDao;
    @Autowired
    protected RepositoryImageInfoDao registryImageInfoDao;
    @Autowired
    protected RepositoryImageDao registryImageDao;
    @Autowired
    protected ImageDao imageDao;
    @Autowired
    protected UserService userService;
    @Autowired
    protected AuthDao authDao;
    @Autowired
    protected RoleDao roleDao;
    @Autowired
    protected ApplicationDao applicationDao;
    @Autowired
    protected LayoutDao layoutDao;
    @Autowired
    protected ApplicationLayoutInfoDao applicationLayoutInfoDao;
    @Autowired
    protected ApplicationImageInfoDao applicationImageInfoDao;
    @Autowired
    protected ApplicationUtil oseUtil;
    @Autowired
    protected DeployHistoryDao deployHistoryDao;
    @Autowired
    protected EventPublisher resourceEventPublisher;
    @Autowired
    protected EnvironmentDao environmentDao;
    @Autowired
    DepartmentDao departmentDao;

    protected BsmResult saveLog(String object, String action, String result, Long userId) {
        AccessLog accessLog = new AccessLog();
        accessLog.setId(IDFactory.instance().uuid());
        accessLog.setAction(action);
        accessLog.setResult(result);
        accessLog.setUserId(userId);
        accessLog.setObject(object);
        accessLog.setGmtCreate(new Date());
        try {
            logDao.save(accessLog);
        } catch (Exception e) {
            logger.error("save access log failure: ", e);
            return new BsmResult(false, "添加操作记录成功！" + e);
        }
        return new BsmResult(true, "添加操作记录成功！");
    }

    protected Repository queryRegistry(Long registryId) {
        // 获取仓库信息
        Repository registry = null;
        try {
            registry = registryDao.query(registryId);
            return registry;
        } catch (Exception e) {
            logger.error("query registry [" + registryId + "] exception: ", e);
            return null;
        }
    }

    protected User queryUser(Long userId, BsmResult bsmResult) {
        User user = null;
        try {
            user = userDao.query(userId);
        } catch (Exception e) {
            logger.error("query user [" + userId + "] failure: ", e);
            bsmResult.setSuccess(false);
            bsmResult.setMessage("获取用户[" + userId + "]信息失败！");
        }
        if (null == user) {
            logger.warn("query user [" + userId + "] empty.");
            bsmResult.setSuccess(false);
            bsmResult.setMessage("获取用户[" + userId + "]信息为空！");
        }
        return user;
    }

    protected Layout queryLayout(Long layoutId, BsmResult bsmResult) {
        Layout layout = null;
        try {
            layout = layoutDao.query(layoutId);
        } catch (Exception e) {
            logger.error("query layout [" + layoutId + "] failure: ", e);
            bsmResult.setSuccess(false);
            bsmResult.setMessage("获取编排文件[" + layoutId + "]信息失败！");
        }
        if (null == layout || StringUtils.isAnyBlank(layout.getFilePath(), layout.getFileName())) {
            logger.warn("query layout [" + layoutId + "] empty.");
            bsmResult.setSuccess(false);
            bsmResult.setMessage("获取编排文件信息为空！");
        }
        return layout;
    }

}
