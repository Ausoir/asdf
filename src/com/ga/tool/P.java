package com.ga.tool;

import com.ga.map.Global;

/**
 * Created by Ga on 5/6/2016.
 */
public class P {
    static P zero = new P();
    public float x, y;
    public P(String p){
        load(p);
    }
    public P(){
        set(0,0);
    }
    public static P getZero(){
        return zero;
    }
    public P(P p){
        set(p);
    }
    public P(float x, float y){
        set(x,y);
    }
    public P(double x, double y){
        set((float) x, (float) y);
    }
    public P set(P p){
        return set(p.x,p.y);
    }
    public P set(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }
    public boolean is(P p){
        return p.x==x && p.y==y;
    }
    public void add(P p){
        add(p.x,p.y);
    }
    public void add(float x, float y){
        this.x += x;
        this.y += y;
    }
    public P mult(float p){
        this.x *= p;
        this.y *= p;
        return this;
    }
    public void mult(float x, float y){
        this.x *= x;
        this.y *= y;
    }
    public P getAtan(P pt, float mag){
        float r = (float) Math.atan2(y-pt.y,x-pt.x);
        return new P(Global.cos(r)*mag,Global.sin(r)*mag);
    }
    public P getAtan(float mag){
        float r = (float) Math.atan2(y,x);
        return new P(Global.cos(r)*mag,Global.sin(r)*mag);
    }
    public float getDist(){
        return (float) Math.sqrt((this.x*this.x)+(this.y*this.y));
    }
    public float getSum(){
        return this.x+this.y;
    }
    public float getDifference(P p){
        return (float)(Math.pow(this.x-p.x,2)+Math.pow(this.y-p.y,2));
    }
    public P getAdd(P p){
        return getAdd(p.x,p.y);
    }
    public P getMinus(){
        return new P(-x,-y);
    }
    public P getAdd(float x, float y){
        return new P(this.x+x, this.y+y);
    }
    public P getMult(float x, float y){
        return new P(this.x*x,this.y*y);
    }
    public String toString(){
        return ((int)x) + "," + ((int)y);
    }
    public void load(String p){
        String[] t = p.split(",");
        set(Float.parseFloat(t[0]), Float.parseFloat(t[1]));
    }
}
