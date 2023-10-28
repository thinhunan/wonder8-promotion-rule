package com.github.thinhunan.wonder8.promotion.rule.model;

import java.util.ArrayList;
/**
 * 单一条件规则的判断范围设置,for example: [#cPID1#cPID2], #c/#p/#k/$/~
 */
public class RuleRangeCollection extends ArrayList<RuleRange> {

    public RuleRangeCollection addRuleRange(RuleRange range){
        this.add(range);
        return this;
    }

    String toRuleString() {
        StringBuilder sb = new StringBuilder("[");
        for (RuleRange r:this) {
            if(r.type == R.ALL){
                return "$";
            }else {
                sb.append("#");
                sb.append(r.type.toString());
                sb.append(r.id);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toRuleString();
    }

    public static RuleRangeCollection parseString(String s){
        if("$".equals(s)){
            return new RuleRangeCollection().addRuleRange(new RuleRange(R.ALL,null));
        } else {
            String[] parts = s.replaceAll("[\\[\\]]","").split("#");
            RuleRangeCollection rules =  new RuleRangeCollection();
            for(int i = 1; i < parts.length ; i++){
                RuleRange range = new RuleRange(R.parseString(parts[i].substring(0,1)),
                        parts[i].substring(1));
                rules.addRuleRange(range);
            }
            return rules;
        }
    }
}
