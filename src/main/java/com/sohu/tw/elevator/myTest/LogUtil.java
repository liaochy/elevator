package com.sohu.tw.elevator.myTest;

import org.apache.log4j.Logger;

/**
 * Utility Class for Debug Use
 * 
 * @author Bluesky
 */
public class LogUtil {
	static final String BR = System.getProperty("line.separator");

	static Logger logger = Logger.getLogger(LogUtil.class);

	public static void debug(String s) {
		logger.debug(s);
	}

	public static void info(String s) {
		logger.info(s);
	}

	public static void error(String s) {
		logger.error(s);
	}

	public static void exception(Exception ex) {
		String s = getErrorStackTrace(ex);
		logger.error(s);
	}

	/**
	 * ��ӡ������Ϣ
	 * 
	 * @param ex
	 *            Exception
	 * @return String
	 */
	public static String getErrorStackTrace(Exception ex) {
		String error = ex.toString() + BR;
		StackTraceElement[] st = ex.getStackTrace();
		for (StackTraceElement s : st) {
			error += "\t " + s + BR;
		}
		return error;
	}
}
