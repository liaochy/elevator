package com.sohu.tw.elevator.plugin.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import net.javaforge.netty.servlet.bridge.ServletBridgeChannelPipelineFactory;
import net.javaforge.netty.servlet.bridge.config.ServletConfiguration;
import net.javaforge.netty.servlet.bridge.config.WebappConfiguration;

import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.ResponderServlet;
import org.apache.avro.ipc.Server;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class AvroNettyServer implements Server {

	private final Channel serverChannel;
	private final ChannelGroup allChannels = new DefaultChannelGroup(
			"avro-netty-server");
	private final ChannelFactory channelFactory;
	private final CountDownLatch closed = new CountDownLatch(1);

	public AvroNettyServer(Responder responder, InetSocketAddress addr)
			throws IOException {
		channelFactory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

		WebappConfiguration webapp = new WebappConfiguration()
				.setMaxContentLength(1048576 * 5).addServletConfigurations(
						new ServletConfiguration(
								new ResponderServlet(responder)));

		bootstrap.setPipelineFactory(new ServletBridgeChannelPipelineFactory(
				webapp));

		serverChannel = bootstrap.bind(addr);
		allChannels.add(serverChannel);
	}

	@Override
	public void start() {
		// No-op.
	}

	@Override
	public void close() {
		ChannelGroupFuture future = allChannels.close();
		future.awaitUninterruptibly();
		channelFactory.releaseExternalResources();
		closed.countDown();
	}

	@Override
	public int getPort() {
		return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
	}

	@Override
	public void join() throws InterruptedException {
		closed.await();
	}

}
