package com.ga.map;

public class MapName {
    public String value;

    public MapName(String p, boolean instance){
        set(instance?"ins/"+p:p);
    }
    private void set(String p){
        value = p;
    }

    private String getBiome(){
        return getMapname().substring(1, 2);
    }
    public int getIndex(){
        return Integer.parseInt(getMapname().substring(2));
    }
    public MapName getNext(int dir){
        return new MapName("_"+getBiome()+(getIndex()+dir), true);
    }
    public boolean isValid(){
        return getIndex() > 0;
    }
    public boolean isField(){
        return value.startsWith("ins/_");
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

    String getMapname(){
        return value.substring(value.lastIndexOf("/")+1);
    }
    String getDir(){
        int index = value.lastIndexOf("/");
        return index == -1 ? "" : value.substring(0, index);
    }
    String getModifiedMapname(String p){
        return getDir()+"/"+p;
    }
    public String toString(){
        return value;
    }
}
