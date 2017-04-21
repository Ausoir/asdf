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
        FOOD(new String[][]{new String[]{"Meat","Meal","Ration"}}),
        SHOP(new String[][]{new String[]{"Katana","Broadsword","Lancesword"},
                new String[]{"Tanto","Fireball","Potion","Scroll"},
                new String[]{"Battery","Shell","Grass","Meat","Starfish","Rock"},
                new String[]{"Cotton Clothing Legging","Cotton Clothing Torso","Fabric Clothing Legging","Fabric Clothing Torso",
                        "Adventurewear Legging", "Adventurewear Torso","Leather Armor Torso","Leather Armor Legging",
                        "Chainmail Torso","Chainmail Legging","Platemail Torso","Platemail Legging","Steel Armor Torso","Steel Armor Legging"}}),
        GUILD(new String[][]{new String[]{"Manual","Spellbook","Scroll","Potion","Battery","Adventurewear Torso","Adventurewear Legging","Ration"}}),
        TRAINING(new String[][]{new String[]{"Manual","Spellbook"}});
        private String[][] items;
        MerchantType(String[][] items){
            this.items = items;
        }
        String[] getItems(Man host){
            return items[host.rand.nextInt(items.length)];
        }
    }
    private MerchantType type;
    private Item[] product;
    private int[] price;
    private Performer[] action;
    private Timer advertise = new Timer(200, false);
    private float low;
    private float high;
    private String ad;
    public MerchantIntelligence(Man man, int merchanttype) {
        super(man);
        low = man.rand.nextFloat()+.3f;
        high = man.rand.nextInt(4)+2;
        man.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player) {
                    hear(p,"shop");
                }
            }
        };
        type = MerchantType.values()[merchanttype-1];
        String r = "I'm selling ";
        int length = host.rand.nextInt(48)+1;
        String[] items = new String[length];
        for (int i = 0; i < length; i++){
            String[] typeItems = type.getItems(man);
            int l = typeItems.length;
            items[i] = typeItems[host.rand.nextInt(l)];
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
            if (i < 8) {
                if (i == Math.min(7,length - 1)) r += "and " + item.getFullName() + "!";
                else r += item.getFullName() + ", ";
            }
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
                int[] newPrice = price;
                for (int i=0;i<newPrice.length;i++){
                    newPrice[i] *= Global.player.getShopPrice();
                }
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