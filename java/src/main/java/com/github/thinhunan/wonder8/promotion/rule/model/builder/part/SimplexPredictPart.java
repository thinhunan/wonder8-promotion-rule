package com.github.thinhunan.wonder8.promotion.rule.model.builder.part;

import com.github.thinhunan.wonder8.promotion.rule.model.builder.SimplexRuleBuilder;

public class SimplexPredictPart {
    protected SimplexRuleBuilder _builder;
    public SimplexPredictPart(SimplexRuleBuilder builder) {
        this._builder = builder;
    }

    public SimplexRuleBuilder expected(int expected) {
        _builder.setExpected(expected);
        return _builder;
    }
}
