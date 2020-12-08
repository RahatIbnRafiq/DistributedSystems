import cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import cluster.management.LeaderElection;

import java.io.IOException;

public class Application implements Watcher {
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int DEFAULT_PORT = 8080;
    private ZooKeeper zookeeper;


    public ZooKeeper connectToZookeeper() throws IOException {
        zookeeper = new ZooKeeper(Application.ZOOKEEPER_ADDRESS,
                Application.SESSION_TIMEOUT,
                this);
        return zookeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to server");
                } else {
                    synchronized (zookeeper) {
                        zookeeper.notifyAll();
                        System.out.println("Disconnected from zookeeper");
                    }
                }
                break;
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

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int currentServerPort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Application app = new Application();
        ZooKeeper zooKeeper = app.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);


        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();

        app.run();
        app.close();

        System.out.println("App has disconnected from zookeeper server. Exiting now.");

    }
}
