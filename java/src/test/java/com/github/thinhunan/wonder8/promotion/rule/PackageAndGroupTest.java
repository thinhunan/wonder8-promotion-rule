package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.BestMatch;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchGroup;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @Author tanzhenlin
 * @Date 2022/8/31 19:39
 **/

public class PackageAndGroupTest {
    @Test
    public void testPackage(){
        List<Item> testItems = Helper.getSeatedTickets();
        Rule rule1 = Interpreter.parseString("[#zVIP:A:1:1-VIP:A:2:10].adjacentSeat(3)->y:VipPackage3:300000").asRule();
        rule1.setGroup(0);
        Rule rule2 = Interpreter.parseString("[#kVipPackage3].count(1)->-10%").asRule();
        rule2.setGroup(1);
        BestMatch bestMatch1 = Strategy.bestChoice(Arrays.asList(rule1), testItems, MatchType.MultiRule, MatchGroup.CrossedMatch);
        Assert.assertEquals(300000-121200*3,bestMatch1.totalDiscount());
        BestMatch bestMatch = Strategy.bestChoice(Arrays.asList(rule1,rule2), testItems, MatchType.MultiRule, MatchGroup.CrossedMatch);
        Assert.assertEquals((300000-121200*3) - 30000,bestMatch.totalDiscount());
    }

    @Test
    public void testMatchGroup(){
        List<Item> seatedTickets = Helper.getSeatedTickets();
        //二楼:A:1:1-5
        //rule1 -2000 rule2 -1800 rule1+rule2 -3800 rule3 -4000
        Rule rule1 = Interpreter.parseString("[#z二楼:A:1:1-二楼:A:1:5].adjacentSeat(2)->y:APackage2:18000").asRule();
        rule1.setGroup(0);
        Rule rule2 = Interpreter.parseString("[#kAPackage2].count(1)->-10%@1").asRule();
        Rule rule3 = Interpreter.parseString("[#k02].count(3)->-4000@1").asRule();
        List<Rule> rules = Arrays.asList(rule1,rule2,rule3);

        BestMatch crossedGroupMatch = Strategy.bestChoice(rules,seatedTickets,
                MatchType.MultiRule,MatchGroup.CrossedMatch);
        Assert.assertEquals(-3800 -4000,crossedGroupMatch.totalDiscount());

        BestMatch sequentialGroupMatch = Strategy.bestChoice(rules,seatedTickets,
                MatchType.MultiRule,MatchGroup.SequentialMatch);
        Assert.assertEquals(-3800*2,sequentialGroupMatch.totalDiscount());
        System.out.println(rule1.toString());
        System.out.println(rule2.toRuleString());
        System.out.println(rule3.toString());
    }

    @Test
    @Ignore
    public void benchmark(){
        List<Item> seatedTickets = Helper.getSeatedTickets();
        //二楼:A:1:1-5
        //rule1 -2000 rule2 -1800 rule1+rule2 -3800 rule3 -4000
        Rule rule1 = Interpreter.parseString("[#z二楼:A:1:1-二楼:A:1:5].adjacentSeat(2)->y:APackage2:18000").asRule();
        rule1.setGroup(0);
        Rule rule2 = Interpreter.parseString("[#kAPackage2].count(1)->-10%").asRule();
        rule2.setGroup(1);
        Rule rule3 = Interpreter.parseString("[#k02].count(3)->-4000").asRule();
        rule3.setGroup(1);
        List<Rule> rules = Arrays.asList(rule1,rule2,rule3);

        long start = System.currentTimeMillis();
        for (int i = 0; i <10000; i++) {
            BestMatch crossedGroupMatch = Strategy.bestChoice(rules,seatedTickets,
                    MatchType.MultiRule,MatchGroup.CrossedMatch);
        }
        long end = System.currentTimeMillis();
        System.out.print("10000 crossed group match ms:");
        System.out.println(end - start);

        for (int i = 0; i <10000; i++) {
            BestMatch sequentialGroupMatch = Strategy.bestChoice(rules,seatedTickets,
                    MatchType.MultiRule,MatchGroup.SequentialMatch);
        }
        long end2 = System.currentTimeMillis();
        System.out.print("10000 sequential group match ms:");
        System.out.println(end2 - end);
    }
}
