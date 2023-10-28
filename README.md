[中文说明](README_CN.md)
#### overall
As one of the most complex activities in the sales process, Marketing Promotion hasn't a good infrastructure to deal. So, the Wonder8.promotion engine comes.

##### Features:

- Support automatically applying the maximum discount from a bunch of marketing rules for a batch of products selected by the user;
- Can apply multiple rules at the same time. The rules can be AND or OR, and the priority of rule combinations can be set;
- Rules can be grouped. The first group is applied first, followed by the second group;
  - It can be limited that the first group of discounts must be applied before calculating the next group of discounts;
  - It can also compare the optimal combination of each group of discount methods;
- Find the best discount with multiple rule matching methods:
    - The best match only matches the rule once;
    - The best single rule matches multiple times;
    - The best multiple rules match multiple times;
- It can support things like buying 12 bottles of water to combine into two boxes of water (another SKU), and two boxes of water can apply another rule;
- It can calculate what products the user should add to get the next discount;
- Provide both server-side Java implementation and client-side JS implementation, so that the client can get the discount results in real time after the discount rules are released;
- Based on specially designed string expressions, various perverted combination gameplay can be flexibly and intuitively expressed, and Builder and Interpreter are provided to convert between strings and structured objects;
- The code structure is clear, and it is more convenient to expand functions and expand various rule combination scenarios.

### Function description
All ideas come from the fact that a marketing discount rule can be abstracted into three parts:
1. Scope of application of the rule (Range)
   1. For the time being, we will express the scope as three layers: category, SPU, and SKU. Different scenarios can be extended. Since strings can be freely concatenated, it is generally not necessary to extend them in general cases. For example, a large category - a small category is equivalent to extending a layer;
   2. One rule can apply to multiple scopes, that is, the scope can be a combination;
2. Requirements of the rule (Predict & Validate)
   1. The requirements of the rule can be abstracted, mainly: the number required, the total value required, the number of types required, and the required bundling of certain items;
   2. The calculation method (Predict) can be extended;
3. Promotion plan (Promotion)
   1. The promotion plan is often as follows: fixed deduction, deduction of a certain amount for every full amount, proportional discount, and direct deduction to a fixed value (one price)
So a marketing rule is: [range].predict(expectedValue).

#### Expression grammar
Wonder8.promotion uses expressions to express and combine marketing rules. For example, to indicate that a group of items includes at least two of the three categories of food-fruit, food-meat, and food-vegetables, a 10% discount is applied: "[#cFOOD-FRUIT#cFOOD-MEAT#cFOOD-VEGETABLE].countCate(2)->-10%".
1. A marketing rule consists of three parts: the scope of the rule, the method of calculating the rule, and the discount results applied by the rule:
    1. [range].predict(expectedVaue) is the format of a rule
2. If the user currently selects 10 items, but not all of them meet the scope of this rule, then it should not be counted. Therefore, the scope is the first setting:
   1. In the [#cCate1#cCate2#……] notation, # is the beginning of the expression of a scope object, c is the type (optional c-category, p-SPU, k-SKU, can be extended), followed by the ID, [] can contain >=1 objects;
   2. \$ represents all: \$.sum(20000); 
   3. ~ represents the reuse of the scope of the previous rule: [#ccate01#ccate02#ccate03].countCate(2) & ~.countSPU(5) & \$.countSKU(5) & ~.sum(10000)) means that in the category cate01, cate02, cate03, the item combination needs to meet the following requirements: 2 categories, 5 SPUs, 5 SKUs, and a total price of 10000. 
3. .predict() represents the calculation method. Currently, it supports countCategory() to calculate the number of categories in the scope, countSPU() to calculate the number of SPUs in the scope, countSKU() to calculate the number of SKUs in the scope, count() to calculate the number of items, oneSKU() to calculate the number of a certain SKU, and sum() to calculate the total price. 
4. expectedValue is an int number, which indicates that the calculation result must be >= this number to pass. 
5. Rules can be combined, using & to represent AND, | to represent OR, and rules can be grouped using (). For example, (rule1&rule2&rule3)|rule4, which means that 1, 2, and 3 must be achieved, or 4 is achieved, can pass the rule: ([#pp01#pp02#pp03].countCate(2) &amp; \$.countSPU(3) &amp; \$.count(5) &amp; \$.sum(10000)) | \$.sum(50000).
6. Each rule consists of a calculation part and a rule discount part, connected by ->;
7. The syntax of the discount part is:
   1. -1000 means a fixed discount of 10 yuan (so the calculation unit for money-related calculations is cents)
   2. -1000/10000 means a discount of 10 yuan for every 100 yuan
   3. -10% means a 10% discount, that is, a 90% discount. The addition of decimal points supports discounts such as -0.5%, which means a discount of 95.5%
   4. 8000 means a fixed price of 80 yuan
   5. -0 means a discount of 0 yuan, 0 means a discount to 0 yuan

#### Rule objects
Corresponding to the expression, there is a system of structured objects:
1. Rule -- corresponds to a complete marketing rule, the main properties are condition indicating the condition rule, and promotion indicating the discount rule
    1. In actual use, because it is necessary to prompt users, display labels, etc., it is necessary to extend the Rule class to provide more additional attributes that are not related to calculation. See the RuleImpl class in the test cases.
2. SimplexRule -- corresponds to a condition rule, the main properties are range indicating the scope of condition calculation, predict indicating the calculation method, and expcteed indicating the value to be achieved.
3. SameRangeRule -- a rule with the same scope as the previous condition rule, using ~ to reuse the range expression of the previous rule.
4. AndCompositeRule -- represents a group of condition rules with and logic. The main property is the components of the sub-rule set, which can be added by addRule(). The sub-rules can be Simplex/SameRange, or AndComposite/OrComposite.
5. OrCompositeRule -- represents a group of condition rules with or logic. Others are the same as AndComposite.
6. The condition of Rule can be Simplex/AndComposite/OrComposite, but not SameRange, otherwise where does SameRange reuse the range rule?
7. Rule/Simplex/SameRange/AndComposite/OrComposite all have corresponding builders. You can find the shortcut to the builder through Builder.rule()/simplex()/and()/or(). See [Rule creation].
8. The condition can be converted between strong typed instances and string expressions through the Rule.toString() method and Interprecter.parseString().

#### Rule creation
The /model/builder/ directory contains a set of builders for creating rules in a structured way, with clear syntax.
```java
public class ConditionBuilderTest {
    @Test
    public void testBuildRule(){
        /*
          There are three ways to create rules:
          1. Builder.rule().xxx().xxx().build();
          2. new RuleBuilder().xxx().xxx().build();
          3. Directly new Rule(), and complete the configuration through the constructor and properties.
         */
        RuleComponent rule1 = Builder.rule()//context is [Rule]
                .simplex().addRangeAll()//context change to simplexRuleBuilder
                .predict(P.SUM).expected(100)
                .end() //end simplexRuleBuilder, context back to RuleBuilder
                .endRule()//end RuleBuilder, context back to Builder, then can continue to build promotion part
                .promotion("-10")
                .build();

        System.out.println(rule1.toString());
    }

    @Test
    public void testBuildSimplexRule(){
        /*
           In addition to Builder.rule() to start orchestrating a complete marketing rule, Builder also has Builder.simplex()/.or()/.and() to start orchestrating a single/OR combination/AND combination. But please note that in addition to .rule() which starts writing a complete marketing rule, other methods only start orchestrating the condition part of the rule. What is eventually .build() is one Rule and one Condition.
        */
        SimplexRule rule1 = Builder.simplex() // same as => new SimplexRuleBuilder()
                .addRangeAll()
                .predict(P.SUM).expected(100)
                .build();
        System.out.println(rule1.toString());
    }

    @Test
    public void testParseRange(){
        SimplexRule rule1 = new SimplexRuleBuilder()
                .range("[#pSPU1#pSPU2]")
                .predict(P.SUM).expected(100)
                .build();
        System.out.println(rule1.toString());
    }


    @Test
    public void testBuildOrCompositeRule(){
        RuleComponent or =  new OrCompositeRuleBuilder()
                .simplex().addRangeAll().predict(P.SUM).expected(100).end()
                .simplex().addRange(R.SPU,"SPUID1").predict(P.COUNT).expected(5).end()
                .sameRange().predict(P.COUNT_SPU).expected(2).end()
                .build();
        System.out.println(or);
    }

    @Test
    public void testBuildAndCompositeRule(){
        RuleComponent and = new AndCompositeRuleBuilder()
                .simplex().addRanges(R.SPU, Arrays.asList("SPUID1","SPUID2")).predict(P.COUNT).expected(5).end()
                .simplex().addRangeAll().predict(P.COUNT_SPU).expected(5).end()
                .sameRange().predict(P.COUNT).expected(10).end()
                .build();
        System.out.println(and);
    }
}
```
For specific usage, please refer to ConditionBuilderTest.java and RuleTest.java under test.

#### Expression Parsing
The Interpreter class implements interpretation of rule strings, which can convert strings into model structures, Interprecter.parseString(ruleString)
```java
public class InterpreterTest {

    @Test
    public void validateCondition() {
        String ruleStr = "($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10)&~.countSPU(2))|$.sum(100)";
        assertTrue(Interpreter.validateCondition(ruleStr));
    }

    @Test
    public void parseString() {
        String ruleStr = "($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10)&~.countSPU(2))|$.sum(100)";
        RuleComponent rule = Interpreter.parseString(ruleStr);
        System.out.println(rule);
        assertEquals(ruleStr,rule.toString());

        ruleStr = "($.count(5)|([#cCATEGORY1#cCATEGORY2].sum(10)&~.countSPU(2)))|$.sum(100)";
        rule = Interpreter.parseString(ruleStr);
        System.out.println(rule);
        assertEquals(ruleStr,rule.toString());

        ruleStr = "(($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|([#cCATEGORY1#cCATEGORY2].sum(10)&~.countSPU(2)))|$.sum(100)";
        rule = Interpreter.parseString(ruleStr);
        System.out.println(rule);
        assertEquals(ruleStr,rule.toString());

        ruleStr = "(($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|[#cCATEGORY1#cCATEGORY2].sum(10))|$.sum(100)";
        rule = Interpreter.parseString(ruleStr);
        System.out.println(rule);
        assertEquals(ruleStr,rule.toString());

        ruleStr = "(($.count(5)&[#cCATEGORY1#cCATEGORY2].sum(10))|[#cCATEGORY1#cCATEGORY2].sum(10))|($.sum(100)&~.countCate(2))";
        rule = Interpreter.parseString(ruleStr);
        System.out.println(rule);
        assertEquals(ruleStr,rule.toString());
    }

    @Test
    public void foldRuleString(){
        String rule = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|[#c01#c02#c03].count(10)&[#c01].sum(10)";
        String expected = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|~.count(10)&[#c01].sum(10)";
        String actual = Interpreter.foldRuleString(rule);
        assertEquals(expected,actual);

        String rule2 = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
        String expected2 = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
        String actual2 = Interpreter.foldRuleString(rule2);
        assertEquals(expected2,actual2);

    }

    @Test
    public void unfoldRuleString(){
        String rule = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|~.count(10)&[#c01].sum(10)";
        String expected = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|[#c01#c02#c03].count(10)&[#c01].sum(10)";
        String actual = Interpreter.unfoldRuleString(rule);
        assertEquals(expected,actual);

        String expected2 = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
        String rule2 = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
        String actual2 = Interpreter.unfoldRuleString(rule2);
        assertEquals(expected2,actual2);
    }
}
```

#### Rule Matching
Rule.check(items);

#### Rule Matching Result
Rule.validate(tickets) -> RuleValidateResult object
result.valid = result.expected vs. result.actual

#### Discount Calculation
Rule.discount(items) -> int. It returns a negative value, which is the discount amount. Note that for the all-in price rules, the discount amount is also calculated by subtracting the current total ticket price from the target price. For example, if the current total selected ticket price is 10,000, and the all-in price rule is 8,000, then it returns -2,000.
> result.isValid()?r.discount(selectedTickets):0

##### Four discount calculation scopes
There are four discount calculation scopes:
Assume there are a total of 9 items, 2 of item 01 priced at 100, 6 of item 02 priced at 121.2, 1 of item 03 priced at 0.5. The rule requires a total of 6 for item 01 and 02, and both must have:
1. The strategy of Strategy.bestMatch() is to achieve the most discounts under the lowest cost. If the promotion is a percentage or amount discount, it will take the higher price tickets, otherwise the lower price ones. In the example above, the result is 1 ticket of 01 and 5 of 02;
    1. If rule A's promotion is a percentage or amount discount, it will also calculate whether matching more tickets to A will bring more discounts.
2. The strategy of Strategy.bestOfOnlyOnceDiscount() is to only allow using a discount rule once, so it calculates the minimum number of tickets needed to meet the rule requirement, but selects the highest price tickets. In the example above, the result is 1 ticket of 01 and 5 of 02;
   1. If rule A's promotion is a percentage or amount discount, it will also calculate whether matching more tickets to A will bring more discounts.
3. Rule.discount() will apply the discount to all tickets. In the example above, the result is all 9 tickets.
4. Rule.discountFilteredItems() will calculate discounts for all tickets within the scope specified in the rule. In the example above, the result is 2 tickets of 01 and 6 of 02, excluding 03.
Note that the Strategy supports multiple matchings and applications of a single rule and multiple rules combined. This is more in line with the concept of "optimal".
```javascript
test("4 discounting algorithm", () => {

    const ruleString = "[#k02#k01].count(6)&~.countCate(2) -> -50%";
    const items = [
        { category: "01", SPU: "01", SKU: "01",price: 10000 },
        { category: "01", SPU: "01", SKU: "01",price: 10000 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "02",price: 121200 },
        { category: "02", SPU: "02", SKU: "03",price: 50 },
    ];

    const rule = Interpreter.parseString(ruleString);
    let expected = 0, actual = 0;
    // In order to perform calculations for rule recommendations, in the rule's own discount calculation method,
    // there is no judgment on whether the rule has been met, so check() needs to be called before invoking.
    if(rule.check(items)){
        // 1st approach, rule.discountFilteredItems(items)
        // Calculates the discount for the products within the rule's scope
        expected = rule.filterItem(items).map(t=>t.price).reduce((p1,p2)=>p1+p2,0) * -0.5;
        actual = rule.discountFilteredItems(items);
        console.log(expected, actual)
        expect(actual).toEqual(expected);

        // 2nd approach, rule.discount(items)
        // Calculates the discount applied to all products
        expected = items.map(t=>t.price).reduce((p1,p2)=>p1+p2,0) * -0.5;
        actual = rule.discount(items);
        console.log(expected, actual)
        expect(actual).toEqual(expected);
    }

    // 3rd approach, Strategy.bestMath()
    // Calculates the products needed to meet the rule matching at the lowest cost
    expected = (items[0].price * 2 + items[2].price *6 ) * -0.5;
    actual = Strategy.bestMatch([rule],items).totalDiscount();
    console.log(expected, actual)
    expect(actual).toEqual(expected);

    // 4th approach, Strategy.bestOfOnlyOnceDiscount()
    // Calculates the minimum number of products needed to meet the rule, but selects the highest priced products
    expected = (items[0].price * 2 + items[2].price * 6 ) * -0.5;
    const match = Strategy.bestOfOnlyOnceDiscount([rule],items)
    actual = match.totalDiscount();
    console.log(expected, actual)
    expect(actual).toEqual(expected);
    console.log(match.more);
});
```
#### Strategy！
> Strategy.bestMatch(rules,items)/Strategy.bestOfOnlyOnceDiscount(rules, items) are deprecated, use bestChoice(rules, items, MatchType type, MatchGroup groupSetting)。
```java
public static BestMatch bestChoice(List<Rule> rules, List<Item> items, MatchType type, MatchGroup groupSetting) {
    //... ...
}
```
```javascript
test('bestMatch',()=> {
    //#region prepare
    let r1 = Builder.rule().simplex()
        .range("[#cc01]")
        .predict(P.COUNT)
        .expected(2)
        .endRule()
        .promotion("-200")
        .build();
    let r2 = Builder.rule().simplex()
        .addRange(R.CATEGORY, "c01")
        .predict(P.COUNT)
        .expected(3)
        .endRule()
        .promotion("-300")
        .build();
    let r3 = Builder.rule().simplex()
        .addRangeAll()
        .predict(P.COUNT)
        .expected(6)
        .endRule()
        .promotion("-10%")
        .build();

    let items = _getSelectedItems();
    let rules = [r1, r2];
    //#endregion
    let bestMatch = Strategy.bestMatch(rules, items);
    expect(bestMatch.matches.length).toEqual(2);
    expect(bestMatch.matches[0].rule).toEqual(r1);
    let bestMatch1 = Strategy.bestChoice(rules,items,MatchType.OneRule);
    expect(bestMatch.matches[0].rule).toEqual(bestMatch1.matches[0].rule);
    expect(bestMatch.totalDiscount()).toEqual(bestMatch1.totalDiscount());

    let bestOfOnce = Strategy.bestOfOnlyOnceDiscount(rules, items);
    bestMatch1 = Strategy.bestChoice(rules,items,MatchType.OneTime);
    expect(bestOfOnce.matches[0].rule).toEqual(bestMatch1.matches[0].rule);
    expect(bestOfOnce.totalDiscount()).toEqual(bestMatch1.totalDiscount());

    // 5 items matched
    items.push(new Item("c01", "p02", "k03", 4000));
    let bestOfMulti = Strategy.bestChoice(rules, items, MatchType.MultiRule);
    expect(2).toEqual(bestOfMulti.matches.length);
    expect(5).toEqual(bestOfMulti.chosen().length);
    expect(-500).toEqual(bestOfMulti.totalDiscount());

    // 6 items matched
    items.push(new Item("c01", "p02", "k03", 4000));
    bestOfMulti = Strategy.bestChoice(rules,items,MatchType.MultiRule);
    expect(6).toEqual(bestOfMulti.chosen().length);
    expect(-600).toEqual(bestOfMulti.totalDiscount());

    // 7 items matched
    items.push(new Item("c01", "p02", "k03", 4000));
    bestOfMulti = Strategy.bestChoice(rules,items,MatchType.MultiRule);
    expect(3).toEqual(bestOfMulti.matches.length);
    expect(7).toEqual(bestOfMulti.chosen().length);
    expect(-700).toEqual(bestOfMulti.totalDiscount());

    // 7 items matched
    const r4 = Builder.rule().simplex().addRange(R.SPU,"p02")
        .predict(P.COUNT).expected(4).endRule()
        .promotion("-2000").build();
    rules = [r1,r2,r3,r4];
    bestOfMulti = Strategy.bestChoice(rules,items,MatchType.MultiRule);
    //expect(3).toEqual(bestOfMulti.matches.length);
    expect(14).toEqual(bestOfMulti.chosen().length);
    expect(-400-300-2000-500-600-700-800-900-200-300).toEqual(bestOfMulti.totalDiscount());

    r3.promotion = "-100";
    bestOfMulti = Strategy.bestChoice(rules,items,MatchType.MultiRule);
    expect(13).toEqual(bestOfMulti.chosen().length);
    expect(-2400).toEqual(bestOfMulti.totalDiscount());
});
```

##### Item Bundling
In marketing campaigns, there is a scenario where purchasing a certain quantity of item A converts it into another SKU. For example, buying 12 bottles of water converts to buying 1 case of water, or buying several SKUs combines into another SKU, such as buying a top plus a bottom converts to buying a set. In this case, if the rule engine can automatically complete the bundling, it will save the application layer a lot of code when creating combination rules. Therefore, a promotion syntax is provided to implement this functionality:
> y:{new SKU}:{new SKU price}

The following rule indicates that three adjacent seats in rows 1 and 2 of VIP zone A can be combined into a VIP package ticket sold at 300,000
>
> [#zVIP:A:1:1-VIP:A:2:10].adjacentSeat(3)->y:VipPackage3:300000

##### Rule Grouping
1. Rules can be calculated in groups. Rules in group 1 can be stacked on top of the results of rules applied in group 0, and so on.
2. Rules in each group can be calculated and stacked sequentially, then the optimal result is taken, which is MatchGroup.SequentialMath.
3. Rules in each group can be intertwined and calculated together, taking all possible optimal results, which is MatchGroup.CrossedMatch.
4. Adding @0 after a rule string indicates the rule is in group 0, @1 indicates group 1.
```javascript
//以下例子应用了扩展场景-剧院座位，多了一个座位的属性，多张邻座票可以组合成一个联票，形成联票后又可以应用联票的优惠规则

function getSeatedItems () {
    return [
        new Item("01", "01", "02", 10000, "Floor2:A:1:1"),
        new Item("01", "01", "02", 10000, "Floor2:A:1:3"),
        new Item("01", "01", "02", 10000, "Floor2:A:1:2"),
        new Item("01", "01", "02", 10000, "Floor2:A:1:5"),
        new Item("01", "01", "02", 10000, "Floor2:A:1:4"),
        new Item("02", "02", "03", 121200, "VIP:A:1:4"),
        new Item("02", "02", "03", 121200, "VIP:A:1:2"),
        new Item("02", "02", "03", 121200, "VIP:A:1:3"),
        new Item("02", "02", "03", 121200, ''),
        new Item("02", "02", "03", 121200, "")];
}

test('testPackage',()=>{
    let testItems = getSeatedItems();
    const rule1 = Interpreter.parseString("[#zVIP:A:1:1-VIP:A:2:10].adjacentSeat(3)->y:VipPackage3:300000");
    rule1.group = 0;
    const rule2 = Interpreter.parseString("[#kVipPackage3].count(1)->-10%");
    rule2.group = 1;
    const bestMatch1 = Strategy.bestChoice([rule1],testItems,MatchType.MultiRule,MatchGroup.CrossedMatch);
    expect(300000-121200*3 ).toEqual(bestMatch1.totalDiscount());
    const bestMatch2 = Strategy.bestChoice([rule1,rule2], testItems, MatchType.MultiRule, MatchGroup.CrossedMatch);
    expect((300000-121200*3) - 30000).toEqual( bestMatch2.totalDiscount());
});

test('testMatchGroup',()=>{
    let seatedItems = getSeatedItems();
    //Floor2:A:1:1-5
    //rule1 -2000 rule2 -1800 rule1+rule2 -3800 rule3 -4000
    const rule1 = Interpreter.parseString("[#zFloor2:A:1:1-Floor2:A:1:5].adjacentSeat(2)->y:APackage2:18000");
    rule1.group=0;
    const rule2 = Interpreter.parseString("[#kAPackage2].count(1)->-10%@1");
    const rule3 = Interpreter.parseString("[#k02].count(3)->-4000@1");
    const rules = [rule1,rule2,rule3];

    const crossedGroupMatch = Strategy.bestChoice(rules,seatedItems,
        MatchType.MultiRule,MatchGroup.CrossedMatch);
    expect(crossedGroupMatch.totalDiscount()).toEqual(-3800 -4000);

    const sequentialGroupMatch = Strategy.bestChoice(rules,seatedItems,
        MatchType.MultiRule,MatchGroup.SequentialMatch);
    expect(sequentialGroupMatch.totalDiscount()).toEqual (-3800*2);
    console.log(rule1.toString());
    console.log(rule2.toRuleString());
    console.log(rule3.toString());
});

```
You can see under the MatchGroup.SequentialMatch mode, it first tried to find 2 package tickets using the rules in group 0, then applied a 90% ticket discount respectively to each package ticket;
Under the MatchGroup.CrossedMatch mode, through calculation, 3 tickets minus 4,000 is more cost effective than 2 tickets making up 1 package ticket then applying 90% discount minus 3,800. Therefore, the final result is 3 tickets minus 4,000, plus 2 tickets making up 1 package ticket then 90% discount minus 3,800.

### Extensibility
The modules of the rule engine are very clear. Facing different tasks, small adjustments can be made within a relatively clear scope and bring global benefits:
- The Range related sections are used to express the matching scope of rules. If there are needs in this aspect, only this part should be changed, for example "The IDs are too long, hope sub-rules with the same scope can reuse the range configuration to reduce rule string length", then we can simply add a new type of Range called SameRange.
- Predict predicates are judgement actions. Adding a new judgement action only requires expanding this part of the code.
- Rule and RuleComponent are the strongly typed expressions of the rules themselves. In addition to expressing rule data, they also undertake:
    - Rule matching
    - Discount calculation
    - Filtering tickets within matching scope
- Strategy and the Match classes are used to automatically optimize among multiple rules and tickets, generally won't need to be changed.
- Interpreter is the string parser. Its workflow generally won't need to change, including breaking down rule combinations and interpreting individual rules.
- Builder is a set of fluent chained creation of various rules and components to assist development.

#### Extending the oneSKU Predicate
Let's look at how to extend a oneSKU predicate to implement the judgment that at least one single SKU must reach a certain quantity.
##### java
- P.java
```java
//predict 
public enum P {
    
    //... ...

    /**
     * Number of a SKU
     */
    ONE_SKU;

    @Override
    public String toString() {
        switch (this){
            //... ...
            case ONE_SKU:return "oneSKU";
        }
    }

    public static P parseString(String s){
        switch (s){
            //... ...
            case "oneSKU": return P.ONE_SKU;
        }
    }
}
```
- validator.java
```java
public class Validator {
    private static HashMap<P, Function<Stream<Ticket>, Integer>> validators
            = new HashMap<P, Function<Stream<Ticket>, Integer>>(){
        {
            // ... ...
            put(P.ONE_SKU,(items) -> {
                return items.collect(
                            Collectors.groupingBy(
                                    t->t.getSKU(),
                                    Collectors.counting()))
                        .values().stream()
                        .max(Long::compare)
                        .orElse(0L).intValue();
            });
        }
    };
```
##### javascript
- enums.js
```javascript
const P = Object.freeze({
    //... ...
    ONE_SKU: {
        name: "oneSKU",
        handler: function(items){
            if(items.length < 1){
                return 0;
            }
            let map = new Map();
            for (const item of items) {
                let count = map.get(item.SKU);
                if(count){
                    map.set(item.SKU,count + 1);
                }
                else{
                    map.set(item.SKU,1);
                }
            }
            return [...map.values()].sort().reverse()[0];
        },
        toString: function (){
            return this.name;
        }
    },
    parseString: function(s){
        switch (s){
            //... ...
            case this.ONE_SKU.name:
                return this.ONE_SKU;
            //... ...
        }
    }
});
//... ...
```
See usage in unit test strategyTest's test_oneSKU().

### Scenario Extension
Different scenarios will have personalized needs. The source code has already implemented the demo scenario (ticket has the key attribute of seat), you can refer to:
1. Range supports z to represent seat
2. Predict adds adjancetSeat to judge if the tickets are adjacent seats in an item combination
3. Use TicketSeatComparator to encapsulate logic to determine seat location relationships based on seat information

### Code structure
｜- /java -- backend java implementation

｜- /java/.../Builder.java -- fluent chained creation of various rules and components

｜- /java/.../Interpreter.java -- string parser, parse rule string into model structure

｜- /java/.../Strategy.java -- automatic calculation among multiple rules and items

｜- /java/.../model -- model classes

｜- /java/.../model/builder -- builders for creating rules in a structured way

｜- /java/.../model/comparator -- comparators for sorting items

｜- /java/.../model/strategy -- automatic calculation among multiple rules and items

｜- /java/.../model/validate -- results for checking if items meet the rule

｜- /js -- frontend javascript implementation, structures and functions are similar to java
### License
[GPL](LICENSE.txt)


