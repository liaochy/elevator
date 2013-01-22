package com.sohu.tw.elevator.metrics;

import com.sohu.tw.goldmine.watchdog.metrics2.*;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.DefaultMetricsSystem;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.MetricMutableCounterInt;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.MetricMutableCounterLong;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.MetricsRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metric for Thread information
 */
public class ThreadMetricsSource implements MetricsSource {
    private MetricsRegistry registry;
    private String threadName;
    private Map<String, ArrayList<String>> statMap = new HashMap<String, ArrayList<String>>();

    private ThreadMetricsSource(String threadName) {
        this.threadName = threadName;
        this.registry = new MetricsRegistry(threadName);
    }


    public void updateStatMap(Map<String, ArrayList<String>> map) {
        this.statMap = map;
    }


    @Override
    public void getMetrics(MetricsBuilder metricsBuilder, boolean all) {
        MetricsRecordBuilder builder = metricsBuilder.addRecord(threadName);
        for (String topicName : statMap.keySet()) {
            MetricMutableCounterInt metric = new MetricMutableCounterInt(topicName, "",statMap.get(topicName).size() );
            metric.snapshot(builder);
        }
        this.registry.snapshot(builder, all);
    }


    private static ConcurrentHashMap<String, ThreadMetricsSource> threadMap = new ConcurrentHashMap<String, ThreadMetricsSource>();
    private static ConcurrentHashMap<String, String> threadLockMap = new ConcurrentHashMap<String, String>();

    public static ThreadMetricsSource getThreadMetrics(String threadName) {
        threadLockMap.putIfAbsent(threadName, threadName);
        ThreadMetricsSource source = null;
        synchronized (threadLockMap.get(threadName)) {
            source = threadMap.get(threadName);
            if (source == null) {
                source = new ThreadMetricsSource(threadName);
                DefaultMetricsSystem.INSTANCE.register(threadName, threadName,
                        source);
                threadMap.put(threadName, source);
            }
        }
        return source;
    }

}
