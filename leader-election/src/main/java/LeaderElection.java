import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
    private ZooKeeper zookeeper;
    private static int SESSION_TIMEOUT = 3000;
    private static String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final String ELECTION_NAMESPACE = "/election";
    private String currentZnodeName;

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
                } else {
                    synchronized (zookeeper) {
                        zookeeper.notifyAll();
                        System.out.println("Disconnected from zookeeper");
                    }
                }
                break;
            case NodeDeleted:
                try {
                    electLeader();
                } catch (Exception e) {
                }
        }
    }

    public void close() throws InterruptedException {
        zookeeper.close();
    }

    public void run() throws InterruptedException {
        synchronized (zookeeper) {
            zookeeper.wait();
        }
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zookeeper.create(znodePrefix,new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("znode name : " + znodeFullPath);

        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/" , "");

    }

    public void electLeader() throws KeeperException, InterruptedException {
        String predecessorZnodeName = "";
        Stat predecessorStat = null;

        while(predecessorStat == null) {
            List<String> children = zookeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);

            if(smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader");
                return;
            }
            else {
                System.out.println("I am not the leader. " + smallestChild + " is the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zookeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }
        System.out.println("I am watching " + predecessorZnodeName);
        System.out.println();
    }



    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from zk server. Exiting the application");
    }
}
