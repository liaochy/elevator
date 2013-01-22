package com.sohu.tw.elevator.metrics;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.goldmine.watchdog.metrics2.*;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.DefaultMetricsSystem;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.MetricMutableCounterLong;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.MetricMutableRate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicMetricsSource implements MetricsSource {

    private static final Log logger = LogFactory
            .getLog(TopicMetricsSource.class);

    private static final Map<String, TopicMetricsSource> sources = new ConcurrentHashMap<String, TopicMetricsSource>();
    private static final ConcurrentHashMap<String, String> topicLock = new ConcurrentHashMap<String, String>();
    private static final String server = ElevatorConfig.getLocalHostIp();

    private MetricMutableCounterLong logSum;
    private MetricMutableRate logTPS;
    private MetricMutableRate kafkaTPS;
    private MetricMutableCounterLong throughPut;
    private MetricMutableRate throughPutPS;
    private MetricMutableCounterLong queueSize;
    private String topicName;

    private TopicMetricsSource(String _topicName) {
        this.topicName = _topicName;
        logSum = new MetricMutableCounterLong(MetricsConstants.ELEVATOR_LOG_SUM_KEY, "", 0L);
        logTPS = new MetricMutableRate(MetricsConstants.ELEVATOR_LOG_TPS_KEY, "");
        kafkaTPS = new MetricMutableRate(MetricsConstants.ELEVATOR_KAFKA_TPS_KEY, "");
        throughPut = new MetricMutableCounterLong(MetricsConstants.ELEVATOR_THROUGHPUT_KEY, "", 0L);
        throughPutPS = new MetricMutableRate(MetricsConstants.ELEVATOR_THROUGHPUT_PS_KEY, "");
        queueSize = new MetricMutableCounterLong(MetricsConstants.ELEVATOR_SERVER_QUEUE_SIZE, "", 0L);
    }

    @Override
    public void getMetrics(MetricsBuilder builder, boolean all) {
        MetricsRecordBuilder record = addRecordInfo(builder, "logSum");
        logSum.snapshot(record, all);

        record = addRecordInfo(builder, "logTPS");
        logTPS.snapshot(record, all);

        record = addRecordInfo(builder, "kafkaTPS");
        kafkaTPS.snapshot(record, all);

        record = addRecordInfo(builder, "throughPut");
        throughPut.snapshot(record, all);

        record = addRecordInfo(builder, "throughPutPS");
        throughPutPS.snapshot(record, all);

        record = addRecordInfo(builder, "queueSize");
        queueSize.snapshot(record, all);
    }

    private MetricsRecordBuilder addRecordInfo(MetricsBuilder builder, String type) {
        long time = System.currentTimeMillis() / 1000;
        MetricsRecordBuilder record = builder.addRecord("elevator_topic");
        record.addCounter("time", "", time);
        MetricsTag logSumTag = new MetricsTag("context", "", "tsdb");
        record.add(logSumTag);
        MetricsTag logSumType = new MetricsTag("type", "", type);
        record.add(logSumType);
        MetricsTag topicName = new MetricsTag("topic", "", this.topicName);
        record.add(topicName);
        MetricsTag serverIp = new MetricsTag("server", "", this.server);
        record.add(serverIp);
        return record;
    }

    /**
     * 用于计算日志吞吐量
     */
    public void incrLogSum() {
        this.logSum.incr(1L);
        this.logTPS.inc(1);
    }

    public synchronized void incrQueueSize(int count) {
        this.queueSize.incr(count);
    }

    public synchronized void decrQueueSize(int count) {
        this.queueSize.incr(count * -1);
    }


    public void incrKafkaSum() {
        this.kafkaTPS.inc(1);
    }

    /**
     * 用于计算网络字节吞吐量
     *
     * @param size
     */
    public void incrLogBytes(long size) {
        this.throughPut.incr(size);
        this.throughPutPS.inc(new Long(size).intValue());
    }

    private static TopicMetricsSource createMetrics(MetricsSystem ms,
                                                    String topic) {
        logger.info("creating metric source " + topic);
        TopicMetricsSource source = ms.register(topic, topic,
                new TopicMetricsSource(topic));
        return source;
    }

    public static void deleteMetrics(String topic) {
        DefaultMetricsSystem.INSTANCE.unregisterSource(topic);
        sources.remove(topic);
        topicLock.remove(topic);
    }

    public static TopicMetricsSource getMetrics(String topic) {
        TopicMetricsSource source = null;
        topicLock.putIfAbsent(topic, topic);
        synchronized (topicLock.get(topic)) {
            source = sources.get(topic);
            if (source == null) {
                source = createMetrics(DefaultMetricsSystem.INSTANCE, topic);
                sources.put(topic, source);
            }
        }
        return sources.get(topic);
    }
}
