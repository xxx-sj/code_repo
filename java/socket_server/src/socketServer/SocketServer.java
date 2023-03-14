package socketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SocketServer {

    private static Object lock = new Object();
    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(10, new WorkerThreadFactory());

        ServerSocket server = null;
        int port = 8088;

        try {

            server = new ServerSocket(port);
            server.setReuseAddress(true);

            System.out.println("server is listened..." + "http://localhost:" + port);

            while(true) {

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
    private static class HttpWorker implements Runnable {
        private final Socket clientSocket;
        public HttpWorker(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;

            try {
                String req = "";
                String clientRequest = "";

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//                while ((clientRequest = in.readLine()) != null) {
////                    if (req.equals("")) {
////                        req = clientRequest;
////                    }
//
//                    System.out.println("request = " + clientRequest);
//                    if(clientRequest.equals("")) {
//                        //If the end of the http request , stop
//                        break;
//                    }
//
////                    if (req != null && !req.equals("")) {}
//                }

                String line = in.readLine();
//                System.out.println("====while is start====");

                this.validate(line);

                while(true) {
                    line = in.readLine();
//                    System.out.println("line = " + line);
                    if (line.equals("")) {
                        break;
                    }
                }
//                System.out.println("====while is ended====");

            } catch (BadRequest e) {
                //error page 작성
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void validate(String httpRequest) {
            this.validateHttpRequest(httpRequest);
            this.validateIsForbidden(httpRequest);
        }

        private void validateHttpRequest(String httpRequest) {
            if (this.validateHttpMethod(httpRequest) || httpRequest.length() < 14 ||
                    !(httpRequest.endsWith("HTTP/1.0") || httpRequest.endsWith("HTTP/1.1"))) {
                //bac request
                throw new BadRequest("Bad request: is not http request");
            }
        }

        private void validateIsForbidden(String httpRequest) {
            String req = httpRequest.substring(4, httpRequest.length() - 9).trim();
            if (req.indexOf("..") > -1 || req.indexOf("/.ht") > -1 || req.endsWith("~")) {

                throw new ForbiddenRequest("is Forbidden request");
            }
        }

        private Boolean validateHttpMethod(String request) {
            return !(request.startsWith("GET") ||
                    request.startsWith("POST") ||
                    request.startsWith("PUT"));

        }
    }

    private static class WorkerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            System.out.println("new Thread name = " + thread.getName());

            return thread;
        }
    }

    private static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }

    private static class ForbiddenRequest extends BadRequest {
        public ForbiddenRequest(String message) {
            super(message);
        }
    }
}
