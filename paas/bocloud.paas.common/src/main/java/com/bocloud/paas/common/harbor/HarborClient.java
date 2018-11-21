package com.bocloud.paas.common.harbor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.common.harbor.model.Project;
import com.bocloud.paas.common.harbor.model.Repository;
import com.bocloud.paas.common.harbor.model.Tag;
import com.bocloud.paas.common.http.BocloudHttpClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Grissom & Misha
 */
public class HarborClient {

    private static Logger logger = LoggerFactory.getLogger(HarborClient.class);
    private static final String API = "/api";
    private static final String PROJECTS = "/projects";
    private static final String REPOSITORIES = "/repositories";
    private static final String TAGS = "/tags";
    private static final String MANIFEST = "/manifest";
    private static final String LOGIN = "/login";
    private static final String LOGOUT = "/log_out";
    private String url = "";
    private HttpClient httpClient;

    public HarborClient() {
    }

    public HarborClient(String url) {
        this.url = url;
        setHttpClient();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @Author: langzi
     * @Description: 获取可信任的httpclient
     * @Date: 11:40 2017/11/1
     */
    private void setHttpClient() {
        BocloudHttpClient client = new BocloudHttpClient();
        httpClient = client.getHttpClient();
    }

    /**
     * @Author: langzi
     * @param username 用户名
     * @param password 认证密码
     * @Description: 判断harbor仓库是否可以通信
     * @Date: 15:40 2017/10/31
     */
    public boolean isConnected(String username, String password) {
        int loginState = login(username, password);
        if (200 == loginState) {
            return true;
        }
        return false;
    }

    /**
     * @Author: langzi
     * @param username harbor用户名
     * @param password harbor登录密码
     * @Description:
     * @Date: 9:34 2017/11/1
     */
    public int login(String username, String password) {
        this.url = this.url + LOGIN + "?principal=" + username + "&password=" + password;
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<String> response = Unirest.post(url).asString();
            return response.getStatus();
        } catch (UnirestException e) {
            logger.error("login harbor repository exception, please check harbor health", e);
            return 500;
        }

    }

    /**
     * @Author: langzi
     * @Description: 退出harbor的登录
     * @Date: 15:23 2017/10/31
     */
    public int logout() {
        this.url = this.url + LOGOUT;
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<String> response = Unirest.get(url).asString();
            return response.getStatus();
        } catch (UnirestException e) {
            logger.error("logout harbor repository exception, please check harbor health", e);
            return 500;
        }
    }

    /**
     * @Author: langzi
     * @Description: 获取harbor仓库下所有的项目信息
     * @Date: 10:23 2017/11/1
     */
    public List<Project> getProject() {
        StringBuilder builder = new StringBuilder(this.url).append(API).append(PROJECTS);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            List<Project> projects = JSONArray.parseArray(response.getBody().toString(), Project.class);
            return projects;
        } catch (UnirestException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @Author: langzi
     * @param projectId 项目的id
     * @Description: 获取项目下面所有的镜像名称
     * @Date: 11:38 2017/11/1
     */
    public List<Repository> getRepositories(String projectId) {
        StringBuilder builder = new StringBuilder(this.url).append(API)
                .append(REPOSITORIES).append("?project_id=").append(projectId);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (code == 200) {
                List<Repository> repositories = JSONArray.parseArray(response.getBody().toString(), Repository.class);
                return repositories;
            }
            return null;
        } catch (UnirestException e) {
            logger.error("logout harbor repository exception, please check harbor health", e);
            return null;
        }
    }

    /**
     * Retrieve tags from a relevant repository.</br>
     * <b>URL</b>: /repositories/tags</br>
     * <b>Method</b>: GET
     *
     * @param repoName [required] (Relevant repository name)
     * @return RepositorieTags
     * @throws IOException
     * @throws HarborClientException
     */
    public List<Tag> getRepositorieTags(String repoName) {
        StringBuilder builder = new StringBuilder(this.url).append(API)
                .append(REPOSITORIES).append("/").append(repoName).append(TAGS);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (code == 200) {
                //List<String> tags = JSONArray.parseArray(response.getBody().toString(), String.class);
                List<Tag> tags = JSONArray.parseArray(response.getBody().toString(), Tag.class);
                return tags;
            }
            return null;
        } catch (UnirestException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retreive manifests from a relevant repository.</br>
     * <b>URL</b>: /repositories/manifests</br>
     * <b>Method</b>: GET
     *
     * @param repoName [required] (Repository name)
     * @param tag      [required] (Tag name)
     * @return
     * @throws IOException
     * @throws HarborClientException
     */
    private Map<String, Object> getManifest(String repoName, String tag) {
        StringBuilder builder = new StringBuilder(this.url).append(API)
                .append(REPOSITORIES).append("/").append(repoName).append(TAGS)
                .append("/").append(tag).append(MANIFEST);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.get(builder.toString()).asJson();
            int code = response.getStatus();
            if (code == 200) {
                JSONObject object = JSONObject.parseObject(response.getBody().toString());
                Map<String, Object> manifest = JSONObject.toJavaObject(object, Map.class);
                return manifest;
            }
            return null;
        } catch (UnirestException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @Author: langzi
     * @param repoName
     * @param tag
     * @Description: 根据镜像的tag，删除镜像
     * @Date: 15:03 2017/11/9
     */
    public boolean deleteTag(String repoName, String tag) {
        StringBuilder builder = new StringBuilder(this.url).append(API)
                .append(REPOSITORIES).append("/").append(repoName).append(TAGS)
                .append("/").append(tag);
        Unirest.setHttpClient(httpClient);
        try {
            HttpResponse<JsonNode> response = Unirest.delete(builder.toString()).asJson();
            int code = response.getStatus();
            if (code == 200) {
                return true;
            }
            return false;
        } catch (UnirestException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * @Author: langziH
     * @param repositories 镜像名称
     * @param tag 镜像版本
     * @Description: 获取仓库中的镜像digest（镜像id）
     * @Date: 11:07 2017/11/1
     */
    public String getDigest(String repositories,String tag) {
        Map<String, Object> map = getManifest(repositories, tag);
        Map<String, Object> manifestMap = (Map<String, Object>) map.get("manifest");
        Map<String, Object> configMap = (Map<String, Object>) manifestMap.get("config");
        String digest = configMap.get("digest").toString();
        return digest;
    }

    /**
     * @Author: langzi
     * @param repositories 镜像名称
     * @param tag 镜像版本
     * @Description: 获取仓库中的镜像digest（镜像id）
     * @Date: 11:07 2017/11/1
     */
    public String getConfig(String repositories,String tag) {
        Map<String, Object> map = getManifest(repositories, tag);
        JSONObject jo = JSONObject.parseObject(map.get("config").toString());
        String config = jo.getString("config");
        return config;
    }

}
