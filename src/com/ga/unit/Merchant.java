package com.ga.unit;

import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;

/**
 * Created by Ga on 7/31/2016.
 */
public class Merchant extends Intelligence {
    enum MerchantType{
        SUBURBAN(new String[]{"Sword","Spear","Dagger"}),
        FOREST(new String[]{"Axe","Fireball","Potion"});
        String[] items;
        MerchantType(String[] items){
            this.items = items;
        }
        Item[] get(){
            Item[] r = new Item[items.length];
            for (int i = 0; i < items.length; i++) {
                r[i] = ItemFactory.produce(items[i],-1);
            }
            return r;
        }
    }
    private MerchantType type;
    private Item[] wares;
    private Timer advertise = new Timer(200, true);
    public Merchant(Man man) {
        super(man);
        host.faction = -1;
        type = MerchantType.values()[man.rand.nextInt(MerchantType.values().length)];
        wares = type.get();
    }
    @Override
    public void hear(P pos, String p){
        super.hear(pos,p);
        if (p.contains("what")) {
            String r = "I'm selling ";
            for (int i = 0; i < type.items.length - 1; i++) {
                r += type.items[i].replace("_"," ") + ",";
            }
            r += " and " + type.items[type.items.length - 1].replace("_"," ") + "!";
            host.say(r);
        } else {
            for (String i : type.items){
                if (p.contains(i)){
                    describe(i);
                }
            }
        }
    }
    private void describe(String p){

    }
    @Override
    public void perceived(Unit p){
        if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                host.say("@c");
            }
        }
    }
    @Override
    public void think(){
        if (advertise.update()) host.say("@ad");
        super.think();
    }
}