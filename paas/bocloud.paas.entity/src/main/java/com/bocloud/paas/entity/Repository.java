package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 仓库对象
 *
 * @author Zaney
 * @data:2017年3月15日
 * @describe:
 */
@Table("repository")
public class Repository extends GenericEntity {
    @PK(value = PKStrategy.AUTO)
    private Long id; // ID
    @Column("type")
    private Integer type;
    @Column("port")
    private Integer port; //仓库端口号
    @Column("protocol_type")
    private Integer protocol;
    @Column("auth_mode")
    private Integer authMode;
    @Column("address")
    private String address;
    @Column("property")
    private Integer property;
    @Column("dept_id")
    private Long deptId;
    @Column("username")
    private String username;
    @Column("password")
    private String password;
    @IgnoreAll
    private String creatorName;
    @IgnoreAll
    private String menderName;
    @IgnoreAll
    private Integer imageSum;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getProtocol() { return protocol; }

    public void setProtocolType(Integer protocol) { this.protocol = protocol; }

    public Integer getAuthMode() { return authMode; }

    public void setAuthMode(Integer authMode) { this.authMode = authMode; }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getMenderName() {
        return menderName;
    }

    public void setMenderName(String menderName) {
        this.menderName = menderName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getProperty() {
        return property;
    }

    public void setProperty(Integer property) {
        this.property = property;
    }

    public Integer getImageSum() {
        return imageSum;
    }

    public void setImageSum(Integer imageSum) {
        this.imageSum = imageSum;
    }

	public void setProtocol(Integer protocol) {
		this.protocol = protocol;
	}

}
