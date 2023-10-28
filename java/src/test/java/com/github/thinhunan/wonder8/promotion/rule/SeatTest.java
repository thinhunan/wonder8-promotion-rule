package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchType;
import com.github.thinhunan.wonder8.promotion.rule.model.Rule;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.BestMatch;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.Match;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author tanzhenlin
 * @Date 2022/8/23 13:47
 **/

public class SeatTest {
    private List<Item> prepareTicket(){
        Item t1 = new Item();
        t1.setPrice(100);
        t1.setSeat("VIP:C:5");
        Item t2 = new Item();
        t2.setPrice(100);
        t2.setSeat("VIP:C:13");
        Item t3 = new Item();
        t3.setPrice(200);
        t3.setSeat("前台区:C:04");
        Item t4 = new Item();
        t4.setPrice(100);
        t4.setSeat("VIP:B:13");
        Item t5 = new Item();
        t5.setPrice(100);
        t5.setSeat("VIP:C:04");
        List<Item> items = new ArrayList<>();
        items.add(t1);
        items.add(t2);
        items.add(t3);
        items.add(t4);
        items.add(t5);
        Item t6 = new Item();
        t6.setPrice(200);
        t6.setSeat("一楼:VIP:C:6");
        items.add(t6);

        Item t7 = new Item();
        t7.setPrice(200);
        t7.setSeat("一楼:vip:c:08");
        items.add(t7);

        Item t8 = new Item();
        t8.setPrice(200);
        t8.setSeat("一楼:VIP:C:7");
        items.add(t8);

        Item t9 = new Item();
        t9.setPrice(200);
        t9.setSeat("一楼:vip:c:09");
        items.add(t9);
        return items;
    }

    @Test
    public void testSeatRange(){
        List<Item> items = prepareTicket();
        Rule r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].count(2)->-10%").asRule();
        Assert.assertEquals(-100, r.discountFilteredItems(items));
        Rule r2 = Interpreter.parseString("[#zVIP:C:5-VIP:C:10#zVIP:C:10-VIP:C:12#zVIP:C:13#zVIP:C:14#zVIP:C:15].count(2)->-10%").asRule();
        Assert.assertEquals(-100, r.discountFilteredItems(items));
    }

    @Test
    public void testSeatRangeMath(){
        Rule r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].adjacentSeat(2)->-10%").asRule();
        List<Item> items = prepareTicket();
        System.out.println(r.validate(items));
    }

    @Test
    public void testAdjacentSeatMath(){
        Rule r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].adjacentSeat(2)->-10%").asRule();
        List<Item> items = prepareTicket();
        BestMatch bestMatch = Strategy.bestChoice(Arrays.asList(r), items, MatchType.MultiRule);
        Assert.assertEquals(2,bestMatch.getMatches().size());
        for (Match m : bestMatch.getMatches()) {
            for (Item t :
                    m.getItems()) {
                System.out.println(t.getSeat());
            }
        }
        bestMatch = Strategy.bestChoice(Arrays.asList(r), items, MatchType.OneTime);
        Assert.assertEquals(1,bestMatch.getMatches().size());
        for (Match m : bestMatch.getMatches()) {
            for (Item t :
                    m.getItems()) {
                System.out.println(t.getSeat());
            }
        }
    }
}
