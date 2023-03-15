package main.socketServer.thread;

import main.socketServer.Utils.HtmlPageBuilder;
import main.socketServer.error.BadRequest;
import main.socketServer.error.ForbiddenRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Objects;

public class HttpWorker implements Runnable {
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

//                String line;
//                while(true) {
//                    line = in.readLine();
//                    System.out.println("line = " + line);
//                    if (line.equals("")) {
//                        break;
//                    }
//                }

            String line = in.readLine();

            if (Objects.isNull(line)) {
                return;
            }
//                System.out.println("====while is start====");
            this.validate(line);
            String request = line.substring(4, line.length() - 9).trim();

            request = URLDecoder.decode(request, "UTF-8");

            if (request.endsWith("/")) {
                request = request.substring(0, request.length() - 1);
            }


            if (request.indexOf(".") > -1) {
                // request dot이 있다면 file
                

            } else {
                // 일반 html 파일 request
            }

//            String page = HtmlPageBuilder.buildErrorPage("200", "bad request", "bad request page not exist");
//            out.println(page);

            while (true) {
                line = in.readLine();
//                    System.out.println("line = " + line);
                if (line.equals("")) {
                    break;
                }
            }
//                System.out.println("====while is ended====");

        } catch (BadRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("400", "bad request", "bad request page not exist");
            out.println(page);
        } catch (ForbiddenRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("403", "forbidden request", "wrong request");
            out.println(page);
        } catch (IOException e) {
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
