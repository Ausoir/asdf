package com.ga.connection;

import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.unit.player.MainPlayer;

import java.util.Arrays;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
//        UDPClient udp = new UDPClient(19191);
//        final TCPClient t = new TCPClient();
//        try {
//            udp.send("HI@127.0.0.1","127.0.0.1",1992);
////            t.download("127.0.0.1",new String[]{"Data/output.txt"});
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        int[][] t = Creation.test();
//        System.out.println(Arrays.deepToString(t));
//        print(t);
//        Creation.create();
//        System.out.println(Arrays.toString(Creation.getWorld(0, 2)));
//        print(Creation.world);
        Creation.test();
    }
    private static void print(int[][] p){
        for (int[] i : p){
            for (int ii : i){
                if (ii == -1) System.out.print("  ");
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
