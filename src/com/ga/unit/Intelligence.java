package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.*;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ga on 6/11/17. asd
 */
public enum Intelligence {
    None{
        public AI make(Unit host) { return process(new AI(host){
            public void update(){}
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
                if (p instanceof Man) {
                    if (target != p) {
                        target = (Man) p;
                        setTaskEcho(Behavior.Idle);
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
                    HashMap<String, ArrayList<String>> stock = FileManager.readDialogue("raw/Shop.txt");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    low = host.rand.nextFloat() + .3f;
                    high = host.rand.nextInt(4) + 2;
                    host.onInteract = new Interactive() {
                        public void interact(Man p) {
                            if (p == Global.player) hear(p, "shop");
                        }
                    };
                    String r = "I'm selling ";
                    int length = host.rand.nextInt(48) + 1;
                    String[] items = new String[length];
                    for (int i = 0; i < length; i++) {
                        ArrayList<String> typeItems = stock.get("General");
                        items[i] = typeItems.get(host.rand.nextInt(typeItems.size()));
                    }
                    product = new Item[length];
                    price = new int[length];
                    action = new Performer[length];
                    for (int i = 0; i < length; i++) {
                        Item item = ((Item) Stock.getByName(items[i], -1)).cancelAdd();
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
                    if (p instanceof Man) {
                        if (target != p) {
                            target = (Man) p;
                            setTaskEcho(Behavior.Idle);
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
                        if (p.equals("shop")) {
                            if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                                if (u.pos.getSubtract(host.pos).getDist() < 160){
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
    }, Hunter{
        public AI make(Unit host){ return process(new AI(host){
                private final int range = 160;
                void onAttack(){
                    if (target != null) {
                        host.say("@atk tgt");
                    }
                }
                public void onHit(){
                    if (task.alertness < 1){
                        setTaskEcho(Behavior.Search);
                    }
                }
                void setState(Actions state){
                    if (state == Actions.Dead){
                        Updater.remove(this);
                        task = Behavior.Idle;
                    } else {
                        if (target != null) {
                            P p = target.pos.getSubtract(host.pos).add(0,-target.getSize()/2);
                            host.target = p;
                            if (host instanceof Man) ((Man)host).weapon.rotateTo(p.getMult(host.dir,host.dir));
                            if (target.state == Actions.Dead){
                                target = null;
                                setTaskEcho(defaultTask);
                            }
                        }
                    }
                }
                boolean blocked(int dir){
                    boolean blocked = false;
                    boolean fall = true;
                    P front = host.pos.getAdd((host.getSpeed()*3+48)*dir,0);
                    if (Math.abs(Global.mapEdge(front)-5) == 1) blocked = true;
                    else {
                        Line pp = new Line(host.pos, front);
                        Line pp2 = new Line(front.getAdd(0, -40), front.getAdd(0, 40));
                        for (Line i : Global.map.land) {
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
                                    setTaskEcho(Behavior.Idle);
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
                            setTaskEcho(Behavior.Hunt);
                            return true;
                        }
                    }
                    return false;
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