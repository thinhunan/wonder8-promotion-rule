package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.*;
import com.github.thinhunan.wonder8.promotion.rule.model.builder.SimplexRuleBuilder;
import com.github.thinhunan.wonder8.promotion.rule.model.strategy.BestMatch;
import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RuleTest {

    @Test
    public void testSingleRule(){
        SimplexRule rule = getSingleRule1();
        RuleComponent ruleFromString = Interpreter.parseString(rule.toString());
        Assert.assertEquals(rule.toString(),ruleFromString.toString());

        rule = getSingleRule2();
        ruleFromString = Interpreter.parseString(rule.toString());
        Assert.assertEquals(rule.toRuleString(),ruleFromString.toString());
    }


    @Test
    public void testCompositRule(){
        SimplexRule rule1 = getSingleRule1();
        SimplexRule rule2 = getSingleRule2();

        RuleComponent rules1 = Builder.and()// same as => new AndCompositRule()
                .addRule(rule1)
                .addRule(rule2).build();
        Assert.assertEquals("$.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10)",rules1.toString());

        CompositeRule rules2 = new OrCompositeRule()
                .addRule(rules1)
                .addRule(getSingleRule3());
        Assert.assertEquals("($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|$.sum(100)",
                rules2.toString());

        rules2 = new OrCompositeRule(Arrays.asList(rules1,getSingleRule3()));
        Assert.assertEquals("($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|$.sum(100)",rules2.toString());

        rules2 = (CompositeRule) Builder.or().addRule(rules1).addRule(getSingleRule3()).build();
        Assert.assertEquals("($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|$.sum(100)",rules2.toString());
    }


    @Test
    public void testCompositRule2(){
        List<String> ids = Arrays.asList("CATEGORY1","CATEGORY2");
        RuleComponent rules1 = Builder.and()
                .simplex().addRangeAll().predict(P.COUNT).expected(5).end()
                .simplex().addRanges(R.CATEGORY,ids).predict(P.SUM).expected(10)
                          .end()
                .build();
        Assert.assertEquals("$.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10)",rules1.toString());

        RuleComponent rules2 = Builder.or()
                .addRule(rules1)
                .addRule(getSingleRule3())
                .build();
        Assert.assertEquals("($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|$.sum(100)",rules2.toString());

    }

    private static SimplexRule getSingleRule1(){
        return new SimplexRuleBuilder()
                .addRangeAll()
                .predict(P.COUNT)
                .expected(5).build();
    }

    private static SimplexRule getSingleRule2(){
        return Builder.simplex()
                .addRange(R.CATEGORY,"CATEGORY1")
                .addRange(R.CATEGORY,"CATEGORY2")
                .predict(P.SUM)
                .expected(10).build();

    }

    private static SimplexRule getSingleRule3(){
        return Builder
                .simplex()
                .addRangeAll()
                .predict(P.SUM)
                .expected(100).build();
    }

    @Test
    public void benchmark() {
        List<Item> selectedItems = Arrays.asList( getSelectedItems());
        String c,p,t,d;
        //满折满减是最简单的规则
        c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000)) & $.sum(50000)";
        p = "-100/10000";
        t = "虎年大礼包500";
        d = "条件：（购买c01c02c02这3个分类中至少2个分类,并且总共有3个以上的SPU的多于5种商品并且SKU总价多于100块） 同时 （总价多于500块）每满100优惠1块";
        RuleImpl r = RuleImpl.myBuilder()
                .condition(c)
                .promotion(p)
                .title(t)
                .description(d)
                .build();

        long startTime = System.currentTimeMillis();
        for(int i = 0 ; i < 10000; i++){
            if(r.check(selectedItems)){
                r.discount(selectedItems);
            }
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = (endTime - startTime);
        System.out.println("10k次共计耗时(ms):"+elapsedTime);

    }

    @Test
    public void testParse(){
        String c = "([#cc01#cc02#cc03].countCate(2) & [#cc01#cc02#cc03].countSPU(5) & $.count(5) & $.sum(10000)) | $.sum(50000)";
        RuleComponent r = Interpreter.parseString(c);

        c = "[#cc01#cc02#cc03].countCate_2&[#cc01#cc02#cc03].countSPU_5&$.count_5&$.sum_10000";
        r = Interpreter.parseString(c);
        c = r.toString();
        RuleComponent r2 = Interpreter.parseString(c);
        System.out.println(r.toString());
        System.out.println(r2.toString());

    }

    @Test
    public void testRules() {
        // c01有3个商品70块，其中p01有2张50块，p02有1张20块,
        // 其它都只有1张20块，10个商品210块
        // k01 30块（3000分），其它都是20块

        String c,p,t,d;
        //满折满减是最简单的规则
        c = "$.sum(10000)";
        p = "-100/10000";
        t = "每满100块减1块";
        d = "条件：每满100块减1块";
        testRule(c,p,t,d,true,-200);

        c = "$.sum(10000)";
        p = "-100";
        t = "满100块后减1块";
        d = "条件：满100块后减1块";
        testRule(c,p,t,d,true,-100);

        c = "$.sum(20000)";
        p = "-10%";
        t = "满200块后打9折";
        d = "条件：满200块后打9折";
        testRule(c,p,t,d,true,-2300);

        //一口价
        c = "$.sum(20000)";
        p = "16000";
        t = "一口价160块";
        d = "满200块一口价160";
        testRule(c,p,t,d,true,-7000);


        //下面是一些复杂的规则组合
        c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000)) & $.sum(50000)";
        p = "-100/10000";
        t = "虎年大礼包500";
        d = "条件：（购买c01c02c02这3个分类中至少2个分类,并且总共有3个以上的SPU的多于5种商品并且SKU总价多于100块） 同时 （总价多于500块）每满100优惠1块";
        testRule(c,p,t,d,false,0);

        c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000))|$.sum(50000)";
        p = "-100/10000";
        t = "虎年大礼包500";
        d = "条件：（购买c01c02c02这3个分类中至少2个分类，并且总共有3个以上的SPU的多于5种商品并且SKU总价多于100块） 或者（总价多于500块）每满100优惠1块";
        testRule(c,p,t,d,true,-200);

        c = "([#cc01#cc02#cc03].countCate(2) & [#cc01#cc02#cc03].countSPU(5) & $.count(5) & $.sum(10000)) | $.sum(50000)";
        p = "-100/10000";
        t = "虎年大礼包";
        d = "条件：购买3个分类中至少2个分类的5个以上的SPU的多于5种商品，并且总价多于100块 或者 总价多于500 每满100优惠1块";
        testRule(c,p,t,d,false,0);

        c = "([#cc01#cc02#cc03].countCate(2) & ~.countSPU(5) & $.count(5) & ~.sum(10000)) | $.sum(50000)";
        p = "-100/10000";
        t = "虎年大礼包";
        d = "条件：购买3个分类中至少2个分类的5个以上的SPU的多于5种商品，并且总价多于100块 或者 总价多于500 每满100优惠1块";
        testRule(c,p,t,d,false,0);

        c = "[#cc01].count(4)&$.sum(10000)";
        p = "-100";
        t = "虎年大礼包2";
        d = "条件：购买c01分类中至少4个商品，并且总价多于100块 优惠1块";
        testRule(c,p,t,d,true,-100);

        c = "[#cc01].count(4)|$.sum(10000)";
        p = "-10%";
        t = "虎年大礼包3";
        d = "条件：购买c01分类中至少4个商品，或者总价多于100块 优惠10%";
        testRule(c,p,t,d,true,-2300);

        c = "[#pp01#pp02].count(3)&[#pp01#pp02].sum(9000)";
        p = "-10%";
        t = "虎年大礼包4";
        d = "条件：购买p01,p02两个SPU中至少3个商品，且总价多于90块 优惠10%";
        testRule(c,p,t,d,true,-2300);

        c = "[#pp01#pp02].count(3)";
        p = "-0";
        t = "虎年大礼包4";
        d = "条件：购买p01,p02两个SPU中至少3个商品 优惠0元";
        testRule(c,p,t,d,true,-0);

        List<Item> items = Arrays.asList(
                new Item("FOOD-FRUIT","APPLE","Red Apple",100),
                new Item("FOOD-FRUIT","APPLE","Red Apple",100),
                new Item("FOOD-VEGETABLE","POTATO","Yellow Potato",200)
        );
        Rule ruleFood = Interpreter.parseString("[#cFOOD-FRUIT#cFOOD-VEGETABLE].countCate(2)->-10%").asRule();
        Assert.assertEquals(true,ruleFood.check(items));
        ruleFood = Interpreter.parseString("[#cFOOD-FRUIT#cFOOD-VEGETABLE].countCate(3)->-10%").asRule();
        Assert.assertEquals(false,ruleFood.check(items));
    }

    @Test
    public void testZeroRules() {
        // c01有3个商品70块，其中p01有2张50块，p02有1张20块,
        // 其它都只有1张20块，10个商品210块
        // k01 30块（3000分），其它都是20块

        String c,p,t,d;
        c = "[#pp01#pp02].count(3)";
        p = "-0";
        t = "虎年大礼包4";
        d = "条件：购买p01,p02两个SPU中至少3个商品 优惠0元";
        testRule(c,p,t,d,true,0);
    }

    @Test
    public void testCountSKU(){
        List<Item> items = Arrays.asList(getSelectedItems());
        Rule r = Builder.rule()
                    .simplex().addRangeAll().predict(P.COUNT).expected(11).endRule()
                    .promotion("-100").build();
        Assert.assertEquals(-100,r.discount(items));

        Rule ruleCountSKU = Interpreter.parseString("$.countSKU(10)->-90").asRule();
        Assert.assertEquals(true,ruleCountSKU.check(items));

        Rule ruleCountSKU2 = Interpreter.parseString("$.countSKU(11)->-90").asRule();
        Assert.assertEquals(false,ruleCountSKU2.check(items));
    }

    private static void testRule(String condition, String promotion, String title, String description, boolean expectedValid, int expectedDiscount) {
        RuleImpl r = RuleImpl.myBuilder()
                .condition(condition)
                .promotion(promotion)
                .title(title)
                .description(description)
                .build();
        List<Item> selectedItems = Arrays.asList( getSelectedItems());

        RuleValidateResult result = r.validate(selectedItems);
        System.out.printf("%s (%s)\n %s \n匹配结果：%s,可优惠 %s分钱\n",
                r.getTitle(), r.getCondition().toRuleString(), r.getDescription(),result.isValid(), result.isValid()?r.discount(selectedItems):0);
        Assert.assertEquals(expectedValid,result.isValid());
        if(result.isValid()) {
            Assert.assertEquals(expectedDiscount, r.discount(selectedItems));
        }
        System.out.println(result);
        System.out.println("---------\n");
    }


    // c01有4个商品90块，其中p01有3张70块，p02有1张20块,其它都只有1张20块，11个商品230块
    // k01 30块（3000分），其它都是20块
    private static Item[] getSelectedItems() {
        Item[] selectedItems = new Item[]{
                new ItemImpl("c01","分类01","p01","SPU01","k01","SKU01",2000),
                new ItemImpl("c01","分类01","p01","SPU01","k01","SKU01",2000),
                new ItemImpl("c01","分类01","p01","SPU01","k02","SKU02",3000),
                new ItemImpl("c01","分类01","p02","SPU02","k03","SKU03",2000),
                new ItemImpl("c02","分类02","p03","SPU03","k04","SKU04",2000),
                new ItemImpl("c03","分类03","p04","SPU04","k05","SKU05",2000),
                new ItemImpl("c04","分类04","p05","SPU05","k06","SKU06",2000),
                new ItemImpl("c05","分类05","p06","SPU06","k07","SKU07",2000),
                new ItemImpl("c06","分类06","p07","SPU07","k08","SKU08",2000),
                new ItemImpl("c07","分类07","p08","SPU08","k09","SKU09",2000),
                new ItemImpl("c08","分类08","p09","SPU09","t10","SKU10",2000)
        };
        return selectedItems;
    }

    @Test @Ignore
    public void testSort(){
        Item[] items = getSelectedItems();
        Stream<Item> sorted =  Arrays.stream(items).sorted(Comparator.comparing(t->((Item)t).getCategory()).reversed().thenComparing(t->((Item)t).getPrice()));
        sorted.forEach(t-> System.out.println((Item)t));
    }

    @Test @Ignore
    public void testListDuplicateAdd(){
        List<Item> items =new ArrayList<>(Arrays.asList(getSelectedItems()));
        System.out.println(items.get(1) == items.get(0));
        List<Item> target = new ArrayList<>();
        for (Item t :
                items) {
            if(!target.contains(t)){
                target.add(t);
            }
        }
        System.out.println(target.size());
    }

    @Test
    public void testStringIndexOf(){
        String rule = "[#k01].count(2)&[#k02].count(1) -> 8000";
        System.out.println(rule.indexOf("->"));
        String[] parts = rule.split("->");
        for (String p :
                parts) {
            System.out.println(p);
        }
    }

    @Test
    public void testRule3(){
        Rule rule = Builder.rule()
                .simplex().addRange(R.SKU, "01").predict(P.COUNT).expected(2)
                .and()
                .simplex().addRange(R.SKU, "02").predict(P.COUNT).expected(1).end()
                .endRule()
                .promotion("8000")
                .build();
        String ruleString = rule.toString();
        Rule rule2 = Interpreter.parseString(ruleString).asRule();
        Assert.assertEquals(rule2.toString(),ruleString);

        String conditionString = rule.getCondition().toString();
        RuleComponent condition2 = Interpreter.parseString(conditionString);
        Assert.assertEquals(condition2.toString(),conditionString);

        List<Item> items = Arrays.asList(
                new ItemImpl("p1", "p1", "s1", "s1", "01", "t1", 300),
                new ItemImpl("p1", "p1", "s1", "s1", "01", "t1", 300),
                new ItemImpl("p1", "p1", "s1", "s1", "02", "t2", 300),
                new ItemImpl("p1", "p1", "s1", "s1", "02", "t2", 300)
        );
        Assert.assertEquals(rule.check(items),true  );

        BestMatch matchResult = Strategy.bestMatch(Arrays.asList(rule), items);
        System.out.println(matchResult);



    }

    @Test
    public void promotionWithDecimal(){
        String promotion = "-0.5%";
        int total = 31000;
        int i = (int)(Float.parseFloat(promotion.substring(0,promotion.length()-1))/100 * total);
        int expect = (int)(31000 * -0.005);
        Assert.assertEquals(expect,i);

        total = 310;
        i = (int)(Float.parseFloat(promotion.substring(0,promotion.length()-1))/100 * total);
        expect = (int)(310 * -0.005);
        Assert.assertEquals(expect,i);

        Rule r = Builder.rule()
                .simplex().addRangeAll().predict(P.SUM).expected(10000).endRule()
                .promotion("-0.5%")
                .build();
        List<Item> selectedItems = Arrays.asList(getSelectedItems());
        int expected = (int)(23000 * -0.005);
        int actual = r.discount(selectedItems);
        Assert.assertEquals(expected,actual);

        System.out.println(Math.round(-10.89f));
        System.out.println((int)-10.89f);
    }

    @Test
    public void testFilter() {
        SimplexRule rule1 = Builder.simplex()
                .range("[#cc01#cc02]")
                .predict(P.COUNT).expected(3)
                .build();
        List<Item> items = Arrays.asList(getSelectedItems());
        List<Item> filtered = items.stream().filter(rule1.getFilter()).collect(Collectors.toList());
        Assert.assertEquals(5, filtered.size());

        SimplexRule rule2 = Builder.simplex()
                .range("[#cc03]")
                .predict(P.COUNT).expected(3)
                .build();

        Rule ruleOr = Builder.rule().or().addRule(rule1).addRule(rule2).endRule().build();
        filtered = items.stream().filter(ruleOr.getFilter()).collect(Collectors.toList());
        Assert.assertEquals(6, filtered.size());

        Rule ruleAnd = Builder.rule().and().addRule(rule1).addRule(rule2).endRule().build();
        filtered = items.stream().filter(ruleAnd.getFilter()).collect(Collectors.toList());
        //Assert.assertEquals(0, filtered.size());
        // and 组合也需要把所有子条件的范围都过滤出来
        Assert.assertEquals(6, filtered.size());
    }

    @Test
    public void testThreeDiscountRange(){
      Rule rule1 = Interpreter.parseString("[#k01#k02#k03].count(4)&~.countCate(2) -> -40%").asRule();
      Rule rule2 = Interpreter.parseString("[#k01#k02#k03].count(6)&~.countCate(2) -> -50%").asRule();
      List<Rule> rules = Arrays.asList(rule1,rule2);
        List<Item> items = Helper.getTestItems();
        BestMatch result = Strategy.bestMatch(rules, items);
        //bestMatch的策略是求最低成本下达成最多优惠，所以规则6条达成，则只计算6条
        //no,no,no! 新的实现对按比例打折的会尝试把范围内的更多商品加进来计算比较，所以会把5+2=7个商品算进来
        //Assert.assertEquals((10000*4+121200*2)/2*-1,result.sumDiscount());
        Assert.assertEquals((10000*5+121200*2)/2*-1,result.totalDiscount());
        //rule.discount()在规则达成的情况下，计算整个商品组合，所以算8条，包括最后那条skuId不在规则范围的
        Assert.assertEquals((10000*5+121200*3)/2*-1,result.getMatches().get(0).getRule().discount(items));
        //rule.discountFiltered()在规则达成的情况下，计算商品组合匹配规则范围的商品，所以最后一条不计算优惠
        Assert.assertEquals((121200 * 2 + 10000 * 5) /2*-1,result.getMatches().get(0).getRule().discountFilteredItems(items));

        items.stream()
                .collect(
                    Collectors.groupingBy(Item::getSKU,Collectors.summingInt(Item::getPrice))
                )
                .forEach((id,sum)-> System.out.println(id + " summing: "+ sum ));
        items.stream()
                .collect(Collectors.groupingBy(Item::getSKU,Collectors.summarizingInt(Item::getPrice)))
                .forEach((id,statistics)-> System.out.println(id + "summarizing:" + statistics));
    }
}