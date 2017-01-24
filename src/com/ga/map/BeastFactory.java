package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Beast;
import com.ga.unit.Faction;
import com.ga.unit.Updater;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Spells;

/**
 * Created by Ga on 8/2/2016.
 */
public class BeastFactory {
    enum Type{
        Dog(Faction.Evil, 1,10,30,10,3,"-5,50/Kick&Pierce"),
        Floater(Faction.Evil, 3,10,1,60,3,"200,800/Flamestride,Flameroll&Flameroll,Fireball"),
        Fire_Spitter(Faction.Evil, 3,10,1,60,3,"300/Fireblast,Idle,Idle,Idle"),
        Slime(Faction.Evil, 1,10,6,10,1,"5/Kick"),
        Spider(Faction.Evil, 1,10,30,10,3,"-5/Kick"),
        Mantis(Faction.Evil, 10,100,40,10,3,"-5/Kick");
        private int level,health,speed,stamina,proficiency;
        private Faction faction;
        private String rangeMove;
        Type(Faction faction, int lvl, int hp, int spd, int arm, int prof, String rangeMove){
            this.faction = faction;
            this.level = lvl;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.proficiency = prof;
            this.rangeMove = rangeMove;
        }
        Beast get(String p){
            Beast r;
            switch (this){
                case Dog:
                    r = new Beast(p){
                        protected void stateSet(Moveset state){
                            super.stateSet(state);
                            if (state == Skills.Pierce){
                                delay.time = 30;
                                vel.x += getSpeed()*3*dir;
                                vel.y -= 20;
                            }
                        }
                    };
                    break;
                case Slime:
                    r = new Beast(p) {
                        protected void stateSet(Moveset state) {
                            super.stateSet(state);
                            if (state == Skills.Kick) {
                                moveTriggered(0);
                                delay.time = 24;
                            }
                        }
                    };
                    break;
                case Mantis:
                    r = new Beast(p);
                    r.clip.organs[1].pt.y -= r.clip.organs[0].ptd.y;
                    break;
                case Floater:
                case Fire_Spitter:
                    r = new Beast(p){
                        public void collide(){
                            super.collide();
                            jump = true;
                        }
                        public P getHand(){
                            return pos.getAdd(0,getReach()+40);
                        }
                        public void stateSet(Moveset state) {
                            boolean spell = state instanceof Spells;
                            super.stateSet(state);
                            if (spell) delay.set(1);
                        }
                        public void update(){
                            super.update();
                            vel.y += .1f;
                            if (state != Actions.Dead) {
                                for (Line i : Global.map.land) {
                                    if (i.lineCross(new Line(pos, pos.getAdd(0, 200)))) {
                                        vel.y += ((i.pt.y + (i.getSlope() * (pos.x + vel.x - i.pt.x))) - (pos.y + 200)) * .01f;
                                        break;
                                    }
                                }
                            }
                        }
                    };
                    r.brain.range = 400;
                    r.gravity = false;
                    break;
                default:
                    r = new Beast(p);
            }
            return r;
        }
        Beast get(int seed, P p){
            String stat = "Level:"+level+"&Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health;
            String t = name()+"@"+seed+"@"+p+"@"+Updater.getId()+"@"+stat+"@"+faction.name()+"@"+rangeMove;
            return get(t);
        }
    }
    public static Beast produce(String p){
        return Type.valueOf(p.split("@")[0]).get(p);
    }
    public static Beast produce(String name, P pt, int seed){
        Beast result;
        String t = Word.wordify(name);
        try {
            result = Type.valueOf(t).get(seed, pt);
        } catch (Exception e) {
            Global.p("Can't find beast ["+t+"]");
            result = Type.Dog.get(seed, pt);
        }
        return result;
    }
}
