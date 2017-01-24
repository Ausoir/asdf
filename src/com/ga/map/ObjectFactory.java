package com.ga.map;

import com.ga.tool.Interactive;
import com.ga.tool.P;
import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.Updater;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 8/2/2016.
 */
public class ObjectFactory {
    private static String[] rotors = new String[]{
            "Propeller","Clockwork"
    };
    private static boolean has(String[] items, String p){
        for (String i : items){
            if (p.startsWith(i)) return true;
        }
        return false;
    }
    public static Matter produce(String name, P pt, int seed){
        return produce(name, pt, seed, 5);
    }
    public static Matter produce(String name, P pt, int seed, int depth){
        Matter result;
        if (has(rotors, name)){
            result = new Matter(name, seed, pt, depth){
                private int side = seed % 2 == 0 ? -1 : 1;
                public void update(){
                    super.update();
                    rotation+=(10f*side)/image.size.x;
                }
            };
            result.setDepth(11);
        } else if (name.equals("PlayerSpawn")) {
            result = new Matter(name, seed, pt, Updater.editor?10:0);
            result.name = "PlayerSpawn";
        } else if (name.equals("Phone_Booth")) {
            result = new Matter(name, seed, pt, depth);
            result.onInteract = new Interactive(80){
                public void interact(Unit p){
                    String t = p.getConversation();
                    if (t.equals("")){
                        Global.server.online();
                        p.say("Waiting for a call...");
                    } else if (t.toLowerCase().startsWith("call")) {
                        Global.server.requestConnection(p.getConversation().substring(5));
                        p.say("Connecting...");
                    }
                    p.setState(Actions.Wait);
                }
            };
        } else {
            result = new Matter(name, seed, pt, depth);
        }
        result.image.size.mult(seed*.1f);
        return result;
    }
}
