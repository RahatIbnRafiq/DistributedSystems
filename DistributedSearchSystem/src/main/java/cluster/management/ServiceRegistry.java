package cluster.management;

import org.apache.zookeeper.ZooKeeper;

public class ServiceRegistry {
    private ZooKeeper zooKeeper;
    private String serviceRegistryZnode;

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceRegistryZnode) {
        this.zooKeeper = zooKeeper;
        this.serviceRegistryZnode = serviceRegistryZnode;
    }
}
