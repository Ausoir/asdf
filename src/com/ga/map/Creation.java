package com.ga.map;

import com.ga.game.FileManager;
import com.ga.unit.player.MainPlayer;

import java.util.Arrays;

/**
 * Created by Ga on 4/17/2017. asd
 */
public class Creation {
    private static int[][][][] nodes;
    public static int[][] world;
    public static void create(){
        if (!FileManager.isFile("Data/map/0")) {
            MainPlayer player = new MainPlayer(-1);
            player.cworld = "Firstworld";
            FileManager.saveCharacter();
        } else {
            FileManager.loadPlayer(true, "0");
        }
        if (!FileManager.isFile("Data/map/"+Global.player.cworld+".map")) {
            world = WorldCreation.create(8,4);
            saveWorld();
        } else {
            world = FileManager.readWorld(Global.player.cworld);
        }
        loadWorld(world);
    }
    private static void saveWorld(){
        String r = "";
        for (int[] y : world){
            for (int x : y){
                if (x == -1) r += '*';
                else r += x;
            }
            r += "\n";
        }
        FileManager.save("map/"+Global.player.cworld+".map",r);
    }
    private static int[][] get(int p){
        int[][] r = new int[4][2];
        if (p == 1){
            r[0] = new int[]{-1,0};
            r[1] = new int[]{0,1};
        } else if (p == 2) {
            r[0] = new int[]{0,1};
        } else if (p == 3) {
            r[0] = new int[]{1,0};
            r[1] = new int[]{0,1};
        } else if (p == 4) {
            r[0] = new int[]{-1,0};
        } else if (p == 5) {
            r[0] = new int[]{-1,0};
            r[1] = new int[]{1,0};
            r[2] = new int[]{0,-1};
            r[3] = new int[]{0,1};
        } else if (p == 6) {
            r[0] = new int[]{1,0};
        } else if (p == 7) {
            r[0] = new int[]{-1,0};
            r[1] = new int[]{0,-1};
        } else if (p == 8) {
            r[0] = new int[]{0,-1};
        } else if (p == 9) {
            r[0] = new int[]{1,0};
            r[1] = new int[]{0,-1};
        }
        return r;
    }
    private static void add(int[][] node, int x, int y){
        for (int i=0;i<node.length;i++){
            if (node[i][0] == 0 && node[i][1] == 0){
                node[i] = new int[]{x,y};
                return;
            }
        }
    }
    private static void loadWorld(int[][] world){
        nodes = new int[world.length][world[0].length][4][2];
        for (int y=0;y<world.length;y++){
            for (int x=0;x<world[y].length;x++){
                int[][] dirs = get(world[y][x]);
                nodes[y][x] = dirs;
            }
        }
        for (int y=0;y<world.length;y++){
            for (int x=0;x<world[y].length;x++){
                int[][] dirs = get(world[y][x]);
                for (int[] i : dirs){
                    add(nodes[y+i[1]][x+i[0]],-i[0],-i[1]);
                }
            }
        }
    }
    public static int[] getWorld(int id, int dir){
        int[] pt = new int[]{id % world[0].length, id / world[0].length};
        int[][] move = get(dir);
        pt = new int[]{pt[0]+move[0][0], pt[1]+move[0][1]};
        if (pt[0] >= 0 && pt[1] >= 0 && pt[0] < world[0].length && pt[1] < world.length){
            int newId = (pt[1]*world[0].length)+pt[0];
            return new int[]{newId, world[pt[1]][pt[0]]};
        }
        assert false;
        return null;
    }
    public static void test(){
        world = FileManager.readWorld("Garr");//WorldCreation.create(8,4);
        print(world);
        System.out.println(Arrays.toString(getWorld(3, 2)));
    }
    private static void print(int[][] p){
        for (int[] i : p){
            for (int ii : i){
                if (ii == -1) System.out.print("* ");
                else if (ii == 0) System.out.print("O ");
                else if (ii == 1) System.out.print((char)9488+" ");
                else if (ii == 2) System.out.print("V ");
                else if (ii == 3) System.out.print((char)9484+" ");
                else if (ii == 4) System.out.print("< ");
                else if (ii == 5) System.out.print((char)9532+" ");
                else if (ii == 6) System.out.print("> ");
                else if (ii == 7) System.out.print((char)9496+" ");
                else if (ii == 8) System.out.print("^ ");
                else if (ii == 9) System.out.print((char)9492+" ");
            }
            System.out.println();
        }
    }
}

class WorldCreation{
    private static int[][] test;
    private static int current,max;
    static int[][] create(int sizeX, int sizeY){
        test = new int[sizeY][sizeX];
        for (int y=0;y<test.length;y++){
            for (int x=0;x<test[0].length;x++) test[y][x] = -1;
        }
        current = 0;
        max = test.length*test[0].length;
        int startX = (int) (Math.random()*test[0].length), startY = (int) (Math.random()*test.length);
        test(new int[]{startX, startY}, 0);
        System.out.println(current+" : "+ max);
        return test;
    }
    private static void move(int x,int y,int to){
        if (to == 2) test(new int[]{x,y+1},8);
        else if (to == 4) test(new int[]{x-1,y},6);
        else if (to == 6) test(new int[]{x+1,y},4);
        else if (to == 8) test(new int[]{x,y-1},2);
    }
    private static void test(int[] p, int from){
        current++;
        boolean leftmost,topmost,rightmost,bottommost;
        leftmost = p[0] == 0 || test[p[1]][p[0]-1] != -1;
        topmost = p[1] == 0 || test[p[1]-1][p[0]] != -1;
        rightmost = p[0] == test[0].length-1 || test[p[1]][p[0]+1] != -1;
        bottommost = p[1] == test.length-1 || test[p[1]+1][p[0]] != -1;
        do {
            if (leftmost && topmost && rightmost && bottommost){
                test[p[1]][p[0]] = 0;
                return;
            }
            test[p[1]][p[0]] = random();
        }
        while (((test[p[1]][p[0]] == 4 || test[p[1]][p[0]] == 7 || test[p[1]][p[0]] == 1 || (test[p[1]][p[0]] == 5 && from != 4)) && leftmost)
                || ((test[p[1]][p[0]] == 7 || test[p[1]][p[0]] == 8 || test[p[1]][p[0]] == 9 || (test[p[1]][p[0]] == 5 && from != 8)) && topmost)
                || ((test[p[1]][p[0]] == 9 || test[p[1]][p[0]] == 6 || test[p[1]][p[0]] == 3 || (test[p[1]][p[0]] == 5 && from != 6)) && rightmost)
                || ((test[p[1]][p[0]] == 1 || test[p[1]][p[0]] == 2 || test[p[1]][p[0]] == 3 || (test[p[1]][p[0]] == 5 && from != 2)) && bottommost)
                || (test[p[1]][p[0]] == 0 && current < max/2));
        if (test[p[1]][p[0]] == 1) {
            move(p[0],p[1],2);
            move(p[0],p[1],4);
        } else if (test[p[1]][p[0]] == 2) {
            move(p[0],p[1],2);
        } else if (test[p[1]][p[0]] == 3) {
            move(p[0],p[1],2);
            move(p[0],p[1],6);
        } else if (test[p[1]][p[0]] == 4) {
            move(p[0],p[1],4);
        } else if (test[p[1]][p[0]] == 5) {
            if (from != 2) move(p[0],p[1],2);
            if (from != 4) move(p[0],p[1],4);
            if (from != 6) move(p[0],p[1],6);
            if (from != 8) move(p[0],p[1],8);
        } else if (test[p[1]][p[0]] == 6) {
            move(p[0],p[1],6);
        } else if (test[p[1]][p[0]] == 7) {
            move(p[0],p[1],8);
            move(p[0],p[1],4);
        } else if (test[p[1]][p[0]] == 8) {
            move(p[0],p[1],8);
        } else if (test[p[1]][p[0]] == 9) {
            move(p[0],p[1],8);
            move(p[0],p[1],6);
        }
    }

    private static int random(){ //1-9
        return (int) (Math.random()*10);
    }
}