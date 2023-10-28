package com.github.thinhunan.wonder8.promotion.rule.model.builder;
import com.github.thinhunan.wonder8.promotion.rule.model.OrCompositeRule;


public class OrCompositeRuleBuilder extends CompositeRuleBuilder {
    protected OrCompositeRuleBuilder(ConditionBuilder builder) {
        super(builder);
    }
    public OrCompositeRuleBuilder(){this(null);}
    @Override
    public OrCompositeRule build() {
        buildComponent();
        return new OrCompositeRule(components);

    }
}
