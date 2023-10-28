package com.github.thinhunan.wonder8.promotion.rule.model.builder.part;

import com.github.thinhunan.wonder8.promotion.rule.model.P;
import com.github.thinhunan.wonder8.promotion.rule.model.R;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.SimplexRuleBuilder;

import java.util.List;

public class SimplexRangePart {
    protected SimplexRuleBuilder _builder;
    public SimplexRangePart(SimplexRuleBuilder builder) {
        this._builder = builder;
    }

    public SimplexRangePart addRange(R type, String id) {
        _builder.addRange(type, id);
        return this;
    }

    public SimplexRangePart addRanges(R type, List<String> ids){
        _builder.addRanges(type,ids);
        return this;
    }

    public SimplexPredictPart predict(P predict) {
        _builder.setPredict(predict);
        return new SimplexPredictPart(_builder);
    }

}
