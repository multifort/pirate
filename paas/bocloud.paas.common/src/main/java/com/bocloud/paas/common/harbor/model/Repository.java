package com.bocloud.paas.common.harbor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;

/**
 * Created by yangxueying on 2016/11/14.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"id", "project_id", "name", "tags_count", "description", "pull_count", "star_count", "creation_time", "update_time"})
public class Repository {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("project_id")
    private Integer projectId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("tags_count")
    private Integer tagsCount;
    @JsonProperty("description")
    private String description;
    @JsonProperty("pull_count")
    private Integer pullCount;
    @JsonProperty("star_count")
    private Integer starCount;
    @JsonProperty("creation_time")
    private String creationTime;
    @JsonProperty("update_time")
    private String updateTime;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("project_id")
    public Integer getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("tags_count")
    public Integer getTagsCount() {
        return tagsCount;
    }

    @JsonProperty("tags_count")
    public void setTagsCount(Integer tagsCount) {
        this.tagsCount = tagsCount;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("pull_count")
    public Integer getPullCount() {
        return pullCount;
    }

    @JsonProperty("pull_count")
    public void setPullCount(Integer pullCount) {
        this.pullCount = pullCount;
    }

    @JsonProperty("star_count")
    public Integer getStarCount() {
        return starCount;
    }

    @JsonProperty("star_count")
    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    @JsonProperty("creation_time")
    public String getCreationTime() {
        return creationTime;
    }

    @JsonProperty("creation_time")
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @JsonProperty("update_time")
    public String getUpdateTime() {
        return updateTime;
    }

    @JsonProperty("update_time")
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", tagsCount=" + tagsCount +
                ", description='" + description + '\'' +
                ", pullCount=" + pullCount +
                ", starCount=" + starCount +
                ", creationTime='" + creationTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
