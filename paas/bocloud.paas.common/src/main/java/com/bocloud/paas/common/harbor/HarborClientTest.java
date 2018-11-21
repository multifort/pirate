package com.bocloud.paas.common.harbor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.common.harbor.model.Project;
import com.bocloud.paas.common.harbor.model.Repository;
import com.bocloud.paas.common.harbor.model.Tag;

import java.util.List;

/**
 * harbor client test
 *
 * @author langzi
 * @email lining@beyondcent.com
 * @time 2017-10-25 11:24
 */
public class HarborClientTest {

    public static void main(String[] args) {
        String url = "http://139.219.239.226";
        String username = "admin", password = "123456";
        HarborClient client = new HarborClient(url);
        int code = client.login(username, password);
        client.setUrl(url);
        //List<Project> projects = client.getProject();
        //projects.stream().forEach(project -> System.out.println(project.getName()+"----"+project.getProjectId()));
        List<Repository> repositories = client.getRepositories("29");

        repositories.stream().forEach(repository -> System.out.println(repository.getName()));
    }

}
