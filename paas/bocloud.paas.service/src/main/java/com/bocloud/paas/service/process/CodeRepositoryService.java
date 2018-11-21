package com.bocloud.paas.service.process;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.CodeRepository;

/**
 * describe: 代码仓库业务层接口
 * @author Zaney
 * @data 2017年10月27日
 */
public interface CodeRepositoryService {
	
	public BsmResult create(CodeRepository codeRepository, RequestUser user);
	
	public BsmResult modify(CodeRepository codeRepository, RequestUser user);
	
	public BsmResult remove(List<Long> ids);
	
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser);
	
	public BsmResult detail(Long id);
	
	public BsmResult status(Long id);
	
}
