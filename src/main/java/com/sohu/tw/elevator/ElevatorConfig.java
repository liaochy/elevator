package com.sohu.tw.elevator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * elevator的配置类
 */
public class ElevatorConfig {
	private static final Log LOG = LogFactory.getLog(ElevatorConfig.class);

	private static Properties p = new Properties();
	private static String localhostIp = null;

	public static int getElevatorPort() {
		String port = p.getProperty("elevatorPort");
		return port == null ? 9090 : Integer.parseInt(port);
	}

	public static int getProducerThreadNum() {
		String time = p.getProperty("producer.thread.num");
		return time == null ? 10 : Integer.parseInt(time);
	}

	public static String getZkConnect() {
		String connection = p.getProperty("zkConnect");
		if (connection == null) {
			LOG.error("zkConnect is not found in config file");
		}
		return connection;
	}

	public static String getZkTopicPath() {
		String connection = p.getProperty("topicPath");
		return connection == null ? "/elevator/topics" : connection;
	}

	public static int getZkTimeout() {
		String time = p.getProperty("zkTimeout");
		return time == null ? 60000 : Integer.parseInt(time);
	}

	public static int getHttpServerPort() {
		String time = p.getProperty("httpServerPort");
		return time == null ? 9089 : Integer.parseInt(time);
	}

	public static boolean getTopicFilter() {
		String time = p.getProperty("topic.filter");
		return time == null ? false : Boolean.parseBoolean(time);
	}

	public static int getLogBatchSize() {
		String logBatchSize = p.getProperty("log.batch.size");
		return logBatchSize == null ? 100 : Integer.parseInt(logBatchSize);
	}

	public static int getLogTimeout() {
		String logTimeout = p.getProperty("log.timeout");
		return logTimeout == null ? 500 : Integer.parseInt(logTimeout);
	}

	public static int getLogQueueTimeout() {
		String logQueueTimeout = p.getProperty("log.queue.timeout");
		return logQueueTimeout == null ? 500 : Integer.parseInt(logQueueTimeout);
	}

	public static String getHeartbeatMagic() {
		String heartbeatMagic = p.getProperty("heartbeat.magic");
		return heartbeatMagic == null ? "ELEVATOR_HEARTBEAT" : heartbeatMagic;
	}

	public static String getVersion() {
		String version = p.getProperty("elevator.version");
		return version == null ? "0.0.0" : version;
	}

	public static String getLocalHostIp() {
		return localhostIp == null ? "0.0.0.0" : localhostIp;
	}

	public static int getSyslogUDPPort() {
		String port = p.getProperty("syslog.udp.port");
		return port == null ? 10514 : Integer.parseInt(port);
	}

	public static int getSyslogTCPPort() {
		String port = p.getProperty("syslog.tcp.port");
		return port == null ? 10515 : Integer.parseInt(port);
	}

	public static String getSyslogUDPIP() {
		String ip = p.getProperty("syslog.udp.ip");
		return ip == null ? "10.11.152.131" : ip;
	}

	static {
		ClassLoader cL = Thread.currentThread().getContextClassLoader();
		if (cL == null) {
			cL = Configuration.class.getClassLoader();
		}
		URL url = cL.getResource("elevator.properties");
		if (url != null)
			try {
				InputStream in = url.openStream();
				p.load(in);
				in.close();
			} catch (IOException e) {
				LOG.error("reading elevator.properties", e);
			}
		else
			LOG.error("elevator.properties not found");
		try {
			localhostIp = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			LOG.error("get localhost ip error", e);
			localhostIp = "unknown";
		}
	}
}
