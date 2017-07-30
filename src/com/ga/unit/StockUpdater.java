package com.ga.unit;

import com.ga.game.FileManager;

import java.util.ArrayList;

public class StockUpdater {
    public static void main(String[] args){ //update RAW seeds accordingly
        int input = Stock.valueOf(args[0]).ordinal();
        FileManager.init();
        ArrayList<String[]> maps = FileManager.fileCache.get("map");
        for (String[] i : maps) {
            StringBuilder newData = new StringBuilder();
            String data = FileManager.read(i[1]);
            String[] line = data.split("%");
            newData.append(line[0]).append("\n").append(line[1]).append("\n").append(line[2]).append("\n");
            if (line.length > 3 && !line[3].equals(";")) {
                String[] sections = line[3].split(";");
                if (sections.length > 0) {
                    for (String section : sections) {
                        int idPos = section.indexOf("@");
                        String name = section.substring(0, idPos);
                        if (isInteger(name)) {
                            int id = Integer.parseInt(name);
                            if (id >= input) id++;
                            newData.append(id);
                        } else newData.append(name);
                        newData.append(section.substring(idPos)).append(";");
                    }
                }
            }
            FileManager.save("map/"+i[0]+".map", newData.toString());
        }
    }
    private static boolean isInteger(String p){
        for (int i=0;i<p.length();i++){
            if (!Character.isDigit(p.charAt(i))) return false;
        }
        return true;
    }
}