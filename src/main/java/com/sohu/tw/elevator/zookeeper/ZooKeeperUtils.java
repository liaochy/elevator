package com.sohu.tw.elevator.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

import java.util.concurrent.CountDownLatch;

/**
 * author:yaqinzhang
 */
public class ZooKeeperUtils {

	public static void waitUntilConnected(ZooKeeper zooKeeper) {
		CountDownLatch connectedLatch = new CountDownLatch(1);
		Watcher watcher = new ConnectedWatcher(connectedLatch);
		zooKeeper.register(watcher);
		if (States.CONNECTING == zooKeeper.getState()) {
			try {
				connectedLatch.await();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	static class ConnectedWatcher implements Watcher {

		private CountDownLatch connectedLatch;

		ConnectedWatcher(CountDownLatch connectedLatch) {
			this.connectedLatch = connectedLatch;
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				connectedLatch.countDown();
			}
		}
	}
}
