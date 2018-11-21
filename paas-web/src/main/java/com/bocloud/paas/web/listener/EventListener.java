package com.bocloud.paas.web.listener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.message.MessagePublisher;
import com.bocloud.common.model.BaseResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;

/**
 * 资源事件监听器
 * 
 * @author dmw
 *
 */
@Component("eventListener")
public class EventListener implements MessageListener {

	private static Logger logger = LoggerFactory.getLogger(EventListener.class);

	@Autowired
	private MessagePublisher publisher;

	@Override
	public void onMessage(Message message) {
		String content = new String(message.getBody());
		if (logger.isDebugEnabled()) {
			logger.debug("get resource event message:{}", content);
		}
		if (StringUtils.isBlank(content)) {// 消息没有内容，直接返回
			return;
		}
		BaseResult<JSONObject> result = JSONTools.isJSON(content);
		if (result.isFailed()) {// 非json数据，直接发送到所有用户
			publisher.pushMessage(content);
			return;
		}
		if (result.getData().containsKey(Common.USERID) && 0 != result.getData().getIntValue(Common.USERID)) {// 发送消息给指定ID的用户前端
			publisher.pushMessage(result.getData().getIntValue(Common.USERID), content);
		} else {// 发送给所有的用户前端
			publisher.pushMessage(content);
		}

	}

}
