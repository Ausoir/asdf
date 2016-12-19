package com.ga.connection;

import com.ga.game.Command;
import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Faction;
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
public class Server implements Updatable {
    private static final String local = "127.0.0.1";
    protected UDPClient udp;
    protected TCPClient tcp;
    private HashMap<Integer, PlayerInput> clientControllers = new HashMap<>();
    private ArrayList<DatagramPacket> clientShortcuts = new ArrayList<>();
    private ArrayList<String> clientIPs = new ArrayList<>();
    private Thread serverThread;
    private static boolean online = false;
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
        try{
            translate("ME@1;");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Updater.addQueue.add(this);
    }

    private void translate(String p) throws Exception {
        int eol = p.indexOf(";");
        if (eol != -1) {
            String[] cmd = p.substring(0, p.indexOf(";")).split("@");
            System.out.println(p+" from "+udp.getClientIP());
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
                    Updater.tick = 0;
                    break;
                case "OK": //client
                    Updater.tick = 0;
                    System.out.println("Acknowledgement received ("+cmd[1]+")");
                    t = udp.getClientIP(); //server ip
                    clientIPs.add(0,t);
                    clientControllers.remove(0);
                    clientControllers.put(Integer.parseInt(cmd[1]), Global.player); //callname = local
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
                    tcp.send(new String[]{"Data/map/0"});
                    break;
                case "KK":
                    System.out.println("Request Followup Received");
                    t = udp.getClientIP();
                    client = clientIPs.indexOf(t);
                    tcp.download(t, new String[]{"Data/map/" + client});
                    syncommand = "HI@" + client;
                    break;
                case "QT": //client
                    break;
                case "DC":
                    t = udp.getClientIP();
                    removeClient(t);
                    System.out.println(t+" Disconnected - moved to "+cmd[1]+" at Position "+cmd[2]);
                    break;
                case "IN":
                    if (online && Math.random() < .2f) return;
                    String[] outputs = cmd[1].split(",");
                    client = clientIPs.indexOf(udp.getClientIP());
                    if (outputs.length == 1) clientControllers.get(client).host.say(outputs[0]);
                    else if (outputs.length == 4) {
                        udp.send("OT@"+cmd[1]);
                        clientControllers.get(client).inputDirect(outputs[1], Integer.parseInt(outputs[2]), Integer.parseInt(outputs[3]));
                    }
                    break;
                case "OT": //client
                    if (online && Math.random() < .2f) return;
                    outputs = cmd[1].split(",");
                    client = clientIPs.indexOf(local);
                    if (outputs.length == 1) clientControllers.get(client).host.say(outputs[0]);
                    else if (outputs.length == 4) clientControllers.get(client).inputEcho(Integer.parseInt(outputs[0]), outputs[1], Integer.parseInt(outputs[2]), Integer.parseInt(outputs[3]));
                    udp.send("OO@"+cmd[1]);
                    break;
                case "OO":
                    break;
                case "AT": //client
                    try {
                        Unit unit = Updater.unitList.get(Integer.parseInt(cmd[1]));
                        unit.target = new P(Integer.parseInt(cmd[3]),Integer.parseInt(cmd[4]));
                        unit.face(unit.target.x > 0 ? 1 : -1);
//                        unit.setStateEcho(cmd[2]);
                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Null ? "+p);
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
            udp.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() { //Sync
        if (syncommand != null) {
            String[] cmd = syncommand.split("@");
            try {
                switch (cmd[0]) {
                    case "HI":
                        PlayerInput pi = FileManager.loadPlayer(cmd[1]);
                        clientControllers.put(Integer.parseInt(cmd[1]), pi);
                        pi.host.pos.set(Updater.getEntrancePos(0));
                        break;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            syncommand = null;
        }
    }
}