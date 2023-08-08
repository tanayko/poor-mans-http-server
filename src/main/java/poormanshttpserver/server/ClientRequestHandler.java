package poormanshttpserver.server;

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

    public ClientRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() throws IOException {
        OutputStream out = this.clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

        String requestLine = in.readLine();
        String httpMethod = requestLine.split("\\s+")[0];
        String httpUri = requestLine.split("\\s+")[1];

        String inputLine;
        Map<String, String> headers = new HashMap<>();
        while (!(inputLine = in.readLine()).equals("")) {
            String[] splitUpLine = inputLine.split(":");
            headers.put(splitUpLine[0].trim(), splitUpLine[1].trim());
        }

        if (httpMethod.equals("GET")
                && httpUri.equals("/helloworld")
                && headers.get("X-My-Header").equals("hello")) {
            logger.debug("200 response");
            out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.US_ASCII));
            out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
        } else {
            logger.debug("500 response");
            out.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes(StandardCharsets.US_ASCII));
            out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
        }

        in.close();
        out.flush();
        out.close();
        this.clientSocket.close();
    }
}
