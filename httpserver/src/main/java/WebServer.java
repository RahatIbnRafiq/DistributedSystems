import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class WebServer {
    private final static String TASK_ENDPOINT = "/task";
    private final static String STATUS_ENDPOINT = "/status";

    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
        System.out.println("Server is listening on port: " + serverPort);
    }

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(this.port),0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        if(headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(),exchange);
            return;
        }

        boolean isDebugMode = false;
        if(headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }

        long startTime = System.nanoTime();


        byte [] requestBytes = readStreamToBytes(exchange.getRequestBody());
        byte [] responseBytes = calculateResponse(requestBytes);

        long endTime = System.nanoTime();

        if (isDebugMode) {
            String debugMessage = String.format("Operation took %d nanoseconds\n", endTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(responseBytes, exchange);
    }

    private byte [] calculateResponse(byte [] requestBytes) {
        String bodyResponse = new String(requestBytes);
        String[] stringNumbers = bodyResponse.split(",");
        BigInteger result = BigInteger.ONE;

        for (String number : stringNumbers) {
            BigInteger bigInteger = new BigInteger(number);
            result = result.multiply(bigInteger);
        }

        return String.format("Result of multiplication is %s\n", result).getBytes();
    }

    private byte[] readStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        String responseBody = "Server is alive!";
        sendResponse(responseBody.getBytes(), exchange);
    }

    private void sendResponse(byte [] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
}
