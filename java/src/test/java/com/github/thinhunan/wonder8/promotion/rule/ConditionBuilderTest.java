package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.P;
import com.github.thinhunan.wonder8.promotion.rule.model.R;
import com.github.thinhunan.wonder8.promotion.rule.model.RuleComponent;
import com.github.thinhunan.wonder8.promotion.rule.model.SimplexRule;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.AndCompositeRuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.OrCompositeRuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.SimplexRuleBuilder;
import org.junit.Test;

import java.util.Arrays;

public class ConditionBuilderTest {
    @Test
    public void testBuildRule(){
        RuleComponent rule1 = Builder.rule()
                .simplex().addRangeAll()
                .predict(P.SUM).expected(100)
                .end() //这里不管调不调都可以backrule
                .endRule()
                .promotion("-10")
                .build();

        System.out.println(rule1.toString());
    }

    @Test
    public void testBuildSimplexRule(){
        SimplexRule rule1 = Builder.simplex() // same as => new SimplexRuleBuilder()
                .addRangeAll()
                .predict(P.SUM).expected(100)
                .build();
        System.out.println(rule1.toString());
    }

    @Test
    public void testParseRange(){
        SimplexRule rule1 = new SimplexRuleBuilder()
                .range("[#cPROGRAMEID1#cPROGRAMEID2]")
                .predict(P.SUM).expected(100)
                .build();
        System.out.println(rule1.toString());
    }


    @Test
    public void testBuildOrCompositeRule(){
        RuleComponent or =  new OrCompositeRuleBuilder()
                .simplex().addRangeAll().predict(P.SUM).expected(100).end()
                .simplex().addRange(R.CATEGORY,"PROGRAMEID1").predict(P.COUNT).expected(5).end()
                .sameRange().predict(P.COUNT_SPU).expected(2).end()
                .build();
        System.out.println(or);
    }

    @Test
    public void testBuildAndCompositeRule(){
        RuleComponent and = new AndCompositeRuleBuilder()
                .simplex().addRanges(R.CATEGORY, Arrays.asList("PROGRAMEID1","PROGRAMEID2")).predict(P.COUNT).expected(5).end()
                .simplex().addRangeAll().predict(P.COUNT_SPU).expected(5).end()
                .sameRange().predict(P.COUNT).expected(10).end()
                .build();
        System.out.println(and);
    }
}