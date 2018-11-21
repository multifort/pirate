package com.bocloud.paas.web.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.model.UploadACK;
import com.bocloud.paas.web.model.UploadBean;
import com.bocloud.paas.web.model.UploadCancel;
import com.bocloud.paas.web.utils.ApplicationConfig;
import com.bocloud.paas.web.utils.FileOperation;
import com.bocloud.common.utils.DateTools;

@Component
public class UploadServiceHandller implements WebSocketHandler {

    private static Logger logger = Logger.getLogger(UploadServiceHandller.class);

    private static final String SAVE_SUCCESS = "SAVE_SUCCESS";
    private static final String SAVE_FAILURE = "SAVE_FAILURE";
    private static final String CANCEL_SUCCESS = "CANCEL_SUCCESS";
    private static final String CANCEL_FAILURE = "CANCEL_FAILURE";
    private static final String UPLOAD_CANCEL = "UPLOAD_CANCEL";

    private static List<WebSocketSession> currentUsers;

    private final ApplicationConfig applicationConfig;

    @Autowired
    public UploadServiceHandller(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * @return the currentUsers
     */
    public static List<WebSocketSession> getCurrentUsers() {
        return currentUsers;
    }

    /**
     * @param currentUsers the currentUsers to set
     */
    public static void setCurrentUsers(List<WebSocketSession> currentUsers) {
        UploadServiceHandller.currentUsers = currentUsers;
    }

    static {
        UploadServiceHandller.setCurrentUsers(new ArrayList<>());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UploadServiceHandller.getCurrentUsers().add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session,
            WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleText(session, (TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            handleBinary(session, (BinaryMessage) message);
        } else {
            logger.error("Unexpected WebSocket message type: " + message);
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    /**
     * 上传并处理字符串
     *
     * @author zjm
     * @date 2017年3月17日
     */
    private void handleText(WebSocketSession session, TextMessage message) {
        try {
            if (message.getPayload().contains("upload")) {
            	String timestamp = "";
                UploadBean uploadBean = JSONObject.parseObject(message.getPayload(), UploadBean.class);
                if (uploadBean.getFilename().contains("\\.")) {
                	timestamp = DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS") + "-"
                            + uploadBean.getFilename().substring(0, uploadBean.getFilename().lastIndexOf("."));
				} else {
					timestamp = DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS") + "-"
	                        + uploadBean.getFilename();
				}
                File file = new File(applicationConfig.getSharePath() + File.separatorChar + timestamp);
                if (!file.exists()) {
                	//修改创建文件夹变成创建文件
                    //file.mkdirs();
                	file.createNewFile();
                }
                // 设置本地暂存目录
                String path = "";
                if (uploadBean.getFilename().contains("\\.")) {
                	path  = applicationConfig.getSharePath() + File.separatorChar + timestamp + File.separatorChar;
                	path = path + uploadBean.getFilename();
                } else {
                	path = applicationConfig.getSharePath() + File.separatorChar + timestamp;
                }
                uploadBean.setTarget(path);
                FileOperation.makeDirs(path);
                session.getAttributes().put("uploadBean", uploadBean);
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadACK(path))));
            } else if (message.getPayload().contains("sendover")) {
                UploadBean uploadBean = (UploadBean) session.getAttributes().get("uploadBean");
                String localPath = uploadBean.getTarget();
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage("TRUE," + localPath))));
            } else if (message.getPayload().contains(UPLOAD_CANCEL)) {
                logger.warn("用户取消文件上传");
                UploadBean uploadBean = (UploadBean) session.getAttributes().get("uploadBean");
                if (deleteFile(uploadBean.getTarget())) {
                    session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadCancel(CANCEL_SUCCESS))));
                } else {
                    session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadCancel(CANCEL_FAILURE))));
                }
            }
        } catch (IOException e) {
            logger.error("上传文件异常：", e);
        }
    }

    @SuppressWarnings("static-access")
    private void handleBinary(WebSocketSession session, BinaryMessage message) throws IOException {
        UploadBean uploadBean = (UploadBean) session.getAttributes().get("uploadBean");
        if (null == uploadBean) {
            return;
        }
        try {
            if (saveFile(message.getPayload(), uploadBean.getTarget())) {
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_SUCCESS))));
            } else {
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_FAILURE))));
                this.deleteFile(uploadBean.getTarget());
            }
        } catch (IOException e) {
            logger.error("上传文件异常：", e);
            session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_FAILURE))));
            this.deleteFile(uploadBean.getTarget());
        }
    }

    /**
     * 将二进制byte[]数组写入文件中
     *
     * @param byteBuffer byte[]数组
     * @param outputFile 文件位置
     * @return 成功: true 失败: false
     */
    private static boolean saveFile(ByteBuffer byteBuffer, String outputFile) {
        FileOutputStream fstream = null;
        File file;
        try {
            file = new File(outputFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            fstream = new FileOutputStream(file, true);
            fstream.write(byteBuffer.array());
        } catch (Exception e) {
            logger.error("saveFile Exception:", e);
            return false;
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e1) {
                    logger.error("close fileimput Exception:", e1);
                }
            }
        }
        return true;
    }

    private static boolean deleteFile(String filepath) {
        File file;
        try {
            file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }
            return true;
        } catch (Exception e) {
            logger.error("Delete file exception:", e);
            return false;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus closeStatus) throws Exception {
        UploadServiceHandller.getCurrentUsers().remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session,
            Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        UploadServiceHandller.getCurrentUsers().remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
