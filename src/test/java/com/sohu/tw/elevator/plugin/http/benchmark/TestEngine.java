package com.sohu.tw.elevator.plugin.http.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:38
 * To change this template use File | Settings | File Templates.
 */
public class TestEngine {
	private static Log log = LogFactory.getLog(TestEngine.class);

	// warm up
	protected void warmUp(long warmUpCount, TestService testervice) {
		long limit = 1L;
		while (limit <= warmUpCount) {
			try {
                limit++;
				testervice.test();
//				benchmarkWorker.doRun();
			} catch (Exception e) {
				log.error("Test exception", e);
			}
		}
	}

	public TestResult test(int concurrencyLevel, int totalRequests, TestService testService,long warmUpCount) {
//		warmUp(warmUpCount, testService);
		int everyThreadCount = totalRequests / concurrencyLevel;
		CyclicBarrier threadStartBarrier = new CyclicBarrier(concurrencyLevel);
		CountDownLatch threadEndLatch = new CountDownLatch(concurrencyLevel);
		AtomicInteger failedCounter = new AtomicInteger();

		TestContext testContext = new TestContext();
		testContext.setTestService(testService);
		testContext.setEveryThreadCount(everyThreadCount);
		testContext.setThreadStartBarrier(threadStartBarrier);
		testContext.setThreadEndLatch(threadEndLatch);
		testContext.setFailedCounter(failedCounter);

		ExecutorService executorService = Executors
				.newFixedThreadPool(concurrencyLevel);

		List<TestThreadWorker> workers = new ArrayList<TestThreadWorker>(
				concurrencyLevel);
		for (int i = 0; i < concurrencyLevel; i++) {
			TestThreadWorker worker = new TestThreadWorker(testContext,
					everyThreadCount);
			workers.add(worker);
		}

		// long start = System.nanoTime();
		for (int i = 0; i < concurrencyLevel; i++) {
			TestThreadWorker worker = workers.get(i);
			executorService.submit(worker);
		}

		try {
			threadEndLatch.await();
		} catch (InterruptedException e) {
			log.error("InterruptedException", e);
		}

		executorService.shutdown();

		// long limit = end - start;s
		// long startLimit = testContext.getStartTime() - start;
		int realTotalRequests = everyThreadCount * concurrencyLevel;
		int failedRequests = failedCounter.get();
		TestResult result = new TestResult();

		SortResult sortResult = getSortedTimes(workers);
		List<Long> allTimes = sortResult.allTimes;

		result.setAllTimes(allTimes);
		List<Long> trheadTimes = sortResult.trheadTimes;
		long totalTime = trheadTimes.get(trheadTimes.size() - 1);

		result.setTestsTakenTime(totalTime);
		result.setFailedRequests(failedRequests);
		result.setTotalRequests(realTotalRequests);
		result.setConcurrencyLevel(concurrencyLevel);
		result.setWorkers(workers);

		return result;

	}

	protected SortResult getSortedTimes(List<TestThreadWorker> workers) {
		List<Long> allTimes = new ArrayList<Long>();
		List<Long> trheadTimes = new ArrayList<Long>();
		for (TestThreadWorker worker : workers) {
			List<Long> everyWorkerTimes = worker.getEveryTimes();

			long workerTotalTime = TestUtils.getTotal(everyWorkerTimes);
			trheadTimes.add(workerTotalTime);

			for (Long time : everyWorkerTimes) {
				allTimes.add(time);
			}
		}
		Collections.sort(allTimes);
		Collections.sort(trheadTimes);
		SortResult result = new SortResult();
		result.allTimes = allTimes;
		result.trheadTimes = trheadTimes;
		return result;
	}

	class SortResult {
		List<Long> allTimes;
		List<Long> trheadTimes;
	}

}

