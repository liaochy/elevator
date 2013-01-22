package com.sohu.tw.elevator.syslog;

import com.sohu.tw.elevator.net.thrift.LogHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * User: yaqinzhang
 * Date: 12-12-14
 */
public class UDPServerPipelineFactory implements ChannelPipelineFactory {
    OrderedMemoryAwareThreadPoolExecutor executor = null;
    LogHandler logHandler = null;

    public UDPServerPipelineFactory(OrderedMemoryAwareThreadPoolExecutor executor, LogHandler logHandler) {
        this.executor = executor;
        this.logHandler = logHandler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("myHandler", new SyslogServerHandler(logHandler));

        return pipeline;
    }
}
