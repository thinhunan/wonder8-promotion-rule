package com.github.thinhunan.wonder8.promotion.rule.model.builder.part;

import com.github.thinhunan.wonder8.promotion.rule.model.builder.SameRangeRuleBuilder;

public class SameRangePredictPart {
    protected SameRangeRuleBuilder _builder;
    public SameRangePredictPart(SameRangeRuleBuilder builder) {
        this._builder = builder;
    }

    public SameRangeRuleBuilder expected(int expected) {
        _builder.setExpected(expected);
        return _builder;
    }
}
