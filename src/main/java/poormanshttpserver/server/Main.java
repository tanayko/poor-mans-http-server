package poormanshttpserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;

public class Main {
    private static final Logger logger
            = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.create();
        } catch (IOException e) {
            if (e instanceof SocketException) {
                logger.debug("Server shut down");
            } else {
                e.printStackTrace();
            }
        }
    }
}
