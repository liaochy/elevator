package com.sohu.tw.elevator.plugin.http;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: yaqinzhang
 * Date: 12-12-11
 */
public class Syslog4JTest {
    private InetAddress address;
    private int port;
    private SyslogIF syslogClient;

    public Syslog4JTest(String host, int port, String pType) {
        this.address = null;

        try {
            this.address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.port = port;

        this.syslogClient = Syslog.getInstance(pType);
        this.syslogClient.getConfig().setHost(this.address.getHostAddress());
        this.syslogClient.getConfig().setPort(port);

    }

    public void syslog(String messageToSend) {
        syslogClient.log(Syslog.FACILITY_LOCAL1, messageToSend);

        String hostName = address.getHostName();
        String ip = address.getHostAddress();

        System.out.println("Send log to " + hostName + "(" + ip + "):" + port);
        System.out.println("messageToSend: " + messageToSend);
    }

    public void shutdown() {
        syslogClient.shutdown();
    }

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 514;

        String pType = "udp";

        Syslog4JTest sm = new Syslog4JTest(host, port, pType);

        for (int i = 5; i < 10; i++) {

            String message = "zyqtest17" + "[SOHUClient]" + i +"[123.125.116.193,10.11.155.49],20120516155256->[reply],reply_id=3659254346,msg_id=3658132754,uid=31165687,to_uid=31165687";
            sm.syslog(message);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sm.shutdown();
    }

}
