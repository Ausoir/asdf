package com.ga.tool;

import com.ga.map.Global;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        T s = new T("[[],[10,1,0,[581.25104,911.0],0.00,7,ins\\B_1_15_0,111111,[[Speed,[20,20]],[Motivation,[0,10]],[Intelligence,[1,1]],[Nutrition,[5,5]],[Agility,[1,1]],[Fatigue,[12,12]],[Level,[1,1]],[Attack,[0,0]],[Strength,[0,1]]],,Player,[[0,0],[[0,x]]],[[Calling,0]],[[Alchemy,1]],[Patch_Wound,Bandage,Make_Scrap],Player,[],,,,[8,53,3763,[735.2759,885.5222],0.01,20,0,[0,0,,0],1],,[[7,9,86638,[741.0306,878.29486],35.00,80,0,[5,0,,0],12,0]]]]");
        Global.p(s.get(1).get(0).get()+"");
    }
}