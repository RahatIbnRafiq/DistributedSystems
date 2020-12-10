import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Aggregator {

    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workerAddresses, List<String> tasks) {
        CompletableFuture<String> [] futures = new CompletableFuture[workerAddresses.size()];
        for (int i=0; i< workerAddresses.size(); i++) {
            futures[i] = this.webClient.sendTask(workerAddresses.get(i), tasks.get(i).getBytes());
        }
        List<String> results = new ArrayList<>();
        for(int i=0; i< tasks.size(); i++) {
            results.add(futures[i].join());
        }
        return results;
    }
}
