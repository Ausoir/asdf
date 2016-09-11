package com.ga.unit;

import com.ga.tool.Timer;
import com.ga.tool.Word;
import com.ga.unit.intelligence.Idle;
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
    private int[] values;
    private Timer advertise = new Timer(200, false);
    private float low = .5f;
    private float high = 4f;
    private int offer = -1;
    public Merchant(Man man) {
        super(man);
        host.faction = Faction.Neutral;
        type = MerchantType.values()[man.rand.nextInt(MerchantType.values().length)];
        wares = type.get();
        values = new int[wares.length];
        for (int i = 0; i < wares.length; i++) {
            Item ware = wares[i];
            values[i] = fluctuate(ware.getValue());
        }
    }
    @Override
    public void hear(Matter u, String p){
        super.hear(u,p);
        task = new Idle(host);
        host.face(u.pos);
        if (u.isFacing(host.pos) && Math.abs(u.pos.x-host.pos.x) < 160)
        if (Word.Interest.is(p)) {
            String r = "I'm selling ";
            for (int i = 0; i < type.items.length - 1; i++) {
                r += wares[i].getFullName().replace("_"," ") + ", ";
            }
            r += "and " + wares[type.items.length - 1].getFullName().replace("_"," ") + "!";
            host.say(r);
        } else if (Word.Negative.is(p)) {
            host.say("@denied");
            offer = -1;
        } else if (Word.Affirmitive.is(p) || Word.Buy.is(p)) {
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
        float fluc = (float) ((host.rand.nextFloat()*(high-low))+low);
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
    public void perceived(Matter p){
        if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                task = new Idle(host);
                if (advertise.over() && !host.isSpeaking()){
                    host.say("@c");
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