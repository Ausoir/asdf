package com.ga.unit.item;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.SyncRand;
import com.ga.unit.*;
import com.ga.unit.man.Man;
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
        Sword(Actions.Slash, 3, .8f, 4, "swing_knife"){
            public boolean isBlunt(){return false;}},
        Spear(Actions.Thrust, 1, .6f, 0, "swing_normal"){
            public boolean isBlunt(){return false;}},
        Knife(Actions.Thrust, 3, .3f, 2, "swing_knife"){
            public boolean isBlunt(){return false;}},
        Swinging(Actions.Slash, 3, 1, 10, "swing_normal"),
        Bashing(Actions.Slash, 1, 1, 10, "swing_normal"),
        Armor(Actions.Rest, 1, 1, 10, "swing_normal"), Scroll(Actions.Read, 1, 0, 0, "scroll"),
        Shooting(Actions.Shoot_Handgun, 3, .5f, 0, "swing_normal"), Rifle(Actions.Shoot_Rifle, 3, 1f, 0, "swing_normal"),
        Bow(Actions.Shoot_Arrow, 1, 1, 0, "swing_normal"), Throwing(Actions.Throw, 3, .1f, 0, "swing_normal"),
        Darting(Actions.Dart, 3, .1f, 0, "swing_normal"), Consumable(Actions.Consume, 1, .1f, 0, "drink");
        public Actions action;
        String sound;
        int centerType;
        public float weightMultiplier;
        float durabilityAddition;
        ItemType(Actions p, int centerType, float weightMod, int durabilityMod, String sound){
            action = p;
            this.centerType = centerType;
            this.weightMultiplier = weightMod;
            this.durabilityAddition = durabilityMod;
            this.sound = sound;
        }
        public boolean isBlunt(){
            return true;
        }
    }
    public enum Material{
        Wooden(.4f,2,1,1,0,new Color(118, 93, 0,60)),
        Cloth(.5f,0,0,.9f,200),
        Wool(.5f,0,0,.9f,200){
            int getWarmth(){
                return 2;
            }
        },
        Silk(.2f,2,1,.8f,10000){
            public void onSet(Item host){
                host.tags.add(Tag.Air);
            }
            int getWarmth(){
                return -1;
            }
        },
        Nylon(.2f,1,0,.6f,2000){
            public void onSet(Item host){
                host.tags.add(Tag.Athletics);
            }
            int getWarmth(){
                return -1;
            }
        },
        Leather(.3f,1,1,1,1500,new Color(133, 78, 32,80)),
        Glass(.1f,1,7,.5f,2000),
        Copper(.4f,4,3,1f,1500,new Color(133, 92, 38,70)){
            public void onSet(Item host){
                host.tags.add(Tag.Channeling);
            }
            int getWarmth(){
                return -1;
            }
        },
        Iron(.5f,8,5,1.5f,2000),
        Silver(.2f,11,7,.8f,9000){
            public void onSet(Item host){
                host.tags.add(Tag.Enchant);
            }
        },
        Gold(.2f,10,9,.5f,16000){
            public void onSet(Item host){
                host.tags.add(Tag.Enchant);
            }
        },
        Steel(.2f,4,6,3,8000),
        Titanium(.1f,5,7,3,30000){
            public void onSet(Item host){
                host.tags.add(Tag.Strength);
            }
        },
        Crystal(.1f,6,7,.2f,12000,new Color(28, 133, 67,140)){
            public void onSet(Item host){
                host.tags.add(Tag.Conjuration);
                host.tags.add(Tag.Channeling);
            }
            int getWarmth(){
                return -1;
            }
        },
        Obsidian(.1f,3,4,.7f,8000){
            public void onSet(Item host){
                host.tags.add(Tag.Curse);
                host.tags.add(Tag.Fire);
                host.tags.add(Tag.Void);
            }
            int getWarmth(){
                return 1;
            }
        },
        Bone(.2f,1,3,.8f,3000){
            public void onSet(Item host){
                host.tags.add(Tag.Curse);
            }
        },
        Other(0,0,0,1,0),
        Organic(.4f,0,0,1,0),
        Stone(.3f,7,5,1,0,new Color(107, 107, 107,255));
        int weight, hardness, value;
        Color color;
        float durabilityMod, prevalence;
        Material(float prevalence, int weight, int hardness, float durabilityMod, int value){
            this.prevalence = prevalence;
            this.weight = weight;
            this.hardness = hardness;
            this.durabilityMod = durabilityMod;
            this.value = value;
        }
        Material(float prevalence, int weight, int hardness, float durabilityMod, int value, Color color){
            this(prevalence, weight, hardness, durabilityMod, value);
            this.color = color;
        }
        void onSet(Item host){}
        int getWarmth(){
            return 0;
        }
        public static Material[] load(String[] plist){
            Material[] list = new Material[plist.length];
            for (int i=0;i<plist.length;i++) list[i] = valueOf(plist[i]);
            return list;
        }
        static Material get(Random seed, Material[] list){
            float pool = 0;
            for (Material i : list) pool += i.prevalence;
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
    }
    enum SpecialtyMod{
        None, Light, Heavy, Sharp, Dull, Tough, Hardened, Runic, Instant, Cursed, Athletic, Strengthening, Branded, Quick, Slow;
        static SpecialtyMod get(Random seed){
            if (seed.nextInt(8) == 0) return values()[seed.nextInt(values().length)];
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
            int r = seed.nextInt(20);
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
        None(2,.3f), Minor(1,.1f), Major(3,.6f), Greater(4,.8f), Ultimate(5,1);
        public int level;
        public float magnitude;
        AdenchantMod(int level, float mag){
            this.level = level;
            this.magnitude = mag;
        }
        public static AdenchantMod get(Random seed){
            int t = seed.nextInt(100);
            if (t == 0) return Ultimate;
            else if (t < 5) return Greater;
            else if (t < 16) return Major;
            else if (t < 40) return Minor;
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
    private SpecialtyMod smod = SpecialtyMod.None;
    protected AdenchantMod ademod = AdenchantMod.None;
    protected Buffs emod = null;
    Material material;
    public int weight, range, speed;
    protected Cooldown cooldown = new Cooldown(3);
    public float probability;
    public int durability, uses;
    public boolean disposable = true;
    public ItemType type = ItemType.Throwing;
    public int value;
    protected Man owner;
    public Item(Map map, String name, int seed, Material[] materials, ItemType type, int durability) {
        super(map, name, seed, 7);
        gravity = true;
        this.durability = durability;
        this.uses = durability;
        setType(type);
        material = Material.get(rand, materials);
        setMaterial(material);
        speed = weight*2;
        value = material.value + 100;
        final Item me = this;
        onInteract = new Interactive(){
            public void interact(Man p){
                if (owner == null){
                    p.setWeapon(me);
                }
            }
        };
        randomSize(new P(.2f,.2f));
        range = Math.max(32, getSize()-8);
    }
    protected void imbueMeta(boolean canSmod, boolean canAdmod, float emodProb){
        if (canSmod) smod = SpecialtyMod.get(rand);
        if (canAdmod && smod != SpecialtyMod.None) admod = AdverbMod.get(rand);
        if (rand.nextFloat() < emodProb) {
            emod = Buffs.values()[rand.nextInt(Buffs.values().length)];
            ademod = AdenchantMod.get(rand);
            probability = ademod.magnitude;
        }
        setModifier(smod);
        applyMetaShade();
        uses = durability;
    }
    private void loadMeta(String p){
        String[] metadata = p.split("_");
        smod = SpecialtyMod.load(metadata[0]);
        admod = AdverbMod.load(metadata[1]);
        if (!metadata[2].equals("")){
            emod = Buffs.values()[Integer.parseInt(metadata[2])];
            ademod = AdenchantMod.load(metadata[3]);
            probability = ademod.magnitude;
        }
        setModifier(smod);
        applyMetaShade();
    }
    private void applyMetaShade(){
        if (emod != null){
            int intensity = ademod.level * 30;
            shade(new Color(48, 156, 200, intensity).getRGB());
        }
    }
    void shade(int color){
        int ctype = image.getCenterType();
        image = new ShadedPicture(rand, name, Shader.Color);
        ((ShadedPicture) image).shade(color);
        image.setCenterType(ctype);
    }
    private void setType(ItemType p){
        this.type = p;
        image.setCenterType(type.centerType);
        if (durability != -1) {
            durability += p.durabilityAddition;
            uses += p.durabilityAddition;
        }
    }
    public Material getMaterial(){
        return material;
    }
    public void setMaterial(Material mat){
        this.material = mat;
        mat.onSet(this);
        if (mat.color != null) shade(mat.color.getRGB());
        this.weight = (int) (material.weight * type.weightMultiplier);
        setModifier(smod);
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.None) admod = AdverbMod.None;
        else if (mod == SpecialtyMod.Sharp) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Dull) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Hardened) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Heavy) this.weight += admod.magnitude;
        else if (mod == SpecialtyMod.Light) this.weight = Math.max(0, this.weight - admod.magnitude);
        else if (mod == SpecialtyMod.Tough) durability *= 2;
        else if (mod == SpecialtyMod.Runic) probability += .2f*admod.magnitude;
        else if (mod == SpecialtyMod.Instant) cooldown.setDuration(0);
        else if (mod == SpecialtyMod.Cursed) tags.add(Tag.Enchant);
        else if (mod == SpecialtyMod.Quick) this.speed = Math.max(0, this.speed - admod.magnitude);
        else if (mod == SpecialtyMod.Slow) this.speed += admod.magnitude;
        else if (mod == SpecialtyMod.Athletic){
            for (int i=0;i<admod.magnitude;i++) tags.add(Tag.Athletics);
        }
        else if (mod == SpecialtyMod.Strengthening){
            for (int i=0;i<admod.magnitude;i++) tags.add(Tag.Strength);
        }
        else if (mod == SpecialtyMod.Branded) {
            for (int i=0;i<admod.magnitude;i++) tags.add(Tags.trainers[seed % Tags.trainers.length]);
        }
    }
    private int getRarity(){
        return 1+(smod == SpecialtyMod.None?0:1)+(emod == null?0:1);
    }
    public Color getRarityColor(){
        int rarity = getRarity();
        Color result = Color.black;
        switch (rarity){
            case 1:
                result = Color.LIGHT_GRAY;
                break;
            case 2:
                result = Color.GREEN;
                break;
            case 3:
                return Color.MAGENTA;
        }
        return result;
    }
    public int getValue(){
        float r = value;
        int adverb = admod.ordinal();
        if (isCursed()) r = Math.max(100,r-500);
        else {
            if (smod != SpecialtyMod.None) r += 2000 + 1000*adverb;
            if (emod != null) r += Math.pow(2,emod.value)*800*ademod.level;
        }
        return (int) (r * getPercentage());
    }
    boolean isCursed(){
        return smod == SpecialtyMod.Cursed;
    }
    private String getRarityDescription(){
        String r = "";
        switch (getRarity()){
            case 1:
                r = "Common";
                break;
            case 2:
                r = "Fine";
                break;
            case 3:
                r = "Extraordinary";
        }
        return r;
    }
    public String getName(){
        return name;
    }
    public String getFullName(){
        String r = "";
        if (admod != AdverbMod.None) r+=admod.name()+" ";
        if (smod != SpecialtyMod.None) r+=smod.name();
        r += " " + getName();
        if (emod != null){
            r+=" of ";
            if (ademod != AdenchantMod.None) r += ademod.name()+" ";
            r+=emod.name();
        }
        return r;
    }
    protected void recharge(int p){
        if (!isInfinite()) uses = Math.min(durability, uses+p);
    }
    public void onEquip(Man host){
        owner = host;
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
            ArrayList<Buffs> list = Buffs.getByTag(Tag.Curse);
            host.buff(list.get(rand.nextInt(list.size())),rand.nextInt(1+admod.magnitude*2)+1);
        }
    }
    public void remove(){
        if (owner != null && owner.weapon == this) owner.setWeapon(null);
        super.remove();
    }
    public void playAttackSound(){
        Sound.play(map, pos, "swing_normal");
    }
    protected void playSound(){
        Sound.play(map, pos, type.sound);
    }
    public float getPercentage(){
        return (float)uses/durability;
    }
    public boolean isUsable(boolean use, int p){ //return: is item in isUsable state
        if (isInfinite() || uses >= p){
            if (!isInfinite()){
                if (use){
                    uses -= p;
                    if (uses == 0){
                        Particle.groundSlam(map, pos, getDepth());
                        if (disposable) remove();
                    } else if (uses > durability) uses = durability;
                }
            }
            return true;
        }
        return false;
    }
    public int getSpeed(){
        return speed;
    }
    public boolean use(Man host){
        if (host.get("Strength").getMax() >= weight) {
            if (host.setState(type.action) == 0) {
                playSound();
                return true;
            }
        }
        return false;
    }
    public boolean trigger(Unit target){
        if (emod != null && cooldown.isReady() && SyncRand.r.nextFloat() < probability && isUsable(true, 1)){
            cooldown.reset();
            target.buff(emod, ademod.level);
            return true;
        }
        return false;
    }
    public void drop(Man host) {
        if (host.weapon == this){
            host.setWeapon(null);
            onUnequip(host);
        }
        pos = new P(host.pos);
        rotation = (float)Global.r.nextInt(628)/100;
    }
    public String getDescription() {
        String r = "Weight: "+weight+"\n";
        r += "Value: "+Dialog.money(getValue())+"\n";
        r += "Uses: "+uses+"/"+durability+"\n";
        r += "Material: "+material.name()+"\n";
        if (tags.size() > 0) r += "Catalyzers: "+tags.toString()+"\n";
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            int cd = cooldown.getCurrentState();
            if (cd > 0){
                r += " "+Dialog.get("?itemEnc",new String[]{getRarityDescription(), ade+emod.name(), cooldown+""});
                r += " "+Dialog.get("itemEncRdy", new String[]{""+cd});
                r += "\n";
            }
        }
        return r;
    }
    public int getDamage() {
        return type.isBlunt() || uses == 0 ? weight : (weight/2)+material.hardness;
    }
    public Line getLine(){
        float len = image.getSize().x * dir * (image.getCenterType()==1?.5f:1) + 2;
        return new Line(pos, pos.getAdd(len*Global.cos(rotation), len*Global.sin(rotation)));
    }
    public void onTimeChange(){
        if (getValue() >= 100 && owner == null) remove();
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
        uses = Integer.parseInt(sections[5]);
        cooldown.load(sections[6]);
        loadMeta(sections[7]);
        return sections;
    }
    public boolean isInfinite(){
        return durability == -1;
    }
    @Override
    public String toString(){
        String t = super.toString();
        String meta = smod+"_"+admod+"_"+(emod==null?"":emod.ordinal())+"_"+ademod;
        if (!t.equals("")) t += "@"+uses+"@"+cooldown.toString()+"@"+meta;
        return t;
    }
}