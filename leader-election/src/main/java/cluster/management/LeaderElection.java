package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
    private ZooKeeper zookeeper;
    private static final String ELECTION_NAMESPACE = "/election";
    private String currentZnodeName;
    private final OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zookeeper, OnElectionCallback onElectionCallback) {
        this.zookeeper = zookeeper;
        this.onElectionCallback = onElectionCallback;

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeDeleted:
                try {
                    electLeader();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
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
                this.onElectionCallback.onElectedToBeLeader();
                return;
            }
            else {
                System.out.println("I am not the leader. " + smallestChild + " is the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zookeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }
        this.onElectionCallback.onWorker();
        System.out.println("I am watching " + predecessorZnodeName);
        System.out.println();
    }
}
