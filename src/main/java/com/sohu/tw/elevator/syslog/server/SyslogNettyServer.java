package com.sohu.tw.elevator.syslog.server;

import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.net.thrift.LogHandler;
import com.sohu.tw.elevator.syslog.TCPServerPipelineFactory;
import com.sohu.tw.elevator.syslog.UDPServerPipelineFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: yaqinzhang Date: 12-12-14
 */
public class SyslogNettyServer {
	private final static Log logger = LogFactory.getLog(SyslogNettyServer.class);
	private static int udpPort = ElevatorConfig.getSyslogUDPPort();
	private static int tcpPort = ElevatorConfig.getSyslogTCPPort();
	private static String udpIp = ElevatorConfig.getSyslogUDPIP();

	public static void start(LogHandler logHandler) {
		OrderedMemoryAwareThreadPoolExecutor eventExecutor = new OrderedMemoryAwareThreadPoolExecutor(16, 1000000L,
				10000000L, 10000L, TimeUnit.MILLISECONDS);

		ConnectionlessBootstrap udpBootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(
				Executors.newCachedThreadPool()));

		udpBootstrap.setPipelineFactory(new UDPServerPipelineFactory(eventExecutor, logHandler));
		udpBootstrap.bind(new InetSocketAddress(udpIp, udpPort));
		logger.info("SyslogNettyServer is starting udp server at port " + udpPort);

		ServerBootstrap tcpBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		tcpBootstrap.setPipelineFactory(new TCPServerPipelineFactory(eventExecutor, logHandler));

		tcpBootstrap.bind(new InetSocketAddress(tcpPort));
		logger.info("SyslogNettyServer is starting tcp server at port " + tcpPort);
	}
}
