import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class LeaderElection implements Watcher {
    private ZooKeeper zookeeper;
    private static int SESSION_TIMEOUT = 3000;
    private static String ZOOKEEPER_ADDRESS = "localhost:2181";

    public void connectToZookeeper() throws IOException {
        this.zookeeper = new ZooKeeper(LeaderElection.ZOOKEEPER_ADDRESS,
                LeaderElection.SESSION_TIMEOUT,
                this);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to server");
                }
                break;
        }
    }

    public static void main(String[] args) throws IOException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
    }
}
