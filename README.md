[English](README_EN.md)
### 概述
为了广泛支持营销活动的复杂与灵活，Wonder8.promotion（旺德發🀎.营销）引擎使用专门设计的表达式高度提炼信息，可以轻松表达营销活动与用户选取的商品组合之间匹配范围、要求、折扣方式，可以设定多条营销规则的逻辑联合、分组、优先级，并且支持多种为用户计算最优折扣的策略。

本引擎功能细节较多，建议用以下步骤来学习和应用：
1. 先通过简单需求场景来熟悉API，此时只需要用到表达式(Rule)，表达式解释器(Interpreter)和优惠计算策略(Strategy)，大部分程序员掌握了表达式语法后，会嫌Builder麻烦而直接拼写表达式字符串，所以Builder都不一定要熟悉，比如：
- 定义规则：买两台512G的黑或白色iPhone15，折扣400元，三台折扣700元：
    - [#kiPhone15-black-512g#kiPhone15-white-512g].count(2)->-40000
    - [#kiPhone15-black-512g#kiPhone15-white-512g].count(3)->-70000
- 然后调用Strategy.bestChoice(rules, items, MatchType.MultiRule)即可在用户购买4台iPhone时计算出折扣800元，购买5台时折扣1100元，以及计算出应用折扣后如果还有余出的物品，用户如何拼单获得更多的折扣。
2. 尝试编写复杂的规则组合熟悉分组、商品组合、多种计算策略和计算范围等概念。
3. 尝试为自己的业务场景扩展引擎功能，本引擎有清晰的结构，各部分相互独立，往往能添加10来行代码即扩展新的功能。

#### 功能特性：
* 支持针对用户选定的一批商品，从一堆营销规则中自动应用最大的优惠；
* 可以同时应用多个规则，规则之间可以是与和或的关系，可以限定规则组合的优先级；
* 可以对规则分组，限定先应用一组，再应用另一组；
    * 可以限定必须应用完一组优惠才能计算下一组优惠；
    * 也可以把各组优惠方式交叉对比最优组合；
* 多种规则匹配方式求最佳优惠：
    * 最优的只匹配一次规则；
    * 最优的单规则多次匹配；
    * 最优的多规则多次匹配；
* 可以支持类似于买12瓶水可以合成两箱水（另一个SKU）,而两箱水又可以应用另一种规则；
* 可以计算推荐用户再添加什么商品可以获得下一个优惠；
* 同时提供服务端Java实现和客户端JS实现，便于下放优惠规则后，客户端实时得出优惠结果；
* 基于专门设计的字符串表达式，各种变态组合玩法可以灵活直观表达，并且提供Builder和Interpreter为字符串和结构化对象间转换；
* 代码结构清晰，进行功能扩展和各类规则组合场景扩展比较方便；


### 功能说明
所有想法源自于一个营销折扣的规则可以抽象成三个部分：
1. 规则适用的范围(Range)
    1. 我们暂且将范围表达成三层：类目、SPU、SKU，不同场景可以扩展，由于字符串可以自由串接，一般情况也不需要扩展，比如:大类目-小类目就相当于扩展了一层；
    2. 一个规则可以有适用多个范围，即范围可以是一个组合；
2. 规则的要求(Predict &amp; Validate)
    1. 规则的要求可以抽象出来，主要是：要求有多少个，要求达到多少总价值，要求含有多少种，必须搭售某个商品等；
    2. 计算方式（Predict）可以扩展；
3. 优惠方案（Promotion）
    1. 优惠方案往往是：固定减多少钱，每满多少钱减多少钱，按比例折扣，直接减到一个固定值（一口价）
       所以一条营销规则就是：[range].predict(expectedValue)。

#### 表达式语法
Wonder8.promotion使用表达式来表达和组合营销规则，如表示当一组商品中包括食品-水果、食品-肉类、食品-蔬菜三大类中至少两个时，优惠10%："[#cFOOD-FRUIT#cFOOD-MEAT#cFOOD-VEGETABLE].countCate(2)->-10%"：
1. 一条营销规则由三部分组成，规则适用的范围，规则计算的方法，规则应用的优惠结果：
    1. [range].predict(expectedVaue)是一条规则的格式
2. 用户当前选择了10个物品，但是并不是每一个物品都符合这条规则的范围，则它不应计算在内。所以适用范围是首要设置的：
    1. [#cCate1#cCate2#……]表示法中，#是一个范围对象的表达开始，c是类型（可选c-类目，p-SPU，k-SKU，可以扩展），后面是ID，[]内可以放>=1个对象；
    2. \$表示全部：\$.sum(20000)；
    3. ~表示复用上一条规则的范围：[#ccate01#ccate02#ccate03].countCate(2) &amp; ~.countSPU(5) &amp; \$.countSKU(5) &amp; ~.sum(10000))，意味着在类目cate01,cate02,cate03这个范围内，物品组合需要满足类目涵盖2个，SPU涵盖5个，SKU涵盖5个，总价达到10000。
3. .predict()表示计算的方法，当前支持countCategory()计算范围内含多少个类目，countSPU()计算范围内含多少个SPU，countSKU()计算范围内含多少个SKU，count（）计算多少个物品，oneSKU()计算某种SKU含多少个，sum()计算价格的合计。
4. expectedValue是一个int数字，表示计算结果要>=这个数， 才能通过。
5. 规则可以联合，用&amp;表示并且，用|表示或者，规则可以分组，用()，比如(rule1&amp;rule2&amp;rule3)|rule4，表示，1、2、3都要达成或者4达成，均可通过规则：
    ```javascript
    "([#pp01#pp02#pp03].countCate(2) & \$.countSPU(3) & \$.count(5) & \$.sum(10000)) | \$.sum(50000)"
   ```
6. 每条规则由计算部分和一个规则优惠部分组成，中间用->连接；
7. 优惠部分的语法是：
    1. -1000 表示固定优惠10块钱（所以钱相关的计算单位是分）
    2. -1000/10000 表示每100块钱优惠10块钱
    3. -10% 表示优惠10%，即打9折，添加了小数点支持比如-0.5%表示优惠95.5%
    4. 8000 表示一口价，80块钱
    5. -0表示优惠0元，0表示优惠到0元

#### 规则对象
对应表达式，有一系统的结构化对象：
1. Rule -- 对应一条完整的营销规则，主要属性是condition 表示条件规则，promotion表示优惠规则
    1. 实际使用过程中，因为要对用户提示，显示标签等，所以需要扩展Rule类，提供更多与计算无关的附加属性，参见测试用例中的RuleImpl类。
2. SimplexRule -- 对应一条条件规则，主要属性是range表示条件计算范围，predict表示计算方法，expcteted表示达标的值。
3.  SameRangeRule -- 与前一条条件规则范围相同的规则，用~复用前述规则的范围表达式。
4.  AndCompositeRule -- 表示and逻辑的条件规则组，主要属性是保存子规则集合的components，可以addRule()添加子规则，子规则可以是Simplex/SameRange，也可以是AndComposite/OrComposite。
5.  OrCompositeRule -- 表示or逻辑的条件规则组，其它同AndComposite
6.  Rule的condition可以是Simplex/AndComposite/OrComposite，不能是SameRange,不然SameRange去哪里复用范围规则
7.  Rule/Simplex/SameRange/AndComposite/OrComposite都有对应的builder,通过Builder.rule()/simplex()/and()/or()可以找到builder的快捷入口。见[规则的创建]
8.  条件可以通过Rule.toString()方法和Interprecter.parseString()来实现强类型实例与字符串表达式之间的互相转换。

#### 规则的创建
/model/builder/目录下有一整套builder用于以结构化的方式创建规则，语法清晰。
```java
public class ConditionBuilderTest {
    @Test
    public void testBuildRule(){
        //创建规则有三种方法：
        //一种是Builder.rule().xxx().xxx().build()
        //第二种是new RuleBuiler().xxx().xxx().build()
        //第三种是直接new Rule()，通过contructor和properties来完成设置
        RuleComponent rule1 = Builder.rule()//上下文是Rule
                .simplex().addRangeAll()//注意这里上下文切换到了simplex条件的编写
                .predict(P.SUM).expected(100)
                .end() //通过.end()结束当前对象编写，返回到上一级，也就是Rule
                .endRule()//因为.end()方法返回的是基类，所以需要.backRule()切换回RuleBuilder才能直接调用.promotion()这样特殊的方法，继续编写下去
                .promotion("-10")
                .build();

        System.out.println(rule1.toString());
    }

    @Test
    public void testBuildSimplexRule(){
        /*
          Builder除了能Builder.rule()来开始编排一个完整的营销规则，
          也还有Builder.simplex()/.or()/.and()来开始编排一个单一/或组合/与组合
          但请注意，除.rule()是开始编写一个完整的营销规则，其它方法只是在开始编排规则中的条件部分
          最终.build()出来的一个是Rule，一个是Condition
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
具体用法可以参见test下的ConditionBuilderTest.java和RuleTest.java。

#### 表达式的解析
Interprecter类实现对规则字符串的解释，可以将字符串转化成模型结构，Interprecter.parseString(ruleString)
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

#### 规则是否匹配
Rule.check(items);

#### 规则匹配结果详情
Rule.validate(tickets) -> RuleValidateResult对象
result.valid = result.expected vs. result.actual

#### 优惠计算
Rule.discount(items) -> int. 返回一个负值，即优惠的数，注意一口价的规则，也是目标价格减去当前票价总和得出的优惠掉的值，比如当前所选票价总和是10000，一口价规则是8000，则返回-2000
> result.isValid()?r.discount(selectedTickets):0

##### 四种计算范围
优惠计算有四种计算范围：
假设总共9个物品，01号100块的2个，02号121.2块的6个，03号0.5块的1个，规则是01，02号总共要6个，并且两种都要有：
1. Strategy.bestMatch()的策略是求最低成本下达成最多优惠，如果是比率折扣，它会取高价票，否则取低价票，上例结果是计算1张01和5张02；
    1. 如果规则A的promotion是满折满减(%,/)，则会同时计算将更多票匹配到A是否会带来更多的优惠
2. Strategy.bestOfOnlyOnceDiscount()的策略是只允许使用一次优惠规则，所以计算达成规则所需的最少张数，但是是最高价格的票，上例结果是计算1张01和5张02；
    1. 如果规则A的promotion是满折满减(%,/)，则会同时计算将更多票匹配到A是否会带来更多的优惠
3. Rule.discount()，会对所有票应用优惠，上例结果是计算所有9张票；
4. Rule.discountFilteredItems()，会对规则指定范围内的所有票计算优惠，上例结果是计算2张01和6张02，不含03；
   注意，Strategy支持单规则多次匹配应用和多条规则联合多次应用，更符合“最优”的概念。
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
    //为了做规则推荐的运算，规则本身算折扣的方法里，
    // 并没有判定规则是否已达成，所以调用前需做check()
    if(rule.check(items)){
        //第1种，rule.discountFilteredItems(items)
        //计算的是规则范围内的这部分商品的折扣
        expected = rule.filterItem(items).map(t=>t.price).reduce((p1,p2)=>p1+p2,0) * -0.5;
        actual = rule.discountFilteredItems(items);
        console.log(expected, actual)
        expect(actual).toEqual(expected);

        //第2种，rule.discount(items)
        //计算的是所有商品应用折扣
        expected = items.map(t=>t.price).reduce((p1,p2)=>p1+p2,0) * -0.5;
        actual = rule.discount(items);
        console.log(expected, actual)
        expect(actual).toEqual(expected);
    }

    //第3种，Strategy.bestMath()
    //计算的是用最低成本达成规则匹配所需要的商品
    expected = (items[0].price * 2 + items[2].price *6 ) * -0.5;
    actual = Strategy.bestMatch([rule],items).totalDiscount();
    console.log(expected, actual)
    expect(actual).toEqual(expected);

    //第4种，Strategy.bestOfOnlyOnceDiscount()
    //计算达成规则所需的最少张数，但是是最高价格的商品
    expected = (items[0].price * 2 + items[2].price * 6 ) * -0.5;
    const match = Strategy.bestOfOnlyOnceDiscount([rule],items)
    actual = match.totalDiscount();
    console.log(expected, actual)
    expect(actual).toEqual(expected);
    console.log(match.more);
});
```
#### 策略！
> Strategy.bestMatch(rules,items)/Strategy.bestOfOnlyOnceDiscount(rules, items) 均已废弃，统一使用bestChoice(rules, items, MatchType type, MatchGroup groupSetting)。
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

##### 商品组合
营销活动中存在购买一定数量A物品，就转换成另一个SKU，比如买12瓶水会变成买一箱水，或者买几个SKU合成另一个SKU，比如买一件上装加一件下装变成一个套装，这个时候如果规则引擎能自动完成合并，那么在组合规则时会少去应用层很多代码，所以提供了一个实现这一功能的promotion语法：
> y:{new SKU}:{new SKU price}


以下规则表示VIP A区的1，2排三个相邻座可以合并成一个VIP套票，卖300000
>
> [#zVIP:A:1:1-VIP:A:2:10].adjacentSeat(3)->y:VipPackage3:300000


##### 规则分组
1. 规则可以分组计算，组别为1的规则可以叠加在组别为0的规则应用的结果上，依此类推
2. 各组规则可以按组依次计算、叠加，再取最优，即MatchGroup.SequentialMath
3. 各组规则可以交织在一起计算、叠加，取所有可能的最优，即MatchGroup.CrossedMatch
4. 规则字符串后加@0，表示规则为第0组，@1表示为第1组
```javascript
//以下例子应用了扩展场景-剧院座位，多了一个座位的属性，多张邻座票可以组合成一个联票，形成联票后又可以应用联票的优惠规则

function getSeatedItems () {
    return [
        new Item("01", "01", "02", 10000, "二楼:A:1:1"),
        new Item("01", "01", "02", 10000, "二楼:A:1:3"),
        new Item("01", "01", "02", 10000, "二楼:A:1:2"),
        new Item("01", "01", "02", 10000, "二楼:A:1:5"),
        new Item("01", "01", "02", 10000, "二楼:A:1:4"),
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
    //二楼:A:1:1-5
    //rule1 -2000 rule2 -1800 rule1+rule2 -3800 rule3 -4000
    const rule1 = Interpreter.parseString("[#z二楼:A:1:1-二楼:A:1:5].adjacentSeat(2)->y:APackage2:18000");
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
可以看到MatchGroup.SequntialMatch模式下，先用0组规则尽量找到了2组套票，然后分别为每张套票应用了一个9折的票面优惠；
在MatchGroup.CrossedMatch模式下，通过计算，3张票减4000比两张票组成1个套票再应用9折减3800要更优惠，所以最终是3张票-4000，再加上两张票形成一个套票再9折-3800

### 功能扩展
规则引擎的模块非常清楚，面对不同的任务，可以在相对明确的范围做少量调整，并带来全局的收益：
- Range相关的部分是用来表达规则的匹配范围，如果有这方面的需求，应该只改动这一部分，比如“ID都太长了，希望相同范围的子规则可以复用范围设置，减少规则字符串长度”，则我们增加一种Range:SameRange表达即可；
- Predict谓词是判断动作，新增了一种判断动作，只需要扩展这部分代码即可；
- Rule、RuleComponent是规则本身的强类型表达，除了规则数据的表达，它们还承担：
    - 规则匹配
    - 折扣计算
    - 匹配范围的票的筛选
- Strategy和一套Match类是用来做多个规则和多张票的自动优选的，一般不会动到；
- Interpreter是字符串解析器，基本它的流程不会需要改动，对规则组合的分解，对单一规则的解释；
- Builder是一套强类型链式创建各种规则的辅助体系。

#### 扩展oneSKU谓词
我们看一下如何扩展一个oneSKU谓词来实现至少有一单个SKU必须要达到多少数量的判断。
##### java
- P.java
```java
//predict 判断动词
public enum P {
    
    //... ...

    /**
     * 某种SKU的数量
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
    //@1 有新的玩法只需在这里加谓词和对应的含义
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
用法见单元测试中的strategyTest中的test_oneSKU()

### 场景扩展
不同的场景会有个性化的需求，源码中已经实现了对演示场景（票多了座位这一半键属性），可以参考：
1. Range支持z表示座位
2. Predict增加adjancetSeat判断商品组合中票是不是连座的
3. 用TicketSeatComparator封装根据座位信息判断不同座位位置关系的逻辑

### 代码结构
｜- /java -- 后端实现，暂时不考虑翻译golang/.net语言版本，电商还是java多  
｜- /java/.../Builder.java -- 表达式构造器入口 !important  
｜- /java/.../Interpreter.java -- 表达式字符串解析器 !important  
｜- /java/.../Strategy.java -- 计算方法入口 !important  
｜- /java/.../model -- 规则结构化类体系  
｜- /java/.../model/builder -- 构造器的处理类  
｜- /java/.../model/comparator -- Item比较逻辑  
｜- /java/.../model/strategy -- 规则计算逻辑 !important  
｜- /java/.../model/validate -- 规则验证结果类  
｜- /js -- 前端javascript实现，代码结构与功能与后端完全一致，暂时不考虑翻译成typescript了
### License
[GPL](LICENSE.CN.txt)
