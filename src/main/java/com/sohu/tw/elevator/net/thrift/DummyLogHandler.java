package com.sohu.tw.elevator.net.thrift;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;

import com.sohu.tw.elevator.metrics.TopicMetricsSource;

public class DummyLogHandler implements LogService.Iface {

	private final Log logger = LogFactory.getLog(DummyLogHandler.class);

	private AtomicLong l = new AtomicLong(0);

	public DummyLogHandler() {
		logger.info("Use the dummy log handler for testing");
		new Thread() {
			public void run() {
				while (true) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						logger.error("Interrupted");
					}
					logger.debug("Current traffic is " + l.getAndSet(0L) + "/S");
				}

			}
		}.start();
	}

	@Override
	public void send(List<LogEntity> logList) throws TException {
		l.getAndAdd(logList.size());
		for (LogEntity log : logList) {
			if (logger.isTraceEnabled()) {
				logger.trace(log.getTopic() + ":" + log.getContent());
			}
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
