import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class StreamTest {
    @Test
    public void test() throws IOException {
        // note test case deals with SlowStream type directly so it can configure it
        final SlowStream stream = new SlowStream("my string content".getBytes(StandardCharsets.UTF_8));
        stream.delay = 100L;

        readFromStream(stream);
    }

    // note contract is InputStream here
    protected void readFromStream(InputStream stream) throws IOException {

        byte[] buf = new byte[100];
        int result;
        do {
            result = stream.read(buf);
            if (result >= 0) {
                print("read " + result + " bytes into a buffer: " + new String(buf, StandardCharsets.UTF_8));
            } else if (result == -1) {
                print("read EOF");
            }
        } while (result != -1); // loop until EOF

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

    }

    protected static void print(String message) {
        System.out.println(Instant.now().truncatedTo(ChronoUnit.MILLIS).toString() + ": " + message);
    }
}