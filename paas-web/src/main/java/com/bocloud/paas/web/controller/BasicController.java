package com.bocloud.paas.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
import com.bocloud.registry.utils.UrlTranslator;

/**
 * @author dmw
 */
@Component("basicController")
public class BasicController {

    private static Logger logger = LoggerFactory.getLogger(BasicController.class);

    private static final BoCloudService SERVICE = BoCloudService.Cmp;

    private final ServiceFactory serviceFactory;

    @Autowired
    public BasicController(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public BsmResult create(String params, String servicePath, HttpServletRequest request,
            String className) {
        JSONObject jsonObject = JSONTools.isJSONObj(params);
        if (null != jsonObject) {
            String url = servicePath + "/create";
            Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
                    request);
            return service.invoke();
        } else {
            logger.warn("class {} 请求方法{}参数格式异常！", className, "create");
            return ResultTools.formatErrResult();
        }
    }

    public BsmResult modify(String params, String servicePath, HttpServletRequest request,
            String className) {
        JSONObject metas = JSONObject.parseObject(params);
        if (null != metas && metas.containsKey(Common.ID)) {
            Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
            String url = UrlTranslator.translate(servicePath + "/modify", metas.getLong(Common.ID));
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.MODIFY, null, paramMap,
                    request);
            return service.invoke();
        } else {
            logger.warn("class {} 请求方法{}参数格式异常！", className, "modify");
            return ResultTools.formatErrResult();
        }
    }

    public BsmResult remove(String params, String servicePath, HttpServletRequest request,
            String className) {
        JSONObject jsonObject = JSONTools.isJSONObj(params);
        if (null != jsonObject && jsonObject.containsKey(Common.ID)) {
            String url = UrlTranslator.translate(servicePath + "/remove", jsonObject.get(Common.ID));
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.REMOVE, null, null, request);
            return service.invoke();
        } else {
            logger.warn("class {} 请求方法{}参数格式异常！", className, "remove");
            return ResultTools.formatErrResult();
        }
    }

    public BsmResult detail(String params, String servicePath, HttpServletRequest request,
            String className) {
        JSONObject jsonObject = JSONTools.isJSONObj(params);
        if (null != jsonObject && jsonObject.containsKey(Common.ID)) {
            String url = UrlTranslator.translate(servicePath + "/detail", jsonObject.get(Common.ID));
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
            return service.invoke();
        } else {
            logger.warn("class {} 请求方法{}参数格式异常！", className, "detail");
            return ResultTools.formatErrResult();
        }
    }

    public BsmResult basic(String params, String servicePath, HttpServletRequest request,
            String className) {
        JSONObject jsonObject = JSONTools.isJSONObj(params);
        if (null != jsonObject && jsonObject.containsKey(Common.ID)) {
            String url = UrlTranslator.translate(servicePath + "/basic", jsonObject.get(Common.ID));
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
            return service.invoke();
        } else {
            logger.warn("class {} 请求方法{}参数格式异常！", className, "basic");
            return ResultTools.formatErrResult();
        }
    }

    public BsmResult operate(String params, String servicePath, String method,
            HttpServletRequest request,
            String className) {
        JSONObject obj = JSONObject.parseObject(params);
        String url = UrlTranslator.translate(servicePath + "/" + method, obj.getLong(Common.ID));
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, null, request);
        return service.invoke();
    }

    public BsmResult list(Integer page, Integer rows, String params, String sorter, boolean simple,
            String servicePath,
            HttpServletRequest request, String className) {
        Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = servicePath + "/list";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }

    /**
     * 自定义方法名的list方法
     *
     * @author sxp
     * @since V1.1 2016-09-01 modify by sxp 使用简洁的方法名list
     */
    public BsmResult list(Integer page, Integer rows, String params, String sorter, boolean simple,
            String servicePath,
            HttpServletRequest request, String className, String methodName) {
        Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = servicePath + "/" + methodName;
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }

    public BsmResult operate(String params, String servicePath, HttpServletRequest request,
            String className) {
        Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
        RemoteService service = serviceFactory.safeBuild(SERVICE, servicePath, BoCloudMethod.OPERATE, null, paramMap,
                request);
        return service.invoke();
    }

    public BsmResult operate(String params, String servicePath, HttpServletRequest request,
            String className,
            String methodName) {
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.hasText(params)) {
            param.put(Common.PARAMS, params);
        }
        String url = servicePath + "/" + methodName;
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }
}
