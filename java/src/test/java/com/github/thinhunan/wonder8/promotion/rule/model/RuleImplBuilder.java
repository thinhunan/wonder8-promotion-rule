package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.Interpreter;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.RuleBuilder;

public class RuleImplBuilder extends RuleBuilder {
    String _title;
    String _description;

    public RuleImplBuilder condition(String condition){
        super.condition(condition);
        return this;
    }

    public RuleImplBuilder promotion(String promotion){
        super.promotion(promotion);
        return this;
    }

    public RuleImplBuilder title(String title){
        this._title = title;
        return this;
    }

    public RuleImplBuilder description(String description){
        this._description = description;
        return this;
    }

    @Override
    public RuleImpl build(){
        RuleImpl r = new RuleImpl();
        if(_promotion != null) {
            r.setPromotion(this._promotion);
        }
        if(_currentSubBuilder != null) {
            r.setCondition(_currentSubBuilder.build());
        }
        else if( _condition!=null ){
            r.setCondition(Interpreter.parseString(this._condition));
        }
        r.setTitle(this._title);
        r.setDescription(this._description);
        return r;
    }
}
