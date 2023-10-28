package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.P;
import com.github.thinhunan.wonder8.promotion.rule.model.RuleRangeCollection;
import com.github.thinhunan.wonder8.promotion.rule.model.SameRangeRule;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.part.SameRangePredictPart;

public class SameRangeRuleBuilder extends SingleRuleBuilder{
    protected SameRangeRuleBuilder(ConditionBuilder ruleBuilder, RuleRangeCollection refRanges) {
        super(ruleBuilder);
        _refRange = refRanges;
    }

    RuleRangeCollection _refRange;

    public RuleRangeCollection getRanges(){
        return _refRange;
    }


    public SameRangePredictPart predict(P predict){
        this.setPredict(predict);
        return new SameRangePredictPart(this);
    }

    @Override
    public SameRangeRule build() {
        SameRangeRule rule = new SameRangeRule();
        rule.setRange(_refRange);
        rule.setExpected(_expected);
        rule.setPredict(_predict);
        return rule;
    }
}
