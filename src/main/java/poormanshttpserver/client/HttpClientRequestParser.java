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

        while (!headersDone) {
            this.in.read(this.buffer);
            stringBuffer.append(new String(this.buffer));
            stringBuffer = this.processCurrentBuffer(stringBuffer);
        }

        this.parseRequestLine(this.requestLine);
        this.parseHeaders(this.headers);

        if (this.clientRequest.headers.get("Content-Type") != null) {
            int bufferLength = Integer.parseInt(this.clientRequest.headers.get("Content-Length"));
            byte[] entityBuffer = new byte[bufferLength];
            byte[] currentDataInBuffer = stringBuffer.toString().getBytes(StandardCharsets.US_ASCII);

            int index = 0;
            for (int i = 0; i < currentDataInBuffer.length; i++) {
                if (currentDataInBuffer[i] == 0) {
                    index = i;
                    break;
                }
            }

            System.arraycopy(currentDataInBuffer, 0, entityBuffer, 0, index);

            while (entityBuffer[bufferLength-1] == 0) {
                this.in.read(entityBuffer);
            }

            this.clientRequest.entity = entityBuffer;
        }

//        InputStreamReader reader = new InputStreamReader(this.in);
//        String stringBuffer;
//        while (this.read(this.buffer) != -1) {
//            if (!inputLine.equals("")) {
//                stringBuffer.append(inputLine).append("\r\n");
//            } else {
//                this.processCurrentBuffer(stringBuffer);
//            }
//        }


//        this.buffer = new byte[1024];
//
//        while(this.in.read(this.buffer) != -1) {
//            this.processCurrentBuffer();
//        };

        return this.clientRequest;
    }

    private void parseHeaders(List<String> headers) {
        for (String header : headers) {
            String[] splitUpHeader = header.split(":");
            this.clientRequest.headers.put(splitUpHeader[0].trim(), splitUpHeader[1].trim());
        }
    }

    private StringBuilder processCurrentBuffer(StringBuilder buffer) throws IOException {
        if (!this.requestLineDone && !(buffer.length() == 0)) {
            buffer = this.fillRequestLine(buffer);
        }

        if (this.requestLineDone && !this.headersDone && !(buffer.length() == 0)) {
            buffer = this.fillHeaders(buffer);
        }

        return buffer;
    }

    private void fillEntityMessage() {
        this.entityMessageBody += this.buffer;
        if (this.buffer.equals("")) {
            this.entityDone = true;
        }
    }

    private void parseRequestLine(String requestLine) {
        String[] parts = requestLine.split("\\s+");
        if (parts.length == 3) {
            this.clientRequest.method = parts[0];
            this.clientRequest.path = parts[1];
            this.clientRequest.httpProtocolVersion = parts[2];
        }
    }

    private StringBuilder fillRequestLine(StringBuilder buffer) throws IOException {
        String[] bufferCurrent = buffer.toString().split("\r\n");

        if (bufferCurrent.length > 1) {
            buffer = new StringBuilder(buffer.substring(bufferCurrent[0].length() + 2));
            this.requestLineDone = true;
            this.requestLine = bufferCurrent[0];
        }

        return buffer;
    }

    private StringBuilder fillHeaders(StringBuilder buffer) {
        String[] bufferCurrent = buffer.toString().split("\r\n");

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
