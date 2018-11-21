package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Authority;
import com.bocloud.paas.model.AuthorityBean;
import com.bocloud.paas.service.user.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	/**
	 * 查询权限树
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PARENTID, required = true) Long parentId) {
		if (null != parentId) {
			return authService.list(parentId);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询图标
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/icon", method = { RequestMethod.POST })
	public BsmResult listIcon() {
		return authService.listIcon();
	}

	/**
	 * 查询所有父节点
	 * 
	 * @return
	 */
	@RequestMapping(value = "/listParents", method = { RequestMethod.POST })
	public BsmResult listParents() {
		return authService.listParents();
	}

	/**
	 * 添加权限
	 * 
	 * @param params
	 *            权限属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param user.getId()
	 *            操作者ID
	 * @return 添加结果
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Authority auth = JSONObject.parseObject(object.toJSONString(), Authority.class);
			if (auth.getCategory().equals("api")) {
				auth.setPriority(0);
			}
			auth.setCreaterId(user.getId());
			return authService.create(auth);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 修改权限
	 * 
	 * @param params
	 *            权限属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            需要修改的权限ID
	 * @param user.getId()
	 *            操作者ID
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			AuthorityBean auth = JSONObject.parseObject(object.toJSONString(), AuthorityBean.class);
			return authService.modify(auth, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 移除权限
	 * 
	 * @param id
	 *            权限ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return authService.remove(id, user.getId());
	}

	/**
	 * 查看权限详细信息
	 * 
	 * @param id
	 *            权限ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return authService.detail(id);
	}

}
