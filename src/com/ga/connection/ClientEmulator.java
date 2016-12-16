package com.ga.connection;

import java.net.DatagramPacket;

/**
 * Created by Ga on 12/13/2016.
 */
public class ClientEmulator extends UDPClient {
    private String message;
    private boolean on = true;
    public ClientEmulator() {}
    public String receive(){
        try {
            while (on) {
                if (message != null){
                    String t = message;
                    message = null;
                    return t;
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void close(){
        on = false;
    }
    public String getClientIP(){
        return "127.0.0.1";
    }
    public void send(DatagramPacket packet, String p) {
        message = p+";";
    }
    public void send(String msg, String ip, int port) {
        message = msg+";";
    }
    public void send(String msg) {
        message = msg+";";
    }
}
