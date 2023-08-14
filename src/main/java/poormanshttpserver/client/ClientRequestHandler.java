package poormanshttpserver.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poormanshttpserver.PathEntry;
import poormanshttpserver.server.Server;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientRequestHandler {
    private final Socket clientSocket;
    private static final Logger logger
            = LoggerFactory.getLogger(ClientRequestHandler.class);
    private ClientRequest clientRequest;
    private OutputStream out;
//    private BufferedReader in;
    private InputStream in;
    private final String[][] httpReplies = {{"100", "Continue"},
            {"101", "Switching Protocols"},
            {"200", "OK"},
            {"201", "Created"},
            {"202", "Accepted"},
            {"203", "Non-Authoritative Information"},
            {"204", "No Content"},
            {"205", "Reset Content"},
            {"206", "Partial Content"},
            {"300", "Multiple Choices"},
            {"301", "Moved Permanently"},
            {"302", "Found"},
            {"303", "See Other"},
            {"304", "Not Modified"},
            {"305", "Use Proxy"},
            {"306", "(Unused)"},
            {"307", "Temporary Redirect"},
            {"400", "Bad Request"},
            {"401", "Unauthorized"},
            {"402", "Payment Required"},
            {"403", "Forbidden"},
            {"404", "Not Found"},
            {"405", "Method Not Allowed"},
            {"406", "Not Acceptable"},
            {"407", "Proxy Authentication Required"},
            {"408", "Request Timeout"},
            {"409", "Conflict"},
            {"410", "Gone"},
            {"411", "Length Required"},
            {"412", "Precondition Failed"},
            {"413", "Request Entity Too Large"},
            {"414", "Request-URI Too Long"},
            {"415", "Unsupported Media Type"},
            {"416", "Requested Range Not Satisfiable"},
            {"417", "Expectation Failed"},
            {"500", "Internal Server Error"},
            {"501", "Not Implemented"},
            {"502", "Bad Gateway"},
            {"503", "Service Unavailable"},
            {"504", "Gateway Timeout"},
            {"505", "HTTP Version Not Supported"}};
    private final Map<String, String> responseCodeToPhrase = Stream.of(this.httpReplies).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public ClientRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run(Map<PathEntry, ClientResponse> pathEntries) throws IOException {
        this.out = this.clientSocket.getOutputStream();
//        this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.in = this.clientSocket.getInputStream();

//        this.parseClientRequest();
        HttpClientRequestParser httpClientRequestParser = new HttpClientRequestParser(this.in);
        this.clientRequest = httpClientRequestParser.run();
        this.sendResponse(pathEntries);

        this.in.close();
        this.out.flush();
        this.out.close();
        this.clientSocket.close();
    }

//    private void parseClientRequest() throws IOException {
//        this.clientRequest = new ClientRequest();
//        this.clientRequest.httpProtocolVersion = Server.HTTP_PROTOCOL_VERSION;
//
////        while (true) {
////            logger.info(in.readLine());
////        }
//
//        String requestLine = in.readLine();
//        this.clientRequest.method = requestLine.split("\\s+")[0];
//        this.clientRequest.path = requestLine.split("\\s+")[1];
//
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) {
//            String[] splitUpLine = inputLine.split(":");
//            this.clientRequest.headers.put(splitUpLine[0].trim(), splitUpLine[1].trim());
//        }
//
//        this.clientRequest.entity = new byte[]{};
//        if (this.clientRequest.headers.get("Content-Type") != null) {
//            String currentBodyLine;
//            while (!(currentBodyLine = in.readLine()).equals("")) {
//                byte[] combined = new byte[this.clientRequest.entity.length + currentBodyLine.getBytes().length];
//                System.arraycopy(this.clientRequest.entity, 0, combined, 0, this.clientRequest.entity.length);
//                System.arraycopy(currentBodyLine.getBytes(), 0, combined, this.clientRequest.entity.length, currentBodyLine.getBytes().length);
//
//                this.clientRequest.entity = combined;
//            }
//        }
//    }

    private void sendResponse(Map<PathEntry, ClientResponse> pathEntries) throws IOException {
        PathEntry pathEntry = new PathEntry(this.clientRequest.method, this.clientRequest.path);
        ClientResponse clientResponse = pathEntries.get(pathEntry);
        if (clientResponse == null) {
            this.out.write((Server.HTTP_PROTOCOL_VERSION + " 404 Not Found\r\n").getBytes(StandardCharsets.US_ASCII));
            this.out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.US_ASCII));
        } else {
            this.out.write((clientResponse.httpProtocolVersion + " " + clientResponse.httpResponseCode + " " + responseCodeToPhrase.get(clientResponse.httpResponseCode) + "\r\n").getBytes(StandardCharsets.US_ASCII));
            this.out.write("Content-Type: text/html\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            if (clientResponse.entity != null) {
                this.out.write(clientResponse.entity);
            }
        }
    }
}
