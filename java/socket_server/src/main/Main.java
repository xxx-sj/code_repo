package main;

import main.socketServer.server.SingleSocketServer;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        SingleSocketServer.singleThreadStart(8088);
    }
}
