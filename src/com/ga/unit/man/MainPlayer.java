package com.ga.unit.man;

import com.ga.game.*;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapName;
import com.ga.tool.Line;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.unit.Faction;
import com.ga.unit.Particle;
import com.ga.unit.Traits;
import com.ga.unit.skill.Actions;

import static com.ga.unit.man.BodyHumanoid.BODY;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    private static int[] reqMot = new int[]{10,20,30,50,80,130,210,340,550,1000};
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

    public String getTitle(){
        if (guild == 1) return "Member";
        else if (guild == 2 && guildVar == 0) return "Contractor";
        else if (guild == 2 && guildVar == 1) return "Guildsman";
        else if (guild == 3) {
            if (guildVar <= 1) return "Associate";
            else if (guildVar <= 2) return "Veteran";
            else if (guildVar <= 3) return "Operative";
            else if (guildVar <= 4) return "Veteran Operative";
            else if (guildVar <= 5) return "Specialist";
            else if (guildVar <= 6) return "Veteran Specialist";
            else if (guildVar <= 7) return "Agent";
            else if (guildVar <= 8) return "Veteran Agent";
            else if (guildVar <= 9) return "Special Agent";
            else if (guildVar <= 10) return "Veteran Special Agent";
        }
        return "";
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
            else if (r == 8) say(Dialog.get("!i"));
            else if (r == 9) say(Dialog.get("!f"));
        }
        return r;
    }

    public boolean hit(Line hitBox, boolean blunt, int damage) {
        boolean r = super.hit(hitBox, blunt, damage);
        if (r) statBar.show(0, 1);
        return r;
    }

    public void level() {
        int level = stats.level.getMax();
        Sound.play(map, pos, "heal");
        Particle.holyExplosion(map, pos.getAdd(clip.organs[BODY].ptd), getDepth());
        if (reqMot.length > level) {
            stats.level.addTotal(1);
            stats.motivation.setTotal(reqMot[level], true);
            say("Leveled up!");
        } else say("Level cannot increase any higher!");
    }

    public void die(int cause, String desc) {
        super.die(cause, desc);
        map.weather.fadeOut();
        delay.time = 100;
        delay.performer = new Performer(()->{
            delay.time = -1;
            String msg = "Sign of death: "+ desc +".\n";
            if (!insuranceMap.isEmpty()) {
                int expiry = insuranceExpiry - Global.hour;
                if (expiry < 0) msg += "Your insurance expired "+expiry+" hours ago.";
                else if (insuranceCost > 0) msg += "The medical guild leaves you to die as you have not payed off your last debt.";
                else {
                    map.updater.forceSleep(1);
                    Menu.menu.showDialog("You fall unconscious...", msg+"Your guild agents haul your body back into the guild hospital for revival...");
                    revive();
                    Global.server.requestChangeMap(insuranceMap+",911");
                    insuranceCost = -insuranceCost;
                    return;
                }
            }
            FileManager.save(FileManager.makeDirectory("Data/map/Player.user"),"");
            FileManager.save(FileManager.makeDirectory("Data/map/World.map"),"");
            FileManager.save(FileManager.makeDirectory("Data/Keymap.txt"),"");
            FileManager.cleanMaps();
            Menu.menu.showDialog("You die...", msg + "\n GAME OVER!", new String[]{"Click here to exit"}, new Performer[]{
                new Performer(()-> System.exit(0))
            });
        });
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
