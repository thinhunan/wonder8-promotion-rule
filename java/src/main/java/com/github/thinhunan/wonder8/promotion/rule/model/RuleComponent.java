package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.List;
import java.util.function.Predicate;

public abstract class RuleComponent {
    public abstract String toRuleString();
    public abstract boolean check(List<Item> items);
    public abstract RuleValidateResult validate(List<Item> items);
    public abstract Predicate<Item> getFilter();

    public Rule asRule(){
        if(this instanceof Rule){
            return (Rule)this;
        }
        throw new ClassCastException("This RuleComponent is not a complete Rule");
    }
}
