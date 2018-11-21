package com.bocloud.paas.service.system.Impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.paas.service.system.EsLogService;
import com.bocloud.paas.service.system.util.EsClient;

/**
 * 日志接口实现
 * 
 * @author luogan
 *
 */
@Service("esLogService")
public class EsLogServiceImpl implements EsLogService {
	private static Logger logger = LoggerFactory.getLogger(EsLogServiceImpl.class);
	
	@Autowired
	private EsClient esClient;

	@Override
	public String get(String index, String type, String id) throws Exception {

		Client client = esClient.getClient();
		// get
		GetResponse response = client.prepareGet().setIndex(index)
				.setType(type).setId(id).setOperationThreaded(false).get();
		String result = response.getSourceAsString();
		return result;
	}

	@Override
	public Long count(String index, String type) throws Exception {

		Client client = esClient.getClient();

		SearchResponse searchResponse = client.prepareSearch()
				.setIndices(index).setTypes(type).get();

		Long count = searchResponse.getHits().getTotalHits();

		try {
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0l;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List queryByBuilders(String index, String type) throws Exception {

		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {

			Client client = esClient.getClient();

			TermQueryBuilder qb = QueryBuilders.termQuery("port", "36390");

			SearchRequestBuilder requestBuilder = client.prepareSearch()
					.setIndices(index).setTypes(type).setQuery(qb);

			SearchResponse searchResponse = requestBuilder
					.addSort(
							SortBuilders.fieldSort("@timestamp").order(
									SortOrder.DESC)).setFrom(0).setSize(10)
					.get();

			// 遍历查询结果
			if (searchResponse != null) {
				SearchHits hits = searchResponse.getHits();
				if (hits != null && hits.getHits() != null) {
					Map<String, Object> hitMap = null;
					for (SearchHit hit : hits.getHits()) {
						hitMap = hit.getSource();
						if (hitMap == null || hitMap.size() <= 0) {
							continue;
						}
						dataList.add(hitMap);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭client
		}
		return dataList;
	}

	public BsmResult list(QueryBuilder queryBuilder, String index, String type,
			String sort, String order, Integer page, Integer pageSize) {

		try {

			Client client = esClient.getClient();

			SearchRequestBuilder searchRequestBuilder = client.prepareSearch();
			
			if (null != index) {
				searchRequestBuilder.setIndices(index);
			}
			if (null != type) {
				searchRequestBuilder.setTypes(type);
			}
			searchRequestBuilder.setQuery(queryBuilder);

			if ((page > 0) && (pageSize > 0)) {
				searchRequestBuilder.setFrom(pageSize * (page - 1));
				searchRequestBuilder.setSize(pageSize);
			}

			SortOrder sortOrder = SortOrder.ASC;

			if (null != order && order.equals("desc")) {
				sortOrder = SortOrder.DESC;
			}

			if ((null != sort) && (!sort.equals(""))) {
				searchRequestBuilder.addSort(sort, sortOrder);
			}

			searchRequestBuilder.setExplain(true);

			// 执行查询
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			SearchResponse response = searchRequestBuilder.get();
			long total = response.getHits().getTotalHits();
			if (response != null) {
				SearchHits hits = response.getHits();
				if (hits != null && hits.getHits() != null) {
					Map<String, Object> hitMap = null;
					for (SearchHit hit : hits.getHits()) {
						hitMap = hit.getSource();
						if (hitMap == null || hitMap.size() <= 0) {
							continue;
						}
						dataList.add(hitMap);
					}
				}
			}
			GridBean gridBean = GridHelper.getBean(page, pageSize,
					Integer.parseInt(String.valueOf(total)), dataList);
			return new BsmResult(true, gridBean, "查询成功");

		} catch (Exception e) {
			List<Map<String, Object>> dataList = null;
			GridBean gridBean = GridHelper.getBean(page, pageSize,
					0, dataList);
			return new BsmResult(false,gridBean, "查询为空");
		}
	}

	@Override
	public BsmResult getSystemLog() {
		BsmResult result = new BsmResult(true,"查询成功");
		//获取server端class类的真是路径
		URL url = Thread.currentThread().getContextClassLoader().getResource("/");
		String realPath = null;
		// 判断操作系统类型
		String symbol = "/";;
		if (System.getProperty("os.name").toUpperCase().contains("windows".toUpperCase())) {
			realPath = url.toString().replace("file:/", "");
		} else {
			realPath = url.toString().replace("file:", "");
		}
		logger.info("系统class类真实路径："+realPath);
		
		// 获取realPath路径的最后三个等级目录
		String[] dirs = realPath.split("/"); ;
		StringBuffer buffer = new StringBuffer();
		buffer.append(symbol).append(dirs[dirs.length-4])
			.append(symbol).append(dirs[dirs.length-3])
			.append(symbol).append(dirs[dirs.length-2])
			.append(symbol).append(dirs[dirs.length-1]);
		
		//截取去除realPath路径的最后三个等级目录的上等级目录
		String path = realPath.split(buffer.toString())[0];
		path += symbol + "logs";
		logger.info("系统部署日志路径："+path);
		
		//遍历日志文件
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		File file = new File(path);
		
		logger.info("系统部署日志路径是否存在："+file.exists());
		
		File[] tempList = file.listFiles();
		JSONObject object = null;
		
		if (null != tempList) {
			for (int i = 0; i < tempList.length; i++) {
				if (tempList[i].isFile()) {
					object = new JSONObject();
					object = new JSONObject();
					object.put("logName", tempList[i].getName());
					object.put("logPath", tempList[i].toString());
					object.put("type", "服务端");
				}
				if (tempList[i].isDirectory()) {
					// nothing todo
				}
				dataList.add(object);
			}
		}
		
		if (dataList.isEmpty()) {
			logger.info("查询为空,没有获取到系统日志信息列表");
		}
		
		result.setData(dataList);
		return result;
	}
}
