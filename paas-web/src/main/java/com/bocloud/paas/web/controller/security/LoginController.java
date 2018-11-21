package com.bocloud.paas.web.controller.security;

import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.bocloud.paas.web.License.License;
import com.bocloud.paas.web.License.LicenseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.model.SSOConfig;
import com.bocloud.paas.web.utils.CommonString;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
//import com.ideal.sso.SsoUtil;

@RestController
@RequestMapping("/")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final BoCloudService SERVICE = BoCloudService.Cmp;

    @Autowired
    private ServiceFactory serviceFactory;
    @Autowired
    private SSOConfig ssoConfig;
    @Autowired
    private LicenseFile licenseFile;

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public BsmResult login(@RequestParam(value = Common.USERNAME, required = true) String username,
                           @RequestParam(value = Common.PASSWORD, required = true) String password,
                           @RequestParam(value = Common.CODE, required = true) String code, HttpServletRequest request) {


        //校验license
        System.out.println("license---" + licenseFile);
        File file = new File(licenseFile.getLicenseFile());
        License license = new License();
        int isValid = license.checkLicense(file);
        if (isValid < 0) {
            return new BsmResult(false, "产品试用期结束，请联系厂商！");
        }


        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(code)) {
            return new BsmResult(false, "用户名，密码和验证码必输！", "500", "用户名，密码和验证码必输！");
        }
        String sessionId = null;
        try {
            sessionId = request.getSession().getId();
        } catch (Exception e) {
            logger.error("验证码已过期", e);
            return new BsmResult(false, "验证码已过期!");
        }
        String captcha = (String) request.getSession().getAttribute(CommonString.CAPTCHA);
        if (StringUtils.isEmpty(captcha)) {
            return new BsmResult(false, "验证码已过期!");
        }
        if (!code.equalsIgnoreCase(captcha)) {
            return new BsmResult(false, "验证码错误！");
        }
        Map<String, Object> param = MapTools.simpleMap(Common.USERNAME, username);
        param.put(Common.SESSIONID, sessionId);
        param.put(Common.PASSWORD, password);
        String url = "/login";
        RemoteService service = serviceFactory.build(SERVICE, url, BoCloudMethod.OPERATE, null, param);
        BsmResult result = service.invoke();
        switch (isValid) {
            case 0:
                result.setMessage("距离产品试用期结束还剩下0天，继续试用请联系生产厂商！");
                break;
            case 1:
                result.setMessage("距离产品试用期结束还剩下1天，继续试用请联系生产厂商！");
                break;
            case 2:
                result.setMessage("距离产品试用期结束还剩下2天，继续试用请联系生产厂商！");
                break;
            case 3:
                result.setMessage("距离产品试用期结束还剩下3天，继续试用请联系生产厂商！");
                break;
        }
        if (result.isSuccess()) {
            this.handle(request, result);
        }
        return result;
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.POST})
    public BsmResult logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return new BsmResult(true, "退出成功！");
    }

    @RequestMapping(value = "/sso", method = {RequestMethod.GET})
    public BsmResult ssoQuery(@RequestParam(value = "query", required = true) String query,
                              HttpServletRequest request) {

        if (StringUtils.isEmpty(query)) {
            return new BsmResult(false, "用户工号不能为空！", "500", "用户工号不能为空！");
        }
        BsmResult result = new BsmResult(false, "");
        String userId = null;
        String sessionId = request.getSession().getId();
		/*try {
			String[] infoList = SsoUtil.getUserInfo(query, ssoConfig.getDomain(), ssoConfig.getPrivateKey());
			if (infoList != null && infoList.length > 0) {
				userId = infoList[0];
				String url = "/sso";
				Map<String, Object> param = MapTools.simpleMap(Common.USERID, userId);
				param.put(Common.SESSIONID, sessionId);
				RemoteService service = serviceFactory.build(SERVICE, url, BoCloudMethod.OPERATE, null, param);
				result = service.invoke();
			}
			if (result.isSuccess()) {
				this.handle(request, result);
			}
		} catch (Exception e) {
			logger.error("工号错误！", e);
			return new BsmResult(false, "用户工号输入错误！");
		}*/
        return result;
    }

    private void handle(HttpServletRequest request, BsmResult result) {

        String data = JSONObject.toJSONString(result.getData());
        JSONObject dataObject = JSONObject.parseObject(data);
        JSONObject userData = dataObject.getJSONObject(Common.USER);
        String roles = dataObject.getString(Common.ROLES);
        String auths = dataObject.getString(Common.AUTHS);
        String authApi = dataObject.getString("authApi");
        String path = request.getContextPath();
        String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path
                + "/";
        HttpSession session = request.getSession();
        session.setAttribute(Common.BASE, base);
        session.setAttribute(Common.API_KEY, dataObject.getString(Common.API_KEY));
        session.setAttribute(Common.SEC_KEY, dataObject.getString(Common.SEC_KEY));
        session.setAttribute(Common.USER, userData);
        session.setAttribute(Common.USERID, userData.getLong(Common.ID));
        session.setAttribute(Common.USERNAME, userData.getString(Common.USERNAME));
        session.setAttribute(Common.ROLES, roles);
        session.setAttribute(Common.AUTHS, auths);
        session.setAttribute("authApi", authApi);
        session.setMaxInactiveInterval(10 * 60);
    }

}
