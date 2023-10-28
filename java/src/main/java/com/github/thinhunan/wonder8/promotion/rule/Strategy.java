package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.P;
import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.SimplexRule;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.BestMatch;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.Calculator;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchGroup;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchType;
import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.*;

public class Strategy {


    /**
     * 找出一组商品和和一堆规则的最佳组合匹配，即多个规则合作下优惠力度最大的匹配结果
     * @param rules {Rule[]}
     * @param items {Item[]}
     * @param type {MatchType}
     *             MatchType.OneTime = 匹配一次
     *             MatchType.OneRule = 匹配一个规则，但这个规则可以匹配多次
     *             MatchType.MultiRule = 可以匹配多个规则，每个规则可以匹配多次
     * @param groupSetting {MatchGroup}
     *             MatchGroup.CrossedMatch = 分组计算，不同组的优惠可叠加，所有规则放在一起求最大优惠
     *             MatchGroup.SequentialMatch = 分组计算，不同组的优惠可叠加，不同组的优惠按组计算后求最大叠加优惠
     * @return {BestMatch}
     */
    public static BestMatch bestChoice(List<Rule> rules, List<Item> items, MatchType type, MatchGroup groupSetting) {
        BestMatch bestMatch = Calculator.calculate(rules, items,type, groupSetting);
        bindSuggestion(bestMatch);
        return bestMatch;
    }

    /**
     * 找出一组商品和和一堆规则的最佳组合匹配，即多个规则合作下优惠力度最大的匹配结果
     * @param rules {Rule[]}
     * @param items {Item[]}
     * @param type {MatchType}
     *             MatchType.OneTime = 匹配一次
     *             MatchType.OneRule = 匹配一个规则，但这个规则可以匹配多次
     *             MatchType.MultiRule = 可以匹配多个规则，每个规则可以匹配多次
     * @return {BestMatch}
     */
    public static BestMatch bestChoice(List<Rule> rules, List<Item> items, MatchType type) {
        return bestChoice(rules, items,type,MatchGroup.CrossedMatch);
    }

    /**
     * 找出一组商品和和一堆规则的最佳匹配，但只应用一条规则
     * @param {Rule[]} rules
     * @param {Item[]} tickets
     * @return {MatchResult}
     */
    @Deprecated
    public static BestMatch bestMatch(List<Rule> rules, List<Item> items) {
        return bestChoice(rules, items,MatchType.OneRule);
    }

    /**
     * 找出一组商品和和一堆规则的最佳匹配，即优惠力度最大的匹配结果
     * 与bestMatch()的区别在于，不管当前选的商品可以拆成匹配多少次，都只按一次匹配来算优惠
     * 匹配的张数是规则的最低要求，价值取最高价格
     * @param {Rule[]} rules
     * @param {Item[]} tickets
     * @return {MatchResult}
     */
    @Deprecated
    public static BestMatch bestOfOnlyOnceDiscount(List<Rule> rules, List<Item> items) {
        return bestChoice(rules, items,MatchType.OneTime);
    }


    private static void bindSuggestion(BestMatch best) {
        if (best != null) {
            List<RuleValidateResult> ss = suggestions(best.getRules(), best.left());
            if (ss != null && ss.size() > 0) {
                best.setSuggestion(ss.get(0));
            }
        }
    }

    /**
     * 获取所有未匹配但是比当前匹配更好的优惠组合建议
     * 最好不直接调用，而通过调用bestMatch()再调用返回结果的suggestion属性来获取拼单建议，
     * 因为会过滤当前商品集合已满足的优惠条件，可能造成没有更多的规则可供建议，而bestMatch的是匹配完后基于剩下的商品来建议
     */
    public static List<RuleValidateResult> suggestions(List<Rule> rules, List<Item> items) {
        //remember discount is negative number
        int minDiscount = 1;
        List<Rule> dontMatchedRules = new ArrayList<>();
        for (Rule r : rules) {
            if (r.check(items)) {
                int discountValue = r.discount(items);
                if (discountValue < minDiscount) {
                    minDiscount = discountValue;
                }
            } else {
                dontMatchedRules.add(r);
            }
        }
        List<RuleValidateResult> results = new ArrayList<>();
        int finalMinDiscount = minDiscount;
        //要小于已匹配的打折，才是更好的打折
        dontMatchedRules.stream().filter(r -> r.discount(items) < finalMinDiscount)
                .forEach(r -> results.add(r.validate(items)));
        return results;
    }

    /**
     * 获取当前未匹配，但是最接近的优惠组合拼单建议
     * 最好不直接调用，而通过调用bestMatch()再调用返回结果的suggestion属性来获取拼单建议，
     * 因为会过滤当前商品集合已满足的优惠条件，可能造成没有更多的规则可供建议，而bestMatch的是匹配完后基于剩下的商品来建议
     */
    public static RuleValidateResult suggestion(List<Rule> rules, List<Item> items) {
        List<RuleValidateResult> results = Strategy.suggestions(rules, items);

        //缺张数优先级高还是差总价优先级高，咱讲道理，按每个商品的平均价来
        int averagePrice = items.stream().map(Item::getPrice).reduce(0, Integer::sum) / items.size();
        RuleValidateResult best = null;
        int moreCount = 10000, moreSum = 100000000;
        for (RuleValidateResult r :results) {
            int[] needs = validateNeeds(r);
            if(moreCount * averagePrice + moreSum > needs[0]*averagePrice + needs[1]){
                moreCount = needs[0];
                moreSum = needs[1];
                best = r;
            }
        }
        return best;
    }

    /*
    * @Returns int[0] = count,int[1] = sum;
    */
    private static int[] validateNeeds(RuleValidateResult result) {
        int[] values = new int[2];
        List<RuleValidateResult> clauses =  result.getClauseResults();
        if(clauses == null || clauses.size() == 0){
            if(result.getRule() instanceof SimplexRule){
                SimplexRule simplex = (SimplexRule) result.getRule();
                if(simplex.getPredict() == P.SUM){
                    values[1] += result.getExpected() - result.getActual();
                }
                else{
                    values[0] += result.getExpected() - result.getActual();
                }
            }
        }
        else{
            for (RuleValidateResult sub : result.getClauseResults()) {
                int[] subNeeds = validateNeeds(sub);
                values[0] += subNeeds[0];
                values[1] += subNeeds[1];
            }
        }
        return values;
    }
}


