package com.ga.map;

import com.ga.game.Observer;

import java.awt.*;

/**
 * Created by Ga on 8/12/2016.
 */
public abstract class Drawable implements Observer {
    private Drawable prev;
    Drawable next;
    float depth = -1;
    public void draw(Graphics2D g){}
    public void setDepth(float depth){
        if (this.depth == -1){
            this.depth = depth;
        } else if (this.depth != depth){
            this.depth = depth;
            if (prev != null) { //not added in yet
                Drawable newprev = prev;
                Drawable newnext = next;
                removeThis();
                prev = newprev;
                next = newnext;
                if (newprev.getDepth() > depth) newprev.reverseAdd(this);
                else newprev.add(this);
            }
        }
    }
    public float getDepth(){
        return depth;
    }
    private void reverseAdd(Drawable p){
        if (prev == null){
            p.setNext(this);
            p.prev = null;
        } else {
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
            next.next = null;
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
    void remove(Drawable p){
        if (this == p) removeThis();
        else if (next != null) {
            next.remove(p);
        }
    }
    void removeThis(){
        if (next != null) next.prev = prev;
        if (prev != null) prev.next = next;
        next = null;
        prev = null;
    }
}
