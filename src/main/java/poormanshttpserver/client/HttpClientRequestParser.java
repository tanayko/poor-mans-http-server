package poormanshttpserver.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpClientRequestParser {
    private final InputStream in;
    private final byte[] buffer = new byte[1024];

    private boolean requestLineDone = false;
    private boolean headersDone = false;
    private boolean entityDone = false;

    private String requestLine;
    private final List<String> headers = new ArrayList<>();
    private String entityMessageBody = "";

    private final ClientRequest clientRequest = new ClientRequest();
    public HttpClientRequestParser(InputStream in) {
        this.in = in;
    }

    public ClientRequest run() throws IOException {
        StringBuilder stringBuffer = new StringBuilder();

        // continuously read input until headers are completely read
        while (!headersDone) {
            this.in.read(this.buffer);
            stringBuffer.append(new String(this.buffer));

            // process input to update buffer and check what has been inputted
            stringBuffer = this.processCurrentBuffer(stringBuffer);
        }

        // parse request line and headers
        this.parseRequestLine(this.requestLine);
        this.parseHeaders(this.headers);

        // check if there is an entity body and handle it if there is
        if (this.clientRequest.headers.get("Content-Type") != null) {
            // create new buffer
            int bufferLength = Integer.parseInt(this.clientRequest.headers.get("Content-Length"));
            byte[] entityBuffer = new byte[bufferLength];

            // get unused data from old buffer
            byte[] currentDataInBuffer = stringBuffer.toString().getBytes(StandardCharsets.US_ASCII);

            // check how much data was in old buffer
            int index = 0;
            for (int i = 0; i < currentDataInBuffer.length; i++) {
                if (currentDataInBuffer[i] == 0) {
                    index = i;
                    break;
                }
            }

            // add old data to new buffer
            System.arraycopy(currentDataInBuffer, 0, entityBuffer, 0, index);

            // continuously read input until new buffer is full
            while (entityBuffer[bufferLength-1] == 0) {
                this.in.read(entityBuffer);
            }

            this.clientRequest.entity = entityBuffer;
        }

        return this.clientRequest;
    }

    private StringBuilder processCurrentBuffer(StringBuilder buffer) throws IOException {
        // if request line is finished being inputted, get the request line and update buffer
        if (!this.requestLineDone && !(buffer.length() == 0)) {
            buffer = this.fillRequestLine(buffer);
        }

        // if headers are finished being inputted, get the headers and update buffer
        if (this.requestLineDone && !this.headersDone && !(buffer.length() == 0)) {
            buffer = this.fillHeaders(buffer);
        }

        return buffer;
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

    private StringBuilder fillRequestLine(StringBuilder buffer) throws IOException {
        String[] bufferCurrent = buffer.toString().split("\r\n");

        if (bufferCurrent.length > 1) {
            // update buffer
            buffer = new StringBuilder(buffer.substring(bufferCurrent[0].length() + 2));
            this.requestLineDone = true;
            this.requestLine = bufferCurrent[0];
        }

        return buffer;
    }

    private StringBuilder fillHeaders(StringBuilder buffer) {
        String[] bufferCurrent = buffer.toString().split("\r\n");

        // if two CRLFs found, get all remaining headers and update buffer to after CRLFs
        // if not, get all complete headers and update buffer to incomplete header
        if (buffer.toString().contains("\r\n\r\n")) {
            this.headersDone = true;
            this.headers.addAll(Arrays.asList(bufferCurrent).subList(0, bufferCurrent.length - 2));
            buffer = new StringBuilder(buffer.substring(buffer.indexOf("\r\n\r\n") + 4));
        } else if (bufferCurrent.length > 1) {
            this.headers.addAll(Arrays.asList(bufferCurrent).subList(0, bufferCurrent.length - 1));
            buffer = new StringBuilder(buffer.substring(0, buffer.indexOf(bufferCurrent[bufferCurrent.length - 1])));
        }

        return buffer;
    }
}
