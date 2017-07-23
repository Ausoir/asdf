package com.ga.unit.player;

import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Line;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Tag;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    private Indicater indicater;
    private Stat stamina;
    public MainPlayer(int seed) {
        super("Me", seed);
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
        if (state == Actions.Use && this.state == Actions.Jump && getStateLevel(Actions.Airstrike) > 0 && this.delay.time > 32) stateSet(Actions.Airstrike);
        else if (state == Actions.Jump && this.state == Actions.Rest) state = Actions.Leap;
        else if (state == Actions.Discard && this.state == Actions.Rest) state = Actions.Throw;
        else if (state == Actions.Rest && this.state == Actions.Walk) state = Actions.Sprint;
        else if (state == Actions.Kick && this.state == Actions.Rest) state = Actions.Sidekick;
        else if (state == Actions.Use && this.state == Actions.Walk){
            if (input.moving > 0 == target.x > 0 && !wieldingNone()) state = Actions.Thrust;
            else if (skillTree.getSkillLevel(Actions.Dash) > 0) state = Actions.Dash;
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
        if (hasTrait(Trait.Dull)) r = 1.1f;
        else if (hasTrait(Trait.Sharp)) r = .9f;
        return r;
    }
    public boolean checkHit(Line hitBox, int damage, boolean blunt) {
        int before = stamina.get();
        boolean r = super.checkHit(hitBox, damage, blunt);
        if (r) indicater.show(before/Math.max(1,stamina.get()));
        return r;
    }
    public void onTimeChange() {
        if (!Stock.getFactoryName(armor.armor[0].factoryId).equals("Stomach Full Helm")) {
            health.starve(1);
            int status = health.getStatus();
            if (status == 0){ //normal
                int normalLevel = Global.player.hasTrait(Trait.Fast_Metabolism)?7:10;
                buff(Buffs.Metabolism, normalLevel-stats.getEffectLevel(Buffs.Metabolism));
            } else if (status == 1) {
                say("Food...");
                die();
            } else if (status == 2) {
                say("I'm starving!");
                health.record("0");
                buff(Buffs.Metabolism, 6);
            } else if (status == 3) say("I don't feel so good...");
            else if (status == 4){
                say("I'm hungry!");
                buff(Buffs.Metabolism, 2);
            }
        }
        if (hasTrait(Trait.Optimistic)) get("Motivation").add(1, false);
        else if (hasTrait(Trait.Pessimistic)) get("Motivation").add(-1, false);
    }
    public void die(){
        super.die();
        Updater.cancelRemove(this);
        Global.map.weather.fadeOut();
    }
    public void revive(){
        super.revive();
        Global.map.weather.fadeIn();
    }
    protected void stateDone(){
        super.stateDone();
        if (input.moving != 0 && state != Actions.Sprint) setState(Actions.Walk);
    }
    protected void stateUpdate(){
        super.stateUpdate();
        if (state == Actions.Walk && input.moving == 0) setState(Actions.Idle);
        else if (state == Actions.Rest) indicater.show(1);
    }
    public void update(){
        super.update();
        if (input.cameraMode) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(input.movement));
        else Global.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
        if (!stamina.isFull()) indicater.show(.5f);
        indicater.update();
    }
    public String[] load(String p){
        String[] r = super.load(p);
        String[] other = p.split("%")[1].split(";");
        Global.time = Integer.parseInt(other[0]);
        stamina = get("Stamina");
        indicater.setMeasuree(stamina);
        return r;
    }
    public void reconnect() {
        lastLand = new Line();
        Updater.addQueue.add(this);
        Updater.addQueue.add(weapon);
        Updater.addQueue.add(speech);
        Updater.addQueue.add(indicater);
        Global.display.add(speech);
    }
}
