package com.ga.unit.player;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Line;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import static com.ga.unit.BodyHumanoid.BODY;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    public PlayerInput input;
    private Indicater indicater;
    public MainPlayer(Map map, int seed) {
        super(map, seed);
        Global.player = this;
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
        if (state == Actions.Use && this.state == Actions.Jump && getStateLevel(Actions.Airstrike) > 0
                && this.delay.time > 32 && hasSkill(Actions.Airstrike)) stateSet(Actions.Airstrike);
        else if (state == Actions.Jump && this.state == Actions.Rest) state = Actions.Leap;
        else if (state == Actions.Discard && this.state == Actions.Rest) state = Actions.Throw;
        else if (state == Actions.Grab && this.state == Actions.Rest) state = Actions.Use2;
        else if (state == Actions.Rest && this.state == Actions.Walk){
            if (vel.x != 0) target.x = dir;
            state = Actions.Sprint;
        }
        int r = super.setState(state);
        if (r == 2) indicater.show(1);
        else if (r == 3) indicater.showCd(getCooldown(state)[0]);
        return r;
    }
    @Override
    public void stateSet(Actions state){
        super.stateSet(state);
        if (state != Actions.Idle){
            input.cameraMode = false;
            if (state == Actions.Walk) face(input.moving, true);
        }
    }
    public float getShopPrice(){
        float r = 1;
        if (hasTrait(Trait.Dull)) r = 1.1f;
        else if (hasTrait(Trait.Sharp)) r = .9f;
        return r;
    }
    public boolean checkHit(Line hitBox, int damage, boolean blunt) {
        int before = get("Stamina").get();
        boolean r = super.checkHit(hitBox, damage, blunt);
        if (r) indicater.show(before/Math.max(1,get("Stamina").get()));
        return r;
    }
    public boolean learnable(){
        return getTotalExpertise() < get("Memory").getMax();
    }
    private int getTotalExpertise(){
        int total = 0;
        for (Tag i : Tags.trainers){
            total += get(i.name()).get()/100;
        }
        return total;
    }
    public void witnessDeath(Unit p){
        if (p.pos.getSubtract(pos).inOval(900,500)){
            int exp = Math.max(0,p.get("Level").get()*3-get("Level").get());
            if (exp > 0) {
                if (learning != null) {
                    boolean fast = hasTrait(Trait.Fast_Learner) && get("Motivation").require(3);
                    if (fast || get("Motivation").require(1)) {
                        Stat progress = get("Progress");
                        progress.add(exp * (fast ? 2 : 1), true);
                        if (progress.isFull()) {
                            learnSkill(learning);
                            exp = Math.max(0, progress.get() - progress.getMax()); //residue exp
                            progress.setMax(0);
                            progress.set(0, true);
                            learning = null;
                        } else exp = 0;
                    }
                }
                level(exp);
            }
        }
    }
    private void level(int p){
        Stat exp = get("Experience");
        Stat level = get("Level");
        exp.add(p, true);
        if (exp.isFull()){
            Sound.play(pos, "heal");
            Particle.holyExplosion(map, pos.getAdd(clip.organs[BODY].ptd), getDepth()-1);
            level.setMax(level.getMax()+1);
            int residue = exp.get() - exp.getMax();
            exp.setMax(10*level.getMax());
            exp.set(residue, true);
            get("Endurance").setMax(1000+level.getMax()*50);
            int prev = 1;
            int current = 1;
            boolean pass = true;
            while (pass) {
                int pprev = prev;
                prev = current;
                current += pprev;
                if (level.getMax() > 30 || 30 - current == level.getMax()){ //decay
                    String[] stats = new String[]{
                            "Stamina", "Strength", "Dexterity", "Agility", "Memory"
                    };
                    get(stats[Global.r.nextInt(5)]).add(-1, true);
                    pass = false;
                } else if (30 - current < level.getMax()) pass = false;
            }
        }
    }
    public void metabolise() {
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
        map.weather.fadeOut();
    }
    public void revive(){
        super.revive();
        map.weather.fadeIn();
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
        if (input.cameraMode) map.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(input.movement));
        else map.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
        if (!get("Stamina").isFull()) indicater.show(.5f);
        indicater.update();
    }
    public String[] load(String p){
        String[] r = super.load(p);
        return r;
    }
    public void disconnect(){
        super.disconnect();
        indicater.remove();
    }
    public void start(){
        super.start();
        indicater = new Indicater(this);
        indicater.setMeasuree(get("Stamina"));
    }
    public String toCharacterString() {
        return super.toCharacterString()+input.toString()+"\n";
    }
}
