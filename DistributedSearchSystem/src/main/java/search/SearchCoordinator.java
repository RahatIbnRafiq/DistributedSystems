package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import constants.Constants;
import model.Result;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequestCallback;
import org.apache.zookeeper.KeeperException;
import utilities.TFIDF;

import java.util.List;

public class SearchCoordinator implements OnRequestCallback {
    private final ServiceRegistry workersServiceRegistry;

    public SearchCoordinator(ServiceRegistry workersServiceRegistry) {
        this.workersServiceRegistry = workersServiceRegistry;
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        SearchModel.Request request = null;
        try {
            request = SearchModel.Request.parseFrom(requestPayload);
            SearchModel.Response response = createResponse(request);
            return response.toByteArray();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return SearchModel.Response.getDefaultInstance().toByteArray();
        }

    }

    private SearchModel.Response createResponse(SearchModel.Request searchRequest) throws KeeperException, InterruptedException {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder();
        System.out.println("Received search query: " + searchRequest.getSearchQuery());

        List<String> searchTerms = TFIDF.getWordsFromLine(searchRequest.getSearchQuery());
        List<String> workers = workersServiceRegistry.getAllServiceAddresses();

        if (workers.isEmpty()) {
            System.out.println("No search workers currently available");
            return searchResponse.build();
        }
        List<Task> tasks = createTasks(workers.size(), searchTerms);
        List<Result> results = sendTasksToWorkers(workers, tasks);

        List<SearchModel.Response.DocumentStats> sortedDocuments = aggregateResults(results, searchTerms);
        searchResponse.addAllRelevantDocuments(sortedDocuments);
        return searchResponse.build();
    }

    @Override
    public String getEndpoint() {
        return Constants.SEARCH_ENDPOINT;
    }
}
