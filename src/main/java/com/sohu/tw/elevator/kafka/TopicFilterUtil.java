package com.sohu.tw.elevator.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicFilterUtil {
    private final static Log logger = LogFactory.getLog(TopicFilterUtil.class);

    public static Map<String, Integer> parseBytesFromZK(byte[] b) {
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        String data = new String(b);
        String[] topicArr = data.split(",");
        for (String topic : topicArr) {
            topicMap.put(topic, 1);
            logger.info("add the topic:" + topic + " to topic list");
        }
        return topicMap;
    }

    public static Map<String, Integer> addTopic(List<String> list) {
        Map<String, Integer> topicMap = new ConcurrentHashMap<String, Integer>();
        for (int i = 0; i < list.size(); i++) {
            topicMap.put(list.get(i), 1);
            logger.info("add the topic:" + list.get(i) + " to topic list");
        }
        return topicMap;
    }

}
