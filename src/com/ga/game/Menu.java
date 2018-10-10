package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.unit.item.Item;

import javax.swing.*;

/**
 * Created by ga on 6/6/17. asd
 */
public class Menu extends JPanel {
    public static Menu menu = new Menu();
    private Panel current;
    private MenuPlayer menuPlayer = new MenuPlayer();

    static void init(boolean veryFirst){
        menu.current = new Panel();
        menu.setLayout(null);
        menu.resize();
        menu.setOpaque(false);
        menu.setFocusable(false);
        Global.display.add(menu);
        if (veryFirst){
            menu.showMenu();
            menu.menuPlayer.command("act");
        }
    }
    private void select(Panel p){
        remove(current);
        current = p;
        add(p);
    }
    public void showMenu(){
        if (current.getClass() != MenuPlayer.class){
            select(menuPlayer);
            menuPlayer.setVisible(false);
        }
        menuPlayer.toggle();
    }
    public void showDialog(String title, String message) {
        select(new MenuDialog());
        ((MenuDialog) current).show(title, message);
    }
    public void showDialog(String title, String message, String[] options, final Performer[] actions) {
        select(new MenuDialog());
        ((MenuDialog) current).show(title, message, options, actions);
    }
    public void showShop(String title, Item[] wares, int[] prices, Performer[] actions){
        select(new MenuShop());
        ((MenuShop) current).show(title, wares, prices, actions);
    }
    void resize(){
        setBounds(0,0, Settings.screenX, Settings.screenY);
        current.resize();
    }
    void onExit(){
        Global.display.requestFocus();
    }
}
