package com.ga.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 4/5/2017. aaa
 */
public class Tags extends ArrayList<Tag> {
    public static final Tags blank = new Tags();
    public static final Tag[] trainers = new Tag[]{
        Tag.Strength, Tag.Athletics, Tag.Electric, Tag.Channeling, Tag.Fire, Tag.Air, Tag.Mutation, Tag.Conjuration
    };

    boolean containsAny(Tags p){
        for (Tag i : p){
            if (contains(i)) return true;
        }
        return false;
    }

    public Tags getTrainers(){
        Tags r = new Tags();
        for (Tag i : this){
            for (Tag ii : trainers){
                if (ii == i){
                    r.add(i);
                    break;
                }
            }
        }
        return r;
    }

    public String toString(){
        String r = "";
        HashMap<Tag, Integer> map = new HashMap<>();
        for (Tag tag : this) {
            map.merge(tag, 1, (a, b) -> a + b);
        }
        for (Map.Entry pair : map.entrySet()){
            if ((int) pair.getValue() > 1) r += pair.getKey() + " [" + pair.getValue()+"], ";
            else r += pair.getKey()+", ";
        }
        if (r.length() > 2) return r.substring(0,r.length()-2);
        return r;
    }
}
