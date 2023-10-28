/**
 * Match result presentation
 * @author tanzhenlin
 * @date 2022/6/29 11:22
 **/
package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.Rule;

import java.util.List;

public class Match {
    Rule rule;
    List<Item> items;

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Match(Rule rule, List<Item> items) {
        this.rule = rule;
        this.items = items;
    }

    public int totalDiscount(){
        if(items == null || items.size() == 0 || rule == null) return 0;
        return rule.discount(items);
    }

    public int count(){
        if(items == null) return 0;
        return items.size();
    }

    public int totalPrice(){
        if(items == null) return 0;
        return items.stream().map(Item::getPrice).reduce(0,Integer::sum);
    }
}
