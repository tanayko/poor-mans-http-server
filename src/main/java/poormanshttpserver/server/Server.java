package poormanshttpserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {
    ServerSocket serverSocket;
    Socket clientSocket;
    OutputStream out;
    BufferedReader in;
    boolean running = true;

    private static final Logger logger
            = LoggerFactory.getLogger(Server.class);

    public void create() throws IOException {
        this.serverSocket = new ServerSocket(8888);

        this.addShutdownHook();

        while (isRunning()) {
            this.clientSocket = this.serverSocket.accept();
            logger.debug("Connected with client");
            Thread thread = new Thread(() -> {
                try {
                    this.out = this.clientSocket.getOutputStream();
                    this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                    this.parseClientInput();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        this.cleanUp();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

    }

    private void cleanUp() throws IOException {
        this.in.close();
        this.out.flush();
        this.out.close();
        this.clientSocket.close();
    }

    private void parseClientInput() throws IOException {
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
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            setRunning(false);
            try {
                this.serverSocket.close();
                logger.debug("Server socket closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
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
