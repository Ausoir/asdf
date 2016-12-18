package com.ga.connection;

import com.ga.game.Command;
import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Unit;
import com.ga.unit.Updater;
import com.ga.unit.intelligence.PlayerInput;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 12/10/2016.
 */
public class Server implements Observer {
    private static final String local = "127.0.0.1";
    protected UDPClient udp;
    protected TCPClient tcp;
    private HashMap<Integer, PlayerInput> clientControllers = new HashMap<>();
    private ArrayList<DatagramPacket> clientShortcuts = new ArrayList<>();
    private ArrayList<String> clientIPs = new ArrayList<>();
    private Thread serverThread;
    private static boolean online = false;
    public static boolean server = true;
    private String syncommand = null;
    private Timer syncCheck = new Timer(500,true);

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

    private void translate(String p) {
        int eol = p.indexOf(";");
        if (eol != -1) {
            String[] cmd = p.substring(0, p.indexOf(";")).split("@");
            System.out.println(p);
            switch (cmd[0]) {
                case "ME":
                    String t = local;
                    PlayerInput pi = FileManager.loadPlayer("0");
                    clientIPs.add(t);
                    clientControllers.put(0, pi);
                    Global.display.input = pi;
                    Global.player = pi;
                    Command.host = pi.host;
                    clientShortcuts.add(UDPClient.getAckPacket(t, Settings.receiverPort));
                    break;
                case "HI":
                    System.out.println("Request received");
                    t = udp.getClientIP();
                    int client = clientIPs.size();
                    acknowledgeConnection(t, client);
                    clientShortcuts.add(UDPClient.getAckPacket(t, Settings.senderPort));
                    String[] entry = new String[client+1];
                    for (int i=0;i<client;i++) entry[i+1] = "Data/map/"+i;
                    entry[0] = "Data/map/Map.map";
                    tcp.send(entry);
                    clientIPs.add(t);
                    break;
                case "OK": //client
                    System.out.println("Acknowledgement received");
                    server = false;
                    t = udp.getClientIP(); //server ip
                    clientIPs.add(0,t);
                    clientControllers.remove(0);
                    clientControllers.put(Integer.parseInt(cmd[1]), Global.player); //callname = local
                    try {
                        final int clientSize = Integer.parseInt(cmd[1]);
                        entry = new String[clientSize+1];
                        for (int i=0;i<clientSize;i++){
                            entry[i+1] = "Data/map/"+i;
                        }
                        entry[0] = "Data/map/Server.map";
                        tcp.download(t, entry);
                        Updater.changeMap = "Server";
                        Updater.changeMapPos = 0;
                        Updater.changeMapEvent = new Performer(){
                            public boolean perform() {
                                for (int i = 0; i < clientSize; i++) {
                                    PlayerInput pi = FileManager.loadPlayer("" + i);
                                    clientControllers.put(i, pi);
                                }
                                return true;
                            }
                        };
                        Global.player.host.id = -clientSize-1;
                        udp.send("KK@1",t,Settings.senderPort);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tcp.send(new String[]{"Data/map/0"});
                    break;
                case "KK":
                    System.out.println("Request Followup Received");
                    t = udp.getClientIP();
                    client = clientIPs.indexOf(t);
                    try {
                        tcp.download(t, new String[]{"Data/map/" + client});
                        syncommand = "HI@" + client;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "QT": //client
                    break;
                case "DC":
                    t = udp.getClientIP();
                    removeClient(t);
                    System.out.println(t+" Disconnected - moved to "+cmd[1]+" at Position "+cmd[2]);
                    break;
                case "IN":
                    if (Integer.parseInt(cmd[1]) < 0 || server)
                        broadcast("OT@" + cmd[1] + "@" + cmd[2] + "@" + cmd[3] + "@" + cmd[4]);
                    break;
                case "OT": //client
                    try {
                        Unit unit = Updater.unitList.get(Integer.parseInt(cmd[1]));
                        unit.target = new P(Integer.parseInt(cmd[3]),Integer.parseInt(cmd[4]));
                        unit.face(unit.target.x > 0 ? 1 : -1);
                        unit.setStateEcho(cmd[2]);
                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Null ? "+p);
                    }
                    break;
                case "SYNC":
                    client = clientIPs.size();
                    broadcast("SYNCC@"+client);
                    entry = new String[client+1];
                    for (int i=0;i<client;i++) entry[i+1] = "Data/map/"+i;
                    entry[0] = "Data/map/Map.map";
                    tcp.send(entry);
                    break;
                case "SYNCC": //client
                    if (!server){
                        try {
                            t = udp.getClientIP();
                            final int clientSize = Integer.parseInt(cmd[1]);
                            entry = new String[clientSize + 1];
                            for (int i = 0; i < clientSize; i++) {
                                entry[i + 1] = "Data/map/" + i;
                            }
                            entry[0] = "Data/map/Server.map";
                            tcp.download(t, entry);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }
    private void removeClient(String p){
        for (DatagramPacket i : clientShortcuts){
            if (i.getAddress().getHostAddress().equals(p)) clientShortcuts.remove(i);
        }
        for (String i : clientIPs){
            if (i.equals(p)) clientIPs.remove(i);
        }
        Iterator it = clientControllers.entrySet().iterator();
        while (it.hasNext()){
            if (((Map.Entry)it.next()).getKey().equals(p)) it.remove();
        }
    }
    private void broadcast(String p){
        try {
            for (DatagramPacket i : clientShortcuts) udp.send(i, p);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void requestConnection(String ip){
        try {
            if (ip.contains(":")){
                Settings.senderPort = Integer.parseInt(ip.split(":")[1]);
                ip = ip.substring(0,ip.indexOf(":"));
            }
            udp.send("HI@"+ip, ip, Settings.senderPort); //add timeout
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void acknowledgeConnection(String ip, int clientSize){ //Acknowledge client
        try {
            udp.send("OK@"+clientSize+"@"+ip, ip, Settings.senderPort);
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
            online = true;
        }
    }
    public void sendFromLocal(String msg){
        try {
            if (!server) udp.send(msg);
            else udp.send(msg,local,Settings.receiverPort);
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
                    PlayerInput pi = FileManager.loadPlayer(cmd[1]);
                    clientControllers.put(Integer.parseInt(cmd[1]), pi);
                    pi.host.pos.set(Updater.getEntrancePos(0));
                    break;
            }
            syncommand = null;
        } else if (syncCheck.update() && server && clientIPs.size() > 1){
//            sendFromLocal("SYNC@1");
        }
    }

}