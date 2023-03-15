package main.socketServer.main;

import main.socketServer.thread.HttpWorker;
import main.socketServer.thread.factory.WorkerThreadFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    public static void main(String[] args) {
        SocketServer.serverStart(8088, 10);
    }

    private static void serverStart(int port, int nThread) {
        ExecutorService executor = Executors.newFixedThreadPool(nThread, new WorkerThreadFactory());

        ServerSocket server = null;

        try {

            server = new ServerSocket(port);
            server.setReuseAddress(true);

            System.out.println("server is listened..." + "http://localhost:" + port);

            while (true) {

                //연결된 소켓 accpet queue에서 가져옴
                Socket client = server.accept();
                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
                executor.execute(new HttpWorker(client));
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
