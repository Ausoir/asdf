package com.ga.unit.player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 4/11/2017. asd
 */
public class Health {
    private String nutrition = "";
    private int max;
    private Player host;

    Health(Player host){
        this.host = host;
        max = host.traits.contains(Trait.Bulky)?36:30;
    }

    public void record(String p){
        if (host.traits.contains(Trait.Sickly) && host.rand.nextInt(7) == 0) p = p.replace('O','X');
        if (host.traits.contains(Trait.Unhealthy)) {
            if (host.rand.nextInt(4) == 0 && p.contains("O")) p = p + "X";
        } else if (host.traits.contains(Trait.Healthy)){
            String r = "";
            for (char i : p.toCharArray()) {
                if (i == 'X' && host.rand.nextBoolean()) r += 'O';
                else r += i;
            }
            p = r;
        }
        if (host.traits.contains(Trait.Damaged_Liver)){
            for (char i : nutrition.substring(0, p.length()).toCharArray()) {
                if (i == 'X') p += "X";
            }
        }
        String r = "";
        for (char i : p.toCharArray()) { //bypass mutation
            if (i == '0') r += 'O';
            else r += i;
        }
        nutrition += r;
        nutrition = nutrition.substring(Math.max(0,nutrition.length()-(max+host.get("Level").get())));
        digest();
    }

    int getStatus(){
        HashMap<Character, Integer> strain = getStrain(nutrition);
        if (nutrition.length() == 0) return 1;
        else if (nutrition.length() < max/3) return 2;
        else if (strain.get('X')+strain.get('O') > max/2) return 3;
        else if (nutrition.length() < max-5) return 4;
        return 0;
    }

    public String getStatusDescription(){
        int status = getStatus();
        if (status == 2) return "Starving";
        else if (status == 3) return "Sick";
        else if (status == 4) return "Hungry";
        return "Normal";
    }

    void starve(int p){
        nutrition = nutrition.substring(p + (host.traits.contains(Trait.Fast_Metabolism) ? 1 : 0));
    }

    private void digest(){
        HashMap<Character, Integer> strain = getStrain(nutrition.substring(0, Math.min(16, nutrition.length())));
        host.get("Stamina").setMax(((strain.get('P')+strain.get('C'))/4)+host.get("Level").get()+2);
        host.get("Strength").setMax((int) Math.ceil((float) strain.get('P')/4) + (strain.get('C')>1?1:0));
        host.get("Memory").setMax(Math.max(1,strain.get('D')-strain.get('X')));
        host.get("Speed").setMax(16-strain.get('X')+strain.get('V')/4 + (strain.get('C')>1?1:0));
        host.get("Endurance").setMax(strain.get('P')*18+strain.get('V')*12+strain.get('C')*80+80);
        host.get("Motivation").setMax(host.traits.contains(Trait.Greedy)?200:100);
    }

    private static HashMap<Character, Integer> getStrain(String nutrition){
        HashMap<Character, Integer> strain = new HashMap<Character, Integer>(){{
            put('P',0);
            put('C',0);
            put('V',0);
            put('D',0);
            put('O',0);
            put('X',0);
        }};
        for (int i=0;i<nutrition.length();i++){
            char c = nutrition.charAt(i);
            strain.put(c,strain.get(c)+1);
        }
        return strain;
    }

    private static String getName(char p){
        String r = "";
        if (p == 'P') r = "Protein";
        else if (p == 'C') r = "Calories";
        else if (p == 'V') r = "Vitamin";
        else if (p == 'D') r = "Detoxicants";
        else if (p == 'O') r = "Other";
        else if (p == 'X') r = "Bad Metals";
        return r;
    }

    public static String mutate(String p){
        String r = "";
        for (char i : p.toCharArray()){
            r += mutate(i, (float) Math.random());
        }
        return r;
    }

    public static char mutate(char p, float v){
        char[] r;
        if (p == 'P') r = new char[]{'C','X','O'};
        else if (p == 'C') r = new char[]{'X','O'};
        else if (p == 'V') r = new char[]{'D','X','X','O','O','O','O','O','C','C','C'};
        else if (p == 'D') r = new char[]{'D'};
        else if (p == 'O') r = new char[]{'X','P','C','V','O','O'};
        else if (p == 'X') r = new char[]{'X'};
        else r = new char[]{'O'};
        return r[(int) (v*r.length)];
    }

    public static String getDescription(String p){
        String r = "";
        HashMap<Character , Integer> strain = getStrain(p);
        for (Map.Entry map : strain.entrySet()){
            r += getName((Character) map.getKey()) + ":" + map.getValue()+"\n";
        }
        return r;
    }

    public void load(String p) {
        if (p.equals(";")) return;
        nutrition = p.substring(1);
    }

    public String toString(){
        return ";"+nutrition;
    }
}
