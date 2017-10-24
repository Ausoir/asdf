package com.ga.map;

import com.ga.game.FileManager;
import com.ga.unit.player.MainPlayer;
import com.ga.unit.player.Trait;
import com.ga.unit.skill.Buffs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Ga on 4/17/2017. asd
 */
public class Creation {
    public static Random r = new Random();
    public static int seed;
    public static char[][] world;

    public static void create(){
        FileManager.loadGame();
        if (!FileManager.isFile("Data/map/World.map")) {
            seed = (int) (Math.random()*1000000);
//            world = WorldCreation.create(16,16);
            world = new char[][]{new char[]{'6','-','-','-','-','-','-','-','-','-','-','-','-','-','-','4'}};
            FileManager.saveWorld();
        } else {
            FileManager.readWorld();
        }
    }
    public static void prepareRandom(int id){
        r = new Random(id+seed);
    }
    public static int getSeed(int p, int pool, int id){
        int seed = getSeed(p);
        seed += (id - (seed % pool))+pool;
        return seed;
    }
    public static int getSeed(int p){
        if (p == -1) return r.nextInt(99999);
        return p;
    }
    public static char getWorld(int id){
        int[] pt = new int[]{id % world[0].length, id / world[0].length};
        return world[pt[1]][pt[0]];
    }
    public static boolean isPassable(char p, int edge){
        return WorldCreation.dictionary.get(p).contains(edge);
    }
    static int[] getWorld(int id, int dir){
        int[] pt = new int[]{id % world[0].length, id / world[0].length};
        int x = dir == 4 ? -1 : dir == 6 ? 1 : 0, y = dir == 8 ? -1 : dir == 2 ? 1 : 0;
        pt = new int[]{pt[0]+x, pt[1]+y};
        if (pt[0] >= 0 && pt[1] >= 0 && pt[0] < world[0].length && pt[1] < world.length){
            int newId = (pt[1]*world[0].length)+pt[0];
            return new int[]{newId, world[pt[1]][pt[0]]};
        }
        return null;
    }
    public static void test(){
        world = WorldCreation.create(14,2);
        print(world, false);
        print(world, true);
        int[] r = getWorld(1, 2);
        System.out.println(r[0]+" , "+(char)r[1]);
//        System.out.println(Arrays.toString(getWorld(4, 2)));
    }
    private static void print(char[][] p, boolean ascii){
        for (char[] i : p){
            for (char ii : i){
                if (ascii) {
                    if (ii == '*') System.out.print("* ");
                    else System.out.print(getAscii(ii));
                } else System.out.print(ii);
            }
            System.out.println();
        }
    }
    private static String getAscii(char p){
        String r = "";
        if (p == '*') r = "* ";
        else if (p == '0') r = "O ";
        else if (p == '1') r = (char)9488+" ";
        else if (p == '2') r = "V ";
        else if (p == '3') r = (char)9484+" ";
        else if (p == '4') r = "< ";
        else if (p == '5') r = (char)9532+" ";
        else if (p == '6') r = "> ";
        else if (p == '7') r = (char)9496+" ";
        else if (p == '8') r = "^ ";
        else if (p == '9') r = (char)9492+" ";
        else if (p == 'I') r = (char)9474+" ";
        else if (p == 'U') r = (char)9524+" ";
        else if (p == 'n') r = (char)9516+" ";
        else if (p == '-') r = (char)9472+" ";
        else if (p == ']') r = (char)9508+" ";
        else if (p == '[') r = (char)9500+" ";
        return r;
    }
}

class WorldCreation{
    private static char[][] test;
    private static int current,max;
    static HashMap<Character, ArrayList<Integer>> dictionary = new HashMap<Character, ArrayList<Integer>>(){{
        put('*', new ArrayList<Integer>(){{}});
        put('0', new ArrayList<Integer>(){{}});
        put('1', new ArrayList<Integer>(){{add(2);add(4);}});
        put('2', new ArrayList<Integer>(){{add(2);}});
        put('3', new ArrayList<Integer>(){{add(2);add(6);}});
        put('4', new ArrayList<Integer>(){{add(4);}});
        put('5', new ArrayList<Integer>(){{add(2);add(4);add(6);add(8);}}); //can't be generated
        put('6', new ArrayList<Integer>(){{add(6);}});
        put('7', new ArrayList<Integer>(){{add(4);add(8);}});
        put('8', new ArrayList<Integer>(){{add(8);}});
        put('9', new ArrayList<Integer>(){{add(6);add(8);}});
        put('I', new ArrayList<Integer>(){{add(2);add(8);}});
        put('-', new ArrayList<Integer>(){{add(4);add(6);}});
        put('U', new ArrayList<Integer>(){{add(4);add(6);add(8);}});
        put('n', new ArrayList<Integer>(){{add(4);add(6);add(2);}});
        put(']', new ArrayList<Integer>(){{add(2);add(8);add(4);}});
        put('[', new ArrayList<Integer>(){{add(2);add(8);add(6);}});
    }};
    static char[][] create(int sizeX, int sizeY){
        test = new char[sizeY][sizeX];
        for (int y=0;y<test.length;y++){
            for (int x=0;x<test[0].length;x++) test[y][x] = '*';
        }
        current = 0;
        max = test.length*test[0].length;
        int startX = (int) (Math.random()*test[0].length), startY = (int) (Math.random()*test.length);
        test(new int[]{startX, startY});
        reconnect();
        System.out.println(current+" : "+max);
        return test;
    }
    private static void move(int x,int y,int to){
        if (to == 2) test(new int[]{x,y+1});
        else if (to == 4) test(new int[]{x-1,y});
        else if (to == 6) test(new int[]{x+1,y});
        else if (to == 8) test(new int[]{x,y-1});
    }
    private static char add(char orig, int dir){
        ArrayList<Integer> definitions = dictionary.get(orig);
        ArrayList<Integer> newdef = new ArrayList<>();
        assert !definitions.contains(dir);
        newdef.addAll(definitions);
        newdef.add(dir);
        for (java.util.Map.Entry i : dictionary.entrySet()){
            ArrayList<Integer> v = (ArrayList<Integer>) i.getValue();
            if (v.size() == newdef.size() && v.containsAll(newdef)) return (char) i.getKey();
        }
        assert false;
        return '*';
    }
    private static void reconnect(){
        char[][] newnodes = new char[test.length][test[0].length];
        for (int y = 0; y < test.length; y++) {
            for (int x = 0; x < test[y].length; x++) {
                newnodes[y][x] = test[y][x];
                if (test[y][x] != '*') {
                    if (x > 0 && dictionary.get(test[y][x - 1]).contains(6))
                        newnodes[y][x] = add(newnodes[y][x], 4);
                    if (y > 0 && dictionary.get(test[y - 1][x]).contains(2))
                        newnodes[y][x] = add(newnodes[y][x], 8);
                    if (x < test[0].length - 1 && dictionary.get(test[y][x + 1]).contains(4))
                        newnodes[y][x] = add(newnodes[y][x], 6);
                    if (y < test.length - 1 && dictionary.get(test[y + 1][x]).contains(8))
                        newnodes[y][x] = add(newnodes[y][x], 2);
                }
            }
        }
        test = newnodes;
    }
    private static void test(int[] p){
        current++;
        boolean leftmost = p[0] == 0 || test[p[1]][p[0]-1] != '*';
        boolean topmost = p[1] == 0 || test[p[1]-1][p[0]] != '*';
        boolean rightmost = p[0] == test[0].length-1 || test[p[1]][p[0]+1] != '*';
        boolean bottommost = p[1] == test.length-1 || test[p[1]+1][p[0]] != '*';
        ArrayList<Integer> definitions;
        do {
            if (leftmost && topmost && rightmost && bottommost){
                test[p[1]][p[0]] = '0';
                return;
            }
            test[p[1]][p[0]] = random();
            definitions = dictionary.get(test[p[1]][p[0]]);
        }
        while ((definitions.contains(4) && leftmost)
                || (definitions.contains(8) && topmost)
                || (definitions.contains(6) && rightmost)
                || (definitions.contains(2) && bottommost)
                || (test[p[1]][p[0]] == '0' && current < max/2));
        for (Integer i : dictionary.get(test[p[1]][p[0]])) move(p[0],p[1],i);
    }

    private static char random(){
        int r = (int) (Math.random()*10);
        return (r+"").charAt(0);
    }
}