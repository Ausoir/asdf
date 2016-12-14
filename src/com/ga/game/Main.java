package com.ga.game;

public class Main {
    private static String[] arguments;
    public static void main(String[] args) {
        arguments = args;
        boolean editor = false;
        int x = 0, y = 0;
        if (arguable(0)) editor = true;
        if (arguable(1)) x = Integer.parseInt(args[1]);
        if (arguable(2)) y = Integer.parseInt(args[2]);
        if (arguable(3)) Settings.receiverPort = Integer.parseInt(args[3]);
        if (arguable(4)) Settings.senderPort = Integer.parseInt(args[4]);
        new Game(editor, x, y);
    }
    private static boolean arguable(int i){
        return arguments.length > i && !arguments[i].equals("");
    }
}
