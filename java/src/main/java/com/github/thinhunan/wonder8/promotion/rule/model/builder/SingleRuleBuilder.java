package com.github.thinhunan.wonder8.promotion.rule.model.builder;
import com.github.thinhunan.wonder8.promotion.rule.model.P;
import com.github.thinhunan.wonder8.promotion.rule.model.RuleRangeCollection;

public abstract class SingleRuleBuilder extends ConditionBuilder {

    protected SingleRuleBuilder(ConditionBuilder ruleBuilder){
        super(ruleBuilder);
    }

    protected RuleRangeCollection _ruleRanges;

    public RuleRangeCollection getRanges(){
        return _ruleRanges;
    }

    protected P _predict;
    public void setPredict(P predict){
        _predict = predict;
    }
    protected int _expected = 0;
    public void setExpected(int expected){
        _expected = expected;
    }
}

