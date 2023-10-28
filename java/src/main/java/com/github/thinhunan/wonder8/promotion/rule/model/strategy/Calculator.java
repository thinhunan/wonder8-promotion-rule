/*
 * Calculate best match using a match chain
 * @author tanzhenlin
 * @date 2022/6/29 11:20
*/
package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.Item;

import java.util.*;
import java.util.stream.Collectors;

public class Calculator {
    public static BestMatch calculate(List<Rule> rules, List<Item> items, MatchType type, MatchGroup groupSetting){
        BestMatch bestMatch = new BestMatch(rules, items,type);
        if(groupSetting == MatchGroup.CrossedMatch){
            _calculate(bestMatch);
        }
        else{
            Map<Integer,List<Rule>> ruleGroups = rules.stream().collect(
                    Collectors.groupingBy(Rule::getGroup,Collectors.toList()));
            ruleGroups.keySet().stream().sorted().forEach(integer -> {
                BestMatch groupMatch = new BestMatch(ruleGroups.get(integer), items,type);
                _calculate(groupMatch);
                for (Match m :groupMatch.getMatches()) {
                    Rule matchedRule = m.getRule();
                    bestMatch.addMatch(m);
                    if(matchedRule.isPackageRule()){
                        replacePackage(matchedRule,m.getItems(), items);
                    }
                }
            });
        }
        return  bestMatch;
    }

    private static void _calculate(BestMatch bestMatch){

        List<Rule> rules = bestMatch.getRules();
        MatchType type = bestMatch.getType();
        MatchChain root = new MatchChain(null,null,
                null, bestMatch.getItems(), rules);
        //以每条规则为起点build chain
        for (Rule rule: rules){
            //每条开始的chain，需要一个新的集合对象
            List<Item> items = new ArrayList<>(bestMatch.getItems());
            MatchChain chain = buildChain(root,rule, items,rules,type);
            if(chain != null){
                root.next.add(chain);
            }
        }

        //计算每条chain的折扣，得到折扣最多的那条
        if(root.next.size() > 0) {
            MatchChain bestChain = root.next.stream()
                    .min(Comparator.comparingInt(MatchChain::totalDiscount)).get();
            while (bestChain != null) {
                //bestMatch.addMatch(bestChain);
                Match m = new Match(bestChain.getRule(), bestChain.getItems());
                bestMatch.addMatch(m);
                bestChain = bestChain.bestChain();
            }
        }
    }

    private static MatchChain buildChain(MatchChain previous, Rule rule,
                                         List<Item> items, List<Rule> rules, MatchType type){

        if (items == null || items.size() < 1) return null;

        RuleMatchItem matcher = new RuleMatchItem(items,rule);
        List<Item> matched = matcher.choose(type);

        if(matched.size()>0){
            List<Item> left = items;
            //套商品规则需要将原始商品组合成套商品id，再进行后续运算
            if(rule.isPackageRule()){
                replacePackage(rule,matched,left);
            }
            else
            {
                left = matcher.more();
            }

            /**
             * 满减满折问题需使用贪婪方式匹配：
             * https://y7bm5epe2b.feishu.cn/wiki/wikcnlF3Tu7yBCqYStaghdD83Xe#fJYY5U
             * */
            if(rule.getPromotion().indexOf('%') > 0 || rule.getPromotion().indexOf('/') > 0){
                List<Item> moreItem = left.stream()
                        .filter(rule.getFilter())
                        .collect(Collectors.toList());
                if(moreItem.size()>0) {
                    for (int i = 0; i < moreItem.size(); i++) {
                        List<Item> subList = moreItem.subList(0, i+1);
                        List<Item> expandedMatched = new ArrayList<>(matched);
                        expandedMatched.addAll(subList);
                        List<Item> reducedLeft = left.stream().filter(t->!subList.contains(t)).collect(Collectors.toList());
                        MatchChain expandedChain = makeChain(previous,rule,rules,type, expandedMatched,reducedLeft);
                        previous.next.add(expandedChain);
                    }
                }
            }

            return makeChain(previous, rule, rules, type, matched, left);
        }
        return null;
    }

    private static MatchChain makeChain(MatchChain previous, Rule matchedRule,
                                        List<Rule> rules, MatchType matchType,
                                        List<Item> matchedItems, List<Item> leftItems) {
        MatchChain chain = new MatchChain(
                previous, matchedRule, matchedItems,
                leftItems, rules);
        //匹配多条规则时，拿所有规则展开
        if( matchType == MatchType.MultiRule){
            for (Rule r : rules) {
                List<Item> itemList = new ArrayList<>(leftItems);
                MatchChain c = buildChain(chain, r, itemList, rules, matchType);
                if (c != null) {
                    chain.next.add(c);
                }
            }
        }
        //区配单条规则时，拿同一条规则进行下去
        else if(matchType == MatchType.OneRule) {
            MatchChain c = buildChain(chain, matchedRule, leftItems, Collections.singletonList(matchedRule), matchType);
            if (c != null) {
                chain.next.add(c);
            }
        }
        return chain;
    }

    //将当前group后续的group中的商品列表中已命中ticket移除，将组成的套商品加入
    private static Item replacePackage(Rule rule, List<Item> matched, List<Item> itemList) {
        for (Item matchedItem : matched) {
            itemList.remove(matchedItem);
        }
        Item firstMatched = matched.get(0);
        Item itemPackage = new Item();
        itemPackage.setSKU(rule.getPackageId());
        itemPackage.setSPU(firstMatched.getSPU());
        itemPackage.setCategory(firstMatched.getCategory());
        int price = matched.stream()
                .map(Item::getPrice).reduce(0,Integer::sum)
                + rule.discount(matched);
        itemPackage.setPrice(price);
        String seat = matched.stream().map(Item::getSeat).reduce("",(s1, s2)->(s1 == null ||s1.isEmpty())?s2:s1+";"+s2);
        itemPackage.setSeat(seat);
        itemList.add(itemPackage);
        return itemPackage;
    }

}
