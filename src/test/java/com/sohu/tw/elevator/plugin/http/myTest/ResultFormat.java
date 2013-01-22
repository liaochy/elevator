package com.sohu.tw.elevator.plugin.http.myTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:37
 * To change this template use File | Settings | File Templates.
 */
public class ResultFormat {
    public static final Log log = LogFactory.getLog(ResultFormat.class);

	public void format(TestResult result, String serviceName, Writer writer) {
		long testsTakenTime = result.getTestsTakenTime();
		int totalRequests = result.getTotalRequests();
		int concurrencyLevel = result.getConcurrencyLevel();

		float takes = toMs(testsTakenTime);

		float tps = (totalRequests * 1000) / takes;

		// float average = toMs((testsTakenTime / totalRequests));

		List<Long> allTimes = result.getAllTimes();

		float average = toMs(getAverage(allTimes));

		int count_50 = totalRequests / 2;
		int count_66 = totalRequests * 66 / 100;
		int count_75 = totalRequests * 75 / 100;
		int count_80 = totalRequests * 80 / 100;
		int count_90 = totalRequests * 90 / 100;
		int count_95 = totalRequests * 95 / 100;
		int count_98 = totalRequests * 98 / 100;
		int count_99 = totalRequests * 99 / 100;

		long longestRequest = allTimes.get(allTimes.size() - 1);
		long shortestRequest = allTimes.get(0);

		StringBuilder view = new StringBuilder();

		view.append(" Service Name:\t").append(serviceName);
		view.append("\r\n Concurrency Level:\t").append(concurrencyLevel);
		view.append("\r\n Time taken for tests:\t").append(takes).append(" ms");
		view.append("\r\n Complete Requests:\t").append(totalRequests);
		view.append("\r\n Failed Requests:\t").append(
				result.getFailedRequests());
		view.append("\r\n Requests per second:\t").append(tps);
		view.append("\r\n Time per request:\t").append(average).append(" ms");
		view.append("\r\n Shortest request:\t").append(toMs(shortestRequest))
				.append(" ms");

		StringBuilder certainTimeView = view;
		certainTimeView
				.append("\r\n Percentage of the requests served within a certain time (ms)");
		certainTimeView.append("\r\n  50%\t").append(
				toMs(allTimes.get(count_50)));
		certainTimeView.append("\r\n  66%\t").append(
				toMs(allTimes.get(count_66)));
		certainTimeView.append("\r\n  75%\t").append(
				toMs(allTimes.get(count_75)));
		certainTimeView.append("\r\n  80%\t").append(
				toMs(allTimes.get(count_80)));
		certainTimeView.append("\r\n  90%\t").append(
				toMs(allTimes.get(count_90)));
		certainTimeView.append("\r\n  95%\t").append(
				toMs(allTimes.get(count_95)));
		certainTimeView.append("\r\n  98%\t").append(
				toMs(allTimes.get(count_98)));
		certainTimeView.append("\r\n  99%\t").append(
				toMs(allTimes.get(count_99)));
		certainTimeView.append("\r\n 100%\t").append(toMs(longestRequest))
				.append(" (longest request)");

        try {
            writer.write(view.toString());
        } catch (IOException e) {
           log.error("IOException:",e);
        }

    }
    public String format(TestResult result, String serviceName){
        StringWriter sw = new StringWriter();
        format(result, serviceName, sw);
        return sw.toString();
    }
	protected float getAverage(List<Long> allTimes) {
		return TestUtils.getAverage(allTimes);
	}

	protected float toMs(long nm) {
		return TestUtils.toMs(nm);
	}

	protected float toMs(float nm) {
		return TestUtils.toMs(nm);
	}
}
