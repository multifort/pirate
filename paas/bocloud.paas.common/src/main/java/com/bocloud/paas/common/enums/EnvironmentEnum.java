package com.bocloud.paas.common.enums;

public enum EnvironmentEnum {

    UNAVAILABLE("1", "不可用"), ACTIVE("2", "激活"), FREEZE("3", "冻结"), ABNORMAL("4", "异常"), CREATEING("5", "创建中"),DEAD("6", "死亡"), KUBERNETES("1",
            "kubernetes"), SWARM("2", "swarm");

    private String code;
    private String describe;

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the describe
     */
    public String getDescribe() {
        return describe;
    }

    /**
     * @param describe the describe to set
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @param code
     * @param describe
     */
    private EnvironmentEnum(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

}
