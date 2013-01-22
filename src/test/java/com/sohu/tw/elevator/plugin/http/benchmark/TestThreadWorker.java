package com.sohu.tw.elevator.plugin.http.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:39
 * To change this template use File | Settings | File Templates.
 */
class TestThreadWorker implements Runnable {

	private TestService service;
	private CyclicBarrier threadStartBarrier;
	private CountDownLatch threadEndLatch;
	private AtomicInteger failedCounter = null;
	private int count;
	private static Log log = LogFactory.getLog(TestThreadWorker.class);

	private List<Long> everyTimes;

	public TestThreadWorker(TestContext testContext, int count) {
		super();
		this.threadStartBarrier = testContext.getThreadStartBarrier();
		this.threadEndLatch = testContext.getThreadEndLatch();
		this.failedCounter = testContext.getFailedCounter();
		this.count = count;

		everyTimes = new ArrayList<Long>(count);

		this.service = testContext.getTestService();
	}

	public List<Long> getEveryTimes() {
		return everyTimes;
	}

	@Override
	public void run() {
		try {
			threadStartBarrier.await();
			doRun();
		} catch (Exception e) {
			log.error("Test exception", e);
		}
	}

	protected void doRun() throws Exception {
		for (int i = 0; i < count; i++) {
			long start = System.nanoTime();
			try {
				// Object result = service.test();
				service.test();
			} catch (Throwable e) {
				failedCounter.incrementAndGet();
				// throw e;
			} finally {
				long stop = System.nanoTime();
				long limit = stop - start;
				everyTimes.add(limit);
			}
		}
		threadEndLatch.countDown();
	}

}
