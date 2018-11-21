package com.bocloud.paas.web.controller.security;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final String BASE_SERVICE = "/auth";

    private static final BoCloudService SERVICE = BoCloudService.Cmp;

    private final ServiceFactory serviceFactory;

    private final BasicController basicController;

    @Autowired
    public AuthController(ServiceFactory serviceFactory, BasicController basicController) {
        this.serviceFactory = serviceFactory;
        this.basicController = basicController;
    }

    /**
     * 获取权限列表
     *
     * @param parentId 请求参数
     * @param request  请求对象
     */
    @RequestMapping(value = "/list", method = {RequestMethod.POST})
    public BsmResult list(@RequestParam(value = Common.PARENTID, required = true) Long parentId,
            HttpServletRequest request) {
        Map<String, Object> param = new HashMap<>();
        if (null != parentId) {
            param.put(Common.PARENTID, parentId);
        }
        String url = BASE_SERVICE + "/list";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }

    /**
     * 查询图标
     */
    @RequestMapping(value = "/list/icon", method = {RequestMethod.POST})
    public BsmResult listIcon(HttpServletRequest request) {
        Map<String, Object> param = new HashMap<>();
        String url = BASE_SERVICE + "/list/icon";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }

    /**
     * 查询父节点
     */
    @RequestMapping(value = "/listParents", method = {RequestMethod.POST})
    public BsmResult listParents(HttpServletRequest request) {
        Map<String, Object> param = new HashMap<>();
        String url = BASE_SERVICE + "/listParents";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
    }

    /**
     * 权限创建
     */
    @RequestMapping(value = "/create", method = {RequestMethod.POST})
    public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
            HttpServletRequest request) {
        return basicController.create(params, BASE_SERVICE, request, AuthController.class.getSimpleName());
    }

    /**
     * 权限修改
     */
    @RequestMapping(value = "/modify", method = {RequestMethod.POST})
    public BsmResult modify(@RequestParam(value = Common.PARAMS, required = false) String params,
            HttpServletRequest request) {
        return basicController.modify(params, BASE_SERVICE, request, AuthController.class.getSimpleName());
    }

    /**
     * 权限删除
     */
    @RequestMapping(value = "/remove", method = {RequestMethod.POST})
    public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
            HttpServletRequest request) {
        return basicController.remove(params, BASE_SERVICE, request, AuthController.class.getSimpleName());
    }

    /**
     * 权限详细
     */
    @RequestMapping(value = "/detail", method = {RequestMethod.GET})
    public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
            HttpServletRequest request) {
        return basicController.detail(params, BASE_SERVICE, request, AuthController.class.getSimpleName());
    }

}
