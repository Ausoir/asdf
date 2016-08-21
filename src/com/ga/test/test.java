package com.ga.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Ga on 8/16/2016.
 */
public class test {
    public static void main(String[] args){
        final UDPClient udp = new UDPClient(8080);
        try {
            udp.send("something",InetAddress.getByName("127.0.0.1"),8080);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(new Runnable(){
            @Override
            public void run(){
                while (true) {
                    try{
                        String t = udp.receive();
                        System.out.println("Received: " + t);
                        Thread.sleep(100);
                    }catch (Exception e){

                    }
                }
            }
        }).start();
    }
}

class UDPClient {
    /**
     * To make this not static. Optimizes? if not remove and make static
     */
//	private static final UDPClient udp = new UDPClient(16); //Singleton or port binding problem occurs
// even in internal server
    private DatagramSocket clientSocket;
    //4 Bytes or commands that does not limit length reads corrupt data(Commands are usually sent twice)
    private int length; //4 bytes for normal commands
    private DatagramPacket receiver;
    public UDPClient(int phost){
        length = 36; //32 mapsize
        receiver = new DatagramPacket(new byte[length], length);
        try{
            clientSocket = new DatagramSocket(phost);
        }catch(Exception e){
            System.out.println("Establishing UDP connection failed");
            e.printStackTrace();
            System.exit(1);
        }
    }
    public int size(){
        return length;
    }

    /**
     * Just in case - maybe add in proper game exit method later?
     */
    public void close(){
        clientSocket.close();
    }

    /**
     * Throw in InetAddress p_ip for param later
     */
    public void send(String p, InetAddress ip, int pclient) throws Exception{
        byte[] sendData = new byte[length];
        sendData = (byteConcat(p.getBytes(), p.getBytes())); //Concats or data corrupts?
        DatagramPacket sender = new DatagramPacket(sendData, sendData.length, ip, pclient);
        clientSocket.send(sender);
    }

    private byte[] byteConcat(byte[] p_1, byte[] p_2){
        byte[] result = new byte[p_1.length + p_2.length];
        System.arraycopy(p_1, 0, result, 0, p_1.length);
        System.arraycopy(p_2, 0, result, p_1.length, p_2.length);
        return result;
    }

    public String receive() throws Exception{
        clientSocket.receive(receiver);
        String result = new String(receiver.getData());
//	    System.out.println("RECEIVING: "+result);
        return result;
    }
    public InetAddress getIP(){
        return receiver.getAddress();
    }
    public int getPort(){
        return receiver.getPort();
    }
}