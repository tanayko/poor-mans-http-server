import org.junit.jupiter.api.Test;
import poormanshttpserver.client.ClientRequest;
import poormanshttpserver.client.HttpClientRequestParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestBufferTest {
    @Test
    public void test() throws IOException {
        String request = "POST /hello HTTP/1.1\r\nHeader1: value1\r\nContent-Type: text/html\r\nContent-Length: 9\r\n\r\nbody data";
        final SlowStream stream = new SlowStream(request.getBytes(StandardCharsets.US_ASCII));
        stream.delay = 100L;
        HttpClientRequestParser parser = new HttpClientRequestParser(stream);
        ClientRequest clientRequest = parser.run();

        assertEquals(clientRequest.path, "/hello");
        assertEquals(clientRequest.httpProtocolVersion, "HTTP/1.1");
        assertEquals(clientRequest.method, "POST");

        assertEquals(clientRequest.headers.get("Header1"), "value1");
        assertEquals(clientRequest.headers.get("Content-Type"), "text/html");
        assertEquals(clientRequest.headers.get("Content-Length"), "9");

        assertEquals(new String(clientRequest.entity), "body data");
    }

    public static class SlowStream extends ByteArrayInputStream {

        public long delay;

        public SlowStream(byte[] buf) {
            super(buf);
        }

        @Override
        public synchronized int read() {
            safeSleep();
            final int b = super.read();
            if (b != -1) {
                print("read a byte: " + new String(new byte[]{(byte) b}, StandardCharsets.UTF_8));
            } else {
                print("reached EOF");
            }

            return b;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            Objects.checkFromIndexSize(off, len, b.length);
            if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte) c;

            int i = 1;

            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }

            return i;
        }

        private void safeSleep() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        protected static void print(String message) {
            System.out.println(Instant.now().truncatedTo(ChronoUnit.MILLIS).toString() + ": " + message);
        }
    }
}
