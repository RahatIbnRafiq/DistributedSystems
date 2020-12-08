package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private String currentZNode;
    private static final String REGISTRY_ZNODE = "/service_registry";
    private ZooKeeper zooKeeper;
    private List<String> allServiceAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistryZnode();
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        currentZNode = this.zooKeeper.create(REGISTRY_ZNODE + "/n_", metadata.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to Service Registry");
    }

    public synchronized List<String> getAllServiceAddresses() throws KeeperException, InterruptedException {
        if (allServiceAddresses == null) {
            updateAddresses();
        }
        return allServiceAddresses;
    }

    public void unregisterFromCluster() {
        try {
            if (currentZNode != null && zooKeeper.exists(currentZNode, false) != null) {
                zooKeeper.delete(currentZNode, -1);
            }
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } catch (KeeperException e) {
            System.out.println(e.toString());
        }

    }

    private void createServiceRegistryZnode() {
        try {
            if(zooKeeper.exists(REGISTRY_ZNODE,false) == null) {
                zooKeeper.create(REGISTRY_ZNODE ,new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } catch (KeeperException e) {
            System.out.println(e.toString());
        }
    }

    public void registerForUpdate() {
        try {
            updateAddresses();
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } catch (KeeperException e) {
            System.out.println(e.toString());
        }
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workerNodes = this.zooKeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>(workerNodes.size());
        System.out.println("Inside updateAddresses");

        for(String workerNode : workerNodes) {
            String workerNodefullPath = REGISTRY_ZNODE + "/" + workerNode;
            Stat stat = zooKeeper.exists(workerNodefullPath,false);
            if(stat == null)
                continue;
            byte [] adressBytes = zooKeeper.getData(workerNodefullPath,false,stat);
            String address = new String(adressBytes);
            addresses.add(address);
        }
        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        System.out.println("Cluster addresses are : " + this.allServiceAddresses);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            System.out.println("Inside process of Service Registry");
            updateAddresses();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
