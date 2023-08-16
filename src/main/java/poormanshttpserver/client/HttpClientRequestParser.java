package poormanshttpserver.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpClientRequestParser {
    ByteArrayOutputStream dataReadSoFar = new ByteArrayOutputStream();
    ByteArrayOutputStream requestLineBuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream headersBuffer = new ByteArrayOutputStream();
    ByteArrayOutputStream entityBuffer = new ByteArrayOutputStream();
    private final InputStream in;
    private final int bufferSize = 1;
    private final byte[] buffer = new byte[bufferSize];

    private boolean requestLineDone = false;
    private boolean headersDone = false;
    private String requestLine;
    private final List<String> headers = new ArrayList<>();
    private final ClientRequest clientRequest = new ClientRequest();
    public HttpClientRequestParser(InputStream in) {
        this.in = in;
    }

    public ClientRequest run() throws IOException {
        // continuously read input until headers are completely read
        int i = 0;
        while (!headersDone || i == bufferSize) {
            i = this.in.read(this.buffer);
            if (i != -1) {
                dataReadSoFar.write(this.buffer);
                // process input to update buffer and check what has been inputted
                this.processCurrentBuffer();
            }
        }

        // parse request line and headers
        this.parseRequestLine(this.requestLine);
        this.parseHeaders(this.headers);

        // check if there is an entity body and handle it if there is
        if (this.clientRequest.headers.get("Content-Type") != null) {
            // create new buffer
            int entityLength = Integer.parseInt(this.clientRequest.headers.get("Content-Length"));

            byte[] bufferBytes = dataReadSoFar.toString(StandardCharsets.US_ASCII).substring(dataReadSoFar.toString().indexOf("\r\n\r\n") + 4).getBytes(StandardCharsets.US_ASCII);
            this.entityBuffer.write(bufferBytes);

            this.clientRequest.entity = new byte[entityLength];
            System.arraycopy(this.entityBuffer.toByteArray(), 0, this.clientRequest.entity, 0, entityLength);
        }

        return this.clientRequest;
    }

    private void processCurrentBuffer() throws IOException {
        // if request line is finished being inputted, get the request line and update buffer
        if (!this.requestLineDone) {
            this.fillRequestLine();
        }

        // if headers are finished being inputted, get the headers and update buffer
        if (this.requestLineDone && !this.headersDone) {
            this.fillHeaders();
        }
    }

    // parse request line
    private void parseRequestLine(String requestLine) {
        String[] parts = requestLine.split("\\s+");
        if (parts.length == 3) {
            this.clientRequest.method = parts[0];
            this.clientRequest.path = parts[1];
            this.clientRequest.httpProtocolVersion = parts[2];
        }
    }

    // parse the headers
    private void parseHeaders(List<String> headers) {
        for (String header : headers) {
            String[] splitUpHeader = header.split(":");
            this.clientRequest.headers.put(splitUpHeader[0].trim(), splitUpHeader[1].trim());
        }
    }

    private void fillRequestLine() throws IOException {
        requestLineBuffer.reset();
        requestLineBuffer.write(dataReadSoFar.toByteArray());
        String[] bufferCurrent = dataReadSoFar.toString().split("\r\n");

        if (bufferCurrent.length > 1) {
            // update headers buffer
            String newBuffer = dataReadSoFar.toString().substring(bufferCurrent[0].length() + 2);
            headersBuffer.write(newBuffer.getBytes(StandardCharsets.US_ASCII));
            requestLineDone = true;
            requestLine = bufferCurrent[0];
        }
    }

    private void fillHeaders() {
        headersBuffer.reset();
        String dataReadSoFarAsString = dataReadSoFar.toString().split("\r\n\r\n")[0];
        String headersAsString = dataReadSoFarAsString.substring(dataReadSoFarAsString.indexOf("\r\n") + 2);
        String[] bufferCurrent = headersAsString.split("\r\n");

        // if two CRLFs found, get all remaining headers and update buffer to after CRLFs
        // if not, get all complete headers and update buffer to incomplete header
        if (dataReadSoFar.toString().contains("\r\n\r\n")) {
            this.headersDone = true;
            System.out.println(this.headers);
            this.headers.addAll(Arrays.asList(bufferCurrent).subList(headers.size(), bufferCurrent.length));
        } else if (bufferCurrent.length > 1) {
            this.headers.addAll(Arrays.asList(bufferCurrent).subList(headers.size(), bufferCurrent.length - 1));
        }
    }
}
