package com.bocloud.paas.service.application.Impl;

import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.*;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.dao.application.LayoutTemplateDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Layout;
import com.bocloud.paas.entity.LayoutTemplate;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.app.resource.CommonServiceImpl;
import com.bocloud.paas.service.application.LayoutTemplateService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service("layoutTemplateService")
public class LayoutTemplateServiceImpl extends CommonServiceImpl implements
        LayoutTemplateService {

	private static final Logger logger = LoggerFactory
			.getLogger(LayoutTemplateServiceImpl.class);

	@Autowired
	private LayoutTemplateDao layoutTemplateDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private LockFactory lockFactory;

	@Override
	public BsmResult list(int page, int rows, List<Param> params,Map<String, String> sorter, Boolean simple) {
		
		GridBean gridBean = null;
		if (null == params) {
			params = Lists.newArrayList();
		}
		if (null == sorter) {
			sorter = Maps.newHashMap();
		}
		sorter.put("gmtCreate", Common.ONE);
		try {
			int total = layoutTemplateDao.count(params);
			if (simple) {
				List<SimpleBean> beans = layoutTemplateDao.list(params, sorter);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<LayoutTemplate> layoutTemplates = layoutTemplateDao.list(
						page, rows, params, sorter);
				if (layoutTemplates != null && !layoutTemplates.isEmpty()) {
					for (LayoutTemplate layoutTemplate : layoutTemplates) {
						// 获取用户信息
						User creator = userDao.query(layoutTemplate
								.getCreaterId());
						layoutTemplate.setCreatorName(creator.getName());
						// 获取修改者信息
						User mender = userDao.query(layoutTemplate
								.getMenderId());
						layoutTemplate.setMenderName(mender.getName());
					}
				}
				gridBean = GridHelper.getBean(page, rows, total,
						layoutTemplates);
			}
			return new BsmResult(true, gridBean, "获取编排模板列表成功");
		} catch (Exception e) {
			logger.error("Query layoutTemplate list Exception:", e);
			return new BsmResult(false, "获取编排模板列表异常");
		}
	}

	@Override
	public BsmResult create(LayoutTemplate layoutTemplate, RequestUser user) {

		BsmResult bsmResult = new BsmResult();
		try {
			// 名称校验
			LayoutTemplate checkResult = layoutTemplateDao
					.checkName(layoutTemplate.getName());
			if (null != checkResult) {
				bsmResult.setMessage("编排模板名称已经存在");
				return bsmResult;
			}
		} catch (Exception e) {
			logger.error("Query layoutTemplate failed", e);
			bsmResult.setSuccess(false);
			bsmResult.setMessage("编排模板名称校验异常！");
			return bsmResult;
		}
		if (!bsmResult.isSuccess()) {
			layoutTemplate.setStatus(BaseStatus.NORMAL.name());
			layoutTemplate.setProps(String.valueOf(1));
			try {
				layoutTemplateDao.save(layoutTemplate);
				bsmResult.setSuccess(true);
				bsmResult.setData(layoutTemplate);
				bsmResult.setMessage("添加编排模板成功！");
			} catch (Exception e) {
				logger.error("layout file save failed", e);
				bsmResult.setSuccess(false);
				bsmResult.setMessage("添加编排模板失败！");
			}
		}

		return bsmResult;

	}

	@Override
	public BsmResult modify(LayoutTemplate bean, Long userId) {
		
		String path = Layout.class.getSimpleName() + "_" + bean.getId();
		HarmonyLock lock = null;
		lock = lockFactory.getLock(path);
		if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
			logger.warn("Get harmonyLock time out!");
			return new BsmResult(false, "请求超时");
		}
		try {
			LayoutTemplate layoutTemplate = layoutTemplateDao.query(bean
					.getId());
			if (null == layoutTemplate) {
				logger.warn("LayoutTemplate does not exist!");
				return new BsmResult(false, "编排模板不存在！");
			}
			BsmResult bsmResult = new BsmResult();
			if (!bsmResult.isSuccess()) {
				layoutTemplate.setName(bean.getName());
				layoutTemplate.setRemark(bean.getRemark());
				layoutTemplate.setProps(bean.getProps());
				layoutTemplate.setMenderId(userId);
				this.layoutTemplateDao.update(layoutTemplate);
			}
			return new BsmResult(true, "编排模板修改成功！");
		} catch (Exception e) {
			logger.error("Modify layoutTemplate exception:", e);
			return new BsmResult(false, "编排模板修改异常！");
		} finally {
			lock.release();
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		
		String path = LayoutTemplate.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时");
			}
			LayoutTemplate object = layoutTemplateDao.query(id);
			if (null == object) {
				logger.warn("LayoutTemplate does not exist!");
				return new BsmResult(false, "编排模板不存在！");
			}
			// 判断模板下是否存在版本，不存在可以删除，存在不允许删除
			boolean result = layoutTemplateDao.remove(id, userId);
			if(result){
				return new BsmResult(true, "编排模板删除成功！");
			}else{
				return new BsmResult(false, "编排模板删除失败！");
			}
		} catch (Exception e) {
			logger.error("Remove layoutTemplate exception:", e);
			return new BsmResult(false, "编排模板删除异常！");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult detail(Long id) {
		
		try {
			LayoutTemplate layoutTemplate = layoutTemplateDao.query(id);
			if (null == layoutTemplate) {
				return new BsmResult(false, "编排模板不存在！");
			}
			// 获取用户信息
			User creator = userDao.query(layoutTemplate.getCreaterId());
			layoutTemplate.setCreatorName(creator.getName());
			// 获取修改者信息
			User mender = userDao.query(layoutTemplate.getMenderId());
			layoutTemplate.setMenderName(mender.getName());
			return new BsmResult(true, layoutTemplate, "获取编排模板成功");
		} catch (Exception e) {
			logger.error("Get layoutTemplate exception：", e);
			return new BsmResult(false, "获取编排模板异常！");
		}
	}

	@Override
	public BsmResult getList() {
		try {
			List<LayoutTemplate> layoutTemplates = layoutTemplateDao.getList();
			return new BsmResult(true, layoutTemplates, "获取编排模板列表成功");
		} catch (Exception e) {
			logger.error("Get layoutTemplates exception：", e);
			return new BsmResult(false, "获取编排模板列表异常！");
		}
	}

}
