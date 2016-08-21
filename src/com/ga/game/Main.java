package com.ga.game;

public class Main {
    public static void main(String[] args) {
        boolean editor = false;
        int x = 0, y = 0;
        if (args.length == 3) {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
        }
        if (args.length > 0 && args[0].equals("editor")) editor = true;
        new Game(editor, x, y);
    }
}
