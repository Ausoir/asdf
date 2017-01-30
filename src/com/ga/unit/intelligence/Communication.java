package com.ga.unit.intelligence;

import com.ga.tool.Performer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 10/7/2016.
 */
public class Communication {
    private HashMap<String, String> reply = new HashMap<String, String>(){{
        put("hi","@greet");
        put("where","I don't know.");
        put("menuShop","The menuShop around here sells all kinds of stuff. You can find one in any town or city.");
    }};
    private HashMap<String, String> is = new HashMap<String, String>(){{
        put("ok","yes");
        put("sure","yes");
        put("yea","yes");
        put("hello","hi");
        put("fine day","hi");
        put("market","menuShop");
        put("buy","menuShop");
    }};
    protected HashMap<String, Performer> react = new HashMap();
    private ArrayList<String> topic = new ArrayList<>();
    protected String current;
    public Communication(){
    }
    protected void addTopic(String p){
        topic.add(p.toLowerCase());
    }
    protected void addReply(String p, String t){
        reply.put(p.toLowerCase(), t);
    }
    protected String hear(String p){
        String refined = p.toLowerCase();
        String t = is.get(refined);
        if (t == null) t = refined;
        if (topic.contains(t)) current = t;
        Performer reaction = react.get(t);
        if (reaction != null){
            reaction.perform();
        }
        t = reply.get(t);
        if (t != null){
            return t;
        }
        return "";
    }
}
