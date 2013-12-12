package com.sohu.tw.elevator.plugin.http;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.metrics.TopicMetricsSource;
import com.sohu.tw.elevator.net.thrift.LogEntity;
import com.sohu.tw.elevator.net.thrift.LogHandler;
import kafka.common.ConsumerRebalanceFailedException;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.*;

public class HttpHandler extends SimpleChannelUpstreamHandler {

    private static Log LOG = LogFactory.getLog(HttpHandler.class);
    private LogHandler logHandler = null;
    private long TIMEOUT = 1000 * 10;
    private int CHUNK_SIZE = 20;

    public HttpHandler(LogHandler logHandler) {
        this.logHandler = logHandler;
        if (System.getProperties().get("elevator.http.timeout") != null) {
            TIMEOUT = Long.parseLong(System.getProperties().get("elevator.http.timeout").toString());
        }
    }

    private void response(ChannelHandlerContext ctx, HttpRequest req,
                          HttpResponse resp) {
        ChannelFuture f = ctx.getChannel().write(resp);
        if (!HttpHeaders.is100ContinueExpected(req))
            f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {

        Object reqObj = e.getMessage();

        if (reqObj instanceof DefaultHttpRequest) {
            HttpRequest req = (HttpRequest) reqObj;
            HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);
            if (!validate(req, resp)) {
                response(ctx, req, resp);
            } else {
                Map<String, String> pMap = parmeters(req);
                setAttachment(ctx, pMap, null);
                if (req.getMethod().compareTo(HttpMethod.PUT) == 0) {
                    doPut(req, resp, pMap);
                    response(ctx, req, resp);
                }
                if (req.getMethod().compareTo(HttpMethod.GET) == 0) {
                    doGet4Chunk(req, resp, pMap, ctx);
                }
            }
        } else {
            HttpChunk req = (HttpChunk) reqObj;
            HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK);
            doPut4Chunk(ctx, req, resp);
        }
    }

    private void doPut4Chunk(ChannelHandlerContext ctxt, HttpChunk req,
                             HttpResponse resp) {
        Map<String, String> pMap = ((Attachment)ctxt.getChannel()
                .getAttachment()).pMap;
        String topic = pMap.get("topic");
        ChannelBuffer buffer = req.getContent();
        byte[] bArr = new byte[buffer.capacity()];
        buffer.getBytes(0, bArr);
        String messages = new String(bArr);
        // error:
        // 一个长的log可能拆分成多了个chunck，所以如果找不到\n的话，都要放到缓存中
        int index = messages.lastIndexOf("\n");
        String[] messageArr = null;
        if (index != -1) {
            messageArr = messages.substring(0, index).split("\\n");
        } else {
            // 认为这是一条记录
            String lastSegement = getAttachment(ctxt).lastSegement + messages;
            setAttachment(ctxt, null, lastSegement);
            return;
        }
        messageArr[0] = getAttachment(ctxt).lastSegement + messageArr[0];
        List<LogEntity> logList = generateLogList(topic, messageArr);
        doSend(logList);
        LOG.info("have sent the logs . size = " + logList.size());
        String lastSegement = messages.substring(index + 1);
        setAttachment(ctxt, null, lastSegement);
    }

    private ConsumerConnector consumer(String groupName, String timeoutMs) {
        Properties props = new Properties();
        props.put("zk.connect", ElevatorConfig.getZkConnect());
        props.put("zk.connectiontimeout.ms", ElevatorConfig.getZkTimeout() + "");
        props.put("groupid", groupName);
        props.put("consumer.timeout.ms", timeoutMs);

        ConsumerConnector consumer = kafka.consumer.Consumer
                .createJavaConsumerConnector(new ConsumerConfig(props));
        return consumer;
    }

    /**
     * return data as chunk
     *
     * @param req  HttpRequest
     * @param resp HttpResponse
     * @param pMap
     * @param ctx
     */
    private void doGet4Chunk(HttpRequest req, HttpResponse resp, Map<String, String> pMap, ChannelHandlerContext ctx) {
        long start = System.currentTimeMillis();
        boolean keepGet = false;

        String topic = getTopic(pMap);
        String groupName = getGroupName(pMap);
        String timeoutMs = getTimeoutMs(pMap);
        int msgCount = getMsgCount(pMap, keepGet);
        LOG.info("HttpHandler.doGet4Chunk[topic=" + topic + ",groupName=" + groupName + ",timeoutMs=" + timeoutMs + ",msgCount=" + msgCount + ",httpTimeout=" + TIMEOUT + "]");

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));

        // prepared the consume connector
        getMessageStream(req, resp, timeoutMs, topicCountMap, topic, groupName, start, msgCount, keepGet, ctx);
    }

    private void getMessageStream(HttpRequest req, HttpResponse resp, String timeoutMs, Map<String, Integer> topicCountMap,
                                  String topic, String groupName, long start, int msgCount,
                                  boolean keepGet, ChannelHandlerContext ctx) {
        resp.setChunked(true);
        resp.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        ChannelFuture f = null;

        StringBuilder str = new StringBuilder();
        ConsumerConnector consumer = null;
        boolean isResponsed = false;
        try {
            consumer = getConsumerConn(topic, groupName, timeoutMs);

            Map<String, List<KafkaStream<Message>>> consumerMap = consumer.createMessageStreams(topicCountMap);
            KafkaStream<Message> stream = consumerMap.get(topic).get(0);
            ConsumerIterator<Message> it = stream.iterator();
            int cnt = 0;
            if (isResponsed == false) {
                f = ctx.getChannel().write(resp);
                isResponsed = true;
            }

            while (it.hasNext()) {
                long end = System.currentTimeMillis() - start;
                if (isServerTimeout(end, cnt))
                    break;
                str = new StringBuilder();
                cnt = getBatchMessage(it, start, msgCount, keepGet, str, cnt);
                if (!ctx.getChannel().isConnected()) {
                    LOG.info("HttpHandler.doGet4Chunk[ctx.getChannel().isConnected() is false!]");
                    break;
                }
                if (str.length() > 0) {
                    HttpChunk chunk = new DefaultHttpChunk(ChannelBuffers.wrappedBuffer(str.toString().getBytes(CharsetUtil.UTF_8)));
                    f = ctx.getChannel().write(chunk);
                }
                if (!keepGet && msgCount == cnt)
                    break;
            }
        } catch (Exception e) {
            if (e instanceof ConsumerTimeoutException) {
                LOG.info("Consumer timeout. groupName = " + groupName + " topic = " + topic);
            } else if (e instanceof ConsumerRebalanceFailedException) {
                resp.setStatus(HttpResponseStatus.CONFLICT);
                if (isResponsed == false) {
                    f = ctx.getChannel().write(resp);
                    isResponsed = true;
                }
                LOG.error("HttpHandler.doGet4Chunk:HttpResponseStatus=" + HttpResponseStatus.CONFLICT + " GroupName = " + groupName + " topic = " + topic, e);
            } else {
                LOG.error("No Messages or server timeout.groupName = " + groupName + " topic = " + topic, e);
            }
            if (str.length() > 0) {
                HttpChunk chunk = new DefaultHttpChunk(ChannelBuffers.wrappedBuffer(str.toString().getBytes(CharsetUtil.UTF_8)));
                f = ctx.getChannel().write(chunk);
            }
        }
        HttpChunk chunk = new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER);
        f = ctx.getChannel().write(chunk);
        if (!HttpHeaders.is100ContinueExpected(req))
            f.addListener(ChannelFutureListener.CLOSE);
        LOG.info("HttpHandler.doGet4Chunk[Return chunked info and close connection!]");
        consumer.shutdown();
        LOG.info("counsumer shut down .");
    }

    private int getBatchMessage(ConsumerIterator<Message> it, long start, int msgCount, boolean keepGet, StringBuilder str, int cnt) {
        int count = 0;
        while (it.hasNext() && count < CHUNK_SIZE) {
            long end = System.currentTimeMillis() - start;
            if (isServerTimeout(end, cnt))
                break;
            str.append(getMessage(it.next().message())).append("\r\n");
            count++;
            cnt++;
            if (!keepGet && msgCount == cnt)
                break;
        }
        return cnt;
    }

    private ConsumerConnector getConsumerConn(String topic, String groupName, String timeoutMs) {
        ConsumerConnector consumer = null;
        consumer = consumer(groupName, timeoutMs);
        if (consumer == null) {
            throw new IllegalStateException("HttpHandler.doGet4Chunk:The consumer is null!");
        }
        return consumer;
    }

    private ChannelFuture writeDefaultResp(ChannelHandlerContext ctx, HttpResponse resp) {
        resp.setChunked(true);
        resp.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        return ctx.getChannel().write(resp);
    }

    private String getTopic(Map<String, String> pMap) {
        String topic = pMap.get("topic");
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("HttpHandler.doGet4Chunk:The argument topic is blank!");
        }
        return topic;
    }

    private String getGroupName(Map<String, String> pMap) {
        String groupName = pMap.get("groupName");
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("HttpHandler.doGet4Chunk:The argument groupName is blank!");
        }
        return groupName;
    }

    private String getTimeoutMs(Map<String, String> pMap) {
        String timeoutMs = pMap.get("timeoutMs");
        if (StringUtils.isBlank(timeoutMs)) {
            timeoutMs = "1000";
        }
        return timeoutMs;
    }

    private int getMsgCount(Map<String, String> pMap, boolean keepGet) {
        int msgCount = 0;
        if (pMap.get("msgCount") == null) {
            keepGet = true;
        } else {
            msgCount = new Integer(pMap.get("msgCount"));
        }
        return msgCount;
    }

    private boolean isServerTimeout(long end, int cnt) {
        if (end >= TIMEOUT) {
            LOG.info("HttpHandler.doGet4Chunk[Server timeout:" + end + ";msg_number:" + cnt + "]");
            return true;
        }
        return false;
    }

    private boolean validate(HttpRequest req, HttpResponse resp) {
        if (req.getUri().equals("/favicon.ico")) {
            resp.setStatus(HttpResponseStatus.NOT_FOUND);
            return false;
        }
        return true;
    }

    private void doPut(HttpRequest req, HttpResponse resp,
                       Map<String, String> pMap) {
        String topic = pMap.get("topic");
        ChannelBuffer buffer = req.getContent();
        byte[] bArr = new byte[buffer.capacity()];
        buffer.getBytes(0, bArr);
        String messages = new String(bArr);
        String[] messageArr = messages.split("\\n");
        List<LogEntity> logList = generateLogList(topic, messageArr);
        doSend(logList);
        LOG.info("have sent the logs . size = " + logList.size());

    }

    private Map<String, String> parmeters(HttpRequest req) {
        Map<String, String> map = new HashMap<String, String>();

        String tmpUri = null;
        if (req.getUri().indexOf("?") >= 0)
            tmpUri = req.getUri().substring(1, req.getUri().indexOf("?"));
        else
            tmpUri = req.getUri().substring(1);
        String[] arr = tmpUri.split("/");
        String topic = arr[1];
        String groupName = null;
        if (arr.length > 2)
            groupName = arr[2];
        map.put("topic", topic);
        map.put("groupName", groupName);
        if (req.getUri().indexOf("?") >= 0) {
            String uri = req.getUri().substring(req.getUri().indexOf("?") + 1);
            String[] kvs = uri.split("&");
            for (String kv : kvs) {
                String[] kvArr = kv.split("=");
                map.put(kvArr[0], kvArr[1]);
            }
            return map;
        } else {
            return map;
        }
    }

    private void doSend(List<LogEntity> logList) {
        logHandler.send(logList);
    }

    private List<LogEntity> generateLogList(String topic, String[] contentArr) {
        List<LogEntity> list = new LinkedList<LogEntity>();
        for (String content : contentArr) {
            if (content.equals(""))
                continue;
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
            LOG.error("metricing log error.", e);
        }
    }

    private String getMessage(Message message) {
        ByteBuffer buffer = message.payload();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }

    private void setAttachment(ChannelHandlerContext ctxt,
                               Map<String, String> pMap, String lastSegment) {
        if (ctxt.getChannel().getAttachment() == null) {
            Attachment a = new Attachment();
            ctxt.getChannel().setAttachment(a);
        }
        Attachment a = (Attachment) ctxt.getChannel().getAttachment();
        if (pMap != null)
            a.pMap = pMap;
        if (lastSegment != null)
            a.lastSegement = lastSegment;
    }

    private Attachment getAttachment(ChannelHandlerContext ctxt) {
        return (Attachment) ctxt.getChannel().getAttachment();
    }

    class Attachment {
        public Map<String, String> pMap = null;
        public String lastSegement = "";
    }
}
