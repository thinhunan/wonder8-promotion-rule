/**
 * Strategy.bestMatch result called by Calculator
 * @author tanzhenlin
 * @date 2022/6/29 11:20
 **/
package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BestMatch {

    MatchType type;

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    List<Rule> rules;

    public List<Rule> getRules() {
        return rules;
    }

    List<Item> items;

    public List<Item> getItems(){
        return items;
    }

    List<Match> matches;

    public List<Match> getMatches() {
        return matches;
    }

    public void addMatch(Match match){
        if(matches == null){
            matches = new ArrayList<>();
        }
        if(match != null){
            matches.add(match);
        }
    }

    RuleValidateResult suggestion;

    public RuleValidateResult getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(RuleValidateResult suggestion) {
        this.suggestion = suggestion;
    }

    public List<Item> chosen() {
        List<Item> chosen = new ArrayList<>();
        for(Match m : matches){
            chosen.addAll(m.getItems());
        }
        return chosen;
    }

    public List<Item> left() {
        List<Item> chosen = chosen();
        return items.stream()
                .filter(t->!chosen.contains(t))
                .collect(Collectors.toList());
    }

    public BestMatch(List<Rule> rules, List<Item> items, MatchType type) {
        this.rules = rules;
        this.items = items;
        this.type = type;
        this.matches = new ArrayList<>();
    }


    public int totalPrice() {
        if (this.matches == null || this.matches.size() == 0) {
            return 0;
        }
        return matches.stream()
                .map(Match::totalPrice)
                .reduce(0, Integer::sum);
    }

    /**
     * 计算总的打折金额，但请注意只以匹配部分为计算依据，如果需要按所有的商品来计算，可以调用{@link Rule#discount(List)} 来计算
     *
     * @return {int}
     */
    public int totalDiscount() {
        if (this.matches == null || this.matches.size() == 0) {
            return 0;
        }
        return matches.stream()
                .map(Match::totalDiscount)
                .reduce(0, Integer::sum);
    }
}
