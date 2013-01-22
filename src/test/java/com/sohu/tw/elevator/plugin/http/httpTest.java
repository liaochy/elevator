package com.sohu.tw.elevator.plugin.http;

import org.junit.Test;


public class httpTest {

    @Test
    public void testPut() throws Exception {
        HttpUtil http = new HttpUtil();
//        String s = "11111123";
//        byte[] response2 = http.putDataAsStream(
//                "http://localhost:9088/logs/sunbo", s.getBytes());
//        System.out.println(new String(response2));

        for (int i = 1; i <= 99; i++) {
            String s =i + "s\n";
            http.putDataAsStream("http://localhost:9088/logs/testzyq1", s.getBytes());
        }
    }

    @Test
    public void testGet() throws Exception {
        HttpUtil http = new HttpUtil();
        long start = System.currentTimeMillis();
        String s = http.requestHttpChunkContent("http://localhosts:9088/logs/sunbo401/aaa?msgCount=10");
        long end = System.currentTimeMillis() - start;
        System.out.println("time = " + end);
    }


}
