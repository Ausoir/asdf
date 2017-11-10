package com.ga.map;

import com.ga.game.Observer;

import java.awt.*;

/**
 * Created by Ga on 8/12/2016.
 */
public abstract class Drawable implements Observer {
    Drawable prev, next;
    float depth = -1;
    public void draw(Graphics2D g){
    }
    public void setDepth(float depth){
        if (this.depth == -1){
            this.depth = depth;
        } else if (this.depth != depth){
            this.depth = depth;
            if (prev != null) { //not added in yet
                removeThis();
                if (prev.getDepth() > depth) prev.reverseAdd(this);
                else prev.add(this);
            }
        }
    }
    public float getDepth(){
        return depth;
    }
    private void reverseAdd(Drawable p){
        if (prev == null) p.setNext(this);
        else {
            if (prev.getDepth() <= p.getDepth()) {
                Drawable origPrev = prev;
                p.setNext(this);
                origPrev.setNext(p);
            } else {
                prev.reverseAdd(p);
            }
        }
    }
    void add(Drawable p){
        if (next == null){
            setNext(p);
        } else if (next.getDepth() > p.getDepth()){
            p.setNext(next);
            setNext(p);
        } else {
            next.add(p);
        }
    }
    private void setNext(Drawable p){
        next = p;
        next.prev = this;
    }
    protected void removeThis(){
        if (prev != null) prev.next = next;
        if (next != null) next.prev = prev;
    }
}
