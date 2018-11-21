package com.bocloud.paas.service.system;

import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;

import com.bocloud.common.model.BsmResult;

/**
 * 日志接口
 * 
 * @author luogan
 *
 */

public interface EsLogService {

	public String get(String index, String type, String id) throws Exception;

	public Long count(String index, String type) throws Exception;

	@SuppressWarnings("rawtypes")
	public List queryByBuilders(String index, String type) throws Exception;

	public BsmResult list(QueryBuilder queryBuilder, String index,
			String type, String sort, String order, Integer page,
			Integer pageSize) throws Exception;
	
	public BsmResult getSystemLog();

}
