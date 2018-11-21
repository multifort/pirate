package com.bocloud.paas.web.License;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: langzi
 * @Date: Created on 2018/5/4
 * @Description:
 */
public class License {
    private String sKey = "abcdef0123456789";
    private String ivParameter = "0123456789abcdef";

    private final static Logger logger = LoggerFactory.getLogger(License.class);

    public int checkLicense(File license) {
        int end = 0;
        String priKey = readToString(license);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String decode = decrypt(priKey);
        String[] plainText = decode.split(":");
        Date endDate = null;
        try {
            endDate = sdf.parse(plainText[2]);
        } catch (ParseException e) {
            logger.error("验证码校验中，日期转换异常！", e);
        }
        String ipAddress = plainText[0];
        String macAddress = plainText[1];
        List<String> keys = getKeys();
        for (String key : keys) {
            String[] validText = key.split(":");
            Date today = null;
            try {
                today = sdf.parse(validText[2]);
            } catch (Exception e) {
                logger.error("验证码校验中，日期转换异常！", e);
            }
            if (ipAddress.equals(validText[0])) {
                if (macAddress.equals(validText[1])) {
                    if (today.after(endDate)) {
                        end = -1;
                        logger.warn("产品服务期已经结束，如果继续使用，请联系厂商，更换license！");
                    } else {
                        end = daysBetween(today, endDate);
                        logger.info("产品距离服务期限还有" + end + "天");
                    }
                }
            }
        }
        return end;
    }

    // 解密
    String decrypt(String sSrc) {
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    String getMACAddress(InetAddress ia) {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                //mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            return sb.toString().toUpperCase();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }


    List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaceEnumeration;
        try {
            networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface ni = networkInterfaceEnumeration.nextElement();
                Enumeration<InetAddress> addressEnumeration = ni.getInetAddresses();
                while (addressEnumeration.hasMoreElements()) {
                    ia = addressEnumeration.nextElement();
                    if (!ia.isLoopbackAddress() && ia.getHostAddress().indexOf(':') == -1) {
                        String ip = ia.getHostAddress();
                        String macAddress = getMACAddress(ia);
                        String key = ip + ":" + macAddress + ":" + sdf.format(date);
                        keys.add(key);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        return keys;
    }

    String readToString(File file) {
        String encoding = "UTF-8";
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    int daysBetween(Date smdate, Date bdate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

}
