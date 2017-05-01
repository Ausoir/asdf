package com.ga.unit.item;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.*;
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
    public enum ItemType {
        Sword(Actions.Slash, "swing_knife", "hit_cut"), Spear(Actions.Thrust, "swing_silent", "hit_bloody"), Mace(Actions.Slash, "swing_silent", "hit_bash"),
        Dagger(Actions.Thrust, "swing_normal", "hit_bloody"), Armor(Actions.Rest, "swing_normal"), Scroll(Actions.Guard, "scroll"),
        Shoot(Actions.Shoot, "swing_normal"), Snipe(Actions.Snipe, "swing_normal"), Bow(Actions.Aim, "swing_silent"),
        Throwing(Actions.Throw, "swing_silent"), Darting(Actions.Dart, "swing_normal"), Consumable(Actions.Consume, "drink"),
        Claw(Actions.Slash, "swing_normal"), Enchant(Actions.Buff, "swing_silent"), Food(Actions.Consume, "eat");
        private Actions action;
        private String sound, soundHit = "hit_punch";
        ItemType(Actions p, String sound){
            action = p;
            this.sound = sound;
        }
        ItemType(Actions p, String sound, String soundHit){
            this(p, sound);
            this.soundHit = soundHit;
        }
        public Actions getAction() {
            return action;
        }
        public String getSound(){
            return sound;
        }
        public String getHitSound(){
            return soundHit;
        }
    }
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Hasty, Lazy, Durable, Tough, Runic, Instant, Comfortable, Cursed, Quick, Slow, Conscious;
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
    int cooldown = 0;
    public int delay = 0;
    public float probability;
    Actions action = Actions.Slash;
    private int[] cdTime = new int[]{0};
    private int durability = 1;
    public int uses = 1;
    private ItemType type = ItemType.Throwing;
    public int speed = -4;
    public int damage = 0;
    public int value = 8;
    public boolean breakable = true;
    public Item(String name, int seed, int weight, int durability) {
        super(name, seed, 100);
        dynamic = true;
        this.name = name;
        this.weight = weight;
        this.durability = durability;
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
        uses = durability;
    }
    private void loadMeta(String p){
        String[] metadata = p.split("_");
        pmod = PrefixMod.load(metadata[0]);
        smod = SpecialtyMod.load(metadata[1]);
        admod = AdverbMod.load(metadata[2]);
        if (!metadata[3].equals("")){
            emod = Buffs.values()[Integer.parseInt(metadata[3])];
            ademod = AdenchantMod.load(metadata[4]);
            probability = ademod.magnitude;
        }
        setModifier(pmod);
        setModifier(smod);
        shade();
    }
    private void shade(){
        int r = getRarity();
        int ctype = image.getCenterType();
        if (r % 10 > 3){
            image = new ShadedPicture(name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(250, 220, 100, 180).getRGB());
        } else if (r >= 100){
            image = new ShadedPicture(name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(48, 156, 200, 180).getRGB());
        }
        image.setCenterType(ctype);
    }
    public void setType(ItemType p){
        this.type = p;
        this.action = p.getAction();
    }
    public ItemType getType(){
        return type;
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Broken){
            speed -= 10;
            durability = (int) (durability*.4f);
        }else if (mod == PrefixMod.Old){
            speed -= 5;
            durability = (int) (durability*.7f);
        }else if (mod == PrefixMod.Fine){
            probability += .1f;
            durability = (int) (durability*1.5f);
        }else if (mod == PrefixMod.Mastercraft){
            speed += 4;
            probability += .2f;
            durability = (int) (durability*2f);
        }else if (mod == PrefixMod.Legendary){
            durability = -1;
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
        else if (mod == SpecialtyMod.Durable) durability *= 2;
        else if (mod == SpecialtyMod.Runic) probability += .2f;
        else if (mod == SpecialtyMod.Instant) delay = 1;
        else if (mod == SpecialtyMod.Comfortable) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Conscious){
            cooldown = ((cooldown+5) * 2)+40;
            durability = -1;
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
    public void onEquip(Man host){
        setDepth(host.getDepth()-1);
        lastLand = host.lastLand;
        save = false;
        mobile = false;
    }
    public void onUnequip(Man host){
        lastLand = host.lastLand;
        save = true;
        pos = pos.getAdd(0,0);
        mobile = true;
        if (smod == SpecialtyMod.Cursed){
            Curse.get(host, 5);
        }
    }
    public void remove(){
        Global.player.inventory.remove(this);
        cancelAdd();
        super.remove();
    }
    protected void playSound(){
        Sound.play(type.getSound());
    }
    public boolean usable(int p){
        if ((isInfinite() || uses >= p) && Global.getTime()[0] >= cdTime[0]){
            if (!isInfinite()){
                uses -= p;
                if (breakable && uses == 0){
                    remove();
                }
            }
            return true;
        }
        return false;
    }
    public void use(Man host){
        playSound();
        cdTime[0] = Global.getTime()[0]+cooldown;
        host.setState(action);
    }
    public void trigger(Unit target){
        if (emod != null && rand.nextFloat() < probability){
            target.buff(emod, ademod.level);
        }
    }
    public void drop(Man host) {
        if (host.weapon == this){
            host.setWeapon(null);
            onUnequip(host);
        }
        pos = new P(host.pos);
        rotation = (float)Global.r.nextInt(628)/100;
    }
    public Item cancelAdd(){
        Updater.cancelAdd(this);
        return this;
    }
    public String getSound(){
        return type.getSound();
    }
    public String getHitSound(){
        return type.getHitSound();
    }
    public String getDescription() {
        String r = "";
        r += Dialog.get("itemDsc",new String[]{getFullName(),weight+"",(isInfinite()?"unlimited":uses+"")});
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            if (cooldown > 0){
                r += " "+Dialog.get("itemEncDsc",new String[]{getRarityDescription(), ade+emod.name(), cooldown+""});
                int cd = cdTime[0] - Global.getTime()[0];
                if (cd <= 0) r += " "+Dialog.get("itemEncRdy");
                else r += " "+Dialog.get("itemEncNRdy", new String[]{cd+""});
            }
        }
        return r;
    }
    public int getDamage() {
        return damage;
    }
    public Line getLine(){
        float len = image.getSize().x * dir;
        return new Line(pos, pos.getAdd(len*Global.cos(rotation), len*Global.sin(rotation)));
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
    public String[] load(String p){
        String[] sections = super.load(p);
        uses = Integer.parseInt(sections[4]);
        loadMeta(sections[5]);
        return sections;
    }
    private boolean isInfinite(){
        return durability == -1;
    }
    @Override
    public String toString(){
        String t = super.toString();
        String meta = pmod+"_"+smod+"_"+admod+"_"+(emod==null?"":emod.ordinal())+"_"+ademod;
        if (!t.equals("")) t += "@"+uses+"@"+meta;
        return t;
    }
}