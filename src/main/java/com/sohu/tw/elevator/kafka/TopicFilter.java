package com.sohu.tw.elevator.kafka;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.net.thrift.LogEntity;
import com.sohu.tw.elevator.zookeeper.ZooKeeperUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicFilter {

    private final Log logger = LogFactory.getLog(TopicFilter.class);
    private Map<String, Integer> topicMap = null;
    private ZooKeeper zk = null;

    public void init() {

        ZkWatcher watcher = new ZkWatcher();

        try {
            zk = new ZooKeeper(ElevatorConfig.getZkConnect(),
                    ElevatorConfig.getZkTimeout(), watcher);
            //wait until connected the zookeeper to avoid the 'ConnectionLossException'
            ZooKeeperUtils.waitUntilConnected(zk);
            Stat stat = zk.exists(ElevatorConfig.getZkTopicPath(), watcher);
            if (stat == null)
                logger.error("path:" + ElevatorConfig.getZkTopicPath()
                        + " should exist");
            else {
                List<String> list = zk.getChildren(ElevatorConfig.getZkTopicPath(), watcher);
                Iterator it = list.iterator();
                topicMap = new ConcurrentHashMap<String, Integer>();
                while (it.hasNext()) {
                    topicMap.put(it.next().toString(), 1);
                }
            }
            logger.info("topic filter start successfully");
        } catch (Exception e) {
            logger.error("create zk error", e);
        }

    }

    public boolean doFilter(LogEntity logEntity) {
        if (ElevatorConfig.getTopicFilter() == false)
            return true;
        if (topicMap.containsKey(logEntity.getTopic())) {
            return true;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace(logEntity.getTopic() + " is not allowed to send data.");
            }
            return false;
        }

    }

    class ZkWatcher implements Watcher {
        private Log logger = LogFactory.getLog(ZkWatcher.class);

        @Override
        public void process(WatchedEvent evt) {
            switch (evt.getType()) {
                case NodeChildrenChanged:
                    logger.info(evt.getPath()
                            + " changed . and will reload the topic List");
                    try {
                        List<String> children = zk.getChildren(evt.getPath(), this);
                        if (children != null) {
                            topicMap = TopicFilterUtil.addTopic(children);
                        }
                    } catch (Exception e) {
                        logger.info("get zk data error. path=" + evt.getPath());
                    }
                    break;
                default:
                    logger.warn(evt.getType().toString()
                            + " cannot be parse by TopicFilter");
                    break;
            }
        }
    }
}
