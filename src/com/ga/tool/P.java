package com.ga.tool;

import com.ga.map.Global;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class P {
    public static final P blank = new P();
    public float x, y;
    public P(String p){
        load(p);
    }
    public P(){
        set(0,0);
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
    public int x(){
        return (int) x;
    }
    public int y(){
        return (int) y;
    }
    public boolean set(P p){
        return set(p.x,p.y);
    }
    public boolean set(float x, float y){
        boolean r = this.x != x || this.y != y;
        this.x = x;
        this.y = y;
        return r;
    }
    public boolean is(P p){
        return is(p.x,p.y);
    }
    public boolean is(float x, float y){
        return this.x == x && this.y == y;
    }
    public P add(P p){
        return add(p.x,p.y);
    }
    public P add(float x, float y){
        this.x += x;
        this.y += y;
        return this;
    }
    public P mult(float p){
        return mult(p,p);
    }
    public P mult(float x, float y){
        this.x *= x;
        this.y *= y;
        return this;
    }
    @SuppressWarnings("SuspiciousNameCombination")
    public P flip(){
        float t = x;
        this.x = y;
        this.y = t;
        return this;
    }
    public P getAtan(float mag){
        float r = (float) Math.atan2(y,x);
        return new P(Global.cos(r)*mag,Global.sin(r)*mag);
    }
    public float getAtan(){
        return (float) Math.atan2(y,x);
    }
    public float getDist(){
        return (float) Math.sqrt((this.x*this.x)+(this.y*this.y));
    }
    public boolean inOval(float x, float y){
        return (Math.abs(this.x) < x && Math.abs(this.y) < y);
    }
    public P getAdd(P p){
        return getAdd(p.x,p.y);
    }
    public P getSubtract(P p){
        return getAdd(-p.x,-p.y);
    }
    public P getAdd(float x, float y){
        return new P(this.x+x, this.y+y);
    }
    public P getMult(float x, float y){
        return new P(this.x*x,this.y*y);
    }
    public P disperse(int x, int y) {
        int dX = 0, dY = 0;
        if (x > 0) dX = Global.r.nextInt(x) - (x / 2);
        if (y > 0) dY = Global.r.nextInt(y) - (y / 2);
        return getAdd(dX, dY);
    }
    public String toString(){
        return ((int)x) + "," + ((int)y);
    }
    public void load(String p){
        String[] t = p.split(",");
        set(Float.parseFloat(t[0]), Float.parseFloat(t[1]));
    }
}
