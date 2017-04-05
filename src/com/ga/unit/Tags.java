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
}
