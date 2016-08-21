package com.ga.unit;

import java.awt.*;

/**
 * Created by Ga on 8/9/2016.
 */
public class Armor extends Item{
    public float armor = 0;
    public Color color1 = Color.green;
    public Color color2 = Color.blue;
    public Armor(String name) {
        super(null);
        this.name = name;
    }
    public Armor(String name, float armor, int weight, Color c1, Color c2) {
        this(name);
        this.armor = armor;
        this.weight = weight;
        this.color1 = c1;
        this.color2 = c2;
    }
}
