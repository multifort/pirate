package com.bocloud.paas.service.system;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Dictionary;

/**
 * 数据字典接口
 * 
 * @author luogan
 *
 */
public interface DictService {

	public BsmResult create(Dictionary dictionary, Long userId);

	public BsmResult modify(Dictionary dictionary, Long userId);

	public BsmResult remove(List<Long> ids, Long userId);

	public BsmResult list(Integer page, Integer rows, List<Param> params,
			Map<String, String> sorter, boolean simple);

	public BsmResult checkKey(String dictKey, Long userId);
	
	public BsmResult statistic();
	
	public BsmResult detail(String dictKey);
	
	public BsmResult batchModify(List<Dictionary> dictionarys, Long userId);
	
	public BsmResult template();

}
