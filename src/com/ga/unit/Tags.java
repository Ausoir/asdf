package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/5/2017. aaa
 */
public class Tags extends ArrayList<Tag> {
    public static final Tag[] trainers = new Tag[]{
        Tag.Combat, Tag.Finesse, Tag.Might, Tag.Athletics, Tag.Aiming, Tag.Energy, Tag.Channeling, Tag.Cunning
    };

    boolean has(Tag p){
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
        int length = size();
        for (int i=0;i<length;i++){
            r += get(i);
            if (i < length-1) r += ", ";
        }
        return r;
    }
}
