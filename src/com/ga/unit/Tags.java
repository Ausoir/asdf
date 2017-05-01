package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/5/2017.
 */
public class Tags extends ArrayList<Tag> {
    public boolean has(Tag p){
        for (Tag i : this) {
            if (i == p) return true;
        }
        return false;
    }

    public boolean has(Tags p){
        for (Tag i : p) {
            if (!contains(i)) return false;
        }
        return true;
    }

    public Tags getTrainers(){
        Tags r = new Tags();
        for (Tag i : this){
            if (i == Tag.Martial || i == Tag.Agility || i == Tag.Jumping || i == Tag.Athletics || i == Tag.Swinging)
                r.add(i);
        }
        return r;
    }

    public String toString(){
        String r = "";
        int length = size();
        for (int i=0;i<length;i++){
            r += get(i);
            if (i < length-1) r += ", ";
        }
        return r;
    }
}
