package poormanshttpserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public void create() throws IOException {
        socket = new Socket("127.0.0.1", 8080);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
