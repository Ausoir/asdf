package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.Interactive;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.item.Item;

/**
 * Created by Ga on 7/31/2016. asd
 */
public class MerchantIntelligence extends ResidentIntelligence {
    private enum MerchantType{
        SUBURBAN(new String[]{"Pistol","Revolver","Rifle"}),
        DESERT(new String[]{"Katana","Broadsword","Lancesword"}),
        FOREST(new String[]{"Axe","Fireball","Potion","Scroll"});
        String[] items;
        MerchantType(String[] items){
            this.items = items;
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
        man.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player) {
                    Global.display.menuShop.show("Trader's Wares - \""+host.getConversation()+"\"", product, price, action);
                }
            }
        };
        type = MerchantType.values()[man.rand.nextInt(MerchantType.values().length)];
        String r = "I'm selling ";
        int length = host.rand.nextInt(12)+52;
        String[] items = new String[length];
        for (int i = 0; i < length; i++){
            int l = type.items.length;
            items[i] = type.items[host.rand.nextInt(l)];
        }
        product = new Item[length];
        price = new int[length];
        action = new Performer[length];
        for (int i = 0; i < length; i++) {
            Item item = ((Item) Stock.getByName(items[i],-1)).cancelAdd();
            int value = fluctuate(item.getValue());
            product[i] = item;
            price[i] = value;
            final int ii = i;
            action[i] = new Performer() {
                final int index = ii;
                public void perform() {
                    product[index] = null;
                    host.say("@sold");
                    Global.display.menuShop.setVisible(false);
                }
            };
            if (i == length - 1) r += "and " + item.getFullName() + "!";
            else r += item.getFullName() + ", ";
        }
        ad = r;
    }
    private int fluctuate(int value){
        float fluc = (host.rand.nextFloat()*(high-low))+low;
        return (int) (value * fluc);
    }
    @Override
    public void hear(Prop u, String p){
        super.hear(u, p);
        if (u == Global.player) {
            if (p.equals("shop")) {
                Global.display.menuShop.show("Trader's Wares - \""+host.getConversation()+"\"", product, price, action);
            }
        }
    }
    @Override
    public void perceived(Prop p){
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