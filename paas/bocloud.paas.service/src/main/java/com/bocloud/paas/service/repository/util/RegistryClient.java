package com.bocloud.paas.service.repository.util;

/**
 * @Author: langzi
 * @Date: Created on 2017/10/27
 * @Description:
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.common.http.BocloudHttpClient;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * registry client
 *
 * @author langzi
 * @email lining@beyondcent.com
 * @time 2017-10-27 16:27
 */
public class RegistryClient {

    private static Logger logger = LoggerFactory.getLogger(RegistryClient.class);
    private static final String VERSION = "/v2/";
    private static final String MAINFESTS = "/manifests/";
    private static final String DOCKER_DIGEST = "Docker-Content-Digest";
    private static final String REPOSITORY = "repositories";
    private static final String TAG = "tags";
    private String url = "";
    private HttpClient httpClient;

    public RegistryClient() {
    }

    public RegistryClient(String url) {
        this.url = url + VERSION;
        getHttpClient();
    }

    /**
     * @Author: langzi
     * @Description: 获取可信任的http链接
     * @Date: 15:48 2017/10/31
     */
    private void getHttpClient() {
        BocloudHttpClient client = new BocloudHttpClient();
        httpClient = client.getHttpClient();
    }

    /**
     * @Author: langzi
     * @Description: 校验镜像仓库的连通性
     * @Date: 17:04 2017/10/27
     */
    public boolean isConnected() {
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            int code = response.getStatus();
            if (200 != code) {
                logger.info("list image names info error");
                return false;
            }
            return true;
        } catch (UnirestException e) {
            logger.error("list image namse exception");
            return false;
        }
    }

    /**
     * @Author: langzi
     * @Description: 获取仓库内镜像的名称
     * @Date: 17:52 2017/10/27
     */
    public String[] listImageNames() {
        StringBuilder builder = new StringBuilder(this.url).append("_catalog");
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (200 != code) {
                logger.info("list image names info error");
                return null;
            }
            JSONObject object = JSONObject.parseObject(response.getBody().toString());
            String[] names = objToArray(object, REPOSITORY);
            return names;
        } catch (UnirestException e) {
            logger.error("list image namse exception", e);
            return null;
        }
    }

    /**
     * @param name 镜像名称
     * @Author: langzi
     * @Description: 根据镜像名称获取镜像的标签（tag）信息
     * @Date: 17:29 2017/10/27
     */
    public String[] listImageTags(String name) {
        StringBuilder builder = new StringBuilder(this.url).append(name).append("/tags/list");
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (200 != code) {
                logger.info("list image tags info error");
                return null;
            }
            JSONObject object = JSONObject.parseObject(response.getBody().toString());
            String[] tags = objToArray(object, TAG);
            return tags;
        } catch (UnirestException e) {
            logger.error("connect registry server exception", e);
            return null;
        }
    }

    /**
     * @param name 镜像名称
     * @param tag  镜像的版本
     * @Author: langzi
     * @Description: 获取镜像的详细信息
     * @Date: 11:30 2017/10/30
     */
    public JSONObject getImageDetail(String name, String tag) {
        StringBuilder builder = new StringBuilder(this.url).append(name)
                .append(MAINFESTS).append(tag);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (200 != code) {
                logger.info("get image detail  info error");
                return null;
            }
            JSONObject object = JSONObject.parseObject(response.getBody().toString());
            return object;
        } catch (UnirestException e) {
            logger.error("connect registry server exception", e);
            return null;
        }
    }

    /**
     * @param name 镜像名称
     * @param tag  镜像的版本
     * @Author: langzi
     * @Description: 获取镜像的摘要信息
     * @Date: 11:39 2017/10/30
     */
    public String getImageDigest(String name, String tag) {
        StringBuilder builder = new StringBuilder(this.url).append(name)
                .append(MAINFESTS).append(tag);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).headers(header).asJson();
            int code = response.getStatus();
            if (200 != code) {
                logger.info("get image digest info error");
                return null;
            }
            Headers headers = response.getHeaders();
            String digest = headers.getFirst(DOCKER_DIGEST);
            return digest;
        } catch (UnirestException e) {
            logger.error("connect registry server exception", e);
            return null;
        }
    }

    /**
     * @param name   镜像名称
     * @param digest 镜像摘要的序列号
     * @Author: langzi
     * @Description:
     * @Date: 13:45 2017/10/30
     */
    public boolean deleteImage(String name, String digest) {
        StringBuilder builder = new StringBuilder(this.url).append(name)
                .append(MAINFESTS).append(digest);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.delete(builder.toString()).headers(header).asJson();
            int code = response.getStatus();
            if (202 != code) {
                logger.info("delete image detail  info error");
                return false;
            }
            return true;
        } catch (UnirestException e) {
            logger.error("delete image : connect registry server exception", e);
            return false;
        }
    }

    /**
     * @param object
     * @param key
     * @Author: langzi
     * @Description: json对象根据key值转换成数组
     * @Date: 18:46 2017/10/27
     */
    private String[] objToArray(JSONObject object, String key) {
        JSONArray jsonArray = object.getJSONArray(key);
        if (jsonArray != null && !jsonArray.isEmpty()) {
            String[] arrays = new String[jsonArray.size()];
            jsonArray.toArray(arrays);
            return arrays;
        } else {
            return null;
        }

    }

}
