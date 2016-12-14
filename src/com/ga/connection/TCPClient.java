package com.ga.connection;

import com.ga.game.Settings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ga on 12/12/2016.
 */
public class TCPClient {
    public void download(String ip, String output) {
        byte[] aByte = new byte[1];
        int bytesRead;
        Socket clientSocket = null;
        InputStream is = null;
        try {
            clientSocket = new Socket(ip , Settings.tcpPort);
            is = clientSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is != null) {
            FileOutputStream fos;
            BufferedOutputStream bos;
            try {
                fos = new FileOutputStream(output);
                bos = new BufferedOutputStream(fos);
                bytesRead = is.read(aByte, 0, aByte.length);
                do {
                    baos.write(aByte);
                    bytesRead = is.read(aByte);
                } while (bytesRead != -1);
                bos.write(baos.toByteArray());
                bos.flush();
                bos.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void send(String input) {
        while (true) {
            ServerSocket welcomeSocket = null;
            Socket connectionSocket = null;
            BufferedOutputStream outToClient = null;
            try {
                welcomeSocket = new ServerSocket(Settings.tcpPort);
                connectionSocket = welcomeSocket.accept();
                outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (outToClient != null) {
                File myFile = new File(input);
                byte[] mybytearray = new byte[(int) myFile.length()];
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(myFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BufferedInputStream bis = new BufferedInputStream(fis);
                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    outToClient.flush();
                    outToClient.close();
                    connectionSocket.close();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

