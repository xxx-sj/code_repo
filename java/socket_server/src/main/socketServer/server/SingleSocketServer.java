package main.socketServer.server;

import main.socketServer.thread.TaskWorkerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleSocketServer {

    private static final LinkedBlockingQueue requestQueue = new LinkedBlockingQueue(10000);
    private static AtomicInteger count = new AtomicInteger(
            0);

    public static void singleThreadStart(int port) {
        ServerSocket server = null;
        Thread taskWorkerThread = null;

        try {

//            server = new ServerSocket(port);
            server = new ServerSocket(port, 10000);
            server.setReuseAddress(true);

            taskWorkerThread = new TaskWorkerThread(requestQueue);
            taskWorkerThread.start();

            System.out.println("server is listened..." + "http://localhost:" + port);

            while (true) {
                //연결된 소켓 accpet queue에서 가져옴
                Socket client = server.accept();
//                client.setKeepAlive(false);

                count.incrementAndGet();
                requestQueue.put(client);
                System.out.println("accept = " + count);


//                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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