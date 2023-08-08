package poormanshttpserver;

import java.util.Random;

public class ThreadExample {
    public static void main(String[] args) throws InterruptedException {
        ThreadRunner threadRunner = new ThreadRunner();
    }
}

class ThreadRunner {
    ThreadRunner() throws InterruptedException {
        System.out.println("hello");
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(new ThreadRunnable(i));
            thread.start();
        }
    }
}

class ThreadRunnable implements Runnable {
    private Random random = new Random();
    private int sequence;
    ThreadRunnable(int i) {
        sequence = i;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(sequence);
    }
}