package com.sohu.tw.elevator.syslog;

import java.util.ArrayList;
import java.util.List;

import com.sohu.tw.elevator.net.thrift.LogEntity;
import com.sohu.tw.elevator.net.thrift.LogHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: yaqinzhang Date: 12-12-13
 */
public class MessageHandler {
	private final static Log logger = LogFactory.getLog(MessageHandler.class);

	public static void handleMessage(String ident, String msg, LogHandler logHandler) {
		LogEntity logEntity = createLogEntity(ident, msg);
		List list = new ArrayList();
		list.add(logEntity);
		if ((logHandler != null) && (list.size() > 0))
			logHandler.send(list);
		else
			logger.error("MessageHandler.handleMessage:logHandler is null!");
	}

	public static LogEntity createLogEntity(String ident, String msg) {
		LogEntity logEntity = new LogEntity();
		if ((StringUtils.isBlank(ident)) || (StringUtils.isBlank(msg))) {
			throw new IllegalArgumentException("MessageHandler.createLogEntity:The topic or msg is blank!");
		}
		logEntity.setTopic(ident);
		logEntity.setContent(msg);
		return logEntity;
	}
}
