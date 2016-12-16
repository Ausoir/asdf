package com.ga.connection;

import com.ga.game.Settings;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ga on 12/12/2016.
 */
public class TCPClient {
    public void download(String ip, String[] output) throws IOException {
        int bytesRead;
        Socket clientSocket = null;
        InputStream is = null;
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ip, Settings.tcpPort), 3000);
            is = clientSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (is != null) {
            FileOutputStream fos;
            BufferedOutputStream bos;
            try {
                for (int i=0;i<output.length;i++) {
                    fos = new FileOutputStream(output[i]);
                    bos = new BufferedOutputStream(fos);
                    byte[] one = new byte[1];
                    bytesRead = is.read(one);
                    do {
                        bos.write(one);
                        bytesRead = is.read(one);
                    } while (bytesRead != -1 && one[0] != 28);
                    bos.flush();
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        clientSocket.close();
    }
    public String send(String[] input) {
        String result = "";
        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;
        BufferedOutputStream outToClient = null;
        try {
            welcomeSocket = new ServerSocket(Settings.tcpPort);
            connectionSocket = welcomeSocket.accept();
            outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
            if (outToClient != null) {
                for (int i=0;i<input.length;i++) {
                    File myFile = new File(input[i]);
                    byte[] mybytearray = new byte[(int) myFile.length()];
                    FileInputStream fis = null;
                    fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(mybytearray, 0, mybytearray.length);
                    result += mybytearray;
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    result += ((char)28);
                    outToClient.write(28);
                }
                outToClient.flush();
                outToClient.close();
                connectionSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

