package com.bocloud.paas.web.License;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: langzi
 * @Date: Created on 2018/5/4
 * @Description:
 */
@Component
public class LicenseFile {

    @Value("${license.file}")
    private String licenseFile;

    public String getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(String licenseFile) {
        this.licenseFile = licenseFile;
    }

}
