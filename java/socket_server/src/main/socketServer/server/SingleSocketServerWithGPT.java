package main.socketServer.server;

import main.socketServer.thread.HttpWorker;
import main.socketServer.thread.TaskWorkerThreadWithGPT;
import main.socketServer.thread.factory.WorkerThreadFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleSocketServerWithGPT {

    private static Queue<Socket> requestQueue = new LinkedList<>();
    private static AtomicInteger count = new AtomicInteger(0);
    private static ExecutorService executor = Executors.newFixedThreadPool(2, new WorkerThreadFactory());

    public static void singleThreadStart(int port) {
        ServerSocket server = null;
        Thread taskWorkerThread = null;

        try {

            server = new ServerSocket(port);
            server.setReuseAddress(true);

//            taskWorkerThread = new TaskWorkerThreadWithGPT(requestQueue);
//            taskWorkerThread.start();

            System.out.println("server is listened..." + "http://localhost:" + port);

            while (true) {
                //연결된 소켓 accpet queue에서 가져옴
                Socket client = server.accept();
//                synchronized (requestQueue) {
//                    requestQueue.add(client);
//                    requestQueue.notify();
//                    count.incrementAndGet();
//                    System.out.println("put count = " + count);
//                }
                executor.execute(new HttpWorker(client));
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}