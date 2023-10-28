package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.RuleComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeRuleBuilder extends ConditionBuilder {
    protected CompositeRuleBuilder(ConditionBuilder builder){
        super(builder);
    }
    protected List<RuleComponent> components;
    public CompositeRuleBuilder addRule(RuleComponent rule){
        if(rule == null){
            return this;
        }
        if(components == null){
            components = new ArrayList<>();
        }
        components.add(rule);
        return this;
    }

    protected  List<ConditionBuilder> builders;
    public CompositeRuleBuilder add(ConditionBuilder builder){
        if(builders == null){
            builders = new ArrayList<>();
        }
        builders.add(builder);
        _currentSubBuilder = builder;
        return this;
    }

    protected void buildComponent(){
        if(components == null){
            components = new ArrayList<>();
        }
        if(builders != null && builders.size() > 0) {
            for (ConditionBuilder b : builders) {
                components.add(b.build());
            }
        }
    }

}
