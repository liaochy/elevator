package com.sohu.tw.elevator.plugin.http.benchmark;


import java.util.concurrent.ConcurrentLinkedQueue;

public class Benchmark {
    TestEngine testEngine = new TestEngine();
    ResultFormat formater = new ResultFormat();
    static int concurrencyLevel = 1;
    static int totalRequests = 1;
    static long warmUpCount = totalRequests * 2;
    HttpUtil http = new HttpUtil();
    static ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

    static {
        if(System.getProperties().getProperty("con")!=null){
            concurrencyLevel = Integer.parseInt(System.getProperties().getProperty("con"));
        }
        if(System.getProperties().getProperty("req")!=null){
            totalRequests = Integer.parseInt(System.getProperties().getProperty("req"));
        }
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
        TestResult testResult = testEngine.test(concurrencyLevel,
                totalRequests, testService, warmUpCount);
        System.out
                .println("=========================================================");
        System.out.println(name + " test result:");
        System.out.println(formater.format(testResult, ""));
    }

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();
        benchmark.test();
    }
}
