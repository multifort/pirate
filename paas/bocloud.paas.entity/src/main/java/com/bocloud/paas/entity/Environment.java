package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;
import com.bocloud.paas.common.enums.EnvironmentEnum;

import java.util.List;

/**
 * 环境实体类
 *
 * @author songsong
 */
@Table("environment")
public class Environment extends GenericEntity {
    @IgnoreAll
    private final String[] statuses = new String[]{EnvironmentEnum.ACTIVE.getCode(), EnvironmentEnum.ABNORMAL.getCode()};

    @PK(value = PKStrategy.AUTO)
    private Long id; // ID
    @Column("platform")
    private Integer platform;
    @Column("proxy")
    private String proxy;
    @Column("port")
    private Integer port;
    @Column("tenant_id")
    private Integer tenantId;
    @Column("dept_id")
    private Long deptId;
    @Column("source")
    private String source;// 当环境处于可用状态时：标明环境中主机如何来的：接管、创建
    @Column("master")
    private String master;// 该环境下的主节点IP，用来存放yaml文件
    @IgnoreAll
    private String mender;// 修改者
    @IgnoreAll
    private String creator;// 创建者
    @IgnoreAll
    private List<Host> hosts;// 环境中包含的主机

    public String[] getStatuses() {
        return statuses;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the platform
     */
    public Integer getPlatform() {
        return platform;
    }

    /**
     * @param platform the platform to set
     */
    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    /**
     * @return the proxy
     */
    public String getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the tenantId
     */
    public Integer getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    /**
     * @return the mender
     */
    public String getMender() {
        return mender;
    }

    /**
     * @param mender the mender to set
     */
    public void setMender(String mender) {
        this.mender = mender;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the hosts
     */
    public List<Host> getHosts() {
        return hosts;
    }

    /**
     * @param hosts the hosts to set
     */
    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the master
     */
    public String getMaster() {
        return master;
    }

    /**
     * @param master the master to set
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * @param id
     * @param platform
     * @param proxy
     * @param port
     * @param tenantId
     * @param source
     * @param master
     * @param mender
     * @param creator
     * @param hosts
     */
    public Environment(Long id, Integer platform, String proxy, Integer port, Integer tenantId, String source,
                       String master, String mender, String creator, List<Host> hosts) {
        super();
        this.id = id;
        this.platform = platform;
        this.proxy = proxy;
        this.port = port;
        this.tenantId = tenantId;
        this.source = source;
        this.master = master;
        this.mender = mender;
        this.creator = creator;
        this.hosts = hosts;
    }

    /**
     *
     */
    public Environment() {
        super();
    }

}
