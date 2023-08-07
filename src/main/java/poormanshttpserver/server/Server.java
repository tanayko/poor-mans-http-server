package poormanshttpserver.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    ServerSocket serverSocket;
    Socket clientSocket;
    OutputStream out;
    BufferedReader in;

    public void create() throws IOException {
        this.serverSocket = new ServerSocket(8888);
        this.clientSocket = this.serverSocket.accept();
        this.out = this.clientSocket.getOutputStream();
        this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

        String requestLine = in.readLine();
        String httpMethod = requestLine.split("\\s+")[0];
        String httpUri = requestLine.split("\\s+")[1];

        String inputLine;
        Map<String, String> headers = new HashMap<>();
        while (!(inputLine = in.readLine()).equals("")) {
            String[] splitUpLine = inputLine.split(":");
            headers.put(splitUpLine[0], splitUpLine[1]);
        }

        if (httpMethod.equals("GET")
        && httpUri.equals("/helloworld")
        && headers.get("X-My-Header").equals(" hello")) {
            this.out.write("HTTP/1.1 200 OK\r\n".getBytes());
            this.out.write("Content-Type: text/html\r\n".getBytes());
        } else {
            this.out.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes());
            this.out.write("Content-Type: text/html\r\n".getBytes());
        }

        this.in.close();
        this.out.flush();
        this.out.close();
        this.serverSocket.close();
        this.clientSocket.close();
    }
}
