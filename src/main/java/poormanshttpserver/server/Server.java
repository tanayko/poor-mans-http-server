package poormanshttpserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poormanshttpserver.client.ClientRequestHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket serverSocket;
    boolean running = true;

    private static final Logger logger
            = LoggerFactory.getLogger(Server.class);

    public void create() throws IOException {
        this.serverSocket = new ServerSocket(8888);

        this.addShutdownHook();

        while (isRunning()) {
            Socket clientSocket = this.serverSocket.accept();
            logger.debug("Connected with client");
            Thread thread = new Thread(() -> {
                ClientRequestHandler handler = new ClientRequestHandler(clientSocket);
                try {
                    handler.run();
                } catch (IOException e) {
                    logger.error("Trouble sending response");
                }
            });
            thread.start();
        }
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
