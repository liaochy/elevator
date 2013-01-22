package com.sohu.tw.elevator.plugin.http;

import com.sohu.tw.elevator.net.thrift.LogHandler;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyServer {

    public void start(LogHandler logHandler) {
        OrderedMemoryAwareThreadPoolExecutor eventExecutor = new OrderedMemoryAwareThreadPoolExecutor(
                16, 1000000, 10000000, 10 * 1000, TimeUnit.MILLISECONDS);

        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.setPipelineFactory(new MyPipelineFactory(eventExecutor,
                logHandler));
        bootstrap.bind(new InetSocketAddress(9088));
    }

}
