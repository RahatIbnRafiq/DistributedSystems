package search;

import constants.Constants;
import model.Result;
import model.Task;
import networking.OnRequestCallback;
import utilities.SerializationUtils;

public class SearchWorker implements OnRequestCallback {
    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        Result result = createResult(task);
        return SerializationUtils.serialize(result);
    }

    @Override
    public String getEndpoint() {
        return Constants.WORKER_TASK_ENDPOINT;
    }
}
