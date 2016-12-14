package com.ga.connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Ga on 12/12/2016.
 */
public class UDPClient {
    private DatagramSocket clientSocket;
    private static int length; //4 bytes for normal commands
    private DatagramPacket receiver;
    private DatagramPacket sender;
    private static String host;
    public UDPClient(){}
    public UDPClient(int receiverPort){
        length = 36; //32 mapsize
        receiver = new DatagramPacket(new byte[length], length);
        try{
            clientSocket = new DatagramSocket(receiverPort);
            host = InetAddress.getLocalHost().getHostAddress();
        }catch(Exception e){
            System.out.println("Establishing UDP connection failed");
            e.printStackTrace();
            System.exit(1);
        }
    }
    public int size(){
        return length;
    }

    public void close(){
        clientSocket.close();
    }

    public void send(String msg, String ip, int port) throws Exception {
        byte[] hello = stringToByte(msg);
        sender = new DatagramPacket(hello, hello.length, InetAddress.getByName(ip), port);
        clientSocket.send(sender);
    }

    public static DatagramPacket getAckPacket(String ip, int port) {
        byte[] hello = stringToByte("OK");
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(hello, hello.length, InetAddress.getByName(ip), port);
        } catch (Exception e){
            e.printStackTrace();
        }
        return packet;
    }

    public void send(String p) throws Exception {
        sender.setData(stringToByte(p));
        clientSocket.send(sender);
    }

    public void send(DatagramPacket packet, String p) throws Exception {
        packet.setData(stringToByte(p));
        clientSocket.send(packet);
    }

    private static byte[] stringToByte(String p){
        String t = p + ";";
        byte[] sendData = new byte[length];
        sendData = (byteConcat(t.getBytes(), t.getBytes())); //Concats or data corrupts?
        return sendData;
    }

    private static byte[] byteConcat(byte[] p_1, byte[] p_2){
        byte[] result = new byte[p_1.length + p_2.length];
        System.arraycopy(p_1, 0, result, 0, p_1.length);
        System.arraycopy(p_2, 0, result, p_1.length, p_2.length);
        return result;
    }

    public String receive() throws Exception{
        clientSocket.receive(receiver);
        String result = new String(receiver.getData());
        return result;
    }
    public String getClientIP(){
        return receiver.getAddress().getHostName();
    }
    public int getClientPort(){
        return receiver.getPort();
    }
    public static int parseIp(String address) {
        int result = 0;
        for(String part : address.split(".")) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }
        return result;
    }
}