package com.ga.connection;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.unit.Updater;
import com.ga.unit.intelligence.PlayerInput;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 12/10/2016.
 */
public class Server implements Observer {
    private static final String local = "127.0.0.1";
    protected UDPClient udp;
    protected TCPClient tcp;
    private HashMap<String, PlayerInput> controllers = new HashMap<>();
    private ArrayList<DatagramPacket> clients = new ArrayList<>();
    private Thread serverThread;
    private boolean online = false;
    private String syncommand = null;
    public void start(){
        udp = new ClientEmulator();
        tcp = new TCPClient();
        serverThread = new Thread(new Runnable(){
            @Override
            public void run(){
                while (true) {
                    try{
                        String t = udp.receive();
                        translate(t);
                        Thread.sleep(10);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
        translate("ME@1;");
        Updater.other.add(this);
    }
    private void translate(String p){
        int eol = p.indexOf(";");
        if (eol != -1) {
            String[] cmd = p.substring(0, p.indexOf(";")).split("@");
            switch (cmd[0]) {
                case "ME":
                    String t = local;
                    requestConnection(t);
                    controllers.put(t, FileManager.loadPlayer(t));
                    clients.add(UDPClient.getAckPacket(t, Settings.senderPort));
                    break;
                case "HI":
                    t = udp.getClientIP();
                    tcp.download(t, "Data/map/" + t);
                    acknowledgeConnection(t);
                    syncommand = "HI@"+t;
                    clients.add(UDPClient.getAckPacket(t, Settings.senderPort));
                    break;
                case "OK": //client
                    System.out.println("Acknowledgement received");
                    tcp.send("Data/map/" + local);
                    break;
                case "IN":
                    broadcast("OT@" + cmd[1]);
                    break;
                case "OT": //client
                    String[] outputs = cmd[1].split(",");
                    Global.player.inputEcho(outputs[0], Integer.parseInt(outputs[1]), Integer.parseInt(outputs[2]));
                    break;
            }
        }
    }
    private void broadcast(String p){
        try {
            for (DatagramPacket i : clients) udp.send(i, p);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void requestConnection(String ip){ //Acknowledge client
        try {
            int port = Settings.senderPort;
            if (ip.contains(":")){
                port = Integer.parseInt(ip.split(":")[1]);
                ip = ip.substring(0,ip.indexOf(":"));
            }
            udp.send("HI@1", ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void acknowledgeConnection(String ip){ //Acknowledge client
        try {
            udp.send("OK@1", ip, Settings.senderPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void online(){
        if (!online) {
            udp.close();
            udp = new UDPClient(Settings.receiverPort);
            try {
                udp.send("None@1", local, Settings.receiverPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
            clients.add(UDPClient.getAckPacket(local, Settings.senderPort));
            online = true;
        }
    }
    public void send(String msg){
        try {
            udp.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() { //Sync
        if (syncommand != null) {
            String[] cmd = syncommand.split("@");
            switch (cmd[0]) {
                case "HI":
                    controllers.put(cmd[1], FileManager.loadPlayer(cmd[1]));
                    break;
            }
            syncommand = null;
        }
    }
}