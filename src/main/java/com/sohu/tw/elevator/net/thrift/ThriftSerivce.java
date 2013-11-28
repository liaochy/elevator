package com.sohu.tw.elevator.net.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Options;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import com.sohu.tw.elevator.ElevatorConfig;

/**
 * thrift 的服务器端
 */
public class ThriftSerivce implements Runnable {

	private static final Log logger = LogFactory.getLog(ThriftSerivce.class);
	LogService.Processor processor;
	private LogHandler handler = null;
	TServer server = null;

	public ThriftSerivce() throws TTransportException {
		handler = new LogHandler();
		processor = new LogService.Processor(handler);
		TServerTransport serverTransport = new TServerSocket(ElevatorConfig.getElevatorPort());

		Options options = new Options();
		options.maxWorkerThreads = 16535;
		server = new TThreadPoolServer(processor, serverTransport, new TTransportFactory(), new TTransportFactory(),
				new TBinaryProtocol.Factory(), new TBinaryProtocol.Factory(), options);

	}

	public LogHandler getLogHandler() {
		return handler;
	}

	@Override
	public void run() {
		logger.info("Thrift server start successfully! port=" + ElevatorConfig.getElevatorPort());
		server.serve();
	}

}
