package com.sohu.tw.elevator.plugin.http.myTest;

import com.sohu.tw.elevator.plugin.http.HttpUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Benchmark {
    TestEngine testEngine = new TestEngine();
    ResultFormat formater = new ResultFormat();
    static int concurrencyLevel = 100;
    static int totalRequests = 1000;
    static long warmUpCount = totalRequests * 2;
    HttpUtil http = new HttpUtil();
    static ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

    static {
        Long count = 1L;
        while (count <= totalRequests) {
            queue.add(count);
            count++;
        }
    }

    public void test() throws Exception {
        TestService testService = new TestService() {
            @Override
            public Object test() throws Exception {
                return http.requestHttpChunkContent("http://localhost:9088/logs/sohuclient/" + queue.poll() + "?msgCount=20");
            }
        };
        loadTest("elevator Get:chunk  benchmark", testService);
    }

    protected void loadTest(String name, TestService testService)
            throws Exception {

//        Object result = testService.test();

        TestResult testResult = testEngine.test(concurrencyLevel,
                totalRequests, testService, warmUpCount);
        System.out
                .println("=========================================================");
        System.out.println(name + " test result:");
//        System.out.println(" Service result:" + result);
        System.out.println(formater.format(testResult, ""));
    }

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();
        benchmark.test();
    }
}
