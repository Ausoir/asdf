package com.ga.map;

public class MapName {
    public String value;
    private boolean isInstance;

    MapName(String p){
        set(p);
    }
    private void set(String p){
        isInstance = !p.startsWith("../");

        if (!isInstance) value = p.substring(3);
        else value = "instance/"+p;
    }

    private String getBiome(){
        return toString().substring(1, 2);
    }
    public int getIndex(){
        return Integer.parseInt(toString().substring(2));
    }
    public MapName getNext(int dir){
        return new MapName("_"+getBiome()+(getIndex()+dir));
    }
    public boolean isValid(){
        return getIndex() > 0;
    }
    public boolean isField(){
        return value.startsWith("instance/_");
    }
    String getBiomeName(){
        switch (getBiome()){
            case "G":
                return "Grassland";
            case "F":
                return "Forest";
            case "D":
                return "Desert";
        }
        return null;
    }

    public boolean equals(String p){
        return toString().equals(p);
    }

    public String toString(){
        return isInstance ? value.substring(9) : value; // "instance/"
    }
}
