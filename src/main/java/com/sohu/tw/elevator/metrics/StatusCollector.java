package com.sohu.tw.elevator.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sohu.tw.goldmine.watchdog.metrics2.Metric;
import com.sohu.tw.goldmine.watchdog.metrics2.MetricsRecord;
import com.sohu.tw.goldmine.watchdog.metrics2.MetricsTag;

public class StatusCollector {
    private static StatusCollector collector = new StatusCollector();
    private Map<String, Map<String, Object>> recordMap = null;
    private Map<String, Map<String, Number>> threadMap = null;

    private StatusCollector() {
        recordMap = new HashMap<String, Map<String, Object>>();
        threadMap = new HashMap<String, Map<String, Number>>();
    }

    public static StatusCollector getInstance() {
        if (collector == null)
            collector = new StatusCollector();
        return collector;

    }

    public void updateRecord(final MetricsRecord record) {

        if (record.name().indexOf("Thread") >= 0) {
            synchronized (threadMap) {
                String recordName = record.name();
                Map<String, Number> map = threadMap.get(recordName);
                if (map == null) {
                    map = new HashMap<String, Number>();
                    threadMap.put(recordName, map);

                }
                for (Metric metric : record.metrics()) {
                    map.put(metric.name(), metric.value());
                }
            }
        } else {
            synchronized (recordMap) {

                String topicName = null;
                String type = null;
                for (MetricsTag tag : record.tags()) {
                    if (tag.name().equals("topic")) {
                        topicName = tag.value();
                    }
                    if (tag.name().equals("type")) {
                        type = tag.value();
                    }
                }

                Map<String, Object> map = recordMap.get(topicName);
                if (map == null) {
                    map = new HashMap<String, Object>();
                    recordMap.put(topicName, map);

                }

                Number value = null;
                for (Metric metric : record.metrics()) {
                    if (!metric.name().equals("time")) {
                        value = metric.value();
                        break;
                    }
                }
                map.put(type, value);
            }
        }
    }

    public List<String> getTopics() {
        List<String> topics = new ArrayList<String>();
        for (String topic : recordMap.keySet()) {
            if (!topic.equals("all")) {
                topics.add(topic);
            }
        }
        return topics;
    }

    public void deleteTopic(String topic) {
        TopicMetricsSource.deleteMetrics(topic);
        recordMap.remove(topic);
    }

    public Map<String, Object> getRecord(String recordName) {
        Map<String, Object> record = null;
        synchronized (this) {
            Map<String, Object> map = recordMap.get(recordName);
            if (map != null) {
                record = new HashMap<String, Object>(map);
                for (String key : map.keySet()) {
                    record.put(key, new Long(map.get(key).toString()));
                }
            }
        }
        return record;
    }

    public Map<String, Map<String, Number>> getThreadInfo() {
        return threadMap;
    }

}
