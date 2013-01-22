package com.sohu.tw.elevator.plugin.http.benchmark;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-25
 * Time: 上午11:39
 * To change this template use File | Settings | File Templates.
 */
public class TestResult {

	private int concurrencyLevel;// �����߳���
	private int totalRequests;// ���������

	private long testsTakenTime;// �ܺ�ʱ
	private int failedRequests;// ʧ���������

	private List<Long> allTimes;// ÿ������ĺ�ʱ

	private List<TestThreadWorker> workers;

	public long getTestsTakenTime() {
		return testsTakenTime;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public int getTotalRequests() {
		return totalRequests;
	}

	public void setTotalRequests(int totalRequests) {
		this.totalRequests = totalRequests;
	}

	public void setTestsTakenTime(long testsTakenTime) {
		this.testsTakenTime = testsTakenTime;
	}

	public int getFailedRequests() {
		return failedRequests;
	}

	public void setFailedRequests(int failedRequests) {
		this.failedRequests = failedRequests;
	}

	public List<Long> getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(List<Long> allTimes) {
		this.allTimes = allTimes;
	}

	public List<TestThreadWorker> getWorkers() {
		return workers;
	}

	public void setWorkers(List<TestThreadWorker> workers) {
		this.workers = workers;
	}

}

