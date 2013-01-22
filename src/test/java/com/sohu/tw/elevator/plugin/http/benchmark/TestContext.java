package com.sohu.tw.elevator.plugin.http.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */
public class TestContext {

	private int everyThreadCount;
	private CyclicBarrier threadStartBarrier;
	private CountDownLatch threadEndLatch;
	private AtomicInteger failedCounter;

	private TestService testService;

	public int getEveryThreadCount() {
		return everyThreadCount;
	}

	public void setEveryThreadCount(int everyThreadCount) {
		this.everyThreadCount = everyThreadCount;
	}

	public CyclicBarrier getThreadStartBarrier() {
		return threadStartBarrier;
	}

	public void setThreadStartBarrier(CyclicBarrier threadStartBarrier) {
		this.threadStartBarrier = threadStartBarrier;
	}

	public CountDownLatch getThreadEndLatch() {
		return threadEndLatch;
	}

	public void setThreadEndLatch(CountDownLatch threadEndLatch) {
		this.threadEndLatch = threadEndLatch;
	}

	public AtomicInteger getFailedCounter() {
		return failedCounter;
	}

	public void setFailedCounter(AtomicInteger failedCounter) {
		this.failedCounter = failedCounter;
	}

	public TestService getTestService() {
		return testService;
	}

	public void setTestService(TestService testService) {
		this.testService = testService;
	}

}

