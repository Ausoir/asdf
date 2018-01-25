package com.ga.connection;

import com.ga.game.FileManager;
import com.ga.game.Input;
import com.ga.game.Processes;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.unit.Faction;
import com.ga.unit.Stock;
import com.ga.unit.Unit;
import com.ga.unit.man.Player;
import com.ga.unit.skill.Actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ga on 12/10/2016. asd
 */
public class Server {
    private final String local = "127.0.0.1";
    private UDPClient udp;
    private TCPClient tcpUploader, tcpDownloader;
    private boolean stop;
    private ListenServer server;

    public void startServer(){
        stop = false;
        Settings.receiverPort = 1993; //todo: remove hardcoding at production
        Settings.senderPort = 1992; //
        Settings.tcpUploadPort = 2019; //
        Settings.tcpDownloadPort = 2018; //
        server = new HostServer(Settings.receiverPort+10);
        start();
    }
    public void startClient(String serverIP) {
        stop = false;
        Settings.receiverPort = 1992; //
        Settings.senderPort = 1993; //
        Settings.tcpUploadPort = 2018; //
        Settings.tcpDownloadPort = 2019; //
        server = new ClientServer();
        start();
        try {
            udp.send("CN", serverIP, Settings.senderPort);
            tcpUploader.upload("Data/map/Player.user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(){
        udp = new UDPClient(Settings.receiverPort);
        tcpUploader = new TCPClient(Settings.tcpUploadPort);
        tcpDownloader = new TCPClient(Settings.tcpDownloadPort);
        Thread serverThread = new Thread(() -> {
            while (!stop) {
                try {
                    String t = udp.receive();
                    server.translate(t);
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    stop = true;
                }
            }
        });
        serverThread.start();
    }

    public boolean isHost(){
        return server == null || server instanceof HostServer;
    }

    public void requestChangeMap(String map){
        if (isHost()) changeMap(map);
        else {
            String[] mapData = map.split("@");
            try {
                Global.p("Reported map change to client server.");
                udp.send("CM}" + mapData[0] + "}" + mapData[1]);
                tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/serverMap.map");
                Global.p("Map downloaded from server.");
                Global.maps.clear();
                Global.maps.add(Global.player.map);
                changeMap("multiplayer/serverMap@" + mapData[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeMap(String p){
        Processes.pause();
        new Timer().schedule(new TimerTask() {
            public void run(){
                String[] mapData = p.split("@");
                Global.player.map.clear();
                Global.maps.clear();
                com.ga.map.Map map = Global.getMap(mapData[0]);
                Global.player.map = map;
                Global.player.pos.set(map.updater.getEntrancePos(Integer.parseInt(mapData[1]), map.getFailsafePos()));
                FileManager.saveCharacter();
                FileManager.loadPlayer();
                Processes.resume();
            }
        }, 1);
    }

    private String[] processMessage(String p){
        return p.substring(0,p.indexOf("]")).split("}");
    }

    class HostServer implements ListenServer, Input{
        class Client{
            int port;
            String ip;
            Player unit;
            Client(Player unit, int port, String ip){
                this.port = port;
                this.ip = ip;
                this.unit = unit;
            }
            void message(String p){
                try {
                    udp2.send(p,ip,port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        UDPClient udp2;
        private HashMap<String, Client> clients = new HashMap<>();
        HostServer(int port){
            udp2 = new UDPClient(port);
            Thread broadcaster = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stop) {
                        try {
                            broadcast();
                            Thread.sleep(25);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            broadcaster.start();
            Global.display.input = this;
        }
        public void translate(String p) {
//            System.out.println("HOST: "+p);
            String[] msg = processMessage(p);
            switch (msg[0]) {
                case "K":
                    clients.get(udp.getClientIP()).unit.input.input(Integer.parseInt(msg[1]),Integer.parseInt(msg[2]),Integer.parseInt(msg[3]));
                    break;
                case "C":
                    clients.get(udp.getClientIP()).unit.input.chat(msg[1],0,0);
                    for (Map.Entry clientsEntry : clients.entrySet()){
                        Client client = ((Client) clientsEntry.getValue());
                        client.message("C}"+client.unit.controlId+"}"+msg[1]);
                    }
                    break;
                case "RU":
                    Client client = clients.get(udp.getClientIP());
                    Unit target = client.unit.map.updater.getLoadedActor(Integer.parseInt(msg[1]));
                    if (target != null) client.message("N}"+msg[1]+"}"+target);
                    break;
                case "CM":
                    tcpUploader.upload("Data/map/"+msg[1]+".map");
                    break;
                case "CN":
                    try {
                        tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/"+udp.getClientIP()+".user");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Player player = (Player) Stock.Player.create(Global.player.map,0,0);
                    Client newest = new Client(player, udp.getClientPort(), udp.getClientIP());
                    player.load(FileManager.read("map/multiplayer/"+udp.getClientIP()+".user"));
                    player.map = Global.player.map;
                    player.faction = Faction.Player2;
                    player.controlId = Global.player.controlId + 1;//Global.getControlId();
                    player.start();
                    clients.put(udp.getClientIP(), newest);
                    FileManager.saveCharacter();
                    FileManager.save("map/multiplayer/serverActors.map", Global.player.map.updater.toActorString());
                    Global.p("Connection established with client at "+udp.getClientIP()+"/"+udp.getClientPort());
                    newest.message("START}"+player.controlId);
                    tcpUploader.upload("Data/map/World.map");
                    tcpUploader.upload("Data/map/"+Global.player.map.name+".map");
                    tcpUploader.upload("Data/map/multiplayer/serverActors.map");
                    tcpUploader.upload("Data/map/Player.user");
                    break;
            }
        }
        void broadcast(){
            for (Map.Entry p : clients.entrySet()){
                Client client = ((Client) p.getValue());
                String msg = "U}";
                for (Unit i : client.unit.map.updater.unitsLoaded) {
                    msg += i.controlId+","+i.state.ordinal()+","+i.pos.x()+","+i.pos.y()+","+i.vel.x()+","+i.vel.y()+","+i.target.x()+","+i.target.y()+"}";
                }
                client.message(msg.substring(0,msg.length()-1));
            }
        }
        @Override
        public void input(int mode, int x, int y) {
            Global.player.input.input(mode,x,y);
        }

        @Override
        public void chat(String p, int x, int y) {
            Global.player.input.chat(p,x,y);
            for (Map.Entry clientsEntry : clients.entrySet()){
                Client client = ((Client) clientsEntry.getValue());
                client.message("C}"+Global.player.controlId+"}"+p);
            }
        }

        @Override
        public void onFocus(boolean focus) {

        }
    }

    class ClientServer implements ListenServer, Input {
        public void translate(String p){
//            System.out.println("CLIENT: "+p);
            String[] msg = processMessage(p);
            switch (msg[0]) {
                case "U": //update
                    for (int i=1;i<msg.length;i++) {
                        String[] v = msg[i].split(",");
                        Unit unit = Global.player.map.updater.getLoadedActor(Integer.parseInt(v[0]));
                        if (unit != null) {
                            unit.setState(Actions.values()[Integer.parseInt(v[1])]);
                            unit.pos.set(Integer.parseInt(v[2]), Integer.parseInt(v[3]));
                            unit.vel.set(Integer.parseInt(v[4]), Integer.parseInt(v[5]));
                            unit.target.set(Integer.parseInt(v[6]), Integer.parseInt(v[7]));
                        } else { //request unit info to recreate it
                            System.out.println("Can't find unit "+v[0]+". Requesting...");
                            try {
                                udp.send("RU}"+v[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case "C":
                    Global.player.map.updater.getLoadedActor(Integer.parseInt(msg[1])).say(msg[2]);
                    break;
                case "N":
                    if (Global.player.map.updater.getLoadedActor(Integer.parseInt(msg[1])) == null)
                        Stock.get(null,msg[2]).start();
                    break;
                case "START":
                    Global.display.input = this;
                    String[] output = new String[]{
                            "Data/map/multiplayer/serverWorld.map", "Data/map/multiplayer/serverMap.map",
                            "Data/map/multiplayer/serverActors.map", "Data/map/multiplayer/serverPlayer.user"
                    };
                    try {
                        tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/serverWorld.map");
                        tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/serverMap.map");
                        tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/serverActors.map");
                        tcpDownloader.download(udp.getClientIP(), "Data/map/multiplayer/serverPlayer.user");
                        output = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (output == null){
                            requestChangeMap("multiplayer/serverMap@0");
                            Player player = (Player) Stock.Player.create(Global.player.map,0,0);
                            player.load(FileManager.read("map/multiplayer/serverPlayer.user"));
                            player.map = Global.player.map;
                            Global.player.controlId = Integer.parseInt(msg[1]);
                            player.faction = Faction.Player2;
                            player.start();
                            System.out.println("Player assigned "+Global.player.controlId);
                            System.out.println("Host assigned "+player.controlId);
                            FileManager.loadActors("multiplayer/serverActors.map");
                        } else {
                            Global.player.say("Server found, but failed to connect!");
                        }
                    }
                    break;
            }
        }

        @Override
        public void input(int mode, int x, int y) {
            try {
                udp.send("K}"+mode+"}"+x+"}"+y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void chat(String p, int x, int y) {
            try {
                udp.send("C}"+p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFocus(boolean focus) {

        }
    }
}

interface ListenServer {
    void translate(String p);
}