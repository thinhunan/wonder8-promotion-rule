package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.Interpreter;

public class RuleBuilder extends ConditionBuilder {
    protected String _condition;
    protected String _promotion;
    private int _group = 0;

    public RuleBuilder() {
        this(null);
    }

    private RuleBuilder(ConditionBuilder parent) {
        super(parent);
    }

    public RuleBuilder condition(String condition){
        this._condition = condition;
        return this;
    }

    public RuleBuilder promotion(String promotion){
        this._promotion = promotion;
        return this;
    }

    public RuleBuilder group(int group){
        this._group = group;
        return this;
    }


    public Rule build(){
        Rule r = new Rule();
        if(_promotion != null) {
            r.setPromotion(this._promotion);
        }
        if(_currentSubBuilder != null) {
            r.setCondition(_currentSubBuilder.build());
        }
        else if( _condition!=null ){
            r.setCondition(Interpreter.parseString(this._condition));
        }
        r.setGroup(this._group);
        return r;
    }

}
