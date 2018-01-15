package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/5/2017. aaa
 */
public class Tags extends ArrayList<Tag> {
    public static final Tag[] trainers = new Tag[]{
        Tag.Agility, Tag.Might, Tag.Athletics, Tag.Electric, Tag.Channeling, Tag.Shadow, Tag.Fire, Tag.Ice, Tag.Air, Tag.Mutation, Tag.Conjuration
    };

    @Override
    public boolean add(Tag p) {
        return !contains(p) && super.add(p);
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
