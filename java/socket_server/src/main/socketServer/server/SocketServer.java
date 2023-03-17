package main.socketServer.server;

import jdk.jfr.events.SocketReadEvent;
import main.socketServer.thread.TaskWorkerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketServer {

    private static final LinkedBlockingQueue requestQueue = new LinkedBlockingQueue(10000);

    private static final Object lock = new Object();

    public static void main(String[] args) {
//        SocketServer.serverStart(8088, 10);
        SocketServer.singleThreadStart(8088);
    }

    private static void singleThreadStart(int port) {
        ServerSocket server = null;
        Thread taskWorkerThread = null;

        try {

            server = new ServerSocket(port);
            server.setReuseAddress(true);

            taskWorkerThread = new TaskWorkerThread(requestQueue);
            taskWorkerThread.start();

            System.out.println("server is listened..." + "http://localhost:" + port);

            while (true) {
                //연결된 소켓 accpet queue에서 가져옴
                Socket client = server.accept();
                synchronized (lock) {
                    requestQueue.put(client);
                }

                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
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

    private static void serverStart(int port, int nThread) {
//        ExecutorService executor = Executors.newFixedThreadPool(nThread, new WorkerThreadFactory());

        ServerSocket server = null;

        try {

            server = new ServerSocket(port);
            server.setReuseAddress(true);

            System.out.println("server is listened..." + "http://localhost:" + port);

            while (true) {
                //연결된 소켓 accpet queue에서 가져옴
                Socket client = server.accept();
                requestQueue.put(client);
                System.out.println("New client connected " + client.getInetAddress().getHostAddress());

//                executor.execute(new HttpWorker(client));
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
