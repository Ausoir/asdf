package com.ga.connection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ga on 12/12/2016.
 */
class TCPClient {
    private int port;
    private Socket socket;
    TCPClient(int port){
        this.port = port;
    }
    void download(String ip, String output) throws IOException {
        int bytesRead;
        InputStream input = null;
        try {
            if (socket == null) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 3000);
            }
            input = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (input != null) {
            FileOutputStream fos;
            BufferedOutputStream bos;
            try {
                fos = new FileOutputStream(output);
                bos = new BufferedOutputStream(fos);
                byte[] one = new byte[1];
                bytesRead = input.read(one);
                while (bytesRead != -1 && one[0] != 28) {
                    bos.write(one);
                    bytesRead = input.read(one);
                }
//                bos.write((char)28);
                bos.flush();
//                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    String upload(String input) {
        String result = "";
        try {
            if (socket == null) {
                ServerSocket welcomeSocket = new ServerSocket(port);
                socket = welcomeSocket.accept();
            }
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            File myFile = new File(input);
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            result += mybytearray;
            output.write(mybytearray, 0, mybytearray.length);
            result += ((char)28);
            output.write(28);
            output.flush();
//            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

