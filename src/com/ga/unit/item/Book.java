package com.ga.unit.item;

import com.ga.game.FileManager;

/**
 * Created by Ga on 1/26/2017.
 */
class Book {
    protected String title, content;
    protected Book(String p){
        String[] t = FileManager.readText("dialog/"+p);
        title = t[0];
        content = "";
        if (t.length > 1) {
            for (int i = 1; i < t.length; i++) {
                content += t[i]+"\n";
            }
        }
    }
}