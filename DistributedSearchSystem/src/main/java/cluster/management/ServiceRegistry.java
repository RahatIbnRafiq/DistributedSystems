package cluster.management;

import org.apache.zookeeper.*;

public class ServiceRegistry {
    private ZooKeeper zooKeeper;
    private String serviceRegistryZnode;
    private String currentZnode = null;

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
}
