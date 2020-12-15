package cluster.management;

import org.apache.zookeeper.*;

public class LeaderElection implements Watcher {
    private static final String ELECTION_NAMESPACE = "/election";
    private ZooKeeper zooKeeper;
    private String currentZNodeName;

    LeaderElection(ZooKeeper zookeeper) {
        this.zooKeeper = zookeeper;
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = this.zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name : " + zNodeFullPath);
        this.currentZNodeName = zNodeFullPath.replace(ELECTION_NAMESPACE+"/" , "");
    }
    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
