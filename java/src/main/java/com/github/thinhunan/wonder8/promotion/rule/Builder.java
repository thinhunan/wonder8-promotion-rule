package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.builder.AndCompositeRuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.OrCompositeRuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.RuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.SimplexRuleBuilder;

public class Builder {
    public static RuleBuilder rule(){
        return new RuleBuilder();
    }

    public static SimplexRuleBuilder simplex(){
        return new SimplexRuleBuilder();
    }

    public static AndCompositeRuleBuilder and(){
        return new AndCompositeRuleBuilder();
    }

    public static OrCompositeRuleBuilder or(){
        return new OrCompositeRuleBuilder();
    }
}
