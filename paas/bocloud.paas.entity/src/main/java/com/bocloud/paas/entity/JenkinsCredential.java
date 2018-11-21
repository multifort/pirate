package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 
 * @author zjm
 * @date 2017年8月23日
 * @describe
 */
@Table("jenkins_credential")
public class JenkinsCredential extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id;
	/**
	 * 凭证id
	 */
	@Column("credential_id")
	private String credentialId;
	/**
	 * 凭证用户名
	 */
	@Column("credential_username")
	private String credentialUsername;
	/**
	 * 凭证用户名
	 */
	@Column("credential_password")
	private String credentialPassword;
	/**
	 * 凭证的范围，目前仅支持Global和System
	 */
	@Column("credential_scope")
	private String credentialScope;
	/**
	 * 凭证的描述
	 */
	@Column("credential_description")
	private String credentialDescription;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getCredentialUsername() {
		return credentialUsername;
	}

	public void setCredentialUsername(String credentialUsername) {
		this.credentialUsername = credentialUsername;
	}

	public String getCredentialPassword() {
		return credentialPassword;
	}

	public void setCredentialPassword(String credentialPassword) {
		this.credentialPassword = credentialPassword;
	}

	public String getCredentialScope() {
		return credentialScope;
	}

	public void setCredentialScope(String credentialScope) {
		this.credentialScope = credentialScope;
	}

	public String getCredentialDescription() {
		return credentialDescription;
	}

	public void setCredentialDescription(String credentialDescription) {
		this.credentialDescription = credentialDescription;
	}

}
