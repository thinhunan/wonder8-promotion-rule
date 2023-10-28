package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

import com.github.thinhunan.wonder8.promotion.rule.model.*;
import com.github.thinhunan.wonder8.promotion.rule.model.comparator.TicketSeatComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * for Strategy.bestMatch()'s calculation
  */
public class RuleMatchItem {

    List<Item> items;
    Rule rule;
    List<Item> chosenItems;

    //有哪些商品可匹配某条规则
    HashMap<RuleComponent, List<Item>> matchGroup;
    //每个商品匹配了多少条规则
    HashMap<Item,Integer> matchCount;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public List<Item> getChosenItems() {
        return chosenItems;
    }

//    public void setChosenItems(List<Item> chosenItems) {
//        this.chosenItems = chosenItems;
//    }

    public RuleMatchItem(List<Item> items, Rule rule){
        if(items == null || items.size() < 1
                || rule == null || rule.getCondition() == null ) {//|| rule.getPromotion() == null){
            throw new IllegalArgumentException("参数不能为空,Rule的条件和优惠必须设置好");
        }
        this.items = items;
        this.rule = rule;
    }

    /*
        calculate tickets to match condition's expectation
     */
    public List<Item> choose(MatchType type){
        if(this.rule.getCondition() instanceof SimplexRule){
            SimplexRule simplexRule = (SimplexRule) this.rule.getCondition();
            if(simplexRule.getPredict() == P.ADJACENT_SEAT){
                this.chosenItems = this.adjacentSeatMatch(simplexRule.getExpected());
                return this.chosenItems;
            }
        }
        //只匹配一次或者优惠方式是优格的比率，则优先把大SKU的匹配出来
        if(type == MatchType.OneTime || isRatio()){
            this.chosenItems = maxPriceMatch();
        }
        else {
            minPriceMatch();
        }
        return this.chosenItems;
    }

    //等比例折扣
    private boolean isRatio(){
        String promotion = rule.getPromotion();
        if(promotion == null) return false;
        return promotion.indexOf('%') > 0 || promotion.indexOf('/') > 0;
    }

    /**
     * 求expected个相邻座位的ticket
     * @param expected 相邻座位所需的个数
     * @return 匹配出来的座位列表
     */
    private List<Item> adjacentSeatMatch(int expected){
        TicketSeatComparator comparator = new TicketSeatComparator();
        //all candidate tickets which matched condition range
        List<Item> sorted = items.stream()
                .filter(rule.getFilter())
                .sorted(comparator)
                .collect(Collectors.toList());

        String lastSeat = "";
        List<Item> chosen = null;
        for (Item t :sorted) {
            String seat = t.getSeat();
            if (!comparator.isNextSeat(lastSeat, seat)) {
                chosen = new ArrayList<>();
            }
            chosen.add(t);
            if (chosen.size() >= expected) {
                return chosen;
            }

            lastSeat = seat;
        }
        return new ArrayList<>();
    }

    /**
     * 最低代价匹配集
     */
    private void minPriceMatch() {
        this.chosenItems = new ArrayList<>();
        this.matchCount = new HashMap<>();// new MatchCount();
        this.matchGroup = new HashMap<>();// new MatchItem();
        for (Item item : this.items) {
            this.matchCount.put(item, 0);
        }
        RuleComponent c = this.rule.getCondition();
        //初始化匹配计数
        this.matchItemForCondition(c);
        this.chooseItemForCondition(c, null);
    }

    /**
     * 最高价格匹配集
     * @return
     */
    private List<Item> maxPriceMatch() {
        if (!rule.check(items)) {
            return new ArrayList<>();
        }

        //all candidate tickets which matched condition range
        List<Item> filtered = items.stream()
                .filter(rule.getFilter())
                .collect(Collectors.toList());

        //for iterating
        Comparator<Item> comparator = Comparator.comparingInt(Item::getPrice);
        if(!isRatio()){
            comparator = comparator.reversed();
        }
        Comparator<Item> seatComparator = new TicketSeatComparator().reversed();
        comparator = comparator.thenComparing(seatComparator);
        List<Item> sorted = filtered.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        //尝试每个商品拿掉看是否还匹配，如果不匹配，则放回，so easy!
        for (Item t : sorted) {
            filtered.remove(t);
            if (!rule.check(filtered)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    /*
        tickets remained after match
     */
    public List<Item> more(){
        return this.items.stream()
                .filter(t-> !this.chosenItems.contains(t))
                .collect(Collectors.toList());
    }

    /*
        Match tickets and rules, for picking ticket strategy:
        1. each ticket matched which conditions;
        2.each condition matched which tickets;
    */
    private void matchItemForCondition(RuleComponent c) {
        if(c instanceof SimplexRule){
            SimplexRule simplex = (SimplexRule)c;
            List<Item> matchedItems;
            if(simplex.getRange().get(0).getType() == R.ALL){
                 matchedItems = this.items;
            }
            else {
                Predicate<Item> filter = simplex.getFilter();
                matchedItems = this.items.stream().filter(filter).collect(Collectors.toList());
            }
            this.matchGroup.put(c, matchedItems);
            for (Item t : matchedItems) {
                this.matchCount.replace(t,this.matchCount.get(t)+1);
            }
        }
        else{
            CompositeRule composite = (CompositeRule) c;
            List<RuleComponent> subConditions = composite.getComponents();
            for (RuleComponent subCondition :
                    subConditions) {
                matchItemForCondition(subCondition);
            }
        }
    }

    /*
        calculate the lowest cost set of ticket to match condition's expectation,
        then merge the set to this.chosenItems
     */
    private List<Item> chooseItemForCondition(RuleComponent c, OrCompositeRule parentOr){
        if(!c.check(this.items)){
            //add all when cannot match condition with all tickets
            return new ArrayList<>(this.items);
        }

        List<Item> chosenItemForCondition = new ArrayList<>();

        //因为同一个商品可区配多条规则，所以基于现在已选的基础上添加候选ticket来完足当前condition的要求
        //如果不在一个or组合中，则每次选出的商品立刻加入全局chosen
        //否则先不加，等到parentOr这一级考量需求最少的那条子规则的匹配集加入全局chosen，因为or只需有一条子规则成立即可
        if( c instanceof SimplexRule){
            SimplexRule simplex = (SimplexRule) c;
            //all tickets match current condition
            List<Item> allCandidates = this.matchGroup.get(c);
            // SUM tickets' price
            if(simplex.getPredict() == P.SUM){
                //candidates in chosen items already reached an aggregated price
                int chosenItemSum = allCandidates.stream()
                        .filter(t-> this.chosenItems.contains(t))
                        .map(Item::getPrice)
                        .reduce(0,Integer::sum);
                int expected = simplex.getExpected();
                if( chosenItemSum < expected ){// not enough
                    Comparator<Item> comparator = Comparator.comparingInt(Item::getPrice);
                    Comparator<Item> seatComparator = new TicketSeatComparator().reversed();
                    comparator = comparator.thenComparing(seatComparator);//按座位倒序取商品，不容易打散相邻座位

                    List<Item> candidates = allCandidates.stream()
                            .filter(t->!this.chosenItems.contains(t))//pick candidate which not chosen
                            .sorted(comparator) // from cheap to expensive
                            .collect(Collectors.toList());
                    int need = expected - chosenItemSum;
                    //has a ticket perfect match price needs?
                    Item perfectItem =  candidates.stream().filter(t->t.getPrice() == need).findFirst().orElse(null);
                    if(perfectItem != null){
                        _distinctAdd(chosenItemForCondition, perfectItem);
                    }
                    else{
                        //no perfect, then combine tickets to match
                        int count = candidates.size();
                        _distinctAdd(chosenItemForCondition,candidates.get(count-1));//pick the biggest one of candidates
                        chosenItemSum += candidates.get(count -1).getPrice();
                        //continuous add cheap candidates until reach condition's expectation
                        for(int i = 0 ; i < count -1 && chosenItemSum < expected; i++) {
                            chosenItemSum += candidates.get(i).getPrice();
                            _distinctAdd(chosenItemForCondition,candidates.get(i));
                            if(chosenItemSum >= expected){
                                //adjust chosen tickets to fix overflowing
                                //比如，A+B超了，A+C更接近需求，那么就要A+B+C-B，得到A+C的组合
                                int exceeding = chosenItemSum - expected;
                                int lastPosition = chosenItemForCondition.size()-1;
                                for(int j = lastPosition; j >= 0; j--) {
                                    if(chosenItemForCondition.get(j).getPrice() == exceeding){
                                        chosenItemForCondition.remove(j);
                                        exceeding = 0;
                                    }
                                    else if(chosenItemForCondition.get(j).getPrice() < exceeding){
                                        chosenItemForCondition.remove(j);
                                        exceeding -= chosenItemForCondition.get(j).getPrice();
                                    }
                                    if(exceeding == 0){
                                        break;
                                    }
                                }
                            }
                        }

                    }
                }
            }
            else {
                //COUNT tickets
                if(!simplex.check(this.chosenItems)){

                    Comparator<Item> seatComparator = new TicketSeatComparator().reversed();
                    Comparator<Object> comparator = Comparator.comparing(matchCount::get).reversed()//匹配规则从多到少
                            .thenComparing(t -> ((Item) t).getPrice() * -1)//价格从高到低
                            .thenComparing((t1,t2)->seatComparator.compare((Item)t1,(Item)t2));//座位从前到后

                    //已选商品 + 候选商品 --> 新集合
                    //尝试将候选商品中的各个商品从新集合中移除
                    //如果移除后check通过，则移除，否则加回
                    //以此来找到最少需要哪些商品加入，才能完成check
                    List<Item> candidates = allCandidates.stream()
                            .filter(t -> !this.chosenItems.contains(t))
                            .sorted(comparator)
                            .collect(Collectors.toList());
                    List<Item> joined = Stream.concat(
                                this.chosenItems.stream(),candidates.stream())
                            .collect(Collectors.toList());
                    List<Item> removed = new ArrayList<>();
                    for (Item t : candidates) {
                        joined.remove(t);
                        if (simplex.check(joined)) {
                            removed.add(t);
                        } else {
                            joined.add(t);
                        }
                    }

                    chosenItemForCondition.addAll(
                            candidates.stream().filter(t->!removed.contains(t))
                                    .collect(Collectors.toList()));

                }
            }
            if(parentOr == null) {
                this.mergeToChosen(chosenItemForCondition);
            }
        }
        else if(c instanceof AndCompositeRule){
            AndCompositeRule and = (AndCompositeRule) c;
            for (RuleComponent sub : and.getComponents()) {
                //calculates sub-conditions
                List<Item> chosenForSubCondition = this.chooseItemForCondition(sub,parentOr);
                if(parentOr == null) {
                    //if not under "or", merge tickets to rule-scope-chosen immediately
                    this.mergeToChosen(chosenForSubCondition);
                }
                //merge tickets to current condition-scope-chosen
                for (Item t : chosenForSubCondition) {
                    _distinctAdd(chosenItemForCondition,t);
                }
            }
        }
        else if(c instanceof  OrCompositeRule){
            OrCompositeRule or = (OrCompositeRule) c;
            if(parentOr == null){
                parentOr = or;//is the most top "or"
            }
            for(RuleComponent sub: or.getComponents()){
                //calculates sub-conditions
                List<Item> chosenForSubCondition = this.chooseItemForCondition(sub,parentOr);
                if(chosenItemForCondition.size() == 0){//first matched sub-condition
                    chosenItemForCondition = chosenForSubCondition;
                }
                else{
                    //we need the lowest cost matched condition
                    if(chosenForSubCondition.size()
                            < chosenItemForCondition.size()){
                        chosenItemForCondition = chosenForSubCondition;
                    }
                }
            }
            if(parentOr == or) {//is the most top "or"
                this.mergeToChosen(chosenItemForCondition);
            }
        }

        return chosenItemForCondition;
    }

    private void mergeToChosen(List<Item> itemList) {
        for (Item t : itemList) {
            _distinctAdd(this.chosenItems,t);
        }
    }

    private void _distinctAdd(List<Item> list, Item item){
        if(!list.contains(item)){
            list.add(item);
        }
    }
}
