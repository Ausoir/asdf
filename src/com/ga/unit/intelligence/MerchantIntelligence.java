package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Matter;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;

import static com.ga.game.Display.shop;

/**
 * Created by Ga on 7/31/2016.
 */
public class MerchantIntelligence extends Intelligence {
    enum MerchantType{
        SUBURBAN(new String[]{"Pistol","Revolver","Rifle"}),
        FOREST(new String[]{"Axe","Fireball","Potion"});
        String[] items;
        MerchantType(String[] items){
            this.items = items;
        }
    }
    class Product{
        Item item;
        int value;
        public Product(Item item, int value){
            this.item = item;
            this.value = value;
        }
    }
    private MerchantType type;
    private Item[] product;
    private int[] price;
    private Performer[] action;
    private Timer advertise = new Timer(200, false);
    private float low = .5f;
    private float high = 4f;
    private String ad;
    public MerchantIntelligence(Man man) {
        super(man);
        comm = new Communication();
        type = MerchantType.SUBURBAN;//MerchantType.values()[man.rand.nextInt(MerchantType.values().length)];
        String r = "I'm selling ";
        int length = host.rand.nextInt(12)+2;
        String[] items = new String[length];
        for (int i = 0; i < length; i++){
            int l = type.items.length;
            items[i] = type.items[host.rand.nextInt(l)];
        }
        product = new Item[length];
        price = new int[length];
        action = new Performer[length];
        for (int i = 0; i < length; i++) {
            Item item = ItemFactory.produce(items[i], -1, 0);
            int value = fluctuate(item.getValue());
            product[i] = item;
            price[i] = value;
            final int ii = i;
            action[i] = new Performer() {
                final int index = ii;
                public boolean perform() {
                    product[index] = null;
                    host.say("@sold");
                    shop.setVisible(false);
                    return true;
                }
            };
            String desc = "This is " + item.getDescription();
            desc += " This is a " + item.getRarityDescription() + " item.";
            desc += " I'll sell it for " + value + " gold!";
            comm.addReply(items[i].toLowerCase(), desc);
            comm.addTopic(items[i]);
            if (i == length - 1) r += "and " + item.getFullName() + "!";
            else r += item.getFullName() + ", ";
        }
        ad = r;
        comm.addReply("shop","@shop");
    }
    private int fluctuate(int value){
        float fluc = (host.rand.nextFloat()*(high-low))+low;
        return (int) (value * fluc);
    }
    @Override
    public void hear(Matter u, String p){
        super.hear(u, p);
        if (u == Global.player.host) {
            if (p.equals("shop")) {
                shop.show("Trader's Wares - \""+host.getConversation()+"\"", product, price, action);
            }
        }
    }
    @Override
    public void perceived(Matter p){
        if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                setTaskEcho(new Idle(host));
                if (advertise.over() && host.getConversation().equals("")){
                    if (host.rand.nextFloat() < .5f) host.say("@c");
                    else host.say(ad);
                    advertise.reset();
                }
            }
        }
    }
    @Override
    public void update(){
        super.update();
        advertise.update();
    }
}