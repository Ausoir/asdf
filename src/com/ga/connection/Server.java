package com.ga.connection;

/**
 * Created by Ga on 12/10/2016. asd
 */
public class Server {
    private static final String local = "127.0.0.1";
    private static UDPClient udp;
    private static TCPClient tcp;
    private static boolean stop = false;
    private static ListenServer server;

    public static void startServer(){
        server = new HostServer();
        start();
    }
    public static void startClient(String serverIP) {
        server = new ClientServer();
        start();
    }

    private static void start(){
        udp = new ClientEmulator();
        tcp = new TCPClient();
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    try {
                        String t = udp.receive();
                        server.translate(t);
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
        server.translate("START");
    }
}

class HostServer extends ListenServer{
    void translate(String p) {
        System.out.println(p);
        switch (p) {
            case "START":
                break;

        }
    }
}

class ClientServer extends ListenServer{
    void translate(String p){
        System.out.println(p);
        switch (p) {
            case "START":
                break;

        }
    }
}

abstract class ListenServer {
    abstract void translate(String p);
}