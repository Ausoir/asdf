package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.map.Global;
import com.ga.tool.*;
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

                void load(String p) {
                    super.load(p);
                    String[] data = FileManager.readText("Data/raw/Shop.txt");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    low = host.rand.nextFloat()*2 + .5f;
                    high = host.rand.nextFloat()*2 + 2.5f;
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
                        Item item = ((Item) Stock.createByName(host.map, items[i], -1));
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

                void shop(){
                    int[] newPrice = prices;
                    for (int i = 0; i < newPrice.length; i++) {
                        newPrice[i] *= Global.player.getShopPrice();
                    }
                    Menu.menu.showShop("Trader's Wares - \"" + host.getConversation() + "\"",
                            products, prices, actions);
                }

                private int fluctuate(int value) {
                    float fluc = host.traits.has(Traits.Trait.Haggler) ? ((high+low+low)/3) : (host.rand.nextFloat() * (high - low)) + low;
                    return (int) (value * fluc);
                }
            });
        }
    }, Dealer{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private String[] wanted;
                private float[] priceR;

                void load(String p) {
                    super.load(p);
                    String[] data = FileManager.readText("Data/raw/Pawn.txt");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    wanted = data[host.seed % data.length].split(";");
                    priceR = new float[wanted.length];
                    for (int i=0;i<priceR.length;i++){
                        priceR[i] = host.rand.nextFloat()+.1f;
                    }
                }

                void shop(){
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
    }, Innkeep {
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private int price;

                void load(String p) {
                    super.load(p);
                    price = (host.rand.nextInt(30)+30)*100+99;
                }

                void shop() {
                    Menu.menu.showDialog("Inn",
                            "Rest for the night? "+Dialog.money(price)+" for 3 hours. Heal Fatigue, Stamina, Vitality, Energy, and get 10 Motivation. Food is paid for.",
                            new String[]{"Pay for the night"},
                            new Performer[]{
                                    new Performer(){
                                        public void perform(){
                                            if (Global.player.requireGold(price)) {
                                                Global.player.get("Nutrition").add(3, true);
                                                if (Global.player.map.updater.sleep(3, true)){
                                                    Global.player.get("Motivation").add(10,false);
                                                    Global.player.get("Fatigue").add(9,false);
                                                    Global.player.get("Agility").heal();
                                                    Global.player.get("Intelligence").heal();
                                                    Global.player.get("Strength").heal();
                                                } else {
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
    }, Trainer {
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private String stat;
                private int price;

                void load(String p) {
                    super.load(p);
                    ArrayList<Traits.Trait> traits = Traits.Trait.getByType(3);
                    String[] stats = new String[traits.size()+3];
                    stats[0] = "Strength";
                    stats[1] = "Agility";
                    stats[2] = "Intelligence";
                    for (int i=3;i<stats.length;i++){
                        stats[i] = traits.get(i-3).name();
                    }
                    stat = stats[type];
                    price = (host.rand.nextInt(30)+30)*100+99;
                }

                void shop() {
                    int[] requirements = Global.player.getLevelRequirements(1);
                    String message = "Would you like to train your "+stat+"?";
                    message += "Requires "+Dialog.money(price)+", "+requirements[0]+" motivation, and "+requirements[1]+" hours.";
                    message += "\nCurrent motivation: "+Global.player.get("Motivation").get();
                    message += "\nCurrent nutrition: "+Global.player.get("Nutrition").get();
                    Menu.menu.showDialog("Trainer", message,
                            new String[]{"Begin training"},
                            new Performer[]{
                                    new Performer(){
                                        public void perform(){
                                            if (Global.player.requireGold(price)) {
                                                if (!Global.player.level(stat, 1)) Global.player.obtainGold(price);
                                            } else {
                                                Global.player.say("Not enough money..");
                                            }
                                        }
                                    }
                            });
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
        String[] data = p.split(";");
        String[] values = data[0].split(":");
        AI r = values()[Integer.parseInt(values[0])].make(host);
        r.type = Integer.parseInt(values[1]);
        host.brain = r;
        r.load(data[1]);
        return r;
    }
}