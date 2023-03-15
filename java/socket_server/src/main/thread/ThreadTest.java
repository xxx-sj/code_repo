package main.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {
    private static AtomicInteger count = new AtomicInteger(1);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4,  new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.getName();
                System.out.println("new Thread = " + t.getName());
                return t;
            }
        });

        while (true) {
            executor.execute(new Worker());
            int i = count.incrementAndGet();
            if (i > 100) {
                break;
            }
        }

        executor.shutdown();
    }

    private static class Worker implements Runnable {
        @Override
        public void run() {
//            System.out.println("run method running Name = " + Thread.currentThread().getName());
            System.out.println("run method running Name = " + Thread.currentThread().getName());
        }
    }
}
