package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.AndCompositeRule;

public class AndCompositeRuleBuilder extends CompositeRuleBuilder {
    protected AndCompositeRuleBuilder(ConditionBuilder builder) {
        super(builder);
    }
    public AndCompositeRuleBuilder(){this(null);}
    @Override
    public AndCompositeRule build() {
        buildComponent();
        return new AndCompositeRule(components);
    }
}
