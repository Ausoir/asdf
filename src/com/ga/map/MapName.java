package com.ga.map;

public class MapName {
    private String value;

    MapName(String p){
        set(p);
    }
    private void set(String p){
        if (p.startsWith("../")) value = p.substring(3);
        else value = "instance/"+p;
    }

    private String getBiome(){
        return getInstanceName().substring(1, 2);
    }
    public int getIndex(){
        return Integer.parseInt(getInstanceName().substring(2));
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
        return value.equals(p);
    }

    public String getInstanceName(){
        return value.substring(9); //instance/
    }

    public String toString(){
        return value;
    }
}
