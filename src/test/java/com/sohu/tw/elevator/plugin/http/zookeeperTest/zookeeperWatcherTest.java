package com.sohu.tw.elevator.plugin.http.zookeeperTest;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: yaqinzhang
 * Date: 12-10-10
 * Time: ����10:25
 * To change this template use File | Settings | File Templates.
 */
public class zookeeperWatcherTest {

    private static String connectString = "goldmine.controlcenter99:2181,goldmine.controlcenter98:2181,goldmine.controlcenter97:2181";
    //            "goldmine.controlcenter99:2181,goldmine.controlcenter98:2181,goldmine.controlcenter97:2181";
    private static int sessionTimeout = 60000;
    protected static ZooKeeper zooKeeper;

    public static void main(String args[]) throws IOException, InterruptedException, KeeperException {
        zooKeeper = new ZooKeeper(connectString, sessionTimeout, null);
        ZooKeeperUtils.waitUntilConnected(zooKeeper);
//        zooKeeper.create(Constants.ZOOKEEPER_TOPICS_PATH + "/zyqTestTopicNode1", "Hello World".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
//        Iterator iterator =zooKeeper.getChildren(Constants.ZOOKEEPER_TOPICS_PATH, true).iterator();

        zooKeeper.delete("/elevator/topics/tw_web_news_testwj", -1);
//        zooKeeper.create("/elevator/topics/test", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        Iterator iterator = zooKeeper.getChildren("/elevator/topics", true).iterator();
        int i = 1;
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString() + " " + i++);
//            zooKeeper.create("/elevator/topics/zyqtest426427" + iterator.next().toString(), "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//            Thread.sleep(5*1000);
//            zooKeeper.delete("/elevator/topics/" + iterator.next().toString(), -1);
        }
//        Thread.sleep(60 * 1000);
        System.exit(1);

    }
}
