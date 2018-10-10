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
                    float itemFluc = host.rand.nextFloat()*.4f+.8f;
                    return (int) ((value/100 * fluctuation * itemFluc + .25f) * 100);
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
                    for (Prop i : inventory){
                        for (int ii=0;ii<wanted.size();ii++){
                            if (i.name.equals(wanted.get(ii).get())){
                                available.add(i);
                                fluctuation.add(priceR[ii]);
                                break;
                            }
                        }
                    }
                    if (available.size() == 0){
                        String list = "I'm a broker. I buy ";
                        for (int i=0;i<wanted.size();i++){
                            if (i != wanted.size()-1) list += wanted.get(i)+"s, ";
                            else if (i > 0) list += "or "+wanted.get(i)+"s.";
                            else list += wanted.get(i)+"s.";
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
                    if (Global.player.insuranceMap.isEmpty()) {
                        Menu.menu.showDialog("Medical Guild Insurer", "Would you like to buy our health insurance plan?\n\n"+
                                " Every time you are in a critical accident, we will bring you here and revive you. We cover all accidents ranging from food poisoning to decapitation.\n"+
                                " The membership fee is "+Dialog.money(hourly)+" per hour."+
                                " The cost per accident is "+Dialog.money(price)+". All insurance coverage is halted until you pay this amount back after an accident.",
                                new String[]{"Sign a contract for a week/42 hours - "+Dialog.money(week),
                                        "Sign a contract for a month/180 hours - "+Dialog.money(month)},
                                new Performer[]{
                                        new Performer() {
                                            public void perform() {
                                                if (Global.player.requireGold(week)) {
                                                    Global.player.insuranceMap = host.map.name.toString();
                                                    Global.player.insuranceExpiry = 42 + Global.hour;
                                                    Global.player.insuranceCost = -price;
                                                    host.say("@sold");
                                                } else Global.player.say("Not enough money...");
                                            }
                                        }, new Performer() {
                                    public void perform() {
                                                if (Global.player.requireGold(month)) {
                                                    Global.player.insuranceMap = host.map.name.toString();
                                                    Global.player.insuranceExpiry = 180 + Global.hour;
                                                    Global.player.insuranceCost = -price;
                                                    host.say("@sold");
                                                } else Global.player.say("Not enough money...");
                                    }
                                }
                                });
                    } else if (Global.player.insuranceCost > 0){ //in debt
                        if (Global.hour % 42 == 0 && Global.player.guild == 1 && Global.player.guildVar != Global.hour){
                            Global.player.guildVar = Global.hour;
                            Global.player.insuranceMap = "";
                            Menu.menu.showDialog("Medical Guild Insurer", "I have cleared your debt of "+Dialog.money(Global.player.insuranceCost)
                                    +". Your next debt void will be in 42 hours.");
                        } else {
                            Menu.menu.showDialog("Medical Guild Insurer - \"You are in debt\"", "You are in debt from your previous insurance contract.",
                                    new String[]{"Pay off my debt of " + Dialog.money(Global.player.insuranceCost)}, new Performer[]{
                                            new Performer() {
                                                public void perform() {
                                                    if (Global.player.requireGold(Global.player.insuranceCost)) {
                                                        Global.player.insuranceMap = "";
                                                        Global.player.say("Payed off debt!");
                                                    }
                                                }
                                            }
                                    });
                        }
                    } else { //already in contract
                        int time = Global.player.insuranceExpiry - Global.hour;
                        if (time < 0){
                            Menu.menu.showDialog("Medical Guild Insurer - \"Contract expired\"", "Your previous contract expired "+time+" hours ago. Speak to me again for another contract.");
                            Global.player.insuranceMap = "";
                        } else {
                            String msg = "You are already in contract"+
                                    ". Your current contract's cost per accident is "+Dialog.money(-Global.player.insuranceCost)+
                                    ". Contract will last for "+time+" more hours.";
                            if (!Global.player.insuranceMap.equals(host.map.name.toString())) msg += "\nNow, compare this to our insurance:"+
                                    " Hourly membership fee of "+Dialog.money(hourly)+" and "+Dialog.money(price)+" down payment per accident!";
                            Menu.menu.showDialog("Insurer - \"Review your contract\"", msg,
                                    new String[]{"Terminate current contract early"}, new Performer[]{
                                            new Performer(){
                                                public void perform(){
                                                    Global.player.insuranceMap = "";
                                                    Global.player.say("Current insurance policy has been terminated early!");
                                                }
                                            }
                                    });
                        }
                    }
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
                            optionActions.add(new Performer() {
                                public void perform() {
                                    if (Global.player.requireGold(price)) {
                                        i.stitch();
                                        host.say("@sold");
                                    } else Global.player.say("Not enough money...");
                                }
                            });
                        }
                    }
                    for (Buffs.Buff i : Global.player.status.getEffects()){
                        optionNames.add("Cure "+i.buff.name()+" - "+Dialog.money(price/2));
                        optionActions.add(new Performer(){
                            public void perform(){
                                if (Global.player.requireGold(price)){
                                    i.finish();
                                    host.say("@sold");
                                } else Global.player.say("Not enough money...");
                            }
                        });
                    }
                    Menu.menu.showDialog("Medical Guild Doctor", "What do you need?",
                            optionNames.toArray(new String[0]), optionActions.toArray(new Performer[0]));
                }
            });
        }
    }, Guildsman{
        public AI make(Unit host) {
            return process(new NeutralAI(host) {
                void shop() {
                    String title = "Guild ";
                    String desc = "";
                    if (type == 1){
                        title = "Medical Guild";
                        desc = ", where patients come first. We are a massive conglomerate of hospitals and alchemists who work together to eradicate all suffering in the world. "+
                                "If you join us, we can void your insurance debt every week.";
                    } else if (type == 2){
                        title = "Merchants Guild";
                        desc = "! We are a mighty combination of translocaters and pawnbrokers who will certainly make your life easier. Join us, and access our massive stock of memoryfoam"+
                        " as well as our brokers who would love to buy from you.";
                    } else if (type == 3){
                        title = "Mercenary Guild";
                        desc = ". We are the main military force of these lands. Anyone is welcome to use our services and help handle quests, but only members will rise in ranks."
                        +" Ranking officers are given troops to command.";
                    }
                    if (Global.player.guild == -1) {
                        Menu.menu.showDialog(title + " " + host.name, "Welcome to the "+title+desc+" \nRegistration fee is "
                                        + Dialog.money(10000) + ". Registration will override any other guild membership you may already have. Do you wish to join us?",
                                new String[]{"Join the " + title}, new Performer[]{new Performer() {
                                    public void perform() {
                                        if (Global.player.requireGold(10000)) {
                                            Global.player.guild = type;
                                            Global.player.guildVar = 0;
                                        }
                                    }
                                }});
                    } else if (Global.player.guild == 1){
                        Menu.menu.showDialog(title + " " + host.name, "Welcome, member."+
                                (Global.hour % 42 == 0?"The debt voiding is in effect right now. Speak to your insurer to void your debt."
                                        :" Your next debt void will be in "+(Global.hour % 42)+" hours."));
                    } else if (Global.player.guild == 2){
                        Menu.menu.showDialog(title + " " + host.name, "Welcome. How can I be of service?",new String[]{"Buy a memoryfoam for "+Dialog.money(500)},
                                new Performer[]{new Performer(){
                                    public void perform(){
                                        if (Global.player.requireGold(500)) Global.player.setWeapon((Item) Stock.Memoryfoam.create(host.map,0,0));
                                        else Global.player.say("Not enough money...");
                                    }
                                }});
                    } else if (Global.player.guild == 3){
                        String rank = "soldier (rank 0)";
                        if (Global.player.guildVar == 1) rank = "specialist (rank 1)";
                        else if (Global.player.guildVar == 2) rank = "leader (rank 2)";
                        else if (Global.player.guildVar == 3) rank = "sergeant (rank 3)";
                        Menu.menu.showDialog(title + " " + host.name, "Welcome, "+rank,new String[]{"Take a member"},
                                new Performer[]{new Performer(){
                                    public void perform(){
                                        if (Global.player.guildVar > 0){
                                        } else Global.player.say("Rank not high enough...");
                                    }
                                }});
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