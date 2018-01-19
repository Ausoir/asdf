package com.ga.unit.man;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Line;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import static com.ga.unit.man.BodyHumanoid.BODY;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    private Indicater indicater;

    public MainPlayer(Map map, int seed) {
        super(map, seed);
        Global.player = this;
    }

    @Override
    public void say(String p) {
        if (p.startsWith("'") && p.endsWith("'")) {
            memorized = p.substring(1, p.length() - 1).replace("@;#", "");
            p = "Memorized " + p;
        }
        speech.say(p);
    }

    @Override
    public int setState(Actions state) {
        if (state == Actions.Use && this.state == Actions.Jump && getStateLevel(Actions.Airstrike) > 0
                && this.delay.time > 32 && hasSkill(Actions.Airstrike)) stateSet(Actions.Airstrike);
        else if (state == Actions.Jump && this.state == Actions.Rest) state = Actions.Leap;
        else if (state == Actions.Discard && this.state == Actions.Rest) state = Actions.Throw;
        else if (state == Actions.Rest && (this.state == Actions.Walk || this.state == Actions.WalkL)) {
            if (vel.x != 0) target.x = dir;
            state = Actions.Sprint;
        } else if (this.state == Actions.Sprint){
            if (state == Actions.Rest || state == Actions.Walk || state == Actions.WalkL) return 6;
        }
        int r = super.setState(state);
        if (r == 2) indicater.show(1);
        else if (r == 3) indicater.showCd(getCooldown(state)[0]);
        return r;
    }

    public float getShopPrice() {
        float r = 1;
        if (traits.has(Traits.Trait.Haggler)) r = .7f;
        return r;
    }

    public boolean hit(Line hitBox, int damage) {
        int before = get("Stamina").get();
        boolean r = super.hit(hitBox, damage);
        if (r) indicater.show(before / Math.max(1, get("Stamina").get()));
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

    public void witnessDeath(Unit p) {
        if (p.faction.canAttack(faction) && p.pos.getSubtract(pos).inOval(900, 500)) {
            int exp = Math.max(0, p.get("Level").get() * 3 - get("Level").get());
            if (exp > 0) level(exp);
        }
    }

    private void level(int p) {
        boolean fast = traits.has(Traits.Trait.Fast_Learner) && get("Motivation").require(3);
        Stat exp = get("Experience");
        Stat level = get("Level");
        if (fast || get("Motivation").require(1)) {
            exp.add(p * (fast ? 2 : 1), true);
            if (exp.isFull()) {
                Sound.play(map, pos, "heal");
                Particle.holyExplosion(map, pos.getAdd(clip.organs[BODY].ptd), getDepth());
                level.setMax(level.getMax() + 1);
                int residue = exp.get() - exp.getMax();
                exp.setMax(10 * level.getMax());
                exp.set(residue, true);
                get("Endurance").setMax(500 + level.getMax() * 50);
                int prev = 1;
                int current = 1;
                boolean pass = true;
                while (pass) {
                    int pprev = prev;
                    prev = current;
                    current += pprev;
                    if (level.getMax() > 30 || 30 - current == level.getMax()) { //decay
                        String[] stats = new String[]{
                                "Stamina", "Strength", "Dexterity", "Intelligence", "Wisdom"
                        };
                        get(stats[Global.r.nextInt(5)]).add(-1, true);
                        pass = false;
                    } else if (30 - current < level.getMax()) pass = false;
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
        if (state == Actions.Rest) indicater.show(1);
    }

    protected void stateFinish() {
        super.stateFinish();
        input.update();
    }

    protected void onMapEdge(int dir){
        if (map.name.startsWith("_M")) Global.server.requestChangeMap("_M"+(Integer.parseInt(map.name.substring(1))+dir)+"@"+(dir<0?0:1));
        else super.onMapEdge(dir);
    }

    public void update() {
        super.update();
        if (!get("Stamina").isFull()) indicater.show(.5f);
        indicater.update();
        map.mapEdgeCenter(Global.cameraTo = pos.getAdd(0, 0));
    }

    public void remove() {
        super.remove();
        indicater.remove();
    }

    public MainPlayer start() {
        super.start();
        indicater = new Indicater(this);
        indicater.setMeasuree(get("Stamina"));
        return this;
    }
}
