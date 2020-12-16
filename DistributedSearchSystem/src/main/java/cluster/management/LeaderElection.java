package cluster.management;

import constants.Constants;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
    private ZooKeeper zooKeeper;
    private String currentZNodeName;
    private OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zookeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zookeeper;
        this.onElectionCallback = onElectionCallback;
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String zNodePrefix = Constants.ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = this.zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name : " + zNodeFullPath);
        this.currentZNodeName = zNodeFullPath.replace(Constants.ELECTION_NAMESPACE+"/" , "");
    }

    public void relectLeader() throws KeeperException, InterruptedException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";
        while(predecessorStat == null) {
            List<String> children = this.zooKeeper.getChildren(Constants.ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (smallestChild.equals(currentZNodeName)) {
                System.out.println("I am the leader");
                onElectionCallback.onElectedToBeLeader();
                return;
            } else {
                System.out.println("I am not the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZNodeName) -1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(Constants.ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }
        onElectionCallback.onworker();
        System.out.println("Watching znode " + predecessorZnodeName);
        System.out.println();

    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeDeleted:
                try {
                    relectLeader();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

    }
}
