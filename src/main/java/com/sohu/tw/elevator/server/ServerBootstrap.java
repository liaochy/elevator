package com.sohu.tw.elevator.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.goldmine.avro.AvroTopicEvent;
import com.sohu.tw.elevator.ElevatorConfig;
import com.sohu.tw.elevator.metrics.StatusSink;
import com.sohu.tw.elevator.net.thrift.LogEntity;
import com.sohu.tw.elevator.net.thrift.LogHandler;
import com.sohu.tw.elevator.net.thrift.ThriftSerivce;
import com.sohu.tw.elevator.plugin.http.AvroTopicEventServerImpl;
import com.sohu.tw.elevator.plugin.http.JettyServer;
import com.sohu.tw.elevator.plugin.http.NettyServer;
import com.sohu.tw.elevator.syslog.server.SyslogNettyServer;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.DefaultMetricsSystem;

/**
 * elevator项目的启动类
 */
public class ServerBootstrap {
	private final static Log logger = LogFactory.getLog(ServerBootstrap.class);

	public static List<LogEntity> convertToLogEntity(List<AvroTopicEvent> evts) {
		List<LogEntity> list = new ArrayList<LogEntity>(evts.size());
		for (AvroTopicEvent evt : evts) {
			list.add(new LogEntity(evt.topic.toString(),
					evt.json ? evt.toString() :  evt.body.toString()));
		}
		return list;
	}

	public static void main(String[] args) {

		JettyServer jettyServer = null;
		try {
			DefaultMetricsSystem.initialize("Elevator");
			StatusSink.instance().init(null);
			final ThriftSerivce thriftServ = new ThriftSerivce();
			new Thread(thriftServ).start();
			jettyServer = new JettyServer();
			jettyServer.start();

			if (ElevatorConfig.useSyslog()) {
				NettyServer server = new NettyServer();
				server.start(thriftServ.getLogHandler());
				SyslogNettyServer.start(thriftServ.getLogHandler());
			}

			AvroTopicEventServerImpl avroServer = new AvroTopicEventServerImpl(
					ElevatorConfig.getAvroPort(), ElevatorConfig.getAvroHttp()) {
				LogHandler loghandler = thriftServ.getLogHandler();

				@Override
				public void append(List<AvroTopicEvent> evts) {
					try {
						loghandler.send(convertToLogEntity(evts));
					} catch (Exception e1) {
						logger.error("AvroTopicEventServerImpl Error", e1);
					}
					super.append(evts);
				}
			};
			avroServer.start();

		} catch (Exception e) {
			if (jettyServer != null)
				jettyServer.destroy();
			logger.error("fatal error. will stop the elevator", e);
			System.exit(1);
		} catch (Error e) {
			logger.error("fatal error. will stop the elevator", e);
			System.exit(1);
		}
	}
}
