package com.bocloud.paas.common.harbor.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

/**
 * Created by yangxueying on 2016/11/14.
 */
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "project_id", "owner_id", "name", "creation_time", "update_time", "deleted", "owner_name",
		"public", "Togglable", "current_user_role_id", "repo_count", "enable_content_trust", "prevent_vulnerable_images_from_running", 
		"prevent_vulnerable_images_from_running_severity", "automatically_scan_images_on_push" })
public class Project {

	@JsonProperty("project_id")
	private Integer projectId;
	@JsonProperty("owner_id")
	private Integer ownerId;
	@JsonProperty("name")
	private String name;
	@JsonProperty("creation_time")
	private String creationTime;
	@JsonProperty("deleted")
	private Integer deleted;
	@JsonProperty("owner_name")
	private String ownerName;
	@JsonProperty("public")
	private Object _public;
	@JsonProperty("Togglable")
	private Boolean togglable;
	@JsonProperty("current_user_role_id")
	private Integer currentUserRoleId;
	@JsonProperty("repo_count")
	private Integer repoCount;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The projectId
	 */
	@JsonProperty("project_id")
	public Integer getProjectId() {
		return projectId;
	}

	/**
	 *
	 * @param projectId
	 *            The project_id
	 */
	@JsonProperty("project_id")
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	/**
	 *
	 * @return The ownerId
	 */
	@JsonProperty("owner_id")
	public Integer getOwnerId() {
		return ownerId;
	}

	/**
	 *
	 * @param ownerId
	 *            The owner_id
	 */
	@JsonProperty("owner_id")
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 *
	 * @return The name
	 */
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param name
	 *            The name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return The creationTime
	 */
	@JsonProperty("creation_time")
	public String getCreationTime() {
		return creationTime;
	}

	/**
	 *
	 * @param creationTime
	 *            The creation_time
	 */
	@JsonProperty("creation_time")
	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 *
	 * @return The deleted
	 */
	@JsonProperty("deleted")
	public Integer getDeleted() {
		return deleted;
	}

	/**
	 *
	 * @param deleted
	 *            The deleted
	 */
	@JsonProperty("deleted")
	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	/**
	 *
	 * @return The ownerName
	 */
	@JsonProperty("owner_name")
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 *
	 * @param ownerName
	 *            The owner_name
	 */
	@JsonProperty("owner_name")
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 *
	 * @return The _public
	 */
	@JsonProperty("public")
	public Object getPublic() {
		return _public;
	}

	/**
	 *
	 * @param _public
	 *            The public
	 */
	@JsonProperty("public")
	public void setPublic(Object _public) {
		this._public = _public;
	}

	/**
	 *
	 * @return The togglable
	 */
	@JsonProperty("Togglable")
	public Boolean getTogglable() {
		return togglable;
	}

	/**
	 *
	 * @param togglable
	 *            The Togglable
	 */
	@JsonProperty("Togglable")
	public void setTogglable(Boolean togglable) {
		this.togglable = togglable;
	}

	/**
	 *
	 * @return The currentUserRoleId
	 */
	@JsonProperty("current_user_role_id")
	public Integer getCurrentUserRoleId() {
		return currentUserRoleId;
	}

	/**
	 *
	 * @param currentUserRoleId
	 *            The current_user_role_id
	 */
	@JsonProperty("current_user_role_id")
	public void setCurrentUserRoleId(Integer currentUserRoleId) {
		this.currentUserRoleId = currentUserRoleId;
	}

	/**
	 *
	 * @return The repoCount
	 */
	@JsonProperty("repo_count")
	public Integer getRepoCount() {
		return repoCount;
	}

	/**
	 *
	 * @param repoCount
	 *            The repo_count
	 */
	@JsonProperty("repo_count")
	public void setRepoCount(Integer repoCount) {
		this.repoCount = repoCount;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return "Project{" + "projectId=" + projectId + ", ownerId=" + ownerId + ", name='" + name + '\''
				+ ", creationTime='" + creationTime + '\'' + ", deleted=" + deleted + ", ownerName='" 
				+ ownerName + '\'' + ", _public=" + _public + ", togglable="
				+ togglable + ", currentUserRoleId=" + currentUserRoleId
				+ ", repoCount=" + repoCount + ", additionalProperties=" + additionalProperties + '}';
	}
}
