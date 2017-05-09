package com.ga.unit.player;

import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Line;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Tag;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    private Indicater indicater;
    private Stat stamina;
    public MainPlayer(int seed) {
        super(seed);
        Global.player = this;
        controlId = -1;
        indicater = new Indicater(this);
    }
    @Override
    public void say(String p){
        if (p.startsWith("'") && p.endsWith("'")){
            memorized = p.substring(1,p.length()-1);
            p = "Memorized "+p;
        }
        speech.say(p);
    }
    @Override
    public int setState(Actions state) {
        if (state == Actions.Use && this.state == Actions.Jump && skillTree.getSkillLevel(Actions.Airstrike) > 0 && this.delay.time > 32) stateSet(Actions.Airstrike);
        else if (state == Actions.Jump && this.state == Actions.Rest && skillTree.getSkillLevel(Actions.Leap) > 0) state = Actions.Leap;
        else if (state == Actions.Rest && this.state == Actions.Walk && skillTree.getSkillLevel(Actions.Sprint) > 0) state = Actions.Sprint;
        else if (state == Actions.Kick && this.state == Actions.Rest && skillTree.getSkillLevel(Actions.Sidekick) > 0) state = Actions.Sidekick;
        else if (state == Actions.Drop && this.state == Actions.Rest && skillTree.getSkillLevel(Actions.Sneak) > 0){
            state = Actions.Rest;
            if (Updater.collideUnit(pos, 360, faction).size() == 0) {
                for (Prop i : Updater.collideProp(pos, -12)) {
                    if (i.getDepth() == depth-1 && i.tags.contains(Tag.Hideable)) {
                        stateSet(Actions.Sneak);
                        setDepth(i.getDepth()-1);
                        break;
                    }
                }
            }
        } else if (input.moving != 0 && this.state == Actions.Climb){
            vel.x = getSpeed()*input.moving;
            stateSet(Actions.Jump);
        } else if (state == Actions.Use && this.state == Actions.Walk && !wieldingNone()){
            if (input.moving > 0 == target.x > 0) state = Actions.Thrust;
            else if (skillTree.getSkillLevel(Actions.Dash) > 0) state = Actions.Dash;
        }
        int cost = state.getCost()+getStatPenalty();
        if (cost > 0){
            indicater.show((float)cost/Math.max(1, stamina.get()));
        }
        int r = super.setState(state);
        if (r == 2){
            indicater.show(1);
        } else if (r == 3){
            indicater.showCd(getCooldown(state)[0]);
        }
        return r;
    }
    @Override
    public void stateSet(Actions state){
        super.stateSet(state);
        if (state != Actions.Idle) input.cameraMode = false;
    }
    public float getShopPrice(){
        float r = 1;
        if (traits.contains(Trait.Dull)) r = 1.1f;
        else if (traits.contains(Trait.Sharp)) r = .9f;
        return r;
    }
    public boolean checkHit(Line hitBox, int damage) {
        int before = stamina.get();
        boolean r = super.checkHit(hitBox, damage);
        if (r) indicater.show(before/Math.max(1,stamina.get()));
        return r;
    }
    public void onTimeChange(int p) {
        if (Stock.getFactoryName(armor.armor[0].factoryId).equals("Stomach Full Helm")) p = 0;
        health.starve(p);
        int status = health.getStatus();
        if (status == 1) {
            say("Food...");
            die();
        } else if (status == 2) {
            say("I'm starving!");
            health.record("0");
        }
        else if (status == 3) say("I don't feel so good...");
        else if (status == 4) say("I'm hungry!");
        if (stats.getEffectsSize() == 0) {
            get("Speed").heal();
            get("Strength").heal();
        }
        if (traits.contains(Trait.Optimistic)) get("Motivation").add(1);
        else if (traits.contains(Trait.Pessimistic)) get("Motivation").add(-1);
    }
    protected void stateDone(){
        super.stateDone();
        if (input.moving != 0) setState(Actions.Walk);
    }
    protected void stateUpdate(){
        super.stateUpdate();
        if (state == Actions.Walk && input.moving == 0) setState(Actions.Idle);
        else if (state == Actions.Rest) indicater.show(1);
    }
    protected void edgeCheck(){
        int edge = Global.mapEdge(pos.getAdd(vel));
        if (edge != 0 && !Global.map.isIndoors() && Creation.isPassable(Creation.getWorld(Global.map.id),edge)){
            Global.map.loadMap(""+edge);
        } else super.edgeCheck();
    }
    public void update(){
        super.update();
        if (input.cameraMode) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(input.movement));
        else Global.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
        if (stamina.isFull()) indicater.hide();
        indicater.update();
    }
    public String[] load(String p){
        String[] r = super.load(p);
        String[] other = p.split("%")[1].split(";");
        Global.loadTime(Integer.parseInt(other[0]));
        stamina = get("Stamina");
        indicater.setMeasuree(stamina);
        return r;
    }
    public void reconnect() {
        Updater.add(this);
        Updater.add(weapon);
        Updater.add(speech);
        Updater.add(indicater);
        Global.display.add(speech);
    }
}
