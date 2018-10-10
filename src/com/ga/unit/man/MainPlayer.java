package com.ga.unit.man;

import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapName;
import com.ga.tool.Line;
import com.ga.tool.Performer;
import com.ga.tool.SyncRand;
import com.ga.tool.T;
import com.ga.unit.Faction;
import com.ga.unit.Particle;
import com.ga.unit.Traits;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import static com.ga.unit.man.BodyHumanoid.BODY;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    private Indicator statBar;
    public int guild = -1, guildVar = 0;
    public String insuranceMap = "";
    public int insuranceExpiry, insuranceCost;

    public MainPlayer(Map map, int seed) {
        super(map, seed);
        Global.player = this;
        Global.cameraTo = pos;
    }

    public void load(T p){
        super.load(p.get(2));
        guild = p.get(0).get(0).getInt();
        guildVar = p.get(0).get(1).getInt();
        insuranceMap = p.get(1).get(0).get();
        insuranceExpiry = p.get(1).get(1).getInt();
        insuranceCost = p.get(1).get(2).getInt();
    }
    public String toString(){
        String r = super.toString();
        return "[["+guild+","+guildVar+"],["+insuranceMap+","+insuranceExpiry+","+insuranceCost+"],["+r+"]]";
    }

    @Override
    public int setState(Actions state) {
        if (state == Actions.Rest && (this.state == Actions.Right || this.state == Actions.Left)) {
            if (vel.x != 0) target.x = dir;
            state = Actions.Sprint;
        } else if (this.state == Actions.Sprint){
            if (state == Actions.Rest || state == Actions.Right || state == Actions.Left) return 6;
        }
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                statBar.show(1, 1);
                if (state.getCost() > stats.strength.getMax()) say("Stamina cost is beyond my limit!");
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
        int level = stats.level.getMax();
        stats.level.addTotal(1);
        stats.motivation.setTotal((level+1)*10, true);
        if (level > 1) {
            int i, current = 1, prev;
            for (i = 1; i < level; i += prev) {
                prev = current;
                current = i;
            }
            if (i != level) { //not fibo
                Stat[] mainStats = new Stat[]{Global.player.stats.strength, Global.player.stats.agility, Global.player.stats.intelligence};
                Stat decayStat;
                int count = 0;
                do {
                    decayStat = mainStats[SyncRand.getInt(mainStats.length)];
                } while (++count < 9 && decayStat.getMax() <= 1);
                if (count >= 9){
                    stats.level.add(-1, true);
                    statChangeNotice += "No stat point is obtained on this level.";
                } else {
                    decayStat.addTotal(-1);
                    statChangeNotice += decayStat.name + " decayed by 1.";
                }
            }
        }
        say("Leveled up! " + statChangeNotice);
    }

    public void die(String cause) {
        super.die(cause);
        map.weather.fadeOut();
        delay.time = 100;
        delay.performer = new Performer(){
            public void perform(){
                delay.time = -1;
                String msg = "Cause: "+cause+"\n";
                if (!insuranceMap.isEmpty()) {
                    int expiry = insuranceExpiry - Global.hour;
                    if (expiry < 0) msg += "Your insurance expired "+expiry+" hours ago.";
                    else if (insuranceCost > 0) msg += "The medical guild leaves you to die as you have not payed off your last debt.";
                    else {
                        map.updater.forceSleep(1);
                        Menu.menu.showDialog("You fall unconscious...", msg+"Your medical guild agents haul your body back into the guild hospital for revival...");
                        revive();
                        Global.server.requestChangeMap(insuranceMap+",911");
                        insuranceCost = -insuranceCost;
                        return;
                    }
                }
                FileManager.save(FileManager.makeDirectory("Data/map/Player.user"),"");
                FileManager.save(FileManager.makeDirectory("Data/map/Actors.map"),"");
                FileManager.save(FileManager.makeDirectory("Data/map/World.map"),"");
                FileManager.save(FileManager.makeDirectory("Data/Keymap.txt"),"");
                FileManager.cleanMaps();
                Menu.menu.showDialog("You die...", msg + "\n GAME OVER!", new String[]{"Click here to exit"}, new Performer[]{
                        new Performer(){
                            public void perform(){
                                System.exit(0);
                            }
                        }
                });
            }
        };
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
        if (traits.getTraitLevel(Traits.Trait.Affinity) > 0) statBar.show(2, 1);
        return super.requireMana(p);
    }

    protected void onMapEdge(int dir){
        if (map.name.isField() && hittable(Faction.Destructive)){
            MapName newMapName = map.name.getNext(dir);
            if (map.updater.sleep(1, false)) {
                vel.set(0,0);
                stateSet(Actions.Idle);
                delay.time = 200;
                Global.server.requestChangeMap(newMapName.toString() +","+ (dir<0?1:2));
            }
        }
        super.onMapEdge(dir);
    }

    public void update() {
        super.update();
        if (!stats.agility.isFull()) statBar.show(0, .7f);
        if (!stats.strength.isFull()) statBar.show(1, .7f);
        if (!stats.intelligence.isFull()) statBar.show(2, .7f);
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
        statBar.setMeasuree(0, stats.agility);
        statBar.setMeasuree(1, stats.strength);
        statBar.setMeasuree(2, stats.intelligence);
        return this;
    }
}
