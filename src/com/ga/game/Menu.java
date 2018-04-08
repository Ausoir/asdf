package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.Performer;
import com.ga.unit.item.Item;

import javax.swing.*;

/**
 * Created by ga on 6/6/17. asd
 */
public class Menu extends JPanel implements Updatable {
    private Panel current;
    private MenuPlayer menu = new MenuPlayer();
    Menu(){
        init();
        setLayout(null);
        resize();
        setOpaque(false);
        setFocusable(false);
    }
    void init(){
        current = new Panel();
    }
    private void select(Panel p){
        Global.player.map.updater.addUpdatee(this);
        remove(current);
        current = p;
        add(p);
    }
    public void showMenu(){
        if (current.getClass() != MenuPlayer.class){
            select(menu);
            menu.setVisible(false);
        }
        menu.toggle();
    }
    public void showDialog(String title, String message) {
        if (current.getClass() != MenuDialog.class){
            select(new MenuDialog());
            ((MenuDialog) current).show(title, message);
        } else ((MenuDialog) current).rewrite(title, message);
    }
    public void showDialog(String title, String message, String[] options, final Performer[] actions) {
        if (current.getClass() != MenuDialog.class) select(new MenuDialog());
        ((MenuDialog) current).show(title, message, options, actions);
    }
    public void showShop(String title, Item[] wares, int[] prices, Performer[] actions){
        if (current.getClass() != MenuShop.class) select(new MenuShop());
        ((MenuShop) current).show(title, wares, prices, actions);
    }
    public void showContainer(String title, Item[] wares, Performer[] actions){
        if (current.getClass() != MenuContainer.class) select(new MenuContainer());
        ((MenuContainer) current).show(title, wares, actions);
    }
    void resize(){
        setBounds(0,0, Settings.screenX, Settings.screenY);
        current.resize();
    }
    void onExit(){
        Global.display.requestFocus();
    }
    @Override
    public void update() {
        current.update();
    }
}
