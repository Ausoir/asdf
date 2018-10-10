package com.ga.unit.item;

import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import java.util.ArrayList;

/**
 * Created by Ga on 3/26/2017. asd
 */
public enum Crafts {
    Forage(new String[]{"Time:1"}, "Forage firewood from trees, sticks from bushes, vegetables from weed, fish from reed, or a rock if nothing is available."){
        public void craft(Man host, int time){
            ArrayList<Prop> props = host.getAvailableProps(e -> true);
            int count = 0;
            for (Prop i : props){
                count++;
                if (count <= 3) {
                    switch (i.name) {
                        case "Tree":
                            Stock.createByName(host.map, "Firewood", 0, -1).start().pos.set(host.pos);
                            break;
                        case "Bush":
                            Stock.createByName(host.map, "Stick", 0, -1).start().pos.set(host.pos);
                            break;
                        case "Weed":
                            Stock.createByName(host.map, "Vegetables", 0, -1).start().pos.set(host.pos);
                            break;
                        case "Reed":
                            Stock.createByName(host.map, "Fish", 0, -1).start().pos.set(host.pos);
                            break;
                        default:
                            count--;
                            break;
                    }
                }
            }
            if (count == 0) Stock.createByName(host.map, "Rock", 0, -1).start().pos.set(host.pos);
        }
    }, Scrap(new String[]{"Time:0","Rock:0"},"Turns the item being held into scrap of corresponding material"){
        public void craft(Man host, int time){
            if (!host.wieldingNone()) {
                host.say("Turned " + host.weapon.getFullName() + " into scrap.");
                Item item = (Item) Stock.Scrap.create(host.map, 1, -1).start();
                item.setMaterial(host.weapon.getMaterial());
                item.durability = item.uses = host.weapon.uses;
                host.weapon.remove();
                host.setWeapon(item);
            } else host.say("Not holding anything I can turn into scrap...");
        }
    }, Repairkit(new String[]{"Time:0"},"Creates a repairkit with scrap. Requires a table or a desk nearby."){
        public void craft(Man host, int time){
            if (host.getAvailableProps(e -> e.name.toLowerCase().contains("table") || e.name.toLowerCase().contains("desk")).getFirst() == null)
                host.say("Need a table to create a Repairkit!");
            else {
                if (host.weapon.factoryId == Stock.Scrap.ordinal()) {
                    Item item = (Item) Stock.Repairkit.create(host.map, 1, 4).start();
                    item.setMaterial(host.weapon.getMaterial());
                    item.durability = item.uses = host.weapon.uses;
                    host.weapon.remove();
                    host.setWeapon(item);
                    host.say("Made a " + item.getFullName() + ".");
                } else host.say("Need a scrap in hand to create a Repairkit!");
            }
        }
    }, Gauze(new String[]{"Time:0","Scrap Cloth:10"},"Treat bleeding."){
        public void craft(Man host, int time){
            host.say("Treated wound.");
            host.status.triggerEffect(Buffs.Bleed);
        }
    }, Scribe(new String[]{"Time:0", "Scrap Cloth:1"},"Create magical scrolls from spells you know. Scrolls can be used without energy."){
        public void craft(Man host, int time){
            ArrayList<Actions> skills = Actions.getScribables();
            skills.removeIf(e -> host.getStateLevel(e) < 1);
            skills.add(Actions.Identify);
            String[] skillNames = new String[skills.size()];
            Performer[] performers = new Performer[skills.size()];
            for (int i = 0; i < skillNames.length - 1; i++) {
                Actions skill = skills.get(i);
                skillNames[i] = "Scribe "+skill.name() + " (requires "+skill.getEnergy()+" Energy)";
                performers[i] = new Performer() {
                    public void perform() {
                        if (host.requireMana(skill.getEnergy())) {
                            Item scroll = (Item) Stock.Scroll.create(host.map, 0, Actions.getScribables().indexOf(skill));
                            host.setWeapon(scroll);
                        } else host.say("Not enough Energy!");
                    }
                };
            }
            Menu.menu.showDialog("Scribe Spell", "Choose a spell to scribe from currently available spells (Your catalysts: "+host.getTags()+")", skillNames, performers);
        }
    }, Brew(new String[]{"Time:1", "Pot:0", "Water:1", "Vegetables:1"},"Create potions. Requires corresponding catalysts."){
        public void craft(Man host, int time){
            ArrayList<Actions> skills = Actions.getScribables();
            skills.removeIf(e -> host.getStateLevel(e) < 1);
            skills.add(Actions.Identify);
            String[] skillNames = new String[skills.size()];
            Performer[] performers = new Performer[skills.size()];
            for (int i = 0; i < skillNames.length - 1; i++) {
                Actions skill = skills.get(i);
                skillNames[i] = "Scribe "+skill.name() + " (requires "+skill.getEnergy()+" Energy)";
                performers[i] = new Performer() {
                    public void perform() {
                        if (host.requireMana(skill.getEnergy())) {
                            Item scroll = (Item) Stock.Scroll.create(host.map, 0, Actions.getScribables().indexOf(skill));
                            host.setWeapon(scroll);
                        } else host.say("Not enough Energy!");
                    }
                };
            }
            Menu.menu.showDialog("Scribe Spell", "Choose a spell to scribe from currently available spells (Your catalysts: "+host.getTags()+")", skillNames, performers);
        }
    };

    private static T recipes = FileManager.readLiterals("Data/raw/Recipe.txt");
    private String[] ingreds;
    private String description;
    Crafts(String[] ingredients, String description){
        this.ingreds = ingredients;
        this.description = description;
    }
    public static String[] getRecipe(String p){
        Crafts craft = getByName(p);
        if (craft != null){
            assert craft.ingreds != null;
            return craft.ingreds;
        }
        T r = recipes.startsWith(p);
        if (r != null) return r.get(1).get().split(",");
        return new String[]{};
    }
    public static String getDescription(String p){
        Crafts craft = getByName(p);
        if (craft != null) return craft.description;
        else return ((Item) Stock.createByName(null, p, -1)).getDescription();
    }
    public static void craftByName(Man host, String p) {
        T r = craft(false, host, p);
        int time = 0;
        for (T i : r){
            if (i.get(0).get().equals("Time")) time = i.get(2).getInt();
            else if (i.get(1).getInt() < i.get(2).getInt()) return;
        }
        if (craft(true, host, p) != null) {
            Crafts craft = getByName(p);
            if (craft != null) craft.craft(host, time);
            else craftFromReg(host, p, time);
        }
    }
    private static void craftFromReg(Man host, String p, int time) {
        Prop result;
        if (p.contains(":")) {
            result = Stock.createByName(host.map, p.split(":")[0], -1);
            ((Item)result).setMaterial(Item.Material.getByName(p.split(":")[1]));
        } else result = Stock.createByName(host.map, p, -1);
        if (result != null && host.map.updater.sleep(time, true)) {
            result.start();
            if (host.wieldingNone()) host.setWeapon((Item) result);
            else result.pos.set(host.pos);
        }
    }
    public void craft(Man host, int time) {
    }
    public static T craft(boolean craft, Man host, String p) {
        T r = new T();
        String[] recipes = getRecipe(p);
        if (recipes != null){
            ArrayList<String> ingredients = new ArrayList<>();
            ArrayList<String> usage = new ArrayList<>();
            for (String i : recipes){
                String[] v = i.split(":");
                ingredients.add(v[0]);
                usage.add(v[1]);
            }
            if (craft && !host.map.updater.sleep(Integer.parseInt(usage.get(0)), true)) return null;
            ArrayList<Prop> availables = host.getAvailableItems();
            for (int i=0;i<ingredients.size();i++) {
                String ingred = ingredients.get(i);
                int q = 0;
                for (Prop prop : availables) {
                    if (ingred.equals(prop.name)) {
                        if (craft) ((Item) prop).isUsable(true, Integer.parseInt(usage.get(i)));
                        q += ((Item) prop).uses;
                    }
                }
                r.add(new T().add(ingred).add(q+"").add(usage.get(i)));
            }
        }
        return r.build();
    }

    public static Crafts getByName(String p) {
        for (Crafts i : values()) {
            if (i.name().equals(p)){
                return i;
            }
        }
        return null;
    }
    public static void load(Man host, T p){
        for (T i : p) host.crafting.add(i.get());
    }

    public String toString(){
        return name();
    }
}
