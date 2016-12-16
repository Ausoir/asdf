package com.ga.connection;

/**
 * Created by Ga on 12/13/2016.
 */
public class Test {
    public static void main(String[] args){
        UDPClient udp = new UDPClient(19191);
        final TCPClient t = new TCPClient();
        try {
            udp.send("HI@127.0.0.1","127.0.0.1",1992);
//            t.download("127.0.0.1",new String[]{"Data/output.txt"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
