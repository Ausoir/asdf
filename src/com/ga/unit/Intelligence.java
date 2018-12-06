package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.map.GenerateBiome;
import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.tool.Timer;
import com.ga.unit.item.Item;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

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
            public boolean perceived(Prop p){
                if (p instanceof Unit) {
                    if (p.hittable(host.faction)) {
                        target = (Unit) p;
                        if (host.getConversation().isEmpty()) host.say("@c tgt");
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
                private float fluctuation;

                void load(T p) {
                    super.load(p);
                    String[] data = FileManager.read("Data/raw/Shop.txt").split("%");
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    fluctuation = host.rand.nextFloat()*.4f+.8f;
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
                        if (item == null){
                            item = (Item) Stock.createByName(host.map, "Error Item", 0);
                            item.name = items[i];
                        }
                        int value = fluctuate(item.getValue());
                        products[i] = item;
                        prices[i] = value;
                        final int finalI = i;
                        actions[i] = new Performer(()->{
                            host.map.updater.addQueue.add(products[finalI]);
                            products[finalI] = null;
                            host.say("@sold");
                        });
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
                    float itemFluc = host.rand.nextFloat()*.4f+.8f;
                    return (int) ((value/100f * fluctuation * itemFluc + .25f) * 100);
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
                            "Rest for 3 hours? Costs "+Dialog.money(price)+". Restore Fatigue, heal 1 Vitality, and gain 10 Motivation. Food is paid for.",
                            new String[]{"Pay"},
                            new Performer[]{
                                    new Performer(()->{
                                            if (Global.player.requireGold(price)) {
                                                Global.player.stats.nutrition.add(3, true);
                                                if (Global.player.map.updater.sleep(3, true)){
                                                    Global.player.stats.motivation.add(10,false);
                                                    Global.player.stats.fatigue.heal();
                                                    Global.player.stats.agility.add(1, false);
                                                    Global.player.stats.strength.heal();
                                                } else {
                                                    Global.player.stats.nutrition.add(-3, true);
                                                    Global.player.obtainGold(price);
                                                }
                                            } else {
                                                Global.player.say(Dialog.get("!$"));
                                            }
                                    })
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
                    String message = "I can teach you useful things if you have the time, motivation, and money. You will, of course, need an extra memory slot.";
                    message += "\nRequires "+Dialog.money(price)+", "+reqMot+" motivation, and "+reqTime+" hours.";
                    message += "\n\nCurrent motivation: "+Global.player.stats.motivation.get();
                    message += "\nCurrent nutrition: "+Global.player.stats.nutrition.get();
                    Menu.menu.showDialog("Trainer", message,
                        new String[]{"Begin training"},
                        new Performer[]{
                            new Performer(()->{
                                if (Global.player.traits.canLearn(trait)) {
                                    if (Global.player.canLearn()) {
                                        if (Global.player.stats.motivation.get() >= reqMot) {
                                            if (Global.player.getGold() >= price) {
                                                if (Global.player.map.updater.sleep(reqTime, true)) {
                                                    Global.player.learnTrait(trait);
                                                    Global.player.stats.motivation.require(reqMot);
                                                    Global.player.requireGold(price);
                                                }
                                            } else Global.player.say(Dialog.get("!$"));
                                        } else Global.player.say(Dialog.get("!m"));
                                    } else Global.player.say(Dialog.get("!I"));
                                } else Global.player.say("Already at max level!");
                            })
                        });
                }
            });
        }
    },
    Dealer{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                private T wanted;
                private float[] priceR;

                void load(T p) {
                    super.load(p);
                    wanted = FileManager.readLiteral("Data/raw/Pawn.txt", host.seed);
                    advertise = new Timer(100+host.rand.nextInt(200), false);
                    priceR = new float[wanted.size()];
                    for (int i=0;i<priceR.length;i++){
                        priceR[i] = host.rand.nextFloat()+.1f;
                    }
                }

                void shop() {
                    super.shop();
                    ArrayList<Prop> inventory = Global.player.getAvailableItems();
                    ArrayList<Prop> available = new ArrayList<>();
                    ArrayList<Float> fluctuation = new ArrayList<>();
                    for (Prop i : inventory) {
                        for (int ii = 0; ii < wanted.size(); ii++) {
                            if (i.name.equals(wanted.get(ii).get())) {
                                available.add(i);
                                fluctuation.add(priceR[ii]);
                                break;
                            }
                        }
                    }
                    if (available.size() == 0) {
                        String list = "I'm a broker. I buy ";
                        for (int i = 0; i < wanted.size(); i++) {
                            if (i != wanted.size() - 1) list += wanted.get(i) + "s, ";
                            else if (i > 0) list += "or " + wanted.get(i) + "s.";
                            else list += wanted.get(i) + "s.";
                        }
                        Menu.menu.showDialog("Merchant Guild Broker", list + " Do you need something?");
                    } else {
                        Performer[] action = new Performer[available.size()];
                        String[] options = new String[action.length];
                        for (int i = 0; i < action.length; i++) {
                            final Item item = (Item) available.get(i);
                            final int price = (int) (item.getValue() * fluctuation.get(i));
                            options[i] = item.getFullName() + " - " + Dialog.money(price);
                            action[i] = new Performer(()-> {
                                Global.player.dropItem(item).remove();
                                Global.player.obtainGold(price);
                                Global.player.say("Sold " + item.getName() + " for " + Dialog.money(price) + "!");
                                host.say("@sold");
                            });
                        }
                        Menu.menu.showDialog("List",
                                "We buy from the listed items.", options, action);
                    }
                }
            });
        }
    }, Insurer {
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
                    if (Global.player.guild == 1) {
                        if (Global.player.insuranceMap.isEmpty()) {
                            Menu.menu.showDialog("Medical Guild Insurer", "Would you like to buy our health insurance plan?\n\n" +
                                            " Every time you are in a critical accident, we will bring you here and revive you. We cover all accidents ranging from food poisoning to decapitation.\n" +
                                            " The membership fee is " + Dialog.money(hourly) + " per hour." +
                                            " The cost per accident is " + Dialog.money(price) + ". All insurance coverage is halted until you pay this amount back after an accident.",
                                    new String[]{"Sign a contract for a week/42 hours - " + Dialog.money(week),
                                            "Sign a contract for a month/180 hours - " + Dialog.money(month)},
                                    new Performer[]{
                                        new Performer(()-> {
                                            if (Global.player.requireGold(week)) {
                                                Global.player.insuranceMap = host.map.name.toString();
                                                Global.player.insuranceExpiry = 42 + Global.hour;
                                                Global.player.insuranceCost = -price;
                                                host.say("@sold");
                                            } else Global.player.say(Dialog.get("!$"));
                                        }), new Performer(()-> {
                                            if (Global.player.requireGold(month)) {
                                                Global.player.insuranceMap = host.map.name.toString();
                                                Global.player.insuranceExpiry = 180 + Global.hour;
                                                Global.player.insuranceCost = -price;
                                                host.say("@sold");
                                            } else Global.player.say(Dialog.get("!$"));
                                        })
                                    });
                        } else if (Global.player.insuranceCost > 0) { //in debt
                            Menu.menu.showDialog("Medical Guild Insurer - \"You are in debt\"", "You are in debt from your previous insurance contract.",
                                    new String[]{"Pay off my debt of " + Dialog.money(Global.player.insuranceCost)}, new Performer[]{
                                        new Performer(()-> {
                                            if (Global.player.requireGold(Global.player.insuranceCost)) {
                                                Global.player.insuranceMap = "";
                                                Global.player.say("Payed off debt!");
                                            }
                                        })
                                    });
                        } else { //already in contract
                            int time = Global.player.insuranceExpiry - Global.hour;
                            if (time < 0) {
                                Menu.menu.showDialog("Medical Guild Insurer - \"Contract expired\"", "Your previous contract expired " + time + " hours ago. Speak to me again for another contract.");
                                Global.player.insuranceMap = "";
                            } else {
                                String msg = "You are already in contract" +
                                        ". Your current contract's cost per accident is " + Dialog.money(-Global.player.insuranceCost) +
                                        ". Contract will last for " + time + " more hours.";
                                if (!Global.player.insuranceMap.equals(host.map.name.toString()))
                                    msg += "\nNow, compare this to our insurance:" +
                                            " Hourly membership fee of " + Dialog.money(hourly) + " and " + Dialog.money(price) + " down payment per accident!";
                                Menu.menu.showDialog("Insurer - \"Review your contract\"", msg,
                                        new String[]{"Terminate current contract early"}, new Performer[]{
                                            new Performer(()-> {
                                                Global.player.insuranceMap = "";
                                                Global.player.say("Current insurance policy has been terminated early!");
                                            })
                                        });
                            }
                        }
                    } else Menu.menu.showDialog("Medical Guild Insurer", "I'm sorry, you must be a member of the Medical Guild to use my services.");
                }
            });
        }
    }, Doctor{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
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
                    int price = 5000;
                    ArrayList<String> optionNames = new ArrayList<>();
                    ArrayList<Performer> optionActions = new ArrayList<>();
                    for (Organ i : Global.player.clip.organs){
                        if (i.amputated) {
                            optionNames.add("Get prosthetics for amputated " + i.name + " - " + Dialog.money(price));
                            optionActions.add(new Performer(()-> {
                                if (Global.player.requireGold(price)) {
                                    i.stitch();
                                    host.say("@sold");
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                        }
                    }
                    for (Buffs.Buff i : Global.player.status.getEffects()){
                        optionNames.add("Cure "+i.buff.name()+" - "+Dialog.money(price/2));
                        optionActions.add(new Performer(()->{
                            if (Global.player.requireGold(price)){
                                i.finish();
                                host.say("@sold");
                            } else Global.player.say(Dialog.get("!$"));
                        }));
                    }
                    Menu.menu.showDialog("Medical Guild Doctor", "What do you need?",
                            optionNames.toArray(new String[0]), optionActions.toArray(new Performer[0]));
                }
            });
        }
    }, Transporter{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                void shop() {
                    ArrayList<String> options = new ArrayList<>();
                    ArrayList<Performer> actions = new ArrayList<>();
                    int cost = 1000;
                    int mapVar = host.map.name.getIndex();
                    int east = 0, west = 0;
                    for (int i=0;i<20;i++){
                        int finalI = i;
                        if (east == 1 && GenerateBiome.getTown(mapVar + i)){
                            options.add("Travel East - "+i+" hours away)");
                            final int fCost = cost + 1000 * i;
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(fCost)){
                                    if (Global.player.map.updater.sleep(1, true)){
                                        Global.server.requestChangeMap(Global.player.map.name.getNext(finalI).toString() +",2");
                                    }
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                            east = 2;
                        } else if (east == 0) east = 1; //disconnected
                        if (west == 1 && GenerateBiome.getTown(mapVar + i)){
                            options.add("Travel West - "+i+" hours away)");
                            final int fCost = cost + 1000 * i;
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(fCost)){
                                    if (Global.player.map.updater.sleep(1, true)){
                                        Global.server.requestChangeMap(Global.player.map.name.getNext(-finalI).toString() +",1");
                                    }
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                            west = 2;
                        } else if (west == 0) west = 1;
                    }
                    Menu.menu.showDialog("Merchant Guild Translocator", "I can translocate you to the nearest towns for "+Dialog.money(cost)
                                    +" in 1 hour regardless of distance.",
                            options.toArray(new String[0]), actions.toArray(new Performer[0]));
                }
            });
        }
    }, Guildsman{
        public AI make(Unit host) {
            int entranceFee = 10000, exitFee = 100000;
            return process(new NeutralAI(host) {
                void shop() {
                    super.shop();
                    String title = "Guild ";
                    String desc = "";
                    if (type == 1){
                        title = "Medical Guild";
                        desc = "We are a massive conglomerate of hospitals and alchemists around the world working together for a better world. "+
                                "Register to become a member and you will be qualified to join our health track program, which allows us to revive you for a fee.";
                    } else if (type == 2){
                        title = "Merchants Guild";
                        desc = "Merchants guild is a guild of brokers continuing the legacy of the once-powerful translocation guild. We are a powerful group of middlemen who can make you a whole lot richer. "+
                                "You may join us and our journeys in the art of investments, which is the secret art of making money with money.";
                    } else if (type == 3){
                        title = "Mercenary Guild";
                        desc = "We are the main military force of these lands. A powerful organization consisting of countless mercenary bands from all over the world, we are both loyal and public. "+
                                "Join us, and access our highly competent mercenary companions for hire.";
                    }
                    if (Global.player.guild == -1) {
                        String finalTitle = title + " " + host.name;
                        Menu.menu.showDialog(finalTitle, "Welcome to the "+title+". "+desc+" \nRegistration fee is "
                                        + Dialog.money(entranceFee) + ". If you change your mind later and wish to join another guild you must pay an exit fee of "+Dialog.money(exitFee)
                                        + ". Do you wish to join us?",
                                new String[]{"Join the " + title}, new Performer[]{new Performer(()->{
                                        if (Global.player.requireGold(entranceFee)) {
                                            Global.player.guild = type;
                                            Global.player.guildVar = 0;
                                            if (type == 1)
                                                Menu.menu.showDialog(finalTitle, "If you wish to register with our health insurance system, please speak to our insurer.");
                                            else if (type == 2)
                                                Menu.menu.showDialog(finalTitle, "Thank you for your patronage. Speak to me again to make an investment.");
                                            else if (type == 3)
                                                Menu.menu.showDialog(finalTitle, "Welcome to the world of mercenaries. Speak to me again for services.");
                                        } else Global.player.say(Dialog.get("!$"));
                                })});
                    } else {
                        ArrayList<String> options = new ArrayList<>(){{add("Pay an exit fee of "+Dialog.money(exitFee)+" and terminate affiliation");}};
                        ArrayList<Performer> actions = new ArrayList<>(){{add(new Performer(()->{
                            if (Global.player.requireGold(exitFee)) {
                                Global.player.guild = -1;
                                Global.player.guildVar = 0;
                            } else Global.player.say(Dialog.get("!$"));
                        }));}};
                        if (type == 1) {
                            options.add("Buy vitamins - "+Dialog.money(2400));
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(2400)){
                                    Global.player.setWeapon((Item) Stock.Vitamin.create(Global.player.map, 1, -1).start());
                                    Global.player.say("Bought Vitamins.");
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                        } else if (type == 2){
                            options.add("Join investment group - "+Dialog.money(40000)+", ");
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(40000)){
                                    Global.player.say("Joined investment group.");
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                        } else if (type == 3){
                            options.add("Upgrade mercenary services - "+Dialog.money(10000));
                            options.add("Change mercenary service type - "+Dialog.money(500));
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(10000)){
                                    Global.player.guildVar++;
                                    if (Global.player.getAvailableItems().removeIfReuse(e -> e.factoryId != Stock.Radio.ordinal()).isEmpty()) {
                                        Global.player.setWeapon((Item) Stock.Radio.create(Global.player.map, 1, -1).start());
                                        Global.player.say("Received call radio.");
                                    } else Global.player.say("Upgraded service.");
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                            actions.add(new Performer(()->{
                                if (Global.player.requireGold(500)){
                                    Global.player.guildVar *= -1;
                                    if (Global.player.guildVar < 0) Global.player.say("Now employing melee mercenaries.");
                                    else Global.player.say("Now employing ranged mercenaries.");
                                } else Global.player.say(Dialog.get("!$"));
                            }));
                        }
                        Menu.menu.showDialog(title + " " + host.name, "Welcome, member. How can I help you?",
                                options.toArray(new String[0]), actions.toArray(new Performer[0]));
                    }
                }
            });
        }
    }, Officer{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                void shop() {
                    super.shop();
                    if (Global.player.guild == 3) {
                        Menu.menu.showDialog("Mercenary Guild Officer", "Recruit",
                                new String[]{"Depart to mission"}, new Performer[]{new Performer(()->{
                                    String mapName = Global.player.map.name.getMapname();
                                    mapName = mapName.substring(0,mapName.lastIndexOf("_"))+"1";
                                    Global.server.requestChangeMap(FileManager.makeDirectory(mapName+",1"));
                                })});
                    } else Menu.menu.showDialog("Mercenary Guild Officer", "Hello. Speak to our recruiter if you wish to join us.");
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