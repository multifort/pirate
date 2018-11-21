package com.bocloud.paas.service.application.Impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.coordinator.harmony.HarmonyLock;
import com.bocloud.coordinator.harmony.LockFactory;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.dao.application.ApplicationLayoutInfoDao;
import com.bocloud.paas.dao.application.LayoutDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Layout;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.app.resource.CommonServiceImpl;
import com.bocloud.paas.service.application.LayoutService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service("layoutService")
public class LayoutServiceImpl extends CommonServiceImpl implements LayoutService {

	private static final Logger logger = LoggerFactory.getLogger(LayoutServiceImpl.class);

	@Autowired
	private LayoutDao layoutDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private LockFactory lockFactory;
	@Autowired
	private ApplicationLayoutInfoDao applicationLayoutInfoDao;

	@Value("${local.storage.path}")
	private String localStoragePath;

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser) {
		GridBean gridBean = null;
		//获取当前用户所在的组织机构以及组织机构下的子机构ID
		String deptIds = userService.listDept(requestUser.getId());
		
		if (null == params) {
			params = Lists.newArrayList();
		}
		if (null == sorter) {
			sorter = Maps.newHashMap();
		}
		sorter.put("gmtCreate", Common.ONE);
		try {
			int total = layoutDao.count(params, deptIds);
			if (simple) {
				List<SimpleBean> beans = layoutDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				List<Layout> layouts = layoutDao.list(page, rows, params, sorter, deptIds);
				if (layouts != null && !layouts.isEmpty()) {
					for (Layout layout : layouts) {
						// 获取用户信息
						User creator = userDao.query(layout.getCreaterId());
						layout.setCreatorName(creator.getName());
						// 获取修改者信息
						User mender = userDao.query(layout.getMenderId());
						layout.setMenderName(mender.getName());
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, layouts);
			}
			return new BsmResult(true, gridBean, "获取编排信息成功");
		} catch (Exception e) {
			logger.error("Query cluster list Exception:", e);
			return new BsmResult(false, "获取编排信息异常");
		}
	}

	@Override
	public BsmResult listUsed(int page, int rows, List<Param> params, Map<String, String> sorter,
			Boolean simple) {
		GridBean gridBean = null;
		if (null == params) {
			params = Lists.newArrayList();
		}
		if (null == sorter) {
			sorter = Maps.newHashMap();
		}
		sorter.put("layout.gmtCreate", Common.ONE);
		try {
			int total = applicationLayoutInfoDao.count(params);
			if (simple) {

			} else {
				List<Application> list = applicationLayoutInfoDao.list(page, rows, params, sorter);
				gridBean = GridHelper.getBean(page, rows, total, list);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("Query application with layOut list fail:", e);
			return new BsmResult(false, "查询失败");
		}
	}

	@Override
	public BsmResult create(Layout layout, RequestUser requestUser) {
		BsmResult bsmResult = new BsmResult();
		String fileDir;
		User user = null;
		try {
			String fileName;
			if (null == (user = getUser(requestUser.getId()))) {
				bsmResult.setMessage("未获取到当前用户信息");
				return bsmResult;
			}
			// 名称校验
			Layout checkResult = layoutDao.checkName(layout.getName());
			if (null != checkResult) {
				bsmResult.setMessage("编排名称已经存在");
				return bsmResult;
			}
			fileDir = localStoragePath + File.separatorChar
					+ DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS") + "-" + layout.getName();
			if ("".equals(layout.getFileName())) {
				fileName = fileDir + File.separatorChar + layout.getName() + ".yaml";
				layout.setFileName(layout.getName() + ".yaml");
			} else {
				fileName = fileDir + File.separatorChar + layout.getFileName();
			}
			bsmResult = FileUtil.createFile(fileName, layout.getFileContent());
		} catch (Exception e) {
			logger.error("file upload failed", e);
			bsmResult.setSuccess(false);
			bsmResult.setMessage("上传文件异常！");
			return bsmResult;
		}
		if (bsmResult.isSuccess()) {
			layout.setStatus(BaseStatus.NORMAL.name());
			layout.setFilePath(fileDir);
			layout.setProps(String.valueOf(1));
			layout.setDeptId(user.getDepartId());
			try {
				layoutDao.save(layout);
				bsmResult.setSuccess(true);
				bsmResult.setData(layout);
				bsmResult.setMessage("添加编排文件成功！");
			} catch (Exception e) {
				logger.error("layout file save failed", e);
				bsmResult.setSuccess(false);
				bsmResult.setMessage("添加编排文件失败！");
			}
		} else {
			bsmResult.setSuccess(false);
			bsmResult.setMessage("上传文件异常！");
		}
		return bsmResult;
	}
	
	private User getUser(Long id) {
		User user = null;
		try {
			user = userDao.query(id);
			if (null == user) {
				logger.warn("该用户不存在");
			}
		} catch (Exception e) {
			logger.error("获取该用户信息异常", e);
		}
		return user;
	}

	@Override
	public BsmResult modify(Layout bean, Long userId) {
		String path = Layout.class.getSimpleName() + "_" + bean.getId();
		HarmonyLock lock = null;
		lock = lockFactory.getLock(path);
		if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
			logger.warn("Get harmonyLock time out!");
			return new BsmResult(false, "请求超时");
		}
		try {
			Layout layout = layoutDao.query(bean.getId());
			if (null == layout) {
				logger.warn("Layout does not exist!");
				return new BsmResult(false, "编排文件不存在！");
			}
			BsmResult bsmResult = new BsmResult();
			if (bean.getFileContent() != null) {
				bsmResult = FileUtil.createFile(layout.getFilePath() + File.separatorChar + layout.getFileName(),
						bean.getFileContent());
			}
			if (bsmResult.isSuccess()) {
				layout.setName(bean.getName());
				layout.setFileName(layout.getFileName());
				layout.setRemark(bean.getRemark());
				layout.setProps(bean.getProps());
				layout.setMenderId(userId);
				this.layoutDao.update(layout);
			}
			return new BsmResult(true, "编排文件修改成功！");
		} catch (Exception e) {
			logger.error("Modify cluster exception:", e);
			return new BsmResult(false, "编排文件修改异常！");
		} finally {
			lock.release();
		}
	}

	@Override
	public BsmResult remove(Long id, Long userId) {
		String path = Layout.class.getSimpleName() + "_" + id;
		HarmonyLock lock = null;
		try {
			lock = lockFactory.getLock(path);
			if (!lock.acquire(path, 10, TimeUnit.SECONDS)) {
				logger.warn("Get harmonyLock time out!");
				return new BsmResult(false, "请求超时");
			}
			Layout object = layoutDao.query(id);
			if (null == object) {
				logger.warn("Layout does not exist!");
				return new BsmResult(false, "编排文件不存在！");
			}
			if (!removeLayoutInfos(id, userId)) {
				return new BsmResult(false, "编排文件删除异常！");
			}
			return new BsmResult(true, "编排文件删除成功！");
		} catch (Exception e) {
			logger.error("Remove cluster exception:", e);
			return new BsmResult(false, "编排文件删除异常！");
		} finally {
			if (null != lock) {
				lock.release();
			}
		}
	}

	@Override
	public BsmResult detail(Long id) {
		try {
			Layout layout = layoutDao.query(id);
			if (null == layout) {
				return new BsmResult(false, "编排文件不存在！");
			}
			// 获取用户信息
			User creator = userDao.query(layout.getCreaterId());
			layout.setCreatorName(creator.getName());
			// 获取修改者信息
			User mender = userDao.query(layout.getMenderId());
			layout.setMenderName(mender.getName());
			String fileContent = FileUtil.readFile(layout.getFilePath() + File.separatorChar + layout.getFileName());
			if (StringUtils.hasText(fileContent)) {
				layout.setFileContent(fileContent);
			}
			return new BsmResult(true, layout, "获取编排文件成功");
		} catch (Exception e) {
			logger.error("Get cluster exception：", e);
			return new BsmResult(false, "获取编排文件信息异常！");
		}
	}

	private boolean removeLayoutInfos(Long id, Long userId) {
		try {

			return layoutDao.remove(id, userId) && layoutDao.deleteLayoutAppInfo(id);
		} catch (Exception e) {
			logger.error("Remove layout infos Excetion", e);
			return false;
		}
	}

	@Override
	public BsmResult getVariablesById(Long id) {
		BsmResult bsmResult = new BsmResult();
		Layout layout = null;
		if (null == (layout = queryLayout(id, bsmResult))) {
			return bsmResult;
		}
		String filePath = layout.getFilePath() + File.separatorChar + layout.getFileName();
		String encoding = "UTF-8";
		File file = new File(filePath);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				System.out.println(lineTxt);
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return new BsmResult(false, "读取文件失败");
		}
		return bsmResult;
	}

}
