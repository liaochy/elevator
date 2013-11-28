package com.sohu.tw.elevator.syslog;

import com.sohu.tw.elevator.net.thrift.LogHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * User: yaqinzhang Date: 12-12-14
 */
public class SyslogServerHandler extends SimpleChannelUpstreamHandler {
	private static final Log logger = LogFactory.getLog(SyslogServerHandler.class);
	private LogHandler logHandler = null;

	protected byte[] rawBytes = null;
	protected int rawLength = -1;
	protected Date date = null;
	protected int level = -1;
	protected int facility = -1;
	protected String host = null;
	protected String message = null;
	protected String ident = null;
	protected String processId = null;
	protected int priority = -1;

	public SyslogServerHandler(LogHandler logHandler) {
		logger.info("SyslogServerHandler initialized!");
		this.logHandler = logHandler;
	}

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object m = e.getMessage();
		if (!(m instanceof ChannelBuffer)) {
			return;
		}

		ChannelBuffer input = (ChannelBuffer) m;
		if (!input.readable()) {
			return;
		}
		int readable = input.readableBytes();
		if (readable <= 0) {
			return;
		}

		byte[] buf = new byte[readable];
		input.readBytes(buf, 0, readable);

		this.message = new String(buf);
		parse();
		MessageHandler.handleMessage(this.ident, this.message, this.logHandler);
	}

	protected void parseIdent() {
		int i = this.message.indexOf(91);
		int j = this.message.indexOf(":");
		int k = this.message.indexOf("]");

		if ((i > -1) && (k > -1)) {
			if ((j <= -1) || (j < k + 1))
				i = j;
		} else {
			i = j;
		}
		this.ident = this.message.substring(0, i).trim();
		if (StringUtils.isBlank(this.ident)) {
			throw new IllegalArgumentException("SyslogServerHandler.parseIdent:the argument ident is blank!");
		}
		this.message = this.message.substring(j + 2);
	}

	protected void parseDate() {
		this.message = this.message.substring(16);
	}

	protected void parseHost() {
		int i = this.message.indexOf(32);

		if (i > -1) {
			this.host = this.message.substring(0, i).trim();
			this.message = this.message.substring(i + 1);

			parseIdent();
		}
	}

	protected void parse() {
		logger.debug("SyslogServerHandler.parse info:" + this.message);
		if (this.message == null) {
			try {
				this.message = new String(this.rawBytes, 0, this.rawLength, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				this.message = new String(this.rawBytes);
				logger.error("SyslogServerHandler.parse:" + e);
			}
		}
		parsePriority();
	}

	protected void parsePriority() {
		if (this.message.charAt(0) == '<') {
			int i = this.message.indexOf(">");

			if ((i <= 4) && (i > -1)) {
				String priorityStr = this.message.substring(1, i);

				int priority = 0;
				try {
					priority = Integer.parseInt(priorityStr);
					this.priority = priority;
					this.facility = (priority >> 3);
					this.level = (priority - (this.facility << 3));

					this.message = this.message.substring(i + 1);

					parseDate();
				} catch (NumberFormatException nfe) {
					logger.error("SyslogServerHandler.parsePriority:" + nfe);
				}

				parseHost();
			}
		}
	}
}