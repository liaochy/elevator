package com.sohu.tw.elevator.syslog;

import com.sohu.tw.elevator.net.thrift.LogHandler;
import com.sohu.tw.elevator.syslog.common.SyslogConstants;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * User: yaqinzhang
 * Date: 12-12-17
 */
public class TCPServerPipelineFactory implements ChannelPipelineFactory {
    OrderedMemoryAwareThreadPoolExecutor executor = null;
    LogHandler logHandler = null;

    public TCPServerPipelineFactory(OrderedMemoryAwareThreadPoolExecutor executor, LogHandler logHandler) {
        this.executor = executor;
        this.logHandler = logHandler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new DelimiterBasedFrameDecoder(SyslogConstants.SYSLOG_BUFFER_SIZE, Delimiters.lineDelimiter()));
        pipeline.addLast("myHandler", new SyslogServerHandler(this.logHandler));

        return pipeline;
    }
}
