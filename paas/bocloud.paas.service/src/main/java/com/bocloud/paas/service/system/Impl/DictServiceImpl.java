package com.bocloud.paas.service.system.Impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.dao.system.DictionaryDao;
import com.bocloud.paas.entity.Dictionary;
import com.bocloud.paas.service.system.DictService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 字典服务实现类
 * 
 * @author luogan
 *
 */
@Service("dictService")
public class DictServiceImpl implements DictService {

	private static Logger logger = LoggerFactory
			.getLogger(DictServiceImpl.class);
	@Autowired
	private DictionaryDao dictionaryDao;

	@Override
	public BsmResult create(Dictionary dictionary, Long userId) {
		try {
			//名称校验
			boolean existed = isExist(dictionary.getName());
			if (existed) {
				return new BsmResult(false, "该系统参数名称已存在，请更换后再添加");
			}
			// 保存数据字典
			dictionary.setCreaterId(userId);
			dictionary.setMenderId(userId);
			dictionary.setOwnerId(userId);
			dictionaryDao.save(dictionary);
			return new BsmResult(true, "创建成功");
		} catch (Exception e) {
			logger.error("创建系统参数异常", e);
			return new BsmResult(false, "创建系统参数异常");
		}
	}
	/**
	 * 判断名称是否存在
	 * @param name
	 * @return
	 */
	private boolean isExist(String name) {
		Dictionary dictionary = null;
		try {
			dictionary = dictionaryDao.query(name);
			if (null == dictionary) {
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error("获取系统参数 name ="+name+" 异常");
			return true;
		}
	}

	@Override
	public BsmResult modify(Dictionary dictionary, Long userId) {
		try {
			// 更新数据字典
			dictionary.setMenderId(userId);
			dictionary.setGmtModify(new Date());
			Boolean updateResult = dictionaryDao.update(dictionary);
			if (updateResult) {
				return new BsmResult(true, "修改成功");
			} else {
				return new BsmResult(false, "修改失败：数据库操作失败");
			}
		} catch (Exception e) {
			logger.error("更改数据字典失败", e);
			return new BsmResult(false, "更改数据字典失败");
		}
	}

	@Override
	public BsmResult remove(List<Long> ids, Long userId) {

		try {
			// 删除数据字典
			if (ListTool.isEmpty(ids)) {
				return new BsmResult(false, "删除失败：传值为空");
			}
			String failResult = "";
			for (Long id : ids) {
				int delResult = dictionaryDao.remove(id, userId);
				if (delResult <= 0) {
					List<Dictionary> dictionarys = dictionaryDao.queryById(id);
					if (!ListTool.isEmpty(dictionarys)) {
						failResult += dictionarys.get(0).getName();
					}
				}
			}
			if (failResult.equals("")) {
				return new BsmResult(true, "删除成功");
			} else {
				return new BsmResult(true, failResult + "删除失败，其余删除成功");
			}
		} catch (Exception e) {
			logger.error("数据字典删除失败", e);
			return new BsmResult(false, "数据字典删除失败");
		}
	}

	@Override
	public BsmResult list(Integer page, Integer rows, List<Param> params,
			Map<String, String> sorter, boolean simple) {
		List<Dictionary> dictionarys = null;
		GridBean gridBean = null;
		try {
			if (null == params) {
				params = Lists.newArrayList();
			}
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = dictionaryDao.count(params);
			if (simple) {
				dictionarys = dictionaryDao.list(params, sorter);
				gridBean = new GridBean(1, 1, total, dictionarys);
			} else {
				dictionarys = dictionaryDao.list(page, rows,
						params, sorter);
				gridBean = GridHelper.getBean(page, rows, total, dictionarys);
			}
			return new BsmResult(true, gridBean, "查询成功");
		} catch (Exception e) {
			logger.error("查询出错", e);
			return new BsmResult(false, "查询出错");
		}
	}

	@Override
	public BsmResult checkKey(String dictKey, Long userId) {

		try {
			if (null != dictKey && !"".equals(dictKey)) {
				Dictionary dictionarys = dictionaryDao
						.queryByKey(dictKey);
				if (null != dictionarys) {
					return new BsmResult(false, "该Key已存在");
				}
			}
			return new BsmResult(true, "该Key可用");
		} catch (Exception e) {
			logger.error("查询失败", e);
			return new BsmResult(false, "校验Key出错");
		}
	}

	@Override
	public BsmResult statistic() {

		try {
			List<Dictionary> dictionarys = dictionaryDao.list();
			Map<String, List<Dictionary>> dicMap = new HashMap<>();
			if (!ListTool.isEmpty(dictionarys)) {
				for (Dictionary dic : dictionarys) {
					if (null == dicMap.get(dic.getPvalue())) {
						List<Dictionary> dicList = new ArrayList<>();
						dicList.add(dic);
						dicMap.put(dic.getPvalue(), dicList);
					} else {
						dicMap.get(dic.getPvalue()).add(dic);
					}
				}
			}
			return new BsmResult(true, dicMap, "获取系统参数成功");
		} catch (Exception e) {
			logger.error("获取系统参数异常", e);
			return new BsmResult(false, "获取系统参数失败");
		}
	}

	@Override
	public BsmResult batchModify(List<Dictionary> dictionarys, Long userId) {

		String result = "";
		String successResult = "";
		String failResult = "";
		for (Dictionary dictionary : dictionarys) {
			try {
				Boolean updateResult = dictionaryDao.update(dictionary);

				if (updateResult) {
					successResult += dictionary.getName() + ",";
				} else {
					failResult += dictionary.getName() + ",";
				}
				if (successResult != "") {
					result = "修改系统参数成功";
				} else if(failResult != "") {
					result = "修改系统参数失败";
				}
				/*if (successResult != "" && failResult != "") {
					result = successResult + "修改成功;" + failResult + "修改失败";
				} else if (successResult != "") {
					result = successResult + "修改成功";
				} else {
					result = failResult + "修改失败";
				}*/
			} catch (Exception e) {
				logger.error("修改系统参数异常", e);
			}
		}

		return new BsmResult(true, result);
	}

	@Override
	public BsmResult detail(String dictKey) {
		Dictionary dictionary = null;
		try {
			if (null != dictKey && !"".equals(dictKey)) {
				dictionary = dictionaryDao.queryByKey(dictKey);
			}
			return new BsmResult(true, dictionary, "查询成功");

		} catch (Exception e) {
			return new BsmResult(false, "查询失败");
		}

	}

	@Override
	public BsmResult template() {
		String fileDir = FileUtil.filePath("resource_template");
		File file = new File(fileDir + File.separatorChar + "dictionary_parameter.yaml");
		String paramContext = FileUtil.readParameters(file);
		return new BsmResult(true, JSON.toJSON(paramContext), "获取成功");
	}

}