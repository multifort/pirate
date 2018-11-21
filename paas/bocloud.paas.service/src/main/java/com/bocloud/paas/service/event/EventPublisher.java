package com.bocloud.paas.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bocloud.common.model.Result;

/**
 * 资源事件结果分发器，用来将异步操作的结果发送到RabbitMQ里面去。
 * 
 * @author dmw
 *
 */
@Component("eventPublisher")
public class EventPublisher {
	private static Logger logger = LoggerFactory.getLogger(EventPublisher.class);
	@Autowired
	private AmqpTemplate amqpTemplate;
	private static String EXCHANGE = "bocloud.paas.fexchange";
	private static String QUEUE = "resource.action.queue.*";

	public Result send(Object message) {
		try {
			this.amqpTemplate.convertAndSend(EXCHANGE, QUEUE, message);
			return new Result(true, "success");
		} catch (AmqpException e) {
			logger.error("Send Resource Event Error{}", e);
			return new Result(false, "发送失败！");
		}
	}
}
