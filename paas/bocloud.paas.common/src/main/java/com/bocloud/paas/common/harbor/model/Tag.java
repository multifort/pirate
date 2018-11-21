package com.bocloud.paas.common.harbor.model;/**
 * @Author: langzi
 * @Date: Created on 2017/11/6
 * @Description:
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;

/**
 * harbor tag
 *
 * @author langzi
 * @email lining@beyondcent.com
 * @time 2017-11-06 12:04
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"digest", "name", "docker_version"})

public class Tag {

    @JsonProperty("digest")
    private String digest;
    @JsonProperty("name")
    private String name;
    @JsonProperty("docker_version")
    private String dockerVersion;

    @JsonProperty("digest")
    public String getDigest() {
        return digest;
    }

    @JsonProperty("digest")
    public void setDigest(String digest) {
        this.digest = digest;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("docker_version")
    public String getDockerVersion() {
        return dockerVersion;
    }

    @JsonProperty("docker_version")
    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "digest='" + digest + '\'' +
                ", name='" + name + '\'' +
                ", dockerVersion='" + dockerVersion + '\'' +
                '}';
    }

}
