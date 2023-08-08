package poormanshttpserver.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ClientRequestHandler {
    private final Socket clientSocket;
    private static final Logger logger
            = LoggerFactory.getLogger(ClientRequestHandler.class);
    private ClientRequest clientRequest;
    private OutputStream out;
    private BufferedReader in;

    public ClientRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() throws IOException {
        this.out = this.clientSocket.getOutputStream();
        this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

        this.parseClientRequest();
        this.sendResponse();

        this.in.close();
        this.out.flush();
        this.out.close();
        this.clientSocket.close();
    }

    private void parseClientRequest() throws IOException {
        this.clientRequest = new ClientRequest();
        this.clientRequest.protocolVersion = "HTTP/1.1";

        String requestLine = in.readLine();
        this.clientRequest.method = requestLine.split("\\s+")[0];
        this.clientRequest.path = requestLine.split("\\s+")[1];

        String inputLine;
        while (!(inputLine = in.readLine()).equals("")) {
            String[] splitUpLine = inputLine.split(":");
            this.clientRequest.headers.put(splitUpLine[0].trim(), splitUpLine[1].trim());
        }
    }

    private void sendResponse() throws IOException {
        if (this.clientRequest.method.equals("GET")
                && this.clientRequest.path.equals("/helloworld")
                && this.clientRequest.headers.get("X-My-Header").equals("hello")) {
            logger.debug("200 response");
            this.out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.US_ASCII));
            this.out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
        } else {
            logger.debug("500 response");
            this.out.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes(StandardCharsets.US_ASCII));
            this.out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
        }
    }
}
