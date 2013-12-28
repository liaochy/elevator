package com.sohu.tw.elevator.plugin.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;

import com.sohu.goldmine.avro.AvroTopicEvent;
import com.sohu.goldmine.avro.AvroTopicEventServer;
import com.sohu.goldmine.avro.AvroTopicEventV2;

public class AvroTopicEventServerImpl implements AvroTopicEventServer {
	private Server server;
	private final int port;
	private boolean http;

	/**
	 * This just sets the port for this AvroServer
	 */
	public AvroTopicEventServerImpl(int port, boolean http) {
		this.port = port;
		this.http = http;
	}

	/**
	 * This blocks till the server starts.
	 */
	public void start() throws IOException {
		Responder res = new SpecificResponder(AvroTopicEventServer.class, this);
		this.server = http ? new HttpServer(res, port) : new AvroNettyServer(
				res, new InetSocketAddress(port));
		this.server.start();
	}

	/**
	 * Stops the FlumeEventAvroServer, called only from the server.
	 */
	public void close() throws AvroRemoteException {
		server.close();
	}

	@Override
	public void append(List<AvroTopicEvent> evt) {

	}

	@Override
	public void appendV2(List<AvroTopicEventV2> evts, boolean json) {
		
	}
}
