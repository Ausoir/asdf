package com.ga.unit.man;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapName;
import com.ga.tool.Line;
import com.ga.tool.SyncRand;
import com.ga.unit.Particle;
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
    private Indicator statBar;

    public MainPlayer(Map map, int seed) {
        super(map, seed);
        Global.player = this;
        Global.cameraTo = pos;
    }

    @Override
    public int setState(Actions state) {
        if (state == Actions.Use && !grounded) state = Actions.Airstrike;
        else if (state == Actions.Jump && this.state == Actions.Rest) state = Actions.Leap;
        else if (state == Actions.Rest && (this.state == Actions.Right || this.state == Actions.Left)) {
            if (vel.x != 0) target.x = dir;
            state = Actions.Sprint;
        } else if (this.state == Actions.Sprint){
            if (state == Actions.Rest || state == Actions.Right || state == Actions.Left) return 6;
        }
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                statBar.show(1, 1);
                if (state.getCost() > get("Strength").getMax()) say("Stamina cost is beyond my limit!");
            } else if (r == 3) {
                statBar.showCd(getCooldown(state).time);
            } else if (r == 4) {
                say("Cannot cast!");
            } else if (r == 5) say("This action is beyond my abilities!");
            else if (r == 6) say("I'm missing a required limb!");
            else if (r == 8) say("Not enough Energy!");
        }
        return r;
    }

    public boolean hit(Line hitBox, boolean blunt, int damage) {
        boolean r = super.hit(hitBox, blunt, damage);
        if (r) statBar.show(0, 1);
        return r;
    }

    public void level() {
        Sound.play(map, pos, "heal");
        Particle.holyExplosion(map, pos.getAdd(clip.organs[BODY].ptd), getDepth());
        String statChangeNotice = "";
        int level = get("Level").getMax();
        if (level > 1) {
            int i, current = 1, prev;
            for (i = 1; i < level; i += prev) {
                prev = current;
                current = i;
            }
            if (i != level) { //not fibo
                Stat decay;
                String decayStat;
                do {
                    decayStat = mainStats[SyncRand.r.nextInt(mainStats.length)];
                    decay = get(decayStat);
                } while (decay.getMax() <= 1);
                decay.addTotal(-1);
                statChangeNotice += decayStat + " decayed by 1.";
            }
        }
        say("Leveled up! " + statChangeNotice);
        get("Level").addTotal(1);
    }

    public void die() {
        super.die();
        map.weather.fadeOut();
    }

    public void revive() {
        super.revive();
        map.weather.fadeIn();
    }

    protected boolean stateFinish() {
        boolean r = super.stateFinish();
        if (r) input.update();
        return r;
    }

    public boolean requireMana(int p) {
        if (traits.getTraitLevel(Traits.Trait.Attunement) > 0) statBar.show(2, 1);
        return super.requireMana(p);
    }

    protected void onMapEdge(int dir){
        if (map.name.isField()){
            MapName newMapName = map.name.getNext(dir);
            if (map.updater.sleep(1, false)) {
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
        if (!get("Agility").isFull()) statBar.show(0, .7f);
        if (!get("Strength").isFull()) statBar.show(1, .7f);
        if (!get("Intelligence").isFull()) statBar.show(2, .7f);
        statBar.update();
        if (input.ctrl) Global.cameraTo = pos.getAdd(target.getMult(.5f,.5f));
        else Global.cameraTo = pos.getAdd(vel.getAdd(0,-Settings.gravity).clamp(-8,-6,8,6).mult(24,8));
    }

    public void remove() {
        super.remove();
        statBar.remove();
    }

    public MainPlayer start() {
        super.start();
        statBar = new Indicator(this);
        statBar.setMeasuree(0, get("Agility"));
        statBar.setMeasuree(1, get("Strength"));
        statBar.setMeasuree(2, get("Intelligence"));
        return this;
    }
}
