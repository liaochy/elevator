package com.sohu.tw.elevator.kafka;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.metrics.ThreadMetricsSource;
import com.sohu.tw.elevator.metrics.TopicMetricsSource;
import com.sohu.tw.elevator.net.thrift.LogEntity;
import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * kafka的producer类
 */
public class KafkaProducerService {

    private static final Log logger = LogFactory
            .getLog(KafkaProducerService.class);
    private BlockingQueue<LogEntity> logQueue = new ArrayBlockingQueue<LogEntity>(65536 * 300);
    private List<KafkaProducer> producerThreadList = new LinkedList<KafkaProducer>();
    private int DEFAULT_BATCH_SIZE = ElevatorConfig.getLogBatchSize();
    private int TIMEOUT = ElevatorConfig.getLogTimeout();
    private int LOG_QUEUE_TIMEOUT = ElevatorConfig.getLogQueueTimeout();
    private String HEARTBEAT_MAGIC = ElevatorConfig.getHeartbeatMagic();

    private TopicFilter topicFilter;


    public void setTopicFilter(TopicFilter topicFilter) {
        if (topicFilter == null)
            throw new IllegalArgumentException("topic filter should be set");
        this.topicFilter = topicFilter;
    }

    public void destroy() {
        for (KafkaProducer producer : producerThreadList) {
            producer.destroy();
        }
    }

    public void init() {
        // 初始化topicfilter
        topicFilter.init();
        // create producer thread
        for (int i = 0; i < ElevatorConfig.getProducerThreadNum(); i++) {
            KafkaProducer t = new KafkaProducer();
            producerThreadList.add(t);
        }

        logger.info("starting all kafka producers Thread");
        for (int i = 0; i < producerThreadList.size(); i++) {
            producerThreadList.get(i).init();
            producerThreadList.get(i).start();
        }
        logger.info("started all producers Thread. size="
                + producerThreadList.size());

    }

    public void sendLogEntry(LogEntity logEntity) {
        boolean suss = false;
        try {
            suss = logQueue.offer(logEntity, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            suss = false;
        }
        if (suss == false)
            logger.info("fail to offer queue!");
        else
            TopicMetricsSource.getMetrics("memory").incrQueueSize(1);
    }

    class KafkaProducer extends Thread {
        private boolean running = true;
        private Producer<String, String> producer = null;
        private Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        private Map<String, Long> timeoutMap = new HashMap<String, Long>();
        private long lastSentLogTimeMs = System.currentTimeMillis();

        public void init() {
            Properties props = new Properties();
            props.setProperty("zk.connect", ElevatorConfig.getZkConnect());
            props.setProperty("zk.sessiontimeout.ms",
                    String.valueOf(ElevatorConfig.getZkTimeout()));
            props.setProperty("zk.connectiontimeout.ms",
                    String.valueOf(ElevatorConfig.getZkTimeout()));
            props.setProperty("serializer.class",
                    "kafka.serializer.StringEncoder");
            props.put("reconnect.interval", Integer.MAX_VALUE + "");
            props.put("buffer.size", (64 * 1024) + "");
            producer = new Producer<String, String>(new ProducerConfig(props));
            running = true;
        }

        public void destroy() {
            running = false;
            interrupt();
            logger.info(Thread.currentThread().getName()
                    + "Kafka producer destroyed successfully!");
        }

        @Override
        public void run() {
            while (running) {
                try {
                    LogEntity log = logQueue.poll(LOG_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);

                    if (log != null) {
                        TopicMetricsSource.getMetrics("memory").decrQueueSize(1);
                        if (isHeartBeat(log)) {
                            continue;
                        }
                        if (!topicFilter.doFilter(log)) {
                            if (logger.isInfoEnabled())
                                logger.info("topic:" + log.getTopic()
                                        + " is a invalid topic.");
                            continue;
                        }
                        dealWithLog(log);
                        metricsLog(log);
                    }
                    if (System.currentTimeMillis() - lastSentLogTimeMs >= 500) {
                        sendTimeoutInfo();
                        lastSentLogTimeMs = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.error("waiting for log comming Interrupted.", e);
                    } else {
                        logger.error("The remote connection is closed.", e);
                        recover();
                    }
                }
            }
            producer.close();
        }

        //deal with log
        private void dealWithLog(LogEntity log) throws Exception {
            String topic = log.getTopic();
            if (map.containsKey(topic)) {
                if (map.get(topic).size() < 1) {
                    timeoutMap.put(topic, System.currentTimeMillis());
                }
                map.get(topic).add(log.getContent());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(log.getContent());
                map.put(topic, list);
                timeoutMap.put(topic, System.currentTimeMillis());
            }
            if (map.get(topic).size() >= DEFAULT_BATCH_SIZE || (System.currentTimeMillis() - timeoutMap.get(topic)) >= TIMEOUT) {
                try {
                    this.producer.send(new ProducerData<String, String>(topic, map.get(topic)));
                    ThreadMetricsSource.getThreadMetrics(this.getName()).updateStatMap(map);
                    map.put(topic, new ArrayList<String>());
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        //check heart beat
        private boolean isHeartBeat(LogEntity log) {
            if (HEARTBEAT_MAGIC.equals(log.getTopic())) {
                logger.info("Accept the heartbeat info[" + log.getContent() + "]");
                return true;
            }
            return false;
        }

        //send timeout info
        private void sendTimeoutInfo() throws Exception {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if ((System.currentTimeMillis() - timeoutMap.get(entry.getKey())) >= TIMEOUT && (((ArrayList) entry.getValue()).size()) > 0) {
                    try {
                        this.producer.send(new ProducerData<String, String>(entry.getKey().toString(), (ArrayList) entry.getValue()));
                        ThreadMetricsSource.getThreadMetrics(Thread.currentThread().getName()).updateStatMap(map);
                        map.put(entry.getKey().toString(), new ArrayList<String>());
                    } catch (Exception e) {
                        throw e;
                    }

                }
            }
        }

        //close the producer's connection and reconnect
        private void recover() {
            producer.close();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.init();
        }

        private void metricsLog(LogEntity log) {
            try {
                TopicMetricsSource.getMetrics("all").incrKafkaSum();
                TopicMetricsSource.getMetrics(log.getTopic()).incrKafkaSum();
            } catch (Exception e) {
                logger.error("metricing log error.", e);
            }
        }
    }
}
