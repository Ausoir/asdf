package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.tool.Timer;
import com.ga.unit.item.Item;
import com.ga.unit.man.Man;
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
            public void load(T p) {
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
                        host.setState(Actions.Idle);
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
            public void onAttack(){
                if (target != null) {
                    host.say("@atk tgt");
                }
            }
            void hear(Prop u, String p){
                if (tasks.peek().getAlertness() < 1) {
                    int dir = u.pos.x - host.pos.x > 0 ? 1 : -1;
                    if (!host.faction.canAttack(u.faction)) {
                        if (u instanceof Man) {
                            if (u.isFacing(host.pos) && Math.abs(u.pos.x - host.pos.x) < 240) {
                                host.face(dir, false);
                                host.setState(Actions.Idle);
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
                    if (p.hittable(host.faction)) {
                        target = (Unit) p;
                        host.say("@c tgt");
                        if (tasks.peek() == Behavior.Hunt) tasks.pop();
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
            return process(new NeutralAI(host) {
                private Item[] products;
                private int[] prices;
                private Performer[] actions;
                private float low;
                private float high;

                void load(T p) {
                    super.load(p);
                    String[] data = FileManager.read("Data/raw/Shop.txt").split("%");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    low = host.rand.nextFloat()*.5f + .5f;
                    high = host.rand.nextFloat() + 1f;
                    String r = "I'm selling ";
                    int length = host.rand.nextInt(20) + 1;
                    String[] items = new String[length];
                    String[] inventory = data[type].split(";");
                    for (int i = 0; i < length; i++) {
                        items[i] = inventory[host.rand.nextInt(inventory.length)];
                    }
                    products = new Item[length];
                    prices = new int[length];
                    actions = new Performer[length];
                    for (int i = 0; i < length; i++) {
                        Item item;
                        if (items[i].contains(":")){
                            String[] itemData = items[i].split(":");
                            item = ((Item) Stock.createByName(host.map, itemData[0], Integer.parseInt(itemData[1]), -1));
                        } else item = ((Item) Stock.createByName(host.map, items[i], -1));
                        int value = fluctuate(item.getValue());
                        products[i] = item;
                        prices[i] = value;
                        final int ii = i;
                        actions[i] = new Performer() {
                            final int index = ii;
                            public void perform() {
                                host.map.updater.addQueue.add(products[index]);
                                products[index] = null;
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

                void shop() {
                    super.shop();
                    Menu.menu.showShop("Trader's Wares - \"" + host.getConversation() + "\"",
                            products, prices, actions);
                }

                private int fluctuate(int value) {
                    float fluc = (host.rand.nextFloat() * (high - low)) + low;
                    return (int) (value * fluc);
                }
            });
        }
    }, Innkeep{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private int price;
                void load(T p) {
                    super.load(p);
                    price = (host.rand.nextInt(30)+30)*100+99;
                }
                void shop() {
                    super.shop();
                    Menu.menu.showDialog("Inn",
                            "Rest for 3 hours? Costs "+Dialog.money(price)+". Heal 1 Vitality, 9 Fatigue, and gain 10 Motivation. Food is paid for.",
                            new String[]{"Pay"},
                            new Performer[]{
                                    new Performer(){
                                        public void perform(){
                                            if (Global.player.requireGold(price)) {
                                                Global.player.stats.nutrition.add(3, true);
                                                if (Global.player.map.updater.sleep(3, true)){
                                                    Global.player.stats.motivation.add(10,false);
                                                    Global.player.stats.fatigue.add(9,false);
                                                    Global.player.stats.agility.add(1, false);
                                                    Global.player.stats.strength.heal();
                                                } else {
                                                    Global.player.stats.nutrition.add(-3, true);
                                                    Global.player.obtainGold(price);
                                                }
                                            } else {
                                                Global.player.say("Not enough money..");
                                            }
                                        }
                                    }, new Performer(){}
                            });
                }
            });
        }
    }, Trainer{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private int price;
                private Traits.Trait trait;

                void load(T p) {
                    super.load(p);
                    price = (host.rand.nextInt(60)+9)*100+99;
                    trait = Traits.Trait.getByType(1).get(host.seed % Traits.Trait.getByType(1).size());
                }
                void shop() {
                    super.shop();
                    int reqMot = 20, reqTime = 2;
                    String message = "I can teach you useful things if you have the time, motivation, and money.";
                    message += "\nRequires "+Dialog.money(price)+", "+reqMot+" motivation, and "+reqTime+" hours.";
                    message += "\n\nCurrent motivation: "+Global.player.stats.motivation.get();
                    message += "\nCurrent nutrition: "+Global.player.stats.nutrition.get();
                    Menu.menu.showDialog("Trainer", message,
                        new String[]{"Begin training"},
                        new Performer[]{
                            new Performer(){
                                public void perform(){
                                    if (Global.player.stats.motivation.get() >= reqMot){
                                        if (Global.player.getGold() >= price) {
                                            if (Global.player.map.updater.sleep(reqTime, true)) {
                                                Global.player.learnTrait(trait);
                                                Global.player.stats.motivation.require(reqMot);
                                                Global.player.requireGold(price);
                                            }
                                        } else Global.player.say("Not enough money...");
                                    } else Global.player.say("Not enough Motivation..");
                                }
                            }
                        });
                }
            });
        }
    }, Dealer{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private String[] wanted;
                private float[] priceR;

                void load(T p) {
                    super.load(p);
                    String[] data = FileManager.read("Data/raw/Pawn.txt").split("%");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    wanted = data[host.seed % data.length].split(";");
                    priceR = new float[wanted.length];
                    for (int i=0;i<priceR.length;i++){
                        priceR[i] = host.rand.nextFloat()+.1f;
                    }
                }

                void shop() {
                    super.shop();
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
                        Menu.menu.showDialog("Buyer", list+" Do you need something?");
                    } else {
                        Performer[] action = new Performer[available.size()];
                        String[] options = new String[action.length];
                        for (int i = 0; i < action.length; i++) {
                            final Item item = (Item) available.get(i);
                            final int price = (int) (item.getValue() * fluctuation.get(i));
                            options[i] = item.getFullName() + " - "+ Dialog.money(price);
                            action[i] = new Performer() {
                                public void perform() {
                                    Global.player.dropItem(item).remove();
                                    Global.player.obtainGold(price);
                                    Global.player.say("Sold "+item.getName()+" for "+Dialog.money(price)+"!");
                                    host.say("@sold");
                                }
                            };
                        }
                        Menu.menu.showDialog("Wanted list",
                                "What would you like to sell?", options, action);
                    }
                }
            });
        }
    }, Guildsman{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private int hourly, price;

                void load(T p) {
                    super.load(p);
                    int pool = host.rand.nextInt(20000) + 20000;
                    float ratio = host.rand.nextFloat() * .8f + .1f;
                    hourly = (int) (pool * ratio * .01f);
                    price = (int) (pool * (1 - ratio));
                }
                public boolean perceived(Prop p){
                    if (p instanceof Unit) {
                        if (p.hittable(host.faction)) {
                            target = (Unit) p;
                            host.say("@c tgt");
                            setTask(Behavior.Hunt);
                            return true;
                        }
                    }
                    return false;
                }
                void shop() {
                    super.shop();
                    int week = hourly*42;
                    int month = hourly*180;
                    if (Global.player.respawnInfo.isEmpty()) {
                        Menu.menu.showDialog("Guildsman", "Would you like to buy our health insurance plan?\n\n" +
                                " Every time you are in a critical accident, we will bring you here and revive you. We cover all accidents ranging from food poisoning to decapitation.\n"+
                                " You must pay an hourly fee of "+Dialog.money(hourly)+"."+
                                " The initial payment for the contract is "+Dialog.money(price)+", and you must pay this amount again after every accident to renew the contract.",
                                new String[]{"Sign a contract for a week (42 hours + initial payment = "+Dialog.money(week+price)+")",
                                        "Sign a contract for a month (180 hours + initial payment = "+Dialog.money(month+price)+")"},
                                new Performer[]{
                                        new Performer() {
                                            public void perform() {
                                                if (Global.player.requireGold(week+price)) {
                                                    Global.player.respawnInfo = 42 + Global.hour + "," + host.map.name.toString();
                                                    host.say("@sold");
                                                } else Global.player.say("Not enough money...");
                                            }
                                        }, new Performer() {
                                    public void perform() {
                                                if (Global.player.requireGold(month+price)) {
                                                    Global.player.respawnInfo = 180 + Global.hour + "," + host.map.name.toString();
                                                    host.say("@sold");
                                                } else Global.player.say("Not enough money...");
                                    }
                                }
                                });
                    } else {
                        int time = Integer.parseInt(Global.player.respawnInfo.split(",")[0])-Global.hour;
                        if (time < 0) Menu.menu.showDialog("Guildsman", "Your previous contract expired "+time+" hours ago.");
                        else Menu.menu.showDialog("Guildsman", "You are already in contract. Your current contract will expire in "+time+" hours.");
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
    public static AI load(Unit host, T p){
        AI r = values()[p.get(0).get(0).getInt()].make(host);
        r.type = p.get(0).get(1).getInt();
        host.brain = r;
        r.load(p.get(1));
        return r;
    }
}