package com.bocloud.paas.common.http;/**
 * @Author: langzi
 * @Date: Created on 2017/10/31
 * @Description:
 */

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * bocloud httpclient
 *
 * @author langzi
 * @email lining@beyondcent.com
 * @time 2017-10-31 15:04
 */
public class BocloudHttpClient {

    private static Logger logger = LoggerFactory.getLogger(BocloudHttpClient.class);
    private HttpClient httpClient;
    public void setHttpClient(){ }

    public HttpClient getHttpClient(){
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            HttpClient client = HttpClients.custom().setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            return client;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
