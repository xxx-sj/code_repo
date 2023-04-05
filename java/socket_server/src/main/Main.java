package main;


import main.socketServer.server.SingleSocketServer;
import main.socketServer.server.SingleSocketServerWithGPT;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

//        SingleSocketServer.singleThreadStart(8088);
        SingleSocketServerWithGPT.singleThreadStart(8088);

    }
}
