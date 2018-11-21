package com.bocloud.paas.web.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("application.properties")
public class ApplicationConfig {

    @Value("${share.path}")
    private String sharePath;

    public String getSharePath() {
        return sharePath;
    }

    public void setSharePath(String sharePath) {
        this.sharePath = sharePath;
    }

}
