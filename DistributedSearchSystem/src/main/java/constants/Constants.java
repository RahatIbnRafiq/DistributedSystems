package constants;

public class Constants {
    public static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    public static final int SESSION_TIMEOUT = 3000;
    public static final String ELECTION_NAMESPACE = "/election";

    public static final String WORKERS_REGISTRY_ZNODE = "/workers_service_registry";
    public static final String STATUS_ENDPOINT = "/status";
    public static final String WORKER_TASK_ENDPOINT = "/task";
    public static final String SEARCH_ENDPOINT = "/search";
    public static final String BOOKS_DIRECTORY = "./resources/books/";
    public static final String COORDINATORS_REGISTRY_ZNODE = "/coordinators_service_registry";
}
