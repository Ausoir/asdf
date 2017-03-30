package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Spells;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;

import static com.ga.unit.BodyHumanoid.FIST;

/**
 * Created by Ga on 12/29/2016. asd
 */
public class MainPlayer extends Player {
    public boolean fastTravel = true;
    public MainPlayer(String p) {
        super(p);
        Global.player = this;
        final Stat hunger = get("Hunger");
        hunger.onChange = new Performer(){
            public void perform() {
                int hung = hunger.get();
                if (hung < -50) {
                    die();
                } else if (hung < 0){
                    get("Health").add(hung);
                    say("I'm starving!");
                } else if (hung < 10){
                    say("I'm hungry!");
                }
            }
        };
    }
    @Override
    public void say(String p){
        if (p.startsWith("'") && p.endsWith("'")){
            memorized = p.substring(1,p.length()-1);
            p = "Memorized "+p;
        }
        speech.say(p);
        Spells t = Spells.getByName(p);
        if (t != null && skillTree.getSkillLevel(t) > 0) setState(t);
    }
    @Override
    public int setState(Moveset state) {
        if (state == Actions.Use && this.state == Actions.Rest && skillTree.getSkillLevel(Actions.Triple) > 0) state = Actions.Triple;
        else if (state == Actions.Use && this.state == Actions.Jump && skillTree.getSkillLevel(Actions.Airstrike) > 0 && this.delay.time > 32) stateSet(Actions.Airstrike);
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
        return super.setState(state);
    }
    @Override
    public void stateSet(Moveset state){
        super.stateSet(state);
        if (weapon.getType() == Item.Tag.Claw)
            weapon.rotation = weapon.getOrgan().getAtan() + (dir < 0 ? 3.14f : 0);
        if (state != Actions.Idle) input.cameraMode = false;
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Idle:
                    if (!get("Stamina").isFull()) clip.setState(Actions.Exhausted);
                    break;
                case Sprint:
                case Walk:
                    float speed = getSpeed();
                    if (speed <= 0) clip.setState(Actions.Exhausted);
                    else clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
                    face(input.moving, true);
                    break;
                case Use:
                    if (weapon.uses.get() == 0) say("Can't use " + weapon.getName());
                    break;
                case Interact:
                    final Player me = this;
                    delay.performer = new Performer() {
                        public void perform() {
                            P reach = pos.getAdd(clip.organs[FIST].ptd);
                            ArrayList<Prop> obj = Updater.collideInteractive(reach);
                            for (Prop i : obj) {
                                if (i.onInteract != null) {
                                    i.onInteract.interact(me);
                                    break;
                                }
                            }
                            for (final Line i : Global.map.land) {
                                if (i.opt == 1) {
                                    if (i.lineCross(new Line(reach.x - (12 * dir), reach.y, reach.x + (12 * dir), reach.y))) {
                                        clip.setState(Actions.Guard);
                                        delay.set(40);
                                        final int firstDir = dir;
                                        delay.performer = new Performer() {
                                            public void perform() {
                                                pos.x = i.pt.x - (24 * dir);
                                                setState(Actions.Climb);
                                                dir = firstDir;
                                                input.moving = 0;
                                                rope = i;
                                            }
                                        };
                                    }
                                }
                            }
                        }
                    };
                    break;
                case Wield:
                    delay.performer = new Performer() {
                        public void perform() {
                            if (selectingItem == -1) {
                                sheathe();
                            } else if (selectingItem < inventory.getItemSize()) {
                                Item item = inventory.getItem(selectingItem);
                                setWeapon(item);
                                inventory.remove(item);
                                selectingItem = -1;
                            } else if (selectingItem >= 10 && selectingItem <= 16) {
                                Sound.play("unwear");
                                int piece = selectingItem - 10;
                                if (sheathe() && !armor.isBare(piece)) {
                                    setWeapon(armor.armor[piece]);
                                    armor.set(null, piece);
                                }
                            }
                        }
                    };
                    break;
                case Duck:
                    cooldown.put(state, new int[]{Global.getTime()[0] + ((armor.armor[1].encumberance + armor.armor[2].encumberance) * 4)});
                    break;
                case Guard:
                    cooldown.put(state, new int[]{Global.getTime()[0] + ((armor.armor[FIST].encumberance + armor.armor[0].encumberance) * 4)});
                    break;
                case Snipe:
                case Shoot:
                    weapon.rotateTo(target.getMult(dir, dir));
                    weapon.rotation += .1 * dir;
            }
        }
    }
    public void onTimeChange(int p) {
        Stat hunger = get("Hunger");
        if (Stock.getFactoryName(armor.armor[0].factoryId).equals("Stomach Full Helm")) p = 0;
        hunger.add(-2 * p, true);
        if (hunger.get() < 10) inventory.eat(hunger.getMax() - hunger.get());
        if (stats.getEffectsSize() == 0) {
            get("Speed").heal();
            get("Strength").heal();
        }
    }
    void onMapChange(){
        reset();
        stateSet(Actions.Wait);
        delay.set(5);
    }
    protected void stateDone(){
        super.stateDone();
        if (input.moving != 0) setState(Actions.Walk);
    }
    protected void stateUpdate(){
        super.stateUpdate();
        if (state == Actions.Walk && input.moving == 0) setState(Actions.Idle);
    }
    public void update(){
        int edge = Global.mapEdge(pos.getAdd(vel));
        if (edge % 2 == 0){
            int hunger = get("Hunger").get();
            if (hunger > 0 || get("Health").get()/(hunger*-2) > 0) Global.map.loadMap(edge);
        }
        super.update();
        if (input.cameraMode) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(input.movement));
        else Global.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
    }
    public void load(String p){
        super.load(p);
        String[] other = p.split("%")[1].split(";");
        Global.setTime(Integer.parseInt(other[3]));
        Global.gameHour = Integer.parseInt(other[4]);
    }
}
