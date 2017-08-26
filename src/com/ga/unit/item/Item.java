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
import com.ga.unit.*;
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
        Sword(Actions.Slash, 3, 1.3f, .7f, "swing_knife"){
            public boolean isBlunt(){
                return false;
            }
        }, Spear(Actions.Thrust, 1, 1, .4f, "swing_silent"){
            public boolean isBlunt(){
                return false;
            }
        }, Knife(Actions.Slash, 3, .3f, .4f, "swing_knife") {
            public boolean isBlunt() {
                return false;
            }
        }, Swinging(Actions.Slash, 3, 2, 2, "swing_silent"){
            public boolean isBlunt() {
                return false;
            }
        },
        Bashing(Actions.Slash, 1, 2, 2, "swing_silent"),
        Armor(Actions.Rest, 1, 2, 1, "swing_normal"), Scroll(Actions.Guard, 1, 0, .5f, "scroll"),
        Shooting(Actions.Shoot, 3, .8f, 1, "swing_normal"), Rifle(Actions.Snipe, 3, 1.2f, 1, "swing_normal"),
        Bow(Actions.Aim, 1, 1, .2f, "swing_silent"), Throwing(Actions.Throw, 3, .2f, 1, "swing_silent"),
        Darting(Actions.Dart, 3, .1f, 1, "swing_normal"), Consumable(Actions.Consume, 1, .2f, 1, "drink");
        Actions action;
        String sound;
        int centerType;
        float weightMultiplier, durabilityMultiplier;
        ItemType(Actions p, int centerType, float mass, float sturdiness, String sound){
            action = p;
            this.centerType = centerType;
            this.weightMultiplier = mass;
            this.durabilityMultiplier = sturdiness;
            this.sound = sound;
        }
        public boolean isBlunt(){
            return true;
        }
    }
    public enum Material{
        Wooden(.3f,2,32,1,new Color(118, 93, 0,60)),
        Cloth(.6f,0,12,0),
        Leather(.3f,1,50,1,new Color(133, 78, 32,140)),
        Glass(.16f,1,1,7){
            public void onSet(Item host){
                host.tags.add(Tag.Ice);
            }
        },
        Brass(.36f,5,110,4,new Color(133, 123, 23,120)),
        Copper(.3f,5,80,3,new Color(133, 92, 38,120)){
            public void onSet(Item host){
                host.tags.add(Tag.Electric);
            }
        },
        Bronze(.24f,5,500,4,new Color(133, 72, 0,200)),
        Iron(.24f,4,600,5),
        Silver(.1f,6,140,3){
            public void onSet(Item host){
                host.tags.add(Tag.Mutation);
            }
        },
        Gold(.16f,12,40,2){
            public void onSet(Item host){
                host.tags.add(Tag.Electric);
            }
        },
        Lead(.2f,7,50,2,new Color(19, 31, 75,180)){
            public void onSet(Item host){
                host.tags.add(Tag.Electric);
            }
        },
        Steel(.1f,4,8000,6),
        Titanium(.01f,3,-1,7),
        Crystal(.04f,8,24,12,new Color(28, 133, 67,120)){
            public void onSet(Item host){
                host.tags.add(Tag.Ice);
            }
        },
        Obsidian(.06f,3,40,4){
            public void onSet(Item host){
                host.tags.add(Tag.Shadow);
                host.tags.add(Tag.Fire);
            }
        },
        Bone(.16f,1,80,3){
            public void onSet(Item host){
                host.tags.add(Tag.Conjuration);
            }
        },
        Other(0,0,10,0),
        Flesh(.05f,0,-1,0){
            public void onSet(Item host){
                host.tags.add(Tag.Mutation);
            }
        },
        Stone(.1f,6,5,5,new Color(107, 107, 107,255));
        int weight, durability, hardness;
        Color color;
        float prevalence;
        Material(float prevalence, int weight, int durability, int hardness){
            this.prevalence = prevalence;
            this.weight = weight;
            this.durability = durability;
            this.hardness = hardness;
        }
        Material(float prevalence, int weight, int durability, int hardness, Color color){
            this(prevalence, weight,durability,hardness);
            this.color = color;
        }
        void onSet(Item host){}
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
            int t = seed.nextInt(100);
            if (t < 1) return Ultimate;
            else if (t < 5) return Greater;
            else if (t < 15) return Major;
            else if (t < 50) return Minor;
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
    public int weight, range;
    public int volume = 1;
    public int encumbrance = 0;
    int cooldown = 0;
    public int delay = 0;
    public float probability;
    private int[] cdTime;
    int durability = 1;
    int uses = 1;
    public ItemType type = ItemType.Throwing;
    public int value = 8;
    Man owner;
    public Item(Updater map, String name, int seed, Material[] materials, ItemType type) {
        super(map, name, seed);
        gravity = true;
        setType(type);
        material = Material.get(rand, materials);
        setMaterial(material);
        cdTime = new int[]{Global.time};
        final Item me = this;
        onInteract = new Interactive(){
            public void interact(Man p){
                if (owner == null){
                    if (p.getVacancy() < encumbrance) p.say("Too encumbering!");
                    p.setWeapon(me);
                }
            }
        };
        randomSize(new P(.2f,.2f));
        range = getSize()-8;
    }
    protected void imbueMeta(boolean canSmod, boolean canAdmod, float emodProb){
        pmod = PrefixMod.get(rand);
        if (canSmod) smod = SpecialtyMod.get(rand);
        if (canAdmod) admod = AdverbMod.get(rand);
        if (rand.nextFloat() < emodProb) {
            emod = Buffs.values()[rand.nextInt(Buffs.values().length)];
            ademod = AdenchantMod.get(rand);
            probability = ademod.magnitude;
        }
        setModifier(pmod);
        setModifier(smod);
        applyMetaShade();
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
        applyMetaShade();
    }
    private void applyMetaShade(){
        if (pmod.ordinal() >= 3){
            shade(new Color(255, 255, 255, 140).getRGB());
        } else if (emod != null){
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
//        this.type.onSet(this);
    }
    public void setMaterial(Material mat){
        this.material = mat;
        mat.onSet(this);
        if (mat.color != null) shade(mat.color.getRGB());
        this.weight = (int) (material.weight * type.weightMultiplier);
        this.durability = (int) (material.durability * type.durabilityMultiplier);
        this.uses = durability;
        setModifier(pmod);
        setModifier(smod);
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
            cooldown = cooldown*2+40;
            if (ademod != AdenchantMod.None){
                if (ademod.level > 3) durability = -1;
                else durability += ademod.level * 100;
            } else durability += 100;
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
            r = Math.max(1,r-400);
        } else if (special) r += (r*adverb);
        if (emod != null){
            r *= emod.value;
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
        if (admod != AdverbMod.None) r+=admod.name();
        if (smod != SpecialtyMod.None) r+=smod.name();
        if (pmod != PrefixMod.None) r+=pmod.name();
        r += " " + getName();
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
        super.remove();
    }
    private void playSound(){
        Sound.play(pos, type.sound);
    }
    public boolean usable(int p){ //return: is item in usable state
        if ((isInfinite() || uses >= p) && Global.time >= cdTime[0]){
            if (!isInfinite()){
                uses -= p;
                if (uses == 0){
                    Particle.groundSlam(map, pos, getDepth());
                    remove();
                }
            }
            return true;
        }
        return false;
    }
    public boolean use(Man host){ //return: is host in a state capable of using item
        if (host.setState(type.action) == 0){
            cdTime[0] = Global.time+cooldown;
            playSound();
            return true;
        }
        return false;
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
    public String getDescription() {
        String r = "";
        r += Dialog.get("itemDsc",new String[]{getFullName(),weight+"",(isInfinite()?"unlimited":uses+""),encumbrance+""});
        r += " This item is made of "+material.name()+".";
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
        uses = Integer.parseInt(sections[5]);
        cdTime[0] = Integer.parseInt(sections[6]);
        loadMeta(sections[7]);
        return sections;
    }
    private boolean isInfinite(){
        return durability == -1;
    }
    @Override
    public String toString(){
        String t = super.toString();
        String meta = pmod+"_"+smod+"_"+admod+"_"+(emod==null?"":emod.ordinal())+"_"+ademod;
        if (!t.equals("")) t += "@"+uses+"@"+cdTime[0]+"@"+meta;
        return t;
    }
}