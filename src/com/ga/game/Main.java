package com.ga.game;

public class Main {
    public static void main(String[] args) {
        boolean editor = false;
        int x = 0, y = 0;
        for (String i : args){
            if (i.equals("-editor")) editor = true;
            if (i.startsWith("-hostPort")) Settings.receiverPort = Integer.parseInt(i.substring(i.indexOf('=')+1));
            if (i.startsWith("-senderPort")) Settings.senderPort = Integer.parseInt(i.substring(i.indexOf('=')+1));
            if (i.startsWith("-screenX")) x = Integer.parseInt(i.substring(i.indexOf('=')+1));
            if (i.startsWith("-screenY")) y = Integer.parseInt(i.substring(i.indexOf('=')+1));
        }
        new Game(editor, x, y);
    }
}
