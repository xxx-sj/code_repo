package main;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;

public class Main {

    public static void main(String[] args) throws IOException {
//        Main.responseServer();
        Main.requestToServer();
    }

    public static void responseServer() throws IOException {
        System.out.println("socket server has started 127.0.0.1:8080 listened");
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            System.out.println("reader.readLine() = " + reader.readLine());
        }
    }


    //TODO request test
    public static void requestToServer() throws IOException {
        System.out.println("request to https://www.daum.net");

        Socket socket = new Socket("www.daum.net", 80);
//        Socket socket = new Socket("www.kocw.net", 80);
//        Socket socket = new Socket("www.naver.com", 80);

        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader in = new BufferedReader(inputStreamReader);

        PrintStream out = new PrintStream(socket.getOutputStream());

        // 요청라인
        out.println("GET / HTTP/1.1");

        // 헤더정보
        out.println("Host: www.daum.net");
//        out.println("Host: www.kocw.net");
//        out.println("Host: www.naver.com");
        out.println("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0)"
                + " AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/30.0.1599.101 Safari/537.36");
        // 공백라인
        out.println();

        StringBuffer sb = new StringBuffer();


        String line = null;
        while((line = in.readLine()) != null) {
            System.out.println(line);
        }

        in.close();
        out.close();
        socket.close();
    }
}


/**
 *
 *
 * HTTP/1.1 301 Moved Permanently
 * Date: Sat, 11 Mar 2023 12:37:38 GMT
 * Content-Type: text/html
 * Content-Length: 162
 * Connection: keep-alive
 * Location: https://www.daum.net
 *
 * <html>
 * <head><title>301 Moved Permanently</title></head>
 * <body>
 * <center><h1>301 Moved Permanently</h1></center>
 * <hr><center>nginx</center>
 * </body>
 * </html>
 */
