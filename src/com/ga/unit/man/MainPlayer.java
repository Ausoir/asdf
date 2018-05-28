package com.ga.unit.man;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapName;
import com.ga.tool.Line;
import com.ga.tool.SyncRand;
import com.ga.unit.Particle;
import com.ga.unit.Tag;
import com.ga.unit.Tags;
import com.ga.unit.Traits;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import static com.ga.unit.man.BodyHumanoid.BODY;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    public static String[] mainStats = new String[]{
            "Strength", "Agility", "Intelligence"
    };
    private Indicator indicator;

    public MainPlayer(Map map, int seed) {
        super(map, seed);
        Global.player = this;
        Global.cameraTo = pos;
    }

    @Override
    public int setState(Actions state) {
        if (state == Actions.Use && !grounded){
            if (vel.y < 0) setState(Actions.Airstrike);
            else if (vel.y > 0) setState(Actions.Downstrike);
        } else if (state == Actions.Jump && this.state == Actions.Rest) state = Actions.Leap;
        else if (state == Actions.Rest && (this.state == Actions.Right || this.state == Actions.Left)) {
            if (vel.x != 0) target.x = dir;
            state = Actions.Sprint;
        } else if (this.state == Actions.Sprint){
            if (state == Actions.Rest || state == Actions.Right || state == Actions.Left) return 6;
        }
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                indicator.show(1);
                if (state.getCost() > get("Strength").getMax()) say("Stamina cost is beyond my limit!");
            } else if (r == 3) {
                indicator.showCd(getCooldown(state).time);
            } else if (r == 4) {
                say("Cannot cast!");
            } else if (r == 5) say("This action is beyond my abilities!");
        }
        return r;
    }

    public float getShopPrice() {
        float r = 1;
        if (traits.has(Traits.Trait.Haggler)) r = .7f;
        return r;
    }

    public boolean hit(Line hitBox, int damage) {
        int before = get("Strength").get();
        boolean r = super.hit(hitBox, damage);
        if (r) indicator.show(before / Math.max(1, get("Strength").get()));
        return r;
    }

    public boolean learnable() {
        return getTotalExpertise() < get("Intelligence").getMax();
    }

    private int getTotalExpertise() {
        int total = 0;
        for (Tag i : Tags.trainers) {
            total += get(i.name()).get() / 100;
        }
        return total;
    }

    public int getLevel(){
        int level = -8;
        for (String mainStat : mainStats){
            level += get(mainStat).getMax();
        }
        return level;
    }

    private void level(String stat) {
        int requiredMotivation = traits.has(Traits.Trait.Fast_Learner) ? 60 : 40;
        int requiredTime = traits.has(Traits.Trait.Fast_Learner) ? 1 : 3;
        if (map.updater.sleep(requiredTime, true) && get("Motivation").require(requiredMotivation)) {
            get(stat).addTotal(1);
            Sound.play(map, pos, "heal");
            Particle.holyExplosion(map, pos.getAdd(clip.organs[BODY].ptd), getDepth());
            int level = getLevel();
            if (level > 1) {
                int i, current = 1, prev;
                for (i=1;i<level;i+=prev){
                    prev = current;
                    current = i;
                }
                if (i != level) { //not fibo
                    Stat decay;
                    do {
                        decay = get(mainStats[SyncRand.r.nextInt(mainStats.length)]);
                    } while (decay.getMax() <= 1);
                    decay.addTotal(-1);
                }
            }
        }
    }

    public void die() {
        super.die();
        map.weather.fadeOut();
    }

    public void revive() {
        super.revive();
        map.weather.fadeIn();
    }

    protected void stateUpdate() {
        super.stateUpdate();
        if (state == Actions.Rest) indicator.show(1);
    }

    protected boolean stateFinish() {
        boolean r = super.stateFinish();
        if (r) input.update();
        return r;
    }

    protected void onMapEdge(int dir){
        if (map.name.isField()){
            MapName newMapName = map.name.getNext(dir);
            if (newMapName.isValid() && map.updater.sleep(1, true)) {
                vel.set(0,0);
                stateSet(Actions.Idle);
                delay.time = 200;
                Global.server.requestChangeMap(newMapName.toString() +"@"+ (dir<0?1:2));
            }
        }
        super.onMapEdge(dir);
    }

    public void update() {
        super.update();
        if (!get("Strength").isFull()) indicator.show(.5f);
        indicator.update();
        if (input.ctrl) Global.cameraTo = pos.getAdd(target.getMult(.5f,.5f));
        else Global.cameraTo = pos.getAdd(vel.getAdd(0,-Settings.gravity).clamp(-10,-10,10,10).mult(50,20));
    }

    public void remove() {
        super.remove();
        indicator.remove();
    }

    public MainPlayer start() {
        super.start();
        indicator = new Indicator(this);
        indicator.setMeasuree(get("Strength"));
        return this;
    }
}
