package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.*;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

/**
 * Created by ga on 6/11/17. asd
 */
public enum Intelligence {
    None{
        public AI make(Unit host) { return process(new AI(host){
            public void update(){}
            public void start(){}
        }); }
    }, Neutral{
        public AI make(Unit host){ return process(new AI(host){
            Timer advertise;
            String ad = "@talk";
            public void load(String p) {
                super.load(p);
                advertise = new Timer(80+host.rand.nextInt(240), false);
            }
            @Override
            public void hear(Prop u, String p){
                super.hear(u, p);
                if (u == Global.player){
                    if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                        if (u.pos.getSubtract(host.pos).getDist() < 160) host.say("@talk");
                    }
                }
            }
            @Override
            public boolean perceived(Prop p){
                if (p.faction.canAttack(host.faction) && p instanceof Unit) {
                    host.face(host.pos.x < p.pos.x ? -1 : 1, true);
                    host.setState(Actions.Sprint);
                } else if (p instanceof Man) {
                    if (target != p) {
                        target = (Man) p;
                        setTask(Behavior.Idle);
                        if (advertise.over() && host.getConversation().equals("")){
                            if (host.rand.nextFloat() < .5f) host.say("@c");
                            else host.say(ad);
                            advertise.reset();
                            return true;
                        }
                    }
                }
                return false;
            }
            @Override
            public void update(){
                super.update();
                advertise.update();
            }
        });}
    }, Hunter{
        public AI make(Unit host){ return process(new AI(host){
            private final int range = 160;
            void onAttack(){
                if (target != null) {
                    host.say("@atk tgt");
                }
            }
            void setState(Actions state){
                if (target != null) {
                    P p = target.pos.getSubtract(host.pos).add(0,-target.getSize()/2);
                    host.target = p;
                    if (host instanceof Man) ((Man)host).weapon.rotateTo(p.getMult(host.dir,host.dir));
                    if (target.state == Actions.Dead){
                        target = null;
                        setTask(Behavior.Wander);
                    }
                }
            }
            boolean blocked(int dir){
                boolean blocked = false;
                boolean fall = true;
                P front = host.pos.getAdd((host.getSpeed()*3+48)*dir,0);
                if (Math.abs(host.map.mapEdge(front)-5) == 1) blocked = true;
                else {
                    Line pp = new Line(host.pos, front);
                    Line pp2 = new Line(front.getAdd(0, -40), front.getAdd(0, 40));
                    for (Line i : host.map.land) {
                        if (i.lineCross(pp) && (Math.abs(i.getSlope()) > Line.MAXSLOPE || !i.clockwise())) {
                            blocked = true;
                        }
                        if (i.lineCross(pp2)) {
                            fall = false;
                        }
                    }
                }
                if (blocked){
                    task.blocked(host, false);
                    return true;
                } else if (fall){
                    task.blocked(host, true);
                    return true;
                }
                return false;
            }
            void hear(Prop u, String p){
                if (task.alertness < 1) {
                    int dir = u.pos.x - host.pos.x > 0 ? 1 : -1;
                    if (!host.faction.canAttack(u.faction)) {
                        if (u instanceof Man) {
                            if (u.isFacing(host.pos) && Math.abs(u.pos.x - host.pos.x) < 240) {
                                host.face(dir, false);
                                setTask(Behavior.Idle);
                            }
                        }
                    } else {
                        host.face(dir, false);
                        perceive(range*2);
                    }
                }
            }
            protected boolean perceived(Prop p){
                if (p instanceof Unit) {
                    if (host.faction.canAttack(p.faction)) {
                        target = (Unit) p;
                        host.say("@c tgt");
                        setTask(Behavior.Hunt);
                        return true;
                    }
                }
                return false;
            }
        });
        }
    }, Merchant{
        public AI make(Unit host) {
            return process(new AI(host) {
                private Item[] product;
                private int[] price;
                private Performer[] action;
                private float low;
                private float high;
                Timer advertise;
                String ad = "@talk";

                void load(String p) {
                    super.load(p);
                    String[] data = FileManager.readText("raw/Shop.txt");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    low = host.rand.nextFloat()*2 + .5f;
                    high = host.rand.nextFloat()*2 + 2.5f;
                    host.onInteract = new Interactive() {
                        public void interact(Man p) {
                            if (p == Global.player) hear(p, "hi");
                        }
                    };
                    String r = "I'm selling ";
                    int length = host.rand.nextInt(48) + 1;
                    String[] items = new String[length];
                    String[] inventory = data[host.seed % data.length].split(";");
                    for (int i = 0; i < length; i++) {
                        items[i] = inventory[host.rand.nextInt(inventory.length)];
                    }
                    product = new Item[length];
                    price = new int[length];
                    action = new Performer[length];
                    for (int i = 0; i < length; i++) {
                        Item item = ((Item) Stock.getByName(host.map, items[i], -1));
                        int value = fluctuate(item.getValue());
                        product[i] = item;
                        price[i] = value;
                        final int ii = i;
                        action[i] = new Performer() {
                            final int index = ii;

                            public void perform() {
                                Updater.addQueue.add(product[index]);
                                product[index] = null;
                                host.say("@sold");
                            }
                        };
                        if (i < 8) {
                            if (i == Math.min(7, length - 1)) r += "and " + item.getFullName() + "!";
                            else r += item.getFullName() + ", ";
                        }
                    }
                    ad = r;
                }

                private int fluctuate(int value) {
                    float fluc = (host.rand.nextFloat() * (high - low)) + low;
                    return (int) (value * fluc);
                }

                public boolean perceived(Prop p){
                    if (p.faction.canAttack(host.faction) && p instanceof Unit) {
                        host.face(host.pos.x < p.pos.x ? -1 : 1, true);
                        host.setState(Actions.Sprint);
                    } else if (p instanceof Man) {
                        if (target != p) {
                            target = (Man) p;
                            setTask(Behavior.Idle);
                            if (advertise.over() && host.getConversation().equals("")){
                                if (host.rand.nextFloat() < .5f) host.say("@c");
                                else host.say(ad);
                                advertise.reset();
                                return true;
                            }
                        }
                    }
                    return false;
                }

                public void update(){
                    super.update();
                    advertise.update();
                }

                public void hear(Prop u, String p) {
                    super.hear(u, p);
                    if (u == Global.player) {
                        if (p.equals("hi")) {
                            if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                                if (u.pos.getSubtract(host.pos).getDist() < 160){
                                    host.say("@shop");
                                    host.face(u.pos.x<host.pos.x?-1:1,true);
                                    int[] newPrice = price;
                                    for (int i = 0; i < newPrice.length; i++) {
                                        newPrice[i] *= Global.player.getShopPrice();
                                    }
                                    Global.display.menu.showShop("Trader's Wares - \"" + host.getConversation() + "\"", product, price, action);
                                }
                            }
                        }
                    }
                }
            });
        }
    }, Pawnbroker{
        public AI make(Unit host) {
            return process(new AI(host) {
                private String[] wanted;
                private float[] priceR;
                Timer advertise;

                void load(String p) {
                    super.load(p);
                    String[] data = FileManager.readText("raw/Pawn.txt");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    host.onInteract = new Interactive() {
                        public void interact(Man p) {
                            if (p == Global.player) hear(p, "hi");
                        }
                    };
                    wanted = data[host.seed % data.length].split(";");
                    priceR = new float[wanted.length];
                    for (int i=0;i<priceR.length;i++){
                        priceR[i] = host.rand.nextFloat()*2;
                    }
                }

                public boolean perceived(Prop p){
                    if (p.faction.canAttack(host.faction) && p instanceof Unit) {
                        host.face(host.pos.x < p.pos.x ? -1 : 1, true);
                        host.setState(Actions.Sprint);
                    } else if (p instanceof Man) {
                        if (target != p) {
                            target = (Man) p;
                            setTask(Behavior.Idle);
                            if (advertise.over() && host.getConversation().equals("")){
                                host.say("@c");
                                advertise.reset();
                                return true;
                            }
                        }
                    }
                    return false;
                }

                public void update(){
                    super.update();
                    advertise.update();
                }

                public void hear(Prop u, String p) {
                    super.hear(u, p);
                    if (u == Global.player) {
                        if (p.equals("hi")) {
                            if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                                if (u.pos.getSubtract(host.pos).getDist() < 160){
                                    host.face(u.pos.x<host.pos.x?-1:1,true);
                                    shop();
                                }
                            }
                        }
                    }
                }

                private void shop(){
                    ArrayList<Prop> inventory = Global.player.getAvailableItems();
                    ArrayList<Prop> available = new ArrayList<>();
                    ArrayList<Float> fluctuation = new ArrayList<>();
                    for (Prop i : inventory){
                        for (int ii=0;ii<wanted.length;ii++){
                            if (i.name.equals(wanted[ii])){
                                available.add(i);
                                fluctuation.add(priceR[ii]);
                                break;
                            }
                        }
                    }
                    if (available.size() == 0){
                        String list = "I'm a broker. I buy ";
                        for (int i=0;i<wanted.length;i++){
                            if (i != wanted.length-1) list += wanted[i]+"s, ";
                            else if (i > 0) list += "or "+wanted[i]+"s.";
                            else list += wanted[i]+"s.";
                        }
                        Global.display.menu.showDialog("Buyer", list+" Do you need something?");
                    } else {
                        Performer[] action = new Performer[available.size()];
                        String[] options = new String[action.length];
                        for (int i = 0; i < action.length; i++) {
                            final Item item = (Item) available.get(i);
                            final int price = (int) (item.getValue() * fluctuation.get(i));
                            options[i] = item.getFullName() + " - "+ Dialog.money(price);
                            action[i] = new Performer() {
                                public void perform() {
                                    item.remove();
                                    Global.player.gold += price;
                                    host.say("@sold");
                                }
                            };
                        }
                        Global.display.menu.showDialog("Wanted list",
                                "What would you like to sell?", options, action);
                    }
                }
            });
        }
    }, Transporter{
        public AI make(Unit host) {
            return process(new AI(host) {
                private String[] options;
                private Performer[] performers;
                void load(String p) {
                    super.load(p);
                    final ArrayList<Integer> prices = new ArrayList<>();
                    ArrayList<Integer> destinations = new ArrayList<>();
                    for (int i=-6;i<=6;i++){
                        if (i != 0 && host.rand.nextInt(6) == 0){
                            int newmap = Integer.parseInt(host.map.name)+i;
                            if (FileManager.isFile("Data/map/"+newmap+".map")){
                                destinations.add(newmap);
                                prices.add(Math.abs(i));
                            }
                        }
                    }
                    options = new String[destinations.size()];
                    performers = new Performer[destinations.size()];
                    for (int i=0;i<destinations.size();i++){
                        final int destination = destinations.get(i);
                        final int price = prices.get(i);
                        options[i] = FileManager.read("Data/map/"+destination+".map").split("%")[0]
                                .split(";")[0] + " - "+Dialog.money(price);
                        performers[i] = new Performer(){
                            public void perform(){
                                if (Global.player.gold >= price) {
                                    Global.player.gold -= price;
                                    host.map.changeMap = destination+"@-1";
                                } else {
                                    Global.player.say("Not enough money..");
                                }
                            }
                        };
                    }
                    host.onInteract = new Interactive() {
                        public void interact(Man p) {
                            if (p == Global.player) hear(p, "transport");
                        }
                    };
                }

                public boolean perceived(Prop p){
                    if (p.faction.canAttack(host.faction) && p instanceof Unit) {
                        host.face(host.pos.x < p.pos.x ? -1 : 1, true);
                        host.setState(Actions.Sprint);
                    }
                    return false;
                }

                public void hear(Prop u, String p) {
                    super.hear(u, p);
                    if (u == Global.player) {
                        if (p.equals("transport")) {
                            if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                                if (u.pos.getSubtract(host.pos).getDist() < 160){
                                    host.face(u.pos.x<host.pos.x?-1:1,true);
                                    Global.display.menu.showDialog("Transportation",
                                        "Where would you like to go?", options, performers);
                                }
                            }
                        }
                    }
                }
            });
        }
    };
    public abstract AI make(Unit host);
    AI process(AI p){
        p.factoryId = ordinal();
        return p;
    }
    public static AI get(String p, Unit host){
        String[] data = p.split(",");
        AI r = values()[Integer.parseInt(data[0])].make(host);
        r.load(p);
        return r;
    }
}