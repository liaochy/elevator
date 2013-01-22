package com.sohu.tw.elevator.metrics;

import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.tw.goldmine.watchdog.metrics2.MetricsRecord;
import com.sohu.tw.goldmine.watchdog.metrics2.MetricsSink;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.DefaultMetricsSystem;

public class StatusSink implements MetricsSink {
	private final Log logger = LogFactory.getLog(StatusSink.class);
	private static StatusSink sink = null;
	private final static String NAME = "elevator.status";
	private final static String DESC = "status collection of the Elevator System";
	private final StatusCollector metrics = StatusCollector.getInstance();

	/**
	 * 单例
	 * 
	 * @return
	 */
	public static synchronized StatusSink instance() {
		if (sink == null) {
			sink = new StatusSink();
		}
		return sink;
	}

	@Override
	public void init(SubsetConfiguration conf) {
		logger.info("init and register RealTimeSink to DefaultMetricsSystem successfully");
		DefaultMetricsSystem.INSTANCE.register(NAME, DESC, instance());

	}
	@Override
	public void flush() {
	}

	@Override
	public void putMetrics(MetricsRecord record) {
		metrics.updateRecord(record);
	}

}
