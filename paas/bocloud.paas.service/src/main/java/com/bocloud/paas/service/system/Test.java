package com.bocloud.paas.service.system;

/**
 * 测试调用es java api 接口
 * 
 * @author luogan
 *
 */

import java.net.InetAddress;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class Test {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {

		Settings settings = Settings.builder()
				.put("cluster.name", "docker-cluster")
				.put("client.transport.sniff", false).build();
		TransportClient client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress
						.getByName("192.168.31.131"), 9300));
		System.out.println("this is client:" + client);

		
		try {

			TermQueryBuilder qb = QueryBuilders.termQuery("port", "36390");
			RangeQueryBuilder qb1 = QueryBuilders
			 .rangeQuery("@timestamp")  
             .from("2017-07-10")  
             .to("2017-07-11")  
             .includeLower(true)
             .includeUpper(false);
			 
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch("logstash-*");

			searchRequestBuilder.setTypes("logs");

			searchRequestBuilder.setQuery(qb1);
			
			searchRequestBuilder.setQuery(qb);

			if ((1 > 0) && (5 > 0)) {

				searchRequestBuilder.setFrom(10 * (1 - 1));

				searchRequestBuilder.setSize(5);

			}

//			SortOrder sortOrder = SortOrder.ASC;

//			if (order.equals("desc")) {
//
//				sortOrder = SortOrder.DESC;
//
//			}
//
//			if ((null != sort) && (!sort.equals(""))) {
//
//				searchRequestBuilder.addSort(sort, sortOrder);
//
//			}

			searchRequestBuilder.setExplain(true);

			SearchResponse response = searchRequestBuilder.get();

			System.out.println(response);
			
			System.out.println(response.getHits().getTotalHits());

		} catch (Exception e) {

			e.printStackTrace();
		}

		
//		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
//		try{
//			TermQueryBuilder qb = QueryBuilders.termQuery("port", "36390");
//		    SearchRequestBuilder requestBuilder = client.prepareSearch()
//		        .setIndices("logstash-*")
//		        .setTypes("logs")
//		        .setQuery(qb);
//		    SearchResponse searchResponse = requestBuilder.addSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.DESC))
//		        .setFrom(0)
//		        .setSize(10)
//		        .get();
//		    //遍历查询结果
//		    if(searchResponse != null){
//		        SearchHits hits = searchResponse.getHits();
//		        if(hits != null && hits.getHits() != null){
//		            Map<String, Object> hitMap = null;
//		            for (SearchHit hit : hits.getHits()){
//		                hitMap = hit.getSource();
//		                if(hitMap == null || hitMap.size() <= 0){
//		                    continue;
//		                }
//		                dataList.add(hitMap );
//		            }
//		        }
//		    }
//		}catch(Exception ex){
//		    ex.printStackTrace();
//		}finally{
//		    //关闭client
//		}
//		System.out.println(dataList);
		
		
//		Map<String, Object> json = new HashMap<String, Object>();
//			  json.put("first_name", "John");
//			 
//			  IndexResponse response = client
//			    .prepareIndex("megacorp", "employee", "3").setSource(json)	
//			    .execute().actionGet();
//			  System.out.println(response);
		
//		GetResponse response = client.prepareGet("megacorp","employee","1")
//		        .setOperationThreaded(false)
//		        .get();
//		System.out.println(response);
//
//		SearchResponse response = client.prepareSearch()
//				.setIndices("logstash-*")
//		        .setTypes("logs")
//		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//		        //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(26))     // Filter
//		        //.setQuery(QueryBuilders.termQuery("first_name", "John"))            // Query
//		        //.setQuery(QueryBuilders.termQuery("join_time", "2014-11-24"))
//		        .setFrom(0).setSize(6).setExplain(true)
//		        .get();
//		System.out.println(response);
//		System.out.println(response.getHits().getTotalHits());
//		List<Map<String, Object>> matchRsult = new LinkedList<Map<String, Object>>();
//        for (SearchHit hit : response.getHits())
//        {
//            matchRsult.add(hit.getSource());
//            //System.out.println(hit.getSourceAsString());
//        }
//
//		System.out.println(matchRsult);
  }

}
