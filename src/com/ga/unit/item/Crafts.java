package com.ga.unit.item;

import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Tag;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import java.util.ArrayList;

/**
 * Created by Ga on 3/26/2017. asd
 */
public enum Crafts {
    Make_Scrap(new String[]{"Time:0", "Hard:1"}){
        public void craft(Man host, int time){
            host.say("Turned "+host.weapon.getName()+" into scrap.");
            Item item = (Item) Stock.Scrap.create(host.map, 1, -1).start();
            item.setMaterial(host.weapon.getMaterial());
            host.weapon.remove();
            host.setWeapon(item);
        }
    }, Treat_Wound(new String[]{"Time:1", "Sharp:1", "Resin:1"}){
        public void craft(Man host, int time){
            host.say("Treated wound.");
            host.status.triggerEffect(Buffs.Bleed);
        }
    }, Scribe_Scroll(new String[]{"Time:0", "Scrap_Cloth:*"}){
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
    }, Brew_Potion(new String[]{"Time:1", "Pot:0", "Water:1", "Weed:*"}){
        public void craft(Man host, int time){
            host.say("Wound patch applied.");
            host.status.triggerEffect(Buffs.Bleed);
        }
    }, Turret(new String[]{"Time:2", "Rock:*", "Rock:*", "Rock:*", "Bush:*", "Pot:*"}){
        public void craft(Man host, int time){
            host.say("Turret built.");
            Stock.createByName(host.map, "Resident", -1).start().pos.set(host.pos.getAdd(host.target));
        }
    };

    private static T recipes = FileManager.readLiterals("Data/raw/Recipe.txt");
    private String[] ingreds;
    Crafts(String[] ingredients){
        this.ingreds = ingredients;
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
    public static void craftByName(Man host, String p) {
        int time = canCraft(host, p);
        if (time >= 0){
            Crafts craft = getByName(p);
            if (craft != null) craft.craft(host, time);
            else craftRaw(host, p, time);
        }
    }
    private static void craftRaw(Man host, String p, int time) {
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
    public static int canCraft(Man host, String p) {
        String[] recipes = getRecipe(p);
        if (recipes != null){
            ArrayList<String> ingredients = new ArrayList<>();
            ArrayList<String> usage = new ArrayList<>();
            for (String i : recipes){
                String[] v = i.split(":");
                ingredients.add(v[0]);
                usage.add(v[1]);
            }
            for (Tag tag : host.getTags()){
                int index = ingredients.indexOf(tag.name());
                if (index != -1 && host.requireMana(1)){
                    int cUsage = Integer.parseInt(usage.get(index));
                    if (cUsage > 1){
                        usage.set(index,(cUsage-1)+"");
                    } else {
                        ingredients.remove(index);
                        usage.remove(index);
                    }
                }
            }
            ArrayList<Prop> availables = host.getAvailableItems();
            availables.addAll(host.getAvailableProps());
            for (Prop i : availables){
                int index = -1;
                indexOf:
                for (int ii=0;ii<ingredients.size();ii++){
                    String current = ingredients.get(ii);
                    String[] currents;
                    if (current.contains("/")) currents = current.split("/");
                    else currents = new String[]{current};
                    for (String current1 : currents) {
                        if (current1.equals(i.name)) {
                            index = ii;
                            break indexOf;
                        }
                    }
                }
                if (index == -1) {
                    for (Tag tag : i.tags){
                        index = ingredients.indexOf(tag.name());
                        if (index != -1 && host.requireMana(1)){
                            ingredients.remove(index);
                            usage.remove(index);
                            index = -1;
                            break;
                        }
                    }
                }
                if (index != -1){
                    ingredients.remove(index);
                    String use = usage.remove(index);
                    if (use.equals("*")) i.remove();
                    else if (!use.equals("0") && i instanceof Item) ((Item) i).isUsable(true, Integer.parseInt(use));
                }
            }
            if (ingredients.size() == 1){
                int time = Integer.parseInt(usage.get(0));
                if (host.stats.nutrition.get() >= time) return time;
            }
        }
        return -1;
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
