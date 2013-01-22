package com.sohu.tw.elevator.plugin.http;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.kafka.KafkaProducerService;
import com.sohu.tw.elevator.kafka.TopicFilter;
import com.sohu.tw.elevator.metrics.TopicMetricsSource;
import com.sohu.tw.elevator.net.thrift.LogEntity;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class LogsServlet extends HttpServlet {

	private Log logger = LogFactory.getLog(LogsServlet.class);

	private KafkaProducerService kafkaServ = null;
	private SimpleConsumer consumer = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		kafkaServ = new KafkaProducerService();
		TopicFilter topicFilter = new TopicFilter();
		kafkaServ.setTopicFilter(topicFilter);
		kafkaServ.init();
	}

	private void doSend(List<LogEntity> logList) {
		if (logList == null || logList.isEmpty()) {
			return;
		}
		for (LogEntity log : logList) {
			if (logger.isTraceEnabled()) {
				logger.trace(log.getTopic() + ":" + log.getContent());
			}
			kafkaServ.sendLogEntry(log);
			metricsLog(log);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// 获得topic
		String topic = req.getPathInfo().substring(1);
		if (topic.indexOf("/") > 0) {
			logger.error("error topic . topic = " + topic);
			throw new ServletException("error topic. topic=" + topic);
		}
		int length = req.getContentLength();
		byte[] bArr = new byte[length];
		req.getInputStream().read(bArr);
		String content = new String(bArr);
		String[] contentArr = content.split("\\r\\n");
		List<LogEntity> logList = generateLogList(topic, contentArr);
		doSend(logList);
		logger.info("have sent the logs . size = " + logList.size());
	}

	private List<LogEntity> generateLogList(String topic, String[] contentArr) {
		List<LogEntity> list = new LinkedList<LogEntity>();
		for (String content : contentArr) {
			LogEntity e = new LogEntity(topic, content);
			list.add(e);
		}
		return list;
	}

	private void metricsLog(LogEntity log) {
		try {
			TopicMetricsSource.getMetrics("all").incrLogSum();
			TopicMetricsSource.getMetrics("all").incrLogBytes(
					new Long(log.getContent().length() * 2));
			TopicMetricsSource.getMetrics(log.getTopic()).incrLogSum();
			TopicMetricsSource.getMetrics(log.getTopic()).incrLogBytes(
					new Long(log.getContent().length() * 2));
		} catch (Exception e) {
			logger.error("metricing log error.", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try{
		String path = req.getPathInfo().substring(1);
		String[] arr = path.split("/");
		if (arr.length > 2) {
			logger.error("error path . path = " + path);
			throw new ServletException("error path. path=" + path);
		}

		Properties props = new Properties();
		props.put("zk.connect", ElevatorConfig.getZkConnect());
		props.put("zk.connectiontimeout.ms", ElevatorConfig.getZkTimeout()+"");
		props.put("groupid", arr[1]);

		ConsumerConnector consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(new ConsumerConfig(props));
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(arr[0], new Integer(1));
		Map<String, List<KafkaStream<Message>>> consumerMap = consumer
				.createMessageStreams(topicCountMap);
		KafkaStream<Message> stream = consumerMap.get(arr[0]).get(0);
		ConsumerIterator<Message> it = stream.iterator();
		int index = 0;
		while (it.hasNext()) {
			String msg = getMessage(it.next().message());
			resp.getWriter().write(msg+"\r\n");
			logger.debug("rev msg : " + msg);
		}
		}catch(Exception e){
			logger.error("do get error",e);
		}
	}

	private String getMessage(Message message) {
		ByteBuffer buffer = message.payload();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return new String(bytes);
	}
}
