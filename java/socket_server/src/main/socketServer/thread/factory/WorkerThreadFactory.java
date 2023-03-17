package main.socketServer.thread.factory;

import java.util.concurrent.ThreadFactory;

public class WorkerThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        System.out.println("new Thread name = " + thread.getName());
        thread.setName("thread pool");

        return thread;
    }
}
