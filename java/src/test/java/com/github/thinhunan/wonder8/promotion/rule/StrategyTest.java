package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.*;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.BestMatch;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.Match;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchGroup;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.MatchType;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class StrategyTest {
    // c01有3个商品70块，其中p01有2张50块，p02有1张20块,其它都只有1张20块，10个商品210块
    // k01 30块（3000分），其它都是20块
    private static List<Item> _getSelectedItems() {
        Item[] selectedItems = new Item[]{
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k01", "SKU01", 2000),
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k01", "SKU01", 2000),
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k02", "SKU02", 3000),
                new ItemImpl("c01", "分类01", "p02", "SPU02", "k03", "SKU03", 4000),
                new ItemImpl("c02", "分类02", "p03", "SPU03", "k04", "SKU04", 5000),
                new ItemImpl("c03", "分类03", "p04", "SPU04", "k05", "SKU05", 6000),
                new ItemImpl("c04", "分类04", "p05", "SPU05", "k06", "SKU06", 7000),
                new ItemImpl("c05", "分类05", "p06", "SPU06", "k07", "SKU07", 8000),
                new ItemImpl("c06", "分类06", "p07", "SPU07", "k08", "SKU08", 9000),
                new ItemImpl("c07", "分类07", "p08", "SPU08", "k09", "SKU09", 2000),
                new ItemImpl("c08", "分类08", "p09", "SPU09", "t10", "SKU10", 3000)
        };
        return Arrays.stream(selectedItems).collect(Collectors.toList());
    }

    private static List<Item> _getRandomPriceItems() {
        Random r = new Random();
        Item[] selectedItems = new Item[]{
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k01", "SKU01", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k01", "SKU01", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c01", "分类01", "p01", "SPU01", "k02", "SKU02", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c01", "分类01", "p02", "SPU02", "k03", "SKU03", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c02", "分类02", "p03", "SPU03", "k04", "SKU04", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c03", "分类03", "p04", "SPU04", "k05", "SKU05", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c04", "分类04", "p05", "SPU05", "k06", "SKU06", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c05", "分类05", "p06", "SPU06", "k07", "SKU07", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c06", "分类06", "p07", "SPU07", "k08", "SKU08", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c07", "分类07", "p08", "SPU08", "k09", "SKU09", (1 + r.nextInt(9)) * 1000),
                new ItemImpl("c08", "分类08", "p09", "SPU09", "t10", "SKU10", (1 + r.nextInt(9)) * 1000)
        };
        return Arrays.stream(selectedItems).collect(Collectors.toList());
    }

    void _logBestMatch(BestMatch bestMatch){
        //System.out.println(JSON.toJSONString(bestMatch, SerializerFeature.PrettyFormat));

        if (bestMatch == null) {
            System.out.println("no match");
            return;
        }
        System.out.println("matches:[");
        for(Match m : bestMatch.getMatches()){
            System.out.println("\tmatch of "+m.getRule()+":[");
            for (Item t : m.getItems()) {
                System.out.println("\t\t" + ((ItemImpl) t).toString());
            }
            System.out.println("\t]");
        }
        System.out.println("]");
        System.out.printf("sum price:%d  \t\tsum discount: %d\n", bestMatch.totalPrice(), bestMatch.totalDiscount());
        System.out.println("left:[");
        for (Item t : bestMatch.left()) {
            System.out.println("\t" + ((ItemImpl) t).toString());
        }
        System.out.println("]");
        System.out.println("suggestion:");
        System.out.println(bestMatch.getSuggestion());
    }


    @Test
    public void bestMatch() {
        Rule r1 = Builder.rule().simplex()
                .range("[#cc01]")
                .predict(P.COUNT)
                .expected(2)
                .endRule()
                .promotion("-200")
                .build();
        Rule r2 = Builder.rule().simplex()
                .addRange(R.CATEGORY, "c01")
                .predict(P.COUNT)
                .expected(3)
                .endRule()
                .promotion("-300")
                .build();
        Rule r3 = Builder.rule().simplex()
                .addRangeAll()
                .predict(P.COUNT)
                .expected(6)
                .endRule()
                .promotion("-10%")
                .build();
        List<Item> items = _getSelectedItems();
        List<Rule> rules = Arrays.asList(r1, r2);

        BestMatch bestMatch = Strategy.bestMatch(rules, items);
        Assert.assertEquals(2, bestMatch.getMatches().size());
        Assert.assertEquals(r1, bestMatch.getMatches().get(0).getRule());
        //System.out.println("best:");
        //_logBestMatch(bestMatch);

        BestMatch bestMatch1 = Strategy.bestChoice(rules, items, MatchType.OneRule);
        Assert.assertEquals(bestMatch.getMatches().get(0).getRule(),bestMatch1.getMatches().get(0).getRule());
        Assert.assertEquals(bestMatch.totalDiscount(),bestMatch1.totalDiscount());

        BestMatch bestOfOnce = Strategy.bestOfOnlyOnceDiscount(rules, items);
        //System.out.println("best of only once discount:");
        //_logBestMatch(bestOfOnce);
        bestMatch1 = Strategy.bestChoice(rules, items,MatchType.OneTime);
        Assert.assertEquals(bestOfOnce.getMatches().get(0).getRule(),bestMatch1.getMatches().get(0).getRule());
        Assert.assertEquals(bestOfOnce.totalDiscount(),bestMatch1.totalDiscount());

        // 5 tickets matched
        items.add(new ItemImpl("c01", "分类01", "p02", "SPU02", "k03", "SKU03", 4000));
        //BestMatch bestOfMulti = Strategy.bestOfMultiRuleApply(rules,tickets);
        BestMatch bestOfMulti = Strategy.bestChoice(rules, items, MatchType.MultiRule);
        Assert.assertEquals(2,bestOfMulti.getMatches().size());
        Assert.assertEquals(5,bestOfMulti.chosen().size());
        Assert.assertEquals(-500,bestOfMulti.totalDiscount());
        //System.out.println("best of multi-rule apply:");
        //_logBestMatch(bestOfMulti);

        // 6 tickets matched
        items.add(new ItemImpl("c01", "分类01", "p02", "SPU02", "k03", "SKU03", 4000));
        bestOfMulti = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        Assert.assertEquals(6,bestOfMulti.chosen().size());
        Assert.assertEquals(-600,bestOfMulti.totalDiscount());
        //System.out.println("best of multi-rule apply:");
        //_logBestMatch(bestOfMulti);

        // 7 tickets matched
        items.add(new ItemImpl("c01", "分类01", "p02", "SPU02", "k03", "SKU03", 4000));
        bestOfMulti = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        Assert.assertEquals(3,bestOfMulti.getMatches().size());
        Assert.assertEquals(7,bestOfMulti.chosen().size());
        Assert.assertEquals(-700,bestOfMulti.totalDiscount());
        System.out.println("best of multi-rule apply:");
        _logBestMatch(bestOfMulti);

        // 7 tickets matched
        Rule r4 = Builder.rule().simplex().addRange(R.SPU,"p02")
                .predict(P.COUNT).expected(4).endRule()
                .promotion("-2000").build();
        rules = Arrays.asList(r1,r2,r3,r4);
        bestOfMulti = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        //Assert.assertEquals(3,bestOfMulti.getMatches().size());
        Assert.assertEquals(14,bestOfMulti.chosen().size());
        Assert.assertEquals(-400-300-2000-500-600-700-800-900-200-300,bestOfMulti.totalDiscount());
        System.out.println("best of multi-rule apply:");
        _logBestMatch(bestOfMulti);

        r3.setPromotion("-100");
        bestOfMulti = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        _logBestMatch(bestOfMulti);
    }


    @Test
    public void testSum() {
        Rule r1 = Builder.rule()
                .simplex()
                .addRange(R.CATEGORY, "c01")
                .predict(P.SUM)
                .expected(8000)
                .endRule()
                .promotion("-900")
                .build();
        List<Item> selectedItems = _getSelectedItems();
        BestMatch bestMatch = Strategy.bestMatch(Arrays.asList(r1), selectedItems);
        Assert.assertEquals(1, bestMatch.getMatches().size());
        //选出20，20，30，40中的20,20,40
        Assert.assertEquals(8000, bestMatch.totalPrice());
        _logBestMatch(bestMatch);

        Rule r2 = Builder.rule()
                .simplex()
                .addRangeAll()
                .predict(P.SUM)
                .expected(8000)
                .endRule()
                .promotion("-800")
                .build();
        bestMatch = Strategy.bestMatch(Arrays.asList(r1, r2), selectedItems);
        Assert.assertEquals(r2, bestMatch.getMatches().get(0).getRule());
        _logBestMatch(bestMatch);//选出的是$.sum(8000),且有6组

        BestMatch bestOfOnce = Strategy.bestOfOnlyOnceDiscount(Arrays.asList(r1, r2), selectedItems);
        System.out.println("best of only once discount:");
        _logBestMatch(bestOfOnce);//选出的是[#cc01].sum(8000),因为只算一组的话，这一条规则优惠90，比$这一条多10
    }


    @Test
    public void testSumOfRandomItems() {
        Rule r1 = Builder.rule()
                .simplex()
                .addRange(R.CATEGORY, "c01")
                .predict(P.SUM)
                .expected(8000)
                .endRule()
                .promotion("-800")
                .build();
        List<Item> selectedItems = _getRandomPriceItems();
        BestMatch bestMatch = Strategy.bestMatch(Arrays.asList(r1), selectedItems);
        _logBestMatch(bestMatch);

        Rule r2 = Builder.rule()
                .simplex()
                .addRangeAll()
                .predict(P.SUM)
                .expected(8000)
                .endRule()
                .promotion("-800")
                .build();
        bestMatch = Strategy.bestMatch(Arrays.asList(r1, r2), selectedItems);
        _logBestMatch(bestMatch);
    }


    @Test
    public void testComposite() {
        Rule and = Builder.rule()
                .and().simplex()
                .range("[#cc01]")
                .predict(P.COUNT)
                .expected(2)
                .end()
                .sameRange()
                .predict(P.SUM)
                .expected(8000)
                .end()
                .endRule()
                .promotion("-800")
                .build();
        System.out.println(and);
        List<Item> items = _getSelectedItems();
        List<Rule> rules = Arrays.asList(and);
        BestMatch bestMatch = Strategy.bestMatch(rules, items);
        _logBestMatch(bestMatch);
        Assert.assertEquals(1,bestMatch.getMatches().size());
        Assert.assertEquals(3,bestMatch.chosen().size());


        Rule or = Builder
                .rule()
                .or()
                .addRule(and.getCondition())
                .simplex()
                .addRangeAll()
                .predict(P.SUM)
                .expected(8000)
                .end()
                .end()
                .endRule()
                .promotion("-800")
                .build();
        System.out.println(or);
        rules = Arrays.asList(or);
        bestMatch = Strategy.bestMatch(rules, items);
        _logBestMatch(bestMatch);
        Assert.assertEquals(6,bestMatch.getMatches().size());
        Assert.assertEquals(11,bestMatch.chosen().size());
    }

    @Test
    public void testOnce() {//总共9个商品，100块的2张，120块的6张，0.5块的1张
        Rule r = Interpreter.parseString("[#k6246d389d1812e77f772d1db].sum(15000)&~.countCate(1) -> -2000/15000").asRule();

        List<Item> items = Arrays.asList(
                new ItemImpl("6246d311d1812e77f772b1fe", "分类01", "6246d321d1812e77f772b61c", "SPU01", "6246d389d1812e77f772d1d6", "SKU01", 10000),
                new ItemImpl("6246d311d1812e77f772b1fe", "分类01", "6246d321d1812e77f772b61c", "SPU01", "6246d389d1812e77f772d1d6", "SKU01", 10000),
                new ItemImpl("6246d311d1812e77f772b1fe", "分类01", "6246d321d1812e77f772b61c", "SPU01", "6246d389d1812e77f772d1db", "SKU02", 19000),
                new ItemImpl("6246d311d1812e77f772b1fe", "分类01", "6246d321d1812e77f772b61c", "SPU01", "6246d389d1812e77f772d1db", "SKU02", 19000)
        );
        List<Rule> rules = Arrays.asList(r);
        BestMatch result = Strategy.bestOfOnlyOnceDiscount(rules, items);
        System.out.println(result.totalDiscount());//should be -2000
        result = Strategy.bestMatch(rules, items);
        System.out.println(result.totalDiscount()); //should be -4000
    }

    @Test
    public void test4DiscountingAlgorithm() {
        //规则是100块的和120块的总共要6张，并且两种商品都要有
        String ruleString = "[#k01#k02].count(6)&~.countCate(2) -> -50%";
        Rule r = Interpreter.parseString(ruleString).asRule();
        List<Item> items = Arrays.asList(
                new ItemImpl("01", "分类01", "01", "SPU01", "01", "SKU01", 10000),
                new ItemImpl("01", "分类01", "01", "SPU01", "01", "SKU01", 10000),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU01", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU02", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU02", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU02", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU02", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "02", "SKU02", 121200),
                new ItemImpl("02", "分类02", "02", "SPU01", "03", "SKU03", 50)
        );
        List<Rule> rules = Arrays.asList(r);

        int expected = 0, actual = 0;
        //为了做规则推荐的运算，规则本身算折扣的方法里，
        // 并没有判定规则是否已达成，所以调用前需做check()
        if (r.check(items)) {
            //第1种，rule.discountFilteredItems(tickets)
            //计算的是规则范围内的这部分商品的折扣
            //expected = tickets.stream().filter(r.getFilter()).map(t -> t.getPriceByFen()).reduce(0, (i1, i2) -> i1 + i2) / -2;
            expected = items.stream().filter(r.getFilter()).map(t -> t.getPrice()).reduce(0, Integer::sum) / -2;
            actual = r.discountFilteredItems(items);
            System.out.println(expected);
            Assert.assertEquals(expected, actual);

            //第2种，rule.discount(tickets)
            //计算的是所有商品应用折扣
            //expected = tickets.stream().map(t -> t.getPriceByFen()).reduce(0, (i1, i2) -> i1 + i2) / -2;
            expected = items.stream().map(t -> t.getPrice()).reduce(0, Integer::sum) / -2;
            actual = r.discount(items);
            System.out.println(expected);
            Assert.assertEquals(expected, actual);
        }

        //第3种，Strategy.bestMath()
        //非比率折扣，计算的是用最低成本达成规则匹配所需要的商品
        //expected = (tickets.get(0).getPriceByFen() * 1 + tickets.get(2).getPriceByFen() * 5) / -2;
        expected = (10000*2 + 121200 * 6)/-2;
        actual = Strategy.bestMatch(rules, items).totalDiscount();
        System.out.println(expected);
        Assert.assertEquals(expected, actual);

        //第4种，Strategy.bestOfOnlyOnceDiscount()
        //计算达成规则所需的最少张数，但是是最高价格的商品
        //expected = (tickets.get(0).getPriceByFen() * 1 + tickets.get(2).getPriceByFen() * 5) / -2;
        BestMatch match = Strategy.bestOfOnlyOnceDiscount(rules, items);
        actual = match.totalDiscount();
        System.out.println(expected);
        Assert.assertEquals(expected, actual);
        System.out.println(match.left());
    }

    @Test
    public void test_oneSKU() {
        String  ruleString2 = "[#k01#k02].oneSKU(2) -> -50%",
                ruleString6 = "[#k01#k02].oneSKU(6) -> -50%",
                ruleString7 = "$.oneSKU(7) -> -50%";
        Rule rule2 = Interpreter.parseString(ruleString2).asRule(),
                rule6 = Interpreter.parseString(ruleString6).asRule(),
                rule7 = Interpreter.parseString(ruleString7).asRule();

        List<Item> items = Arrays.asList(
                new ItemImpl("01", "01", "01", "01", "01", "01", 10000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 10000),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "03", "03", 50)
        );

        Assert.assertTrue(rule2.check(items));
        Assert.assertEquals((10000 * 2 + 121200 * 6 + 50) / -2, rule2.discount(items));//01,02,03
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, rule2.discountFilteredItems(items));//01,02

        Assert.assertTrue(rule6.check(items));
        Assert.assertEquals((10000 * 2 + 121200 * 6 + 50) / -2, rule6.discount(items));//01,02,03
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, rule6.discountFilteredItems(items));//01,02
        BestMatch match = Strategy.bestMatch(Collections.singletonList(rule6), items);
        //Assert.assertEquals(121200 * 6 / -2, match.totalDiscount());//02 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2
        match = Strategy.bestOfOnlyOnceDiscount(Collections.singletonList(rule6), items);
        //Assert.assertEquals(121200 * 6 / -2, match.totalDiscount());//-2 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        Assert.assertTrue(!rule7.check(items)); //dont match

        match = Strategy.bestMatch(Collections.singletonList(rule2), items);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, match.totalDiscount());//01 * 2,02 * 6

        match = Strategy.bestOfOnlyOnceDiscount(Collections.singletonList(rule2), items);
        //Assert.assertEquals((121200 * 2) / -2, match.totalDiscount());//02 * 2
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        List<Rule> rules = Arrays.asList(rule2,rule6,rule7);
        match = Strategy.bestMatch(rules, items);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, match.totalDiscount());//matches rule2, gets 4 match groups

        match = Strategy.bestOfOnlyOnceDiscount(rules, items);
        //Assert.assertEquals((121200 * 6) / -2, match.totalDiscount());//matches rule6, gets 1 match group
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2
    }


    @Test
    public void testoneSKUSum() {
        String  ruleString2 = "[#k01#k02].oneSKUSum(20000) -> -50%",
                ruleString6 = "[#k01#k02].oneSKUSum(727200) -> -50%",
                ruleString7 = "$.oneSKUSum(727201) -> -50%";
        Rule rule2 = Interpreter.parseString(ruleString2).asRule(),
                rule6 = Interpreter.parseString(ruleString6).asRule(),
                rule7 = Interpreter.parseString(ruleString7).asRule();

        List<Item> items = Arrays.asList(
                new ItemImpl("01", "01", "01", "01", "01", "01", 10000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 10000),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "02", "02", 121200),
                new ItemImpl("02", "02", "02", "02", "03", "03", 50)
        );

        Assert.assertTrue(rule2.check(items));
        Assert.assertEquals((10000 * 2 + 121200 * 6 + 50) / -2, rule2.discount(items));//01,02,03
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, rule2.discountFilteredItems(items));//01,02

        Assert.assertTrue(rule6.check(items));
        Assert.assertEquals((10000 * 2 + 121200 * 6 + 50) / -2, rule6.discount(items));//01,02,03
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, rule6.discountFilteredItems(items));//01,02

        Assert.assertTrue(!rule7.check(items)); //dont match

        //#region deprecated api
        BestMatch match = Strategy.bestMatch(Collections.singletonList(rule6), items);
        //Assert.assertEquals(121200 * 6 / -2, match.totalDiscount());//02 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2
        match = Strategy.bestOfOnlyOnceDiscount(Collections.singletonList(rule6), items);
        //Assert.assertEquals(121200 * 6 / -2, match.totalDiscount());//02 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        match = Strategy.bestMatch(Collections.singletonList(rule2), items);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, match.totalDiscount());//01 * 2,02 * 6

        match = Strategy.bestOfOnlyOnceDiscount(Collections.singletonList(rule2), items);
        //Assert.assertEquals(121200  / -2, match.totalDiscount());//02 * 2
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        List<Rule> rules = Arrays.asList(rule2,rule6,rule7);
        match = Strategy.bestMatch(rules, items);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, match.totalDiscount());//matches rule2, gets 4 match groups

        match = Strategy.bestOfOnlyOnceDiscount(rules, items);
        //Assert.assertEquals((121200 * 6) / -2, match.totalDiscount());//matches rule6, gets 1 match group
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2
        //#endregion

        //#region new api
        BestMatch best = Strategy.bestChoice(Collections.singletonList(rule6), items,MatchType.OneRule);
        //Assert.assertEquals(121200 * 6 / -2, best.totalDiscount());//02 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2
        best = Strategy.bestChoice(Collections.singletonList(rule6), items,MatchType.OneTime);
        //Assert.assertEquals(121200 * 6 / -2, best.totalDiscount());//02 * 6
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        best = Strategy.bestChoice(Collections.singletonList(rule2), items,MatchType.OneRule);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, best.totalDiscount());//01 * 2,02 * 6

        best = Strategy.bestChoice(Collections.singletonList(rule2), items, MatchType.OneTime);
        //Assert.assertEquals(121200  / -2, best.totalDiscount());//02 * 1
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        rules = Arrays.asList(rule2,rule6,rule7);
        best = Strategy.bestChoice(rules, items,MatchType.OneRule);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, best.totalDiscount());//matches rule2, gets 7 match groups


        best = Strategy.bestChoice(rules, items, MatchType.OneTime);
        //Assert.assertEquals((121200 * 6) / -2, best.totalDiscount());//matches rule6, gets 1 match group
        Assert.assertEquals((121200 * 6 +10000*2) / -2, match.totalDiscount());//02 * 6 + 01 *2

        best = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        Assert.assertEquals((10000 * 2 + 121200 * 6) / -2, best.totalDiscount());

        Rule rule8 = Interpreter.parseString("[#k01#k02#k03].oneSKUSum(727200) -> -60%").asRule();
        rules=Arrays.asList(rule2,rule6,rule7,rule8);
        best = Strategy.bestChoice(rules, items,MatchType.MultiRule);
        Assert.assertEquals((10000 * 2 + 121200 * 6 +50) * 6 / -10 , best.totalDiscount());

        //#endregion
    }

    @Test
    public void testDiscountPerPrice(){
        List<Item> items = Arrays.asList(
                new ItemImpl("01", "01", "01", "01", "01", "01", 39000),
                new ItemImpl("01", "01", "01", "01", "02", "01", 11000),
                new ItemImpl("02", "02", "02", "02", "03", "02", 11000)
        );
        Rule    rule1 = Interpreter.parseString("$.count(3)->-7000/15000").asRule(),
                rule2 = Interpreter.parseString("$.count(1)->-1000/10000").asRule(),
                rule3 = Interpreter.parseString("$.sum(15000)->-7000/15000").asRule();
        List<Rule> rules = Arrays.asList(rule1,rule2);
        BestMatch bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        System.out.println(bestMatch.totalDiscount());//-28000

        List<Rule> rules2 = Arrays.asList(rule2,rule3);
        bestMatch = Strategy.bestChoice(rules2, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        System.out.println(bestMatch.totalDiscount());//-21000

        System.out.println(rule1.discountFilteredItems(items));//-28000

        System.out.println(rule3.discountFilteredItems(items));//-28000
    }

    @Test
    public void testPackage(){
        List<Item> items = Arrays.asList(
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000)
        );

        Rule    rule1 = Interpreter.parseString("[#k01].count(1) -> y:f01:1500@0").asRule(),
                rule2 = Interpreter.parseString("[#k01].count(2) -> y:f02:4000@0").asRule();

        List<Rule> rules = Arrays.asList(rule1,rule2);
        BestMatch bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        System.out.println(bestMatch.totalDiscount());

        rules = Arrays.asList(rule2,rule1);
        bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        System.out.println(bestMatch.totalDiscount());
    }



    /**
    * 测试满减满折这类按比例的规则
    * */
    @Test
    public void testRatioRule(){
        List<Item> items = Arrays.asList(
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000),
                new ItemImpl("01", "01", "01", "01", "01", "01", 3000)
        );

        Rule    rule1 = Interpreter.parseString("[#k01].count(3) -> -10%@0").asRule(),
                rule2 = Interpreter.parseString("[#k01].sum(9000) -> -200/1000@0").asRule();

        List<Rule> rules = Arrays.asList(rule1);
        BestMatch bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        Assert.assertEquals(-1200,bestMatch.totalDiscount());

        rules = Arrays.asList(rule2,rule1);
        bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
        Assert.assertEquals(-2400,bestMatch.totalDiscount());
    }
}