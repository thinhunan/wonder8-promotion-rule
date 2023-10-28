package com.github.thinhunan.wonder8.promotion.rule.model;


import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 以"或者"为关系组织的条件规则组合
 */
public class OrCompositeRule extends CompositeRule {

    public OrCompositeRule(List<RuleComponent> components){
        this.components = components;
    }


    public OrCompositeRule(){
        this.components = new ArrayList<>();
    }


    @Override
    String getCombinator() {
        return "|";
    }

    @Override
    boolean CombineResult(boolean r1, boolean r2) {
        return (r1||r2);
    }

    @Override
    public boolean check(List<Item> items) {
        for(RuleComponent rule : components) {
            RuleValidateResult r = rule.validate(items);
            if(r.isValid()){
                return true;
            }
        }
        return false;
    }
}
