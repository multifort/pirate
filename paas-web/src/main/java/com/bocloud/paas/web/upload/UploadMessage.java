package com.bocloud.paas.web.upload;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import com.bocloud.paas.web.model.MessageCategory;

/**
 * 上传文件消息
 *
 * @author zero
 */
public class UploadMessage implements Message<Object> {

    private MessageCategory category;

    private String content;

    /**
     * @return the category
     */
    public MessageCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(MessageCategory category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public MessageHeaders getHeaders() {
        return null;
    }

    @Override
    public Object getPayload() {
        return null;
    }

    public UploadMessage(String content) {
        this.setCategory(MessageCategory.UPLOAD);
        this.setContent(content);
    }
}
