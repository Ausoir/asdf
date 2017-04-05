package com.ga.unit.item;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.skill.*;

import java.awt.*;
import java.util.Random;

/**
 * Created by Ga on 8/5/2016. asd
 */
public class Item extends Prop {
    public enum Tag{
        Sword(Actions.Slash, DamageType.Cut, "swing_knife", "hit_cut"), Spear(Actions.Thrust, DamageType.Cut, "swing_silent", "hit_bloody"), Mace(Actions.Slash, DamageType.Bash, "swing_silent", "hit_bash"),
        Dagger(Actions.Thrust, DamageType.Cut, "swing_normal", "hit_bloody"), Armor(Actions.Rest, DamageType.Push, "swing_normal"), Scroll(Actions.Guard, DamageType.Push, "scroll"),
        Shoot(Actions.Shoot, DamageType.Cut, "swing_normal"), Snipe(Actions.Snipe, DamageType.Cut, "swing_normal"),
        Throwing(Actions.Throw, DamageType.Cut, "swing_silent"), Darting(Actions.Dart, DamageType.Cut, "swing_normal"), Consumable(Actions.Consume, DamageType.Push, "drink"),
        Claw(Actions.Slash, DamageType.Cut, "swing_normal"), Enchant(Actions.Buff, DamageType.Push, "swing_silent"), Food(Actions.Consume, DamageType.Push, "eat");
        private Moveset action;
        private DamageType type;
        private String sound, soundHit = "hit_punch";
        Tag(Moveset p, DamageType type, String sound){
            action = p;
            this.type = type;
            this.sound = sound;
        }
        Tag(Moveset p, DamageType type, String sound, String soundHit){
            this(p, type, sound);
            this.soundHit = soundHit;
        }
        public Moveset getAction() {
            return action;
        }
        public DamageType getDamageType(){
            return type;
        }
        public String getSound(){
            return sound;
        }
        public String getHitSound(){
            return soundHit;
        }
    }
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Hasty, Lazy, Durable, Tough, Runic, Instant, Comfortable, Cursed, Quick, Slow, Recharging;
        static SpecialtyMod get(Random seed){
            if (seed.nextGaussian() > 1) return values()[seed.nextInt(values().length)];
            return None;
        }
        static SpecialtyMod load(String p){
            for (SpecialtyMod i : values()){
                if ((""+i.ordinal()).equals(p)) return i;
            }
            return None;
        }
        public String toString() {
            return ""+ordinal();
        }
    }
    enum PrefixMod{
        None(125), Broken(20), Old(20), Fine(15), Mastercraft(4), Legendary(1);
        static int pool;
        int weight;
        PrefixMod(int weight){
            expandPool(weight);
            this.weight = weight;
        }
        void expandPool(int weight){
            pool += weight;
        }
        static PrefixMod get(Random seed){
            int r = seed.nextInt(pool);
            int t = 0;
            for (PrefixMod i : values()){
                t+=i.weight;
                if (t >= r) return i;
            }
            return None;
        }
        static PrefixMod load(String p){
            for (PrefixMod i : values()){
                if ((""+i.ordinal()).equals(p)) return i;
            }
            return None;
        }
        public String toString() {
            return ""+ordinal();
        }
    }
    enum AdverbMod{
        None(1), Very(2), Super(3), Ultra(4);
        int magnitude;
        AdverbMod(int mag){
            this.magnitude = mag;
        }
        static AdverbMod load(String p){
            for (AdverbMod i : values()){
                if ((""+i.ordinal()).equals(p)) return i;
            }
            return None;
        }
        static AdverbMod get(Random seed){
            int r = seed.nextInt(100);
            if (r < 1) return Ultra;
            else if (r < 3) return Super;
            else if (r < 8) return Very;
            return None;
        }
        public String toString(){
            return ""+ordinal();
        }
    }
    public enum AdenchantMod{
        None(2,.15f), Minor(1,.1f), Major(3,.25f), Greater(4,.35f), Ultimate(5,.4f);
        public int level;
        public float magnitude;
        AdenchantMod(int level, float mag){
            this.level = level;
            this.magnitude = mag;
        }
        public static AdenchantMod get(Random seed){
            float t = (float) seed.nextGaussian();
            if (t > 2.5) return Ultimate;
            else if (t > 2) return Greater;
            else if (t > 1.5) return Major;
            else if (t < -1) return Minor;
            else return None;
        }
        static AdenchantMod load(String p){
            for (AdenchantMod i : values()){
                if ((""+i.ordinal()).equals(p)) return i;
            }
            return None;
        }
        public String toString(){
            return ""+ordinal();
        }
    }
    AdverbMod admod = AdverbMod.None;
    protected AdenchantMod ademod = AdenchantMod.None;
    private SpecialtyMod smod = SpecialtyMod.None;
    private PrefixMod pmod = PrefixMod.None;
    protected Buffs emod = null;
    public int weight;
    public int volume = 1;
    protected int cooldown = 0;
    public int delay = 0;
    public float probability;
    Moveset action = Actions.Slash;
    private int[] lastused = new int[]{0};
    public Stat uses = new Stat(-1);
    private Tag type = Tag.Throwing;
    private Organ organ = null;
    public int speed = -4;
    public int damage = 0;
    public int value = 8;
    public boolean breakable = true;
    public Item(String name, int seed, int weight, int uses) {
        super(name, seed, 100);
        dynamic = true;
        this.name = name;
        this.weight = weight;
        this.uses.setTotal(uses);
        gravity = true;
        final Item me = this;
        onInteract = new Interactive(){
            public void interact(Man p){
                p.setWeapon(me);
            }
        };
        randomSize(new P(.2f,.2f));
    }
    protected void imbueMeta(boolean canSmod, boolean canAdmod, float emodProb, boolean canAdemod){
        pmod = PrefixMod.get(rand);
        if (canSmod) smod = SpecialtyMod.get(rand);
        if (canAdmod) admod = AdverbMod.get(rand);
        if (rand.nextFloat() < emodProb) {
            emod = Buffs.values()[rand.nextInt(Buffs.values().length)];
            if (canAdemod) ademod = Item.AdenchantMod.get(rand);
            probability = ademod.magnitude;
        }
        setModifier(pmod);
        setModifier(smod);
        shade();
    }
    public void loadMeta(String p){
        String[] metadata = p.split("_");
        pmod = PrefixMod.load(metadata[0]);
        smod = SpecialtyMod.load(metadata[1]);
        admod = AdverbMod.load(metadata[2]);
        if (!metadata[3].equals("")){
            emod = Buffs.values()[Integer.parseInt(metadata[3])];
            ademod = AdenchantMod.load(metadata[4]);
            probability = ademod.magnitude;
        }
        shade();
    }
    private void shade(){
        int r = getRarity();
        if (r % 10 > 3){
            image = new ShadedPicture(name, Shader.Shine);
            ((ShadedPicture) image).shade();
        } else if (r >= 100){
            image = new ShadedPicture(name, Shader.Glow);
            ((ShadedPicture) image).shade();
        }
    }
    public void setType(Tag p){
        this.type = p;
        this.action = p.getAction();
        this.damageType = p.getDamageType();
    }
    public Tag getType(){
        return type;
    }
    public void onField(){
        if (organ == null) super.onField();
    }
    public void setOrgan(Organ p){
        if (p == null){
            save = true;
            pos = pos.getAdd(0,0);
            if (this.organ != null) lastLand = this.organ.host.lastLand;
        } else {
            setDepth(p.host.getDepth()-1);
            save = false;
        }
        this.organ = p;
    }
    public Organ getOrgan() {
        return organ;
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Broken){
            speed -= 10;
            uses.add((int) (uses.getMax()*-.7f));
        }else if (mod == PrefixMod.Old){
            speed -= 5;
            uses.add((int) Math.min(-1,uses.getMax()*-.3f));
        }else if (mod == PrefixMod.Fine){
            uses.addTotal(Math.max(1,uses.getMax()));
            probability += .1f;
        }else if (mod == PrefixMod.Mastercraft){
            speed += 4;
            probability += .2f;
        }else if (mod == PrefixMod.Legendary){
            speed += 7;
            probability += .3f;
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.None) admod = AdverbMod.None;
        else if (mod == SpecialtyMod.Sharp) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Dull) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Tough) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Heavy) this.weight += admod.magnitude;
        else if (mod == SpecialtyMod.Light) this.weight = Math.max(0, this.weight + (-1 * admod.magnitude));
        else if (mod == SpecialtyMod.Feather) this.weight /= 2*admod.magnitude;
        else if (mod == SpecialtyMod.Quick) speed = admod.magnitude;
        else if (mod == SpecialtyMod.Slow) speed = -admod.magnitude;
        else if (mod == SpecialtyMod.Hasty) cooldown *= 1-(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Lazy) cooldown *= 1+(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Durable) uses.addTotal(uses.getMax());
        else if (mod == SpecialtyMod.Runic) probability += .2f;
        else if (mod == SpecialtyMod.Instant) delay = 1;
        else if (mod == SpecialtyMod.Comfortable) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Recharging){
            cooldown = ((cooldown+5) * 2)+40;
            uses.setTotal(-1);
        }
    }
    private int getRarity(){
        return pmod.ordinal()+(smod != SpecialtyMod.None?10:0)+(emod != null?100:0);
    }
    public Color getRarityColor(){
        int rarity = getRarity();
        Color result = Color.black;
        switch (rarity){
            case 1:
                result = Color.LIGHT_GRAY;
                break;
            case 2:
                result = Color.WHITE;
                break;
            case 3:
                result = Color.GREEN;
                break;
            case 4:
                return Color.CYAN;
            case 5:
                return Color.MAGENTA;
        }
        if (rarity >= 100){
            result = Color.BLUE;
        }
        return result;
    }
    public int getValue(){
        float r = value;
        int adverb = admod.ordinal()+1;
        boolean special = smod != SpecialtyMod.None;
        switch (pmod.ordinal()){
            case 0:
                r *= 1;
                break;
            case 1:
                r *= .8f;
                break;
            case 2:
                r *= .9f;
                break;
            case 3:
                r *= 5;
                break;
            case 4:
                r *= 15;
                break;
            default:
                r *= 100;
                break;
        }
        if (smod == SpecialtyMod.Cursed){
            r *= .1f;
        } else if (special) r += (r*adverb);
        if (emod != null){
            r += 500;
            r *= 2;
        }
        return (int) r;
    }
    private String getRarityDescription(){
        String r = Dialog.get("item1");
        int rare = getRarity();
        if (rare >= 10) r = Dialog.get("item2");
        if (rare >= 100) r = Dialog.get("item3");
        if (rare % 5 == 4) r = Dialog.get("item4");
        else if (rare % 5 == 5) r = Dialog.get("item5");
        return r;
    }
    public String getName(){
        return name;
    }
    public String getFullName(){
        String r = "";
        if (admod != AdverbMod.None) r+=admod.name()+" ";
        if (smod != SpecialtyMod.None) r+=smod.name()+" ";
        if (pmod != PrefixMod.None) r+=pmod.name()+" ";
        r += getName();
        if (emod != null){
            r+=" of ";
            if (ademod != AdenchantMod.None){
                r += ademod.name()+" ";
            }
            r+=emod.name();
        }
        r = r.replace("_"," ");
        return r;
    }
    public void onUnequip(Man host){
        setOrgan(null);
        if (smod == SpecialtyMod.Cursed){
            Curse.get(host, 5);
        }
    }
    boolean canUse(){
        return uses.get() != 0 && Global.checkCooldown(lastused, cooldown) <= 0;
    }
    protected void playSound(){
        Sound.play(type.getSound());
    }
    public boolean use(int p){
        if (canUse()){
            if (uses.get() != -1) uses.add(-p);
            if (breakable && uses.get() == 0) remove();
            return true;
        }
        return false;
    }
    public void trigger(Unit target){
        if (emod != null && rand.nextFloat() < probability){
            target.stats.addEffect(emod, ademod.level);
        }
    }
    public boolean use(Man host){
        playSound();
        Global.cooldown(lastused, cooldown);
        host.setState(action);
        return true;
    }
    public void drop(Man host) {
        if (host.weapon == this) host.setWeapon(null);
        pos = new P(host.pos);
        setOrgan(null);
        rotation = (float)Global.r.nextInt(628)/100;
    }
    public Item cancelAdd(){
        Updater.cancelAdd(this);
        return this;
    }
    public void remove(){
        if (organ != null) {
            ((Man) organ.host).setWeapon(null);
            setOrgan(null);
        } else Global.player.inventory.remove(this);
        cancelAdd();
        super.remove();
    }
    public String getSound(){
        return type.getSound();
    }
    public String getHitSound(){
        return type.getHitSound();
    }
    public String getDescription() {
        String r = "";
        int uses = this.uses.get();
        r += Dialog.get("itemDsc",new String[]{getFullName(),weight+"",(uses==-1?"unlimited":uses+"")});
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            if (cooldown > 0){
                r += " "+Dialog.get("itemEncDsc",new String[]{getRarityDescription(), ade+emod.name(), cooldown+""});
                int cd = Global.checkCooldown(lastused, cooldown);
                if (cd <= 0) r += " "+Dialog.get("itemEncRdy");
                else r += " "+Dialog.get("itemEncNRdy", new String[]{cd+""});
            }
        }
        return r;
    }
    @Override
    public P getPoint(){
        P result = pos.getAdd(organ.ptd);
        float x = image.getSize().x*.5f*dir;
        result.add(x*Global.cos(rotation),(int) (x*Global.sin(rotation)));
        return result;
    }
    public int getDamage() {
        return damage;
    }
    public Line getLine(){
        return new Line(pos.getAdd(organ.pt), getPoint());
    }
    @Override
    public void draw(Graphics2D g){
        if (organ != null) {
            P t = pos.getAdd(organ.ptd);
            image.draw(g, t.x, t.y, rotation, dir);
        } else image.draw(g, pos.x, pos.y, rotation, dir);
    }
    public String forceToString(){
        boolean forced = false;
        if (!save){
            forced = true;
            save = true;
        }
        String r = toString();
        if (forced) save = false;
        return r;
    }
    @Override
    public String toString(){
        String t = super.toString();
        String meta = pmod+"_"+smod+"_"+admod+"_"+(emod==null?"":emod.ordinal())+"_"+ademod;
        if (!t.equals("")) t += "@"+uses+"@"+meta+"@"+String.format(java.util.Locale.US,"%.2f", rotation);
        return t;
    }
    public void update(){
        if (organ == null) super.update();
    }
}