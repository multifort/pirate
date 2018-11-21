package com.bocloud.paas.service.process.Impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.paas.dao.process.CodeRepositoryDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.CodeRepository;
import com.bocloud.paas.entity.CodeRepository.Status;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.service.process.CodeRepositoryService;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;

/**
 * @Describe: 流程编排 数据总览业务层实现
 * @author Zaney
 * @2017年6月14日
 */
@Service("codeRepositoryService")
public class CodeRepositoryServiceImpl implements CodeRepositoryService {
	private static Logger logger = LoggerFactory.getLogger(CodeRepositoryServiceImpl.class);
	
	@Autowired
	private CodeRepositoryDao codeRepositoryDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserService userService;
	

	@Override
	public BsmResult create(CodeRepository codeRepository, RequestUser requestUser) {
		BsmResult bsmResult = new BsmResult();
		User user = null;
		if (null == (user = getUser(requestUser.getId()))) {
			bsmResult.setMessage("未获取到当前用户信息");
			return bsmResult;
		}
		//校验是否存在
		boolean existed = existed(codeRepository.getName());
		if (existed) {
			bsmResult.setMessage("该名称已存在，请更换后再关联");
			return bsmResult;
		}
		
		try {
			codeRepository.setCreaterId(user.getId());
			codeRepository.setMenderId(user.getId());
			codeRepository.setOwnerId(user.getId());
			codeRepository.setDeptId(user.getDepartId());
			codeRepository.setStatus(String.valueOf(CodeRepository.Status.ACTIVATE.ordinal()));//状态默认锁定
			boolean saved = codeRepositoryDao.baseSave(codeRepository);
			if (saved) {
				bsmResult.setSuccess(true);
				bsmResult.setMessage("关联代码库成功");
			} else {
				bsmResult.setMessage("关联代码库失败");
			}
		} catch (Exception e) {
			logger.error("关联代码库异常", e);
			bsmResult.setMessage("关联代码库异常");
		}
		return bsmResult;
	}
	
    /**
	 * 获取用户信息
	 * @param id
	 * @return
	 */
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
	public BsmResult modify(CodeRepository codeRepository, RequestUser user) {
		BsmResult bsmResult = new BsmResult();
		//获取对象信息
		CodeRepository codeRepo = list(codeRepository.getId());
		if (null == codeRepo) {
			bsmResult.setMessage("未获取到对象信息");
			return bsmResult;
		}
		
		//修改数据库字段
		codeRepo.setGmtModify(new Date());
		codeRepo.setCodeSource(codeRepository.getCodeSource());
		codeRepo.setUsername(codeRepository.getUsername());
		codeRepo.setPassword(codeRepository.getPassword());
		codeRepo.setRemark(codeRepository.getRemark());
		codeRepo.setMenderId(user.getId());
		boolean updated = update(codeRepo);
		if (updated) {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("修改成功");
		} else {
			bsmResult.setMessage("修改失败");
		}
		
		return bsmResult;
	}

	@Override
	public BsmResult remove(List<Long> ids) {
		BsmResult bsmResult = new BsmResult();
		int count = 0;
		for (Long id : ids) {
			try {
				boolean deleted = codeRepositoryDao.delete(CodeRepository.class, id);
				if (!deleted) {
					logger.error("删除代码仓库对象失败");
				} else {
					count ++;
				}
			} catch (Exception e) {
				logger.error("删除代码仓库对象异常", e);
				continue;
			}
		}
		
		if (count == ids.size()) {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("删除成功");
		} else {
			bsmResult.setMessage("删除一些对象信息失败");
		}
		return bsmResult;
	}

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser) {
		List<CodeRepository> codeRepositories;
		GridBean gridBean;
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(requestUser.getId());
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = 0;
			total = codeRepositoryDao.count(params, deptIds);
			if (simple) {
				codeRepositories = codeRepositoryDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, codeRepositories);
			} else {
				codeRepositories = codeRepositoryDao.list(page, rows, params, sorter, deptIds);
				gridBean = GridHelper.getBean(page, rows, total, codeRepositories);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("list code Repository exception:", e);
			return new BsmResult(false, "查询代码仓库列表异常");
		}
	}

	@Override
	public BsmResult detail(Long id) {
		BsmResult bsmResult = new BsmResult(true, "获取成功");
		CodeRepository codeRepository = list(id);
		if (null == codeRepository) {
			logger.warn("未获取到对象信息");
		}
		bsmResult.setData(codeRepository);
		return bsmResult;
	}
	
	/**
	 * 校验对象是否存在，true|存在，false|不存在
	 * @param name
	 * @return
	 */
	private boolean existed(String name){
		CodeRepository codeRepository = null;
		try {
			codeRepository = codeRepositoryDao.existed(name);
			if (null == codeRepository) {
				return false;
			}
		} catch (Exception e) {
			logger.error("获取代码仓库对象信息异常", e);
		}
		return true;
	}
	
	/**
	 * 修改代码仓库对象信息
	 * @param codeRepository
	 * @param fields
	 * @return
	 */
	private boolean update(CodeRepository codeRepository){
		boolean saved = false;
		try {
			saved = codeRepositoryDao.update(codeRepository);
			
		} catch (Exception e) {
			logger.error("修改对象信息异常", e);
		}
		return saved;
	}
	
	/**
	 * 获取代码仓库对象信息
	 * @param id
	 * @return
	 */
	private CodeRepository list(Long id){
		CodeRepository codeRepository = null;
		try {
			codeRepository = codeRepositoryDao.detail(id);
		} catch (Exception e) {
			logger.error("获取代码仓库对象信息异常", e);
		}
		return codeRepository;
	}

	@Override
	public BsmResult status(Long id) {
		BsmResult bsmResult = new BsmResult();
		//获取对象信息
		CodeRepository codeRepository = list(id);
		
		if (null == codeRepository) {
			bsmResult.setMessage("未获取到对象信息");
			return bsmResult;
		}
		
		//修改状态
		String newStatusName = null;
		String newStatus = null;
		Status status = CodeRepository.Status.values()[Integer.valueOf(codeRepository.getStatus())];
		
		switch (status) {
		case ACTIVATE:
			newStatusName = "锁定";
			newStatus = String.valueOf(CodeRepository.Status.Lock.ordinal());
			break;
        case Lock:
        	newStatusName = "激活";
			newStatus = String.valueOf(CodeRepository.Status.ACTIVATE.ordinal());;
			break;

		}
		
		//修改数据库状态信息
		codeRepository.setStatus(newStatus);
		boolean updated = update(codeRepository);
		
		if (updated) {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("已被"+newStatusName);
		} else {
			bsmResult.setMessage(newStatusName+"失败");
		}
		return bsmResult;
	}
	
}
