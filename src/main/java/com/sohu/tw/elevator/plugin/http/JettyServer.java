package com.sohu.tw.elevator.plugin.http;

import com.sohu.tw.elevator.ElevatorConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.URL;

public class JettyServer {
	private static final Log logger = LogFactory.getLog(JettyServer.class);
	private Server server = null;
	private WebAppContext ctxt = null;

	public void start() {

		server = new Server(ElevatorConfig.getHttpServerPort());
		final String CONTEXTPATH = "/";

		String webApps = "";
		try {
			webApps = getWebAppsPath();
		} catch (IOException e) {
			logger.error("webapps is not found in classpath", e);
		}
		ctxt = new WebAppContext(webApps, CONTEXTPATH);
		logger.info("war url = " + webApps);
		server.setHandler(ctxt);

		addInternalServlet("logLevel", "/logLevel",
				com.sohu.tw.elevator.plugin.http.ElevatorLogLevel.Servlet.class);
		addInternalServlet("logs", "/logs/*",
				com.sohu.tw.elevator.plugin.http.LogsServlet.class);
		try {
			server.start();
		} catch (Exception e) {
			logger.error("Jetty Server start error.", e);
		}

	}

	protected String getWebAppsPath() throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("elevator-webapps");
		if (url == null)
			throw new IOException("webapps not found in CLASSPATH");
		return url.toString();
	}

	public void addInternalServlet(String name, String pathSpec,
			Class<? extends HttpServlet> clazz) {
		ServletHolder holder = new ServletHolder(clazz);
		if (name != null) {
			holder.setName(name);
		}
		ctxt.addServlet(holder, pathSpec);
	}

	public void destroy() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.info("http server stop error", e);
		}
	}
}
