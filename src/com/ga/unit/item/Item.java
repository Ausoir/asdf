package com.ga.unit.item;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.map.Updater;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Man;
import com.ga.unit.Prop;
import com.ga.unit.Tag;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ga on 8/5/2016. asd
 */
public class Item extends Prop {
    public enum ItemType {
        Sword(Actions.Slash, 1.3f, .7f, "swing_knife"){
            boolean isBlunt(){
                return false;
            }
        }, Spear(Actions.Thrust, 1, .4f, "swing_silent"){
            boolean isBlunt(){
                return false;
            }
        }, Knife(Actions.Slash, .3f, .4f, "swing_knife") {
            boolean isBlunt() {
                return false;
            }
        },
        Bashing(Actions.Slash, 2, 2, "swing_silent"),
        Armor(Actions.Rest, 2, 1, "swing_normal"), Scroll(Actions.Guard, 0, .5f, "scroll"),
        Shooting(Actions.Shoot, .8f, 1, "swing_normal"), Rifle(Actions.Snipe, 1.2f, 1, "swing_normal"),
        Bow(Actions.Aim, 1, .2f, "swing_silent"), Throwing(Actions.Throw, .2f, 1, "swing_silent"),
        Darting(Actions.Dart, .1f, 1, "swing_normal"), Consumable(Actions.Consume, .2f, 1, "drink");
        Actions action;
        String sound;
        float weightMultiplier, durabilityMultiplier;
        ItemType(Actions p, float mass, float sturdiness, String sound){
            action = p;
            this.weightMultiplier = mass;
            this.durabilityMultiplier = sturdiness;
            this.sound = sound;
        }
        boolean isBlunt(){
            return true;
        }
        void onSet(Item host){
        }
    }
    public enum Material{
        Wooden(.3f,2,16,1,new Color(118, 93, 0,60)){
            void onSet(Item host) {
                host.tags.add(Tag.Combustible);
            }
        }, Cloth(.6f,0,3,0){
            void onSet(Item host) {
                host.tags.add(Tag.Combustible);
            }
        }, Leather(.3f,1,6,1,new Color(133, 78, 32,140)){
            void onSet(Item host) {
                host.tags.add(Tag.Combustible);
            }
        }, Glass(.05f,1,1,7),
        Brass(.36f,3,40,3,new Color(133, 123, 23,120)),
        Copper(.32f,4,24,4,new Color(133, 92, 38,120)),
        Bronze(.24f,4,70,4,new Color(133, 72, 0,200)),
        Iron(.24f,5,100,4),
        Silver(.16f,6,40,6),
        Lead(.2f,6,48,5,new Color(19, 31, 75,180)),
        Steel(.1f,4,120,6),
        Titanium(.001f,3,800,6),
        Crystal(.04f,8,24,12,new Color(28, 133, 67,120)),
        Bone(.16f,1,28,3),
        Other(0,0,10,0),
        Flesh(.05f,1,3,0),
        Stone(.1f,6,5,5,new Color(107, 107, 107,255));
        int weight, durability, hardness;
        Color color;
        float prevalence;
        static float pool;
        Material(float prevalence, int weight, int durability, int hardness){
            expandPool(prevalence);
            this.prevalence = prevalence;
            this.weight = weight;
            this.durability = durability;
            this.hardness = hardness;
        }
        Material(float prevalence, int weight, int durability, int hardness, Color color){
            this(prevalence, weight,durability,hardness);
            this.color = color;
        }
        void onSet(Item host){
        }
        static Material get(Random seed, Material[] list){
            float r = seed.nextFloat()*pool;
            float t = 0;
            float highest = -1;
            Material highestMat = null;
            for (Material i : list){
                if (i.prevalence > highest){
                    highest = i.prevalence;
                    highestMat = i;
                }
                t += i.prevalence;
                if (t >= r) return i;
            }
            return highestMat;
        }
        static void expandPool(float p){
            pool += p;
        }
    }
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Hasty, Lazy, Durable, Tough, Runic, Instant, Cursed, Conscious;
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
        None(160), Awful(30), Old(40), Fine(16), Mastercraft(1);
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
        None(2,.2f), Minor(1,.1f), Major(3,.5f), Greater(4,.7f), Ultimate(5,.95f);
        public int level;
        public float magnitude;
        AdenchantMod(int level, float mag){
            this.level = level;
            this.magnitude = mag;
        }
        public static AdenchantMod get(Random seed){
            float t = (float) seed.nextGaussian();
            if (t > 2) return Ultimate;
            else if (t > 1.6) return Greater;
            else if (t > 1.2) return Major;
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
    Material material;
    public int weight;
    public int volume = 1;
    int cooldown = 0;
    public int delay = 0;
    public float probability;
    private int[] cdTime = new int[]{0};
    int durability = 1;
    int uses = 1;
    ItemType type = ItemType.Throwing;
    public int value = 8;
    Man owner;
    public Item(String name, int seed, Material[] materials, ItemType type) {
        super(name, seed);
        dynamic = true;
        gravity = true;
        setType(type);
        material = Material.get(rand, materials);
        setMaterial(material);
        this.weight = (int) (material.weight * type.weightMultiplier);
        this.durability = (int) (material.durability * type.durabilityMultiplier);
        this.uses = durability;
        final Item me = this;
        onInteract = new Interactive(){
            public void interact(Man p){
                if (owner == null) p.setWeapon(me);
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
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(250, 220, 100, 180).getRGB());
        } else if (r >= 100){
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(48, 156, 200, 180).getRGB());
        }
        image.setCenterType(ctype);
    }
    private void setType(ItemType p){
        this.type = p;
        this.type.onSet(this);
    }
    void setMaterial(Material mat){
        this.material = mat;
        mat.onSet(this);
        if (mat.color != null) {
            int ctype = image.getCenterType();
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(mat.color.getRGB());
            image.setCenterType(ctype);
        }
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Awful){
            weight += 2;
        } else if (mod == PrefixMod.Old){
            durability = (int) (durability*.5f);
        } else if (mod == PrefixMod.Fine){
            probability += .1f;
            durability = (int) (durability*1.5f);
        } else if (mod == PrefixMod.Mastercraft){
            weight = Math.max(0, weight-1);
            probability += .2f;
            durability = (int) (durability*2f);
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
        else if (mod == SpecialtyMod.Hasty) cooldown *= 1-(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Lazy) cooldown *= 1+(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Durable) durability *= 2;
        else if (mod == SpecialtyMod.Runic) probability += .2f;
        else if (mod == SpecialtyMod.Instant) delay = 1;
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
        owner = host;
        setDepth(host.getDepth());
        lastLand = host.lastLand;
        save = false;
        mobile = false;
    }
    public void onUnequip(Man host){
        owner = null;
        lastLand = host.lastLand;
        save = true;
        pos = pos.getAdd(0,0);
        mobile = true;
        if (smod == SpecialtyMod.Cursed){
            ArrayList<Buffs> list = Buffs.getByTag(Tag.Debuff);
            host.buff(list.get(rand.nextInt(list.size())),rand.nextInt(9)+1);
        }
    }
    public void remove(){
        if (owner != null && owner.weapon == this) owner.setWeapon(null);
        cancelAdd();
        super.remove();
    }
    void onBreak(){
        remove();
    }
    private void playSound(){
        Sound.play(type.sound);
    }
    public boolean usable(int p){
        if ((isInfinite() || uses >= p) && Global.time >= cdTime[0]){
            if (!isInfinite()){
                uses -= p;
                if (uses == 0){
                    onBreak();
                }
            }
            return true;
        }
        return false;
    }
    public void use(Man host){
        playSound();
        cdTime[0] = Global.time+cooldown;
        host.setState(type.action);
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
    public String getDescription() {
        String r = "";
        r += Dialog.get("itemDsc",new String[]{getFullName(),weight+"",(isInfinite()?"unlimited":uses+"")});
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            if (cooldown > 0){
                r += " "+Dialog.get("itemEncDsc",new String[]{getRarityDescription(), ade+emod.name(), cooldown+""});
                int cd = cdTime[0] - Global.time;
                if (cd <= 0) r += " "+Dialog.get("itemEncRdy");
                else r += " "+Dialog.get("itemEncNRdy", new String[]{cd+""});
            }
        }
        return r;
    }
    public int getDamage() {
        return Global.r.nextInt(Math.max(1,weight));
    }
    public Line getLine(){
        float len = image.getSize().x * dir + 4;
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