package com.sohu.tw.elevator.plugin.http;

import com.sohu.tw.elevator.net.thrift.LogHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class MyPipelineFactory implements ChannelPipelineFactory {
	OrderedMemoryAwareThreadPoolExecutor executor = null;
	private LogHandler logHandler = null;
    ExecutionHandler handler = null;

	public MyPipelineFactory(OrderedMemoryAwareThreadPoolExecutor executor,
			LogHandler logHandler) {
		this.executor = executor;
		this.logHandler = logHandler;
        this.handler = new ExecutionHandler(executor);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();


		pipeline.addLast("decoder", new HttpRequestDecoder());
		// pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

		// Insert OrderedMemoryAwareThreadPoolExecutor before your blocking
		// handler
		pipeline.addLast("pipelineExecutor", handler);
		HttpHandler handler = new HttpHandler(logHandler);
		// MyHandler contains code that blocks
		pipeline.addLast("handler", handler);

		return pipeline;
	}

}
