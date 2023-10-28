/*
 * 计算多规则最优规则组合时的链
 * @author tanzhenlin
 * @date 2022/6/28 13:38
 */
package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchChain extends Match {

    MatchChain previousChain;
    List<MatchChain> next;
    List<Rule> rules;
    List<Item> left;


    public MatchChain(
            MatchChain previousChain,
            Rule matchRule,
            List<Item> matchItems,
            List<Item> left,
            List<Rule> rules){
        super(matchRule, matchItems);
        this.previousChain = previousChain;
        this.left = left;
        this.rules = rules;
        this.next = new ArrayList<>();
    }

    /**
     * 将链路中每一环折得最低的汇总，就是最低折扣
     */
    @Override
    public int totalDiscount(){
        int d = 0;
        for (MatchChain one : next) {
            int oneDiscount = one.totalDiscount();
            d = Math.min(d, oneDiscount);
        }
        return super.totalDiscount() + d;
    }

    public MatchChain bestChain(){
        return next.stream().min(Comparator.comparingInt(MatchChain::totalDiscount)).orElse(null);
    }
}
