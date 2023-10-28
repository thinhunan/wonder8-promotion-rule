package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.R;
import com.github.thinhunan.wonder8.promotion.rule.model.RuleRange;
import com.github.thinhunan.wonder8.promotion.rule.model.RuleRangeCollection;
import com.github.thinhunan.wonder8.promotion.rule.model.SimplexRule;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.part.SimplexRangePart;

import java.util.Collection;

public class SimplexRuleBuilder extends SingleRuleBuilder {
    protected SimplexRuleBuilder(ConditionBuilder ruleBuilder){
        super(ruleBuilder);
    }
    public SimplexRuleBuilder(){this(null);}

    public SimplexRangePart addRange(R type, String id){
        if(_ruleRanges == null){
            _ruleRanges = new RuleRangeCollection();
        }
        _ruleRanges.add(new RuleRange(type,id));
        return new SimplexRangePart(this);
    }

    public SimplexRangePart addRanges(R type, Collection<String> ids){
        if(_ruleRanges == null){
            _ruleRanges = new RuleRangeCollection();
        }
        if(ids != null){
            for (String id : ids) {
                _ruleRanges.add(new RuleRange(type,id));
            }
        }
        return new SimplexRangePart(this);
    }

    public SimplexRangePart addRangeAll(){
        addRange(R.ALL,null);
        return new SimplexRangePart(this);
    }


    public SimplexRangePart range(String rangeString){
         _ruleRanges = RuleRangeCollection.parseString(rangeString);
         return new SimplexRangePart(this);
    }


    @Override
    public SimplexRule build() {
        SimplexRule rule = new SimplexRule();
        rule.setRange(_ruleRanges);
        rule.setExpected(_expected);
        rule.setPredict(_predict);
        return rule;
    }
}


