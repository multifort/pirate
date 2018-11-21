package com.bocloud.paas.web.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bocloud.common.model.BaseResult;
import com.bocloud.common.utils.SystemTool;

/**
 * 界面跳转控制器
 * 
 * @author dmw
 *
 */
@Controller
@RequestMapping("/")
public class IndexController {

	/**
	 * 跳转 到主页
	 * 
	 * @return
	 */
	@RequestMapping("/")
	public ModelAndView index() {
		return new ModelAndView("/index");
	}

	/**
	 * 跳转到404界面
	 * 
	 * @return
	 */
	@RequestMapping("/404")
	public ModelAndView none() {
		return new ModelAndView("/404");
	}

	/**
	 * 跳转到500界面
	 * 
	 * @return
	 */
	@RequestMapping("/500")
	public ModelAndView error() {
		return new ModelAndView("/500");
	}

	@RequestMapping("/status")
	@ResponseBody
	public BaseResult<Map<String, Object>> status() {
		SystemTool systemTool = new SystemTool();
		return systemTool.status();
	}
}
