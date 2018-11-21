package com.bocloud.paas.web.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.model.UploadACK;
import com.bocloud.paas.web.model.UploadCancel;
import com.bocloud.paas.web.model.UploadReady;
import com.bocloud.paas.web.model.UploadType;
import com.bocloud.common.utils.DateTools;

@Component
public class UploadPictureHandller implements WebSocketHandler {

    private static Logger logger = Logger.getLogger(UploadPictureHandller.class);

    private static final String SAVE_SUCCESS = "SAVE_SUCCESS";
    private static final String SAVE_FAILURE = "SAVE_FAILURE";
    private static final String CANCEL_SUCCESS = "CANCEL_SUCCESS";
    private static final String CANCEL_FAILURE = "CANCEL_FAILURE";
    private static final String UPLOAD_CANCEL = "UPLOAD_CANCEL";
    private static final String UPLOAD_READY = "UPLOAD_READY";

    private static List<WebSocketSession> currentUsers;

    @Value("${applicationStore.picture.storage.path}")
	private String storePicturePath;
    
    @Value("${configManage.file.storage.path}")
   	private String cmFilePath;

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
        UploadPictureHandller.currentUsers = currentUsers;
    }

    static {
        UploadPictureHandller.setCurrentUsers(new ArrayList<>());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UploadPictureHandller.getCurrentUsers().add(session);
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
     * @param session
     * @param message
     */
    private void handleText(WebSocketSession session, TextMessage message) {
        try {
            if (message.getPayload().contains("upload")) {
            	String timestamp = "";
            	PictureBean pictureBean = JSONObject.parseObject(message.getPayload(), PictureBean.class);
            	
            	if(null == pictureBean.getName() || "".equals(pictureBean.getName())){
            		timestamp = DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS");
            	} else {
            		timestamp = DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS") + "-"
                            + pictureBean.getName().substring(0, pictureBean.getName().lastIndexOf("."));
            	}
        		
        		//根据不同模块文件上传，选择相应的存放路径
        		String typePath = judgeType(pictureBean.getType());
            
                String dirPath = typePath + File.separatorChar + timestamp;
                File fileDir = new File(dirPath);
                if (!fileDir.exists()) {
                	//修改创建文件夹变成创建文件
                	fileDir.mkdirs();
                }
                // 设置本地暂存目录
                String path = dirPath + File.separatorChar + pictureBean.getName();
                File file = new File(path);
                if (!file.exists()) {
                	file.createNewFile();
				}
                pictureBean.setDirPath(dirPath);
                pictureBean.setTargetPath(path);
                session.getAttributes().put("pictureBean", pictureBean);
            	
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadACK(pictureBean.getDirPath()))));
            } else if (message.getPayload().contains("ready")) {
            	PictureBean pictureBean = JSONObject.parseObject(message.getPayload(), PictureBean.class);
            	String targetPath = pictureBean.getDirPath() + File.separatorChar + pictureBean.getName();
            	pictureBean.setTargetPath(targetPath);
            	
            	session.getAttributes().put("pictureBean", pictureBean);
            	session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadReady(UPLOAD_READY))));
            	
            } else if (message.getPayload().contains("sendover")) {
            	PictureBean pictureBean = (PictureBean) session.getAttributes().get("pictureBean");
                String dirPath = pictureBean.getDirPath();
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage("TRUE," + dirPath))));
            } else if (message.getPayload().contains(UPLOAD_CANCEL)) {
                logger.warn("用户删除已上传的图片");
                String targetPath = null;
                PictureBean pictureBean = JSONObject.parseObject(message.getPayload(), PictureBean.class);
                if(null == pictureBean){
                	pictureBean = (PictureBean) session.getAttributes().get("pictureBean");
                	targetPath = pictureBean.getTargetPath();
                } else {
                	targetPath = pictureBean.getDirPath() + File.separatorChar + pictureBean.getName();
                }
                
                if (deleteFile(targetPath)) {
                    session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadCancel(CANCEL_SUCCESS))));
                } else {
                    session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadCancel(CANCEL_FAILURE))));
                }
            }
        } catch (IOException e) {
            logger.error("上传图片异常：", e);
        }
    }
    
    /**
     * 判断功能模块类型
     * @param type
     * @return
     */
    private String judgeType(String type){
    	UploadType uploadType = UploadType.values()[Integer.valueOf(type)];
    	
    	switch (uploadType) {
		case STORE_PICTURE:
			
			return storePicturePath;
		
        case CONFIG_MANAGE_FILE:
			
        	return cmFilePath;

		default:
			return null;
		}
    }

    @SuppressWarnings("static-access")
    private void handleBinary(WebSocketSession session, BinaryMessage message) throws IOException {
    	PictureBean pictureBean = (PictureBean) session.getAttributes().get("pictureBean");
        if (null == pictureBean) {
            return;
        }
        try {
            if (saveFile(message.getPayload(), pictureBean.getTargetPath())) {
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_SUCCESS))));
            } else {
                session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_FAILURE))));
                this.deleteFile(pictureBean.getTargetPath());
            }
        } catch (IOException e) {
            logger.error("上传图片异常：", e);
            session.sendMessage(new TextMessage(JSONObject.toJSONString(new UploadMessage(SAVE_FAILURE))));
            this.deleteFile(pictureBean.getTargetPath());
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
        UploadPictureHandller.getCurrentUsers().remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session,
            Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        UploadPictureHandller.getCurrentUsers().remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
