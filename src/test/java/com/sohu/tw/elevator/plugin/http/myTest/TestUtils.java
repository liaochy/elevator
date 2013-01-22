package com.sohu.tw.elevator.plugin.http.myTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:43
 * To change this template use File | Settings | File Templates.
 */
public class TestUtils {

	public static long getTotal(List<Long> times) {
		long total = 0;
		for (Long time : times) {
			total = total + time;
		}
		return total;
	}

	public static float getAverage(List<Long> allTimes) {
		long total = getTotal(allTimes);
		return total / (float) allTimes.size();
	}

	public static float toMs(long nm) {
		return nm / 1000000f;
	}

	public static float toMs(float nm) {
		return nm / 1000000f;
	}
}
