package poormanshttpserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poormanshttpserver.PathEntry;
import poormanshttpserver.client.ClientRequestHandler;
import poormanshttpserver.client.ClientResponse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {
    ServerSocket serverSocket;
    boolean running = true;
    Map<PathEntry, ClientResponse> pathEntries = new HashMap<>();
    public static String HTTP_PROTOCOL_VERSION = "HTTP/1.1";

    private static final Logger logger
            = LoggerFactory.getLogger(Server.class);

    public void create() throws IOException {
        this.populatePathEntries();

        this.serverSocket = new ServerSocket(8888);

        this.addShutdownHook();

        while (isRunning()) {
            Socket clientSocket = this.serverSocket.accept();
            logger.debug("Connected with client");
            Thread thread = new Thread(() -> {
                ClientRequestHandler handler = new ClientRequestHandler(clientSocket);
                try {
                    handler.run(this.pathEntries);
                } catch (IOException e) {
                    logger.error("Trouble sending response");
                }
            });
            thread.start();
        }
    }

    private void populatePathEntries() {
        PathEntry pathEntry1 = new PathEntry("GET", "/hello");
        ClientResponse clientResponse1 = new ClientResponse();
        clientResponse1.httpResponseCode = "200";
        clientResponse1.httpProtocolVersion = HTTP_PROTOCOL_VERSION;
        clientResponse1.httpHeaders.put("X-My-Header", "hello");
        clientResponse1.entity = "world".getBytes(StandardCharsets.US_ASCII);

        PathEntry pathEntry2 = new PathEntry("GET", "/hellogoodbye");
        ClientResponse clientResponse2 = new ClientResponse();
        clientResponse2.httpResponseCode = "500";
        clientResponse2.httpProtocolVersion = HTTP_PROTOCOL_VERSION;

        PathEntry pathEntry3 = new PathEntry("POST", "/hello");
        ClientResponse clientResponse3 = new ClientResponse();
        clientResponse3.httpResponseCode = "200";
        clientResponse3.httpProtocolVersion = HTTP_PROTOCOL_VERSION;
        clientResponse3.entity = "goodbye".getBytes(StandardCharsets.US_ASCII);

        PathEntry pathEntry4 = new PathEntry("POST", "/hello-goodbye");
        ClientResponse clientResponse4 = new ClientResponse();
        clientResponse4.httpResponseCode = "200";
        clientResponse4.httpProtocolVersion = HTTP_PROTOCOL_VERSION;
        clientResponse4.httpHeaders.put("X-My-Header-1", "hello");
        clientResponse4.httpHeaders.put("X-My-Header-2", "goodbye");
        clientResponse4.entity = "goodbye".getBytes(StandardCharsets.US_ASCII);

        pathEntries.put(pathEntry1, clientResponse1);
        pathEntries.put(pathEntry2, clientResponse2);
        pathEntries.put(pathEntry3, clientResponse3);
        pathEntries.put(pathEntry4, clientResponse4);
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            setRunning(false);
            try {
                this.serverSocket.close();
                logger.debug("Server socket closed");
            } catch (IOException e) {
                logger.error("Trouble shutting down server");
            }
        }));
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
