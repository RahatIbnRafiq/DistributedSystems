package search;

import com.google.protobuf.InvalidProtocolBufferException;
import constants.Constants;
import model.proto.SearchModel;
import networking.OnRequestCallback;

public class SearchCoordinator implements OnRequestCallback {
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

    @Override
    public String getEndpoint() {
        return Constants.SEARCH_ENDPOINT;
    }
}
