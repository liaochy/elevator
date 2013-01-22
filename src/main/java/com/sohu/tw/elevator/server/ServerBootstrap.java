package com.sohu.tw.elevator.server;

import com.sohu.tw.elevator.metrics.StatusSink;
import com.sohu.tw.elevator.net.thrift.ThriftSerivce;
import com.sohu.tw.elevator.plugin.http.JettyServer;
import com.sohu.tw.elevator.plugin.http.NettyServer;
import com.sohu.tw.elevator.syslog.server.SyslogNettyServer;
import com.sohu.tw.goldmine.watchdog.metrics2.lib.DefaultMetricsSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * elevator项目的启动类
 */
public class ServerBootstrap {
	private final static Log logger = LogFactory.getLog(ServerBootstrap.class);

	public static void main(String[] args) {
		JettyServer jettyServer = null;
		try {
			DefaultMetricsSystem.initialize("Elevator");
			StatusSink.instance().init(null);
			ThriftSerivce thriftServ = new ThriftSerivce();
			new Thread(thriftServ).start();
            jettyServer = new JettyServer();
			jettyServer.start();

			NettyServer server = new NettyServer();
			server.start(thriftServ.getLogHandler());
            SyslogNettyServer.start(thriftServ.getLogHandler());

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
