import cluster.management.LeaderElection;
import cluster.management.ServiceRegistry;
import constants.Constants;
import org.apache.zookeeper.*;

import java.io.IOException;

public class Application implements Watcher {
    private ZooKeeper zookeeper;
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int currentServerPort = 8080;
        if (args.length == 1) {
            currentServerPort = Integer.parseInt(args[0]);
        }
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper,Constants.WORKERS_REGISTRY_ZNODE);
        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.relectLeader();

        application.run();
        application.close();
        System.out.println("Disconnected from Zookeeper, exiting application");

    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zookeeper = new ZooKeeper(Constants.ZOOKEEPER_ADDRESS,Constants.SESSION_TIMEOUT,this);
        return this.zookeeper;
    }

    public void run() throws InterruptedException {
        synchronized (this.zookeeper) {
            zookeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zookeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zookeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zookeeper.notifyAll();
                    }
                }
        }
    }
}
