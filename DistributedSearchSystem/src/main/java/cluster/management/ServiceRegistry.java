package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private ZooKeeper zooKeeper;
    private String serviceRegistryZnode;
    private String currentZnode = null;
    private List<String> allServiceAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceRegistryZnode) {
        this.zooKeeper = zooKeeper;
        this.serviceRegistryZnode = serviceRegistryZnode;
        createServiceRegistryNode();
    }

    private void createServiceRegistryNode() {
        try {
            if(zooKeeper.exists(this.serviceRegistryZnode, false) == null) {
                this.zooKeeper.create(this.serviceRegistryZnode,new byte [] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        if(this.currentZnode != null) {
            System.out.println("Already registered to the service registry");
            return;
        }
        this.currentZnode = zooKeeper.create(this.serviceRegistryZnode + "/n_",
                metadata.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to the service registry");
    }

    public void unregisterFromcluster() {
        try {
            if(this.currentZnode != null && zooKeeper.exists(this.currentZnode, false) != null) {
                zooKeeper.delete(this.currentZnode, -1);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workers = zooKeeper.getChildren(serviceRegistryZnode, true);
        List<String> addresses = new ArrayList<>(workers.size());

        for(String worker : workers) {
            String serviceFullPath = this.serviceRegistryZnode + "/" + worker;
            Stat stat = zooKeeper.exists(serviceFullPath, false);
            if(stat == null)
                continue;
            byte[] addressBytes = zooKeeper.getData(serviceFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }
        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        System.out.println("The cluster addresses are: " + this.allServiceAddresses);
    }

    public void registerForUpdates() {
        try {
            updateAddresses();
        } catch (KeeperException e) {
        } catch (InterruptedException e) {
        }
    }

    public synchronized List<String> getAllServiceAddresses() throws KeeperException, InterruptedException {
        if (allServiceAddresses == null) {
            updateAddresses();
        }
        return allServiceAddresses;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
