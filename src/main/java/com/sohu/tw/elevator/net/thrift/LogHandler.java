package com.sohu.tw.elevator.net.thrift;

import com.sohu.tw.elevator.kafka.KafkaProducerService;
import com.sohu.tw.elevator.kafka.TopicFilter;
import com.sohu.tw.elevator.metrics.TopicMetricsSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class LogHandler implements LogService.Iface {
	private static final Log logger = LogFactory.getLog(LogHandler.class);
	public KafkaProducerService service;

	public LogHandler() {
		KafkaProducerService kafkaServ = new KafkaProducerService();
		TopicFilter topicFilter = new TopicFilter();
		kafkaServ.setTopicFilter(topicFilter);
		kafkaServ.init();
		this.service = kafkaServ;
	}

	public KafkaProducerService getService() {
		return this.service;
	}

	@Override
	public void send(List<LogEntity> logList) {
		if (logList == null || logList.isEmpty()) {
			return;
		}
		for (LogEntity log : logList) {
			if (logger.isTraceEnabled()) {
				logger.trace(log.getTopic() + ":" + log.getContent());
			}
			service.sendLogEntry(log);
			metricsLog(log);
		}
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
}
