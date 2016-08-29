package com.ga.unit;

import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.tool.Word;
import com.ga.unit.intelligence.Idle;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;

import java.util.Dictionary;

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
    private int[] values;
    private Timer advertise = new Timer(200, true);
    private float low = .5f;
    private float high = 4f;
    private int offer = -1;
    public Merchant(Man man) {
        super(man);
        host.faction = -1;
        type = MerchantType.values()[man.rand.nextInt(MerchantType.values().length)];
        wares = type.get();
        values = new int[wares.length];
        for (int i = 0; i < wares.length; i++) {
            Item ware = wares[i];
            values[i] = fluctuate(ware.getValue());
        }
    }
    @Override
    public void hear(Unit u, String p){
        super.hear(u,p);
        task = new Idle(host);
        host.face(u.pos);
        if (p.contains("what")) {
            String r = "I'm selling ";
            for (int i = 0; i < type.items.length - 1; i++) {
                r += type.items[i].replace("_"," ") + ",";
            }
            r += " and " + type.items[type.items.length - 1].replace("_"," ") + "!";
            host.say(r);
        } else if (Word.Affirmitive.is(p) || p.contains("buy")) {
            if (target != null && offer != -1){
                if (target instanceof Player){
                    Player player = ((Player) target);
                    if (player.gold >= values[offer]){
                        if (player.inventory.vacant()){
                            player.gold -= values[offer];
                            player.addItem(wares[offer]);
                            host.say("@sold");
                        }
                    }
                }
            }
        } else {
            for (int i=0;i<wares.length;i++){
                if (p.contains(wares[i].name.toLowerCase())) describe(i);
            }
        }
    }
    private int fluctuate(int value){
        float fluc = (host.rand.nextFloat()*(high-low))+low;
        return (int) (value * fluc);
    }
    private void describe(int offer){
        this.offer = offer;
        String r = "This is "+wares[offer].getDescription();
        r += " This is a "+wares[offer].getRarityDescription()+" item.";
        r += " I'll sell it for "+values[offer]+" gold!";
        host.say(r);
    }
    @Override
    public void perceived(Unit p){
        if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                host.say("@c");
                task = new Idle(host);
            }
        }
    }
    @Override
    public void think(){
        if (advertise.update()) host.say("@ad");
        super.think();
    }
}