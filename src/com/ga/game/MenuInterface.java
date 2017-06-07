package com.ga.game;

import com.ga.map.Updatable;
import com.ga.tool.Performer;
import com.ga.unit.item.Item;

import javax.swing.*;

/**
 * Created by ga on 6/6/17. asd
 */
public class MenuInterface extends JPanel implements Updatable {
    private static Panel current = new Panel();
    private static Menu menu = new Menu();
    MenuInterface(){
        setLayout(null);
        resize();
        setOpaque(false);
        setFocusable(false);
    }
    private void show(Panel p){
        removeAll();
        current = p;
        add(p);
    }
    public void showMenu(){
        if (current.getClass() != Menu.class) show(menu);
        menu.toggle();
    }
    public void showDialog(String title, String message) {
        if (current.getClass() != MenuDialog.class) show(new MenuDialog());
        ((MenuDialog) current).show(title, message);
    }
    void showDialog(String title, String message, String[] options, final Performer[] actions) {
        if (current.getClass() != MenuDialog.class) show(new MenuDialog());
        ((MenuDialog) current).show(title, message, options, actions);
    }
    public void showShop(String title, Item[] wares, int[] prices, Performer[] actions){
        if (current.getClass() != MenuShop.class) show(new MenuShop());
        ((MenuShop) current).show(title, wares, prices, actions);
    }
    public void showContainer(String title, Item[] wares, Performer[] actions){
        if (current.getClass() != MenuContainer.class) show(new MenuContainer());
        ((MenuContainer) current).show(title, wares, actions);
    }
    void resize(){
        setBounds(0,0, Settings.screenX,Settings.screenY);
        current.resize();
    }
    @Override
    public void update() {
        current.update();
    }
}
