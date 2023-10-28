import Item from "../model/./Item";
import {Builder} from "../builder";
import { MatchType, P, R } from '../model/enums'
import Strategy from "../strategy";
import Interpreter from "../Interpreter";
import MatchGroup from '../model/enums/MatchGroup'

function _getSelectedItems() {
    return [
        new Item("c01", "p01", "k01", 2000),
        new Item("c01", "p01", "k01", 2000),
        new Item("c01", "p01", "k02", 3000),
        new Item("c01", "p02", "k03", 4000),
        new Item("c02", "p03", "k04", 5000),
        new Item("c03", "p04", "k05", 6000),
        new Item("c04", "p05", "k06", 7000),
        new Item("c05", "p06", "k07", 8000),
        new Item("c06", "p07", "k08", 9000),
        new Item("c07", "p08", "k09", 2000),
        new Item("c08", "p09", "t10", 3000)
    ];
}

function getRandomInt(min,max) {
    if(max<min){
        max = min +1;
    }
    return min + Math.floor(Math.random() * max);
}

function _getRandomPriceItems(number=11) {
    let items = new Array(0);
    for (let i = 0; i < number; i++) {
        items.push(new Item("c0"+getRandomInt(1,number-5),
            "p0"+getRandomInt(1,number-4),
            "k0"+getRandomInt(1,number-3),
            getRandomInt(1, 9)*1000));
    }
    return items;
}

function _logMatchResult( r) {
    console.log(JSON.stringify(r,null,2));
}

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

test('testSum',()=> {
    let r1 = Builder.rule()
        .simplex()
        .addRange(R.CATEGORY, "c01")
        .predict(P.SUM)
        .expected(8000)
        .endRule()
        .promotion("-900")
        .build();
    let selectedItems  = _getSelectedItems();
    let bestMatch = Strategy.bestMatch([r1], selectedItems);
    expect(bestMatch.matches.length).toEqual(1);
    //选出20，20，30，40中的20,20,40
    expect(bestMatch.totalPrice()).toEqual(8000);
    _logMatchResult(bestMatch);
    let r2 = Builder.rule()
        .simplex()
        .addRangeAll()
        .predict(P.SUM)
        .expected(8000)
        .endRule()
        .promotion("-800")
        .build();
    bestMatch = Strategy.bestMatch([r1, r2], selectedItems);
    expect(bestMatch.matches[0].rule).toEqual(r2);
    _logMatchResult(bestMatch);//选出的是$.sum(8000),且有6组
    let bestOfOnce = Strategy.bestOfOnlyOnceDiscount([r1, r2], selectedItems);
    console.log("best of only once discount:");
    _logMatchResult(bestOfOnce);//选出的是[#cc01].sum(8000),因为只算一组的话，这一条规则优惠90，比$这一条多10
});

test('testSumOfRandomItems',()=> {
    let r1 = Builder.rule()
        .simplex()
        .addRange(R.CATEGORY, "c01")
        .predict(P.SUM)
        .expected(8000)
        .endRule()
        .promotion("-800")
        .build();
    let selectedItems = _getRandomPriceItems(50);
    let bestMatch = Strategy.bestMatch([r1], selectedItems);
    //_logMatchResult(bestMatch);
    let r2 = Builder.rule()
        .simplex()
        .addRangeAll()
        .predict(P.SUM)
        .expected(8000)
        .endRule()
        .promotion("-800")
        .build();
    bestMatch = Strategy.bestMatch([r1, r2], selectedItems);
    _logMatchResult(bestMatch);
});

test('testComposite',()=> {
    let and = Builder.rule()
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
    console.log(and);
    let items = _getSelectedItems();
    let rules = [and];
    let bestMatch = Strategy.bestMatch(rules, items);
    _logMatchResult(bestMatch);
    expect(bestMatch.matches.length).toEqual(1);
    expect(bestMatch.chosen().length).toEqual(3);

    let or = Builder
        .rule()
        .or()
        .addRule(and.condition)
        .simplex()
        .addRangeAll()
        .predict(P.SUM)
        .expected(8000)
        .end()
        .end()
        .endRule()
        .promotion("-800")
        .build();
    console.log(or);
    rules = [or];
    bestMatch = Strategy.bestMatch(rules, items);
    _logMatchResult(bestMatch);
    expect(bestMatch.matches.length).toEqual(6);
    expect(bestMatch.chosen().length).toEqual(11);
});

test("ronghui", ()=>{
    const rule = Builder.rule().and()
        .simplex().addRange(R.SKU, "62188d011fcf7b610bf6e7be").predict(P.COUNT).expected(2).end()
        .simplex().addRange(R.SKU, "6218e8571fcf7b316a60da44").predict(P.COUNT).expected(1).end()
        .endRule()
        .promotion("8000")
        .build();
    const ruleString = rule.toString();
    const rule2 = Interpreter.parseString(ruleString);
    expect(rule2.toString()).toEqual(ruleString);

    const conditionString = rule.condition.toString();
    const condition2 = Interpreter.parseString(conditionString);
    expect(condition2.toString()).toEqual(conditionString);

    let items = [
        new Item("p1", "s1", "62188d011fcf7b610bf6e7be", 300),
        new Item("p1", "s1", "62188d011fcf7b610bf6e7be", 300),
        new Item("p1", "s1", "6218e8571fcf7b316a60da44", 300),
        new Item("p1", "s1", "6218e8571fcf7b316a60da44", 300)
    ];
    expect(rule.check(items)).toBeTruthy();

    const matchResult = Strategy.bestMatch([rule], items);
    _logMatchResult(matchResult);
});

test("best",()=>{
    const ruleString = "[#kk01].count(2)&[#kk02].count(3) -> 1000";
    const rule = Interpreter.parseString(ruleString);
    const k01 = {category: 'c01', SPU: 'p01', SKU: 'k01', price: 100000};
    const k02 = {category: 'c01', SPU: 'p02', SKU: 'k02', price: 200000};
    const k03 = {category: 'c01', SPU: 'p01', SKU: 'k01', price: 100000};
    const k04 = {category: 'c01', SPU: 'p02', SKU: 'k02', price: 200000};
    const k05 = {category: 'c01', SPU: 'p02', SKU: 'k02', price: 200000};
    let match = Strategy.bestMatch([rule],[k01,k02,k03,k04,k05]);
    //console.log(match);
    match = Strategy.suggestion([rule],[k01,k02,k03,k04]);
    console.log(match);
});

test("test discount per xxx", ()=>{
    const rule = Builder.rule()
        .simplex().addRangeAll().predict(P.SUM).expected(10000)
        .endRule().promotion("-1000/10000").build();
    const itemArray = [new Item("c01","p01","k01",15000)];
    const result = Strategy.bestMatch([rule],itemArray);
    expect(result.totalDiscount()).toEqual(-1000);
});

test("TEST DISCOUNT", () => {
    const rule = ["[#k62400cec34fa961d487cbf26].count(1)&[#k62400cec34fa961d487cbf2a].count(1)&[#k62400e0934fa961d487cc209].count(1)&[#k62400e0934fa961d487cc20f].count(1) -> 0"];
    const items = [
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc20f", "price": 199 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc20f", "price": 199 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc209", "price": 100 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc209", "price": 100 },

        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf26", "price": 100 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf26", "price": 100 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf2a", "price": 199 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf2a", "price": 199 }];
    const result = Strategy.bestMatch(rule.map(r => Interpreter.parseString(r)), items)
    console.log(result.totalDiscount());
});

test("TEST DISCOUNT", () => {
    const rule = [
        "[#k623b16105f45b972c03c93bc].count(1)&[#k623b16105f45b972c03c93c5].count(1)&[#k623b16105f45b972c03c93bf].count(1)&[#k623b16105f45b972c03c93c9].count(1) -> 11100",
    ];
    const items = [
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93bf", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93bc", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93c5", "price": 16600 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93c9", "price": 16600 },

        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93bf", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93bc", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93c5", "price": 16600 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93c9", "price": 16600 },

        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93bc", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9036", "SKU": "623b16105f45b972c03c93bc", "price": 10000 },

        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93bf", "price": 10000 },
        { "category": "623b15775f45b972c03c8fc2", "SPU": "623b15885f45b972c03c9039", "SKU": "623b16105f45b972c03c93bf", "price": 10000 },
    ];
    const r = Interpreter.parseString(rule[0]);
    const result = Strategy.bestMatch([r],items).suggestion;
    console.log(result);
});

test("TEST DISCOUNT2", () => {
    const rule = ["[#k62400cec34fa961d487cbf26].count(1)&[#k62400cec34fa961d487cbf2a].count(1)&[#k62400e0934fa961d487cc209].count(1)&[#k62400e0934fa961d487cc20f].count(1) -> 0"];
    const items = [
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc20f", "price": 199 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc20f", "price": 199 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc209", "price": 100 },
        { "category": "62400d5c34fa961d487cc058", "SPU": "62400d66d1812e6a99faee63", "SKU": "62400e0934fa961d487cc209", "price": 100 },

        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf26", "price": 100 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf26", "price": 100 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf2a", "price": 199 },
        { "category": "6240075e34fa961d487cb312", "SPU": "62400784d1812e6a99fae18c", "SKU": "62400cec34fa961d487cbf2a", "price": 199 }];
    const result = Strategy.bestMatch(rule.map(r => Interpreter.parseString(r)), items)
    console.log(result.totalDiscount());
});

test("test discount with sumDiscount", () => {
    const rule = ["[#k6246d389d1812e77f772d1db].sum(15000)&~.countCate(1) -> -2000/15000"];
    const items = [
        {
            "category": "6246d311d1812e77f772b1fe",
            "SPU": "6246d321d1812e77f772b61c",
            "SKU": "6246d389d1812e77f772d1d6",
            "price": 10000,
        },
        {
            "category": "6246d311d1812e77f772b1fe",
            "SPU": "6246d321d1812e77f772b61c",
            "SKU": "6246d389d1812e77f772d1d6",
            "price": 10000,
        },
        {
            "category": "6246d311d1812e77f772b1fe",
            "SPU": "6246d321d1812e77f772b61c",
            "SKU": "6246d389d1812e77f772d1db",
            "price": 19000,
        }
    ];
    const result = Strategy.bestOfOnlyOnceDiscount(rule.map(r => Interpreter.parseString(r)), items)
    console.log(result.totalDiscount());
});

test("test 3 discount range with items", () => {
    const rule = [
        "[#k01#k02#k03#k04].count(4)&~.countCate(2) -> -40%",
        "[#k01#k02#k03#k04].count(6)&~.countCate(2) -> -50%"
    ];
    const items = [
        { "category": "01", "SPU": "01", "SKU": "02", "price": 10000, },
        { "category": "01", "SPU": "01", "SKU": "02", "price": 10000, },
        { "category": "01", "SPU": "01", "SKU": "02", "price": 10000, },
        { "category": "01", "SPU": "01", "SKU": "02", "price": 10000, },
        { "category": "01", "SPU": "01", "SKU": "02", "price": 10000, },
        { "category": "02", "SPU": "02", "SKU": "03", "price": 121200, },
        { "category": "02", "SPU": "02", "SKU": "03", "price": 121200, },
        { "category": "02", "SPU": "02", "SKU": "05", "price": 121200, },
    ];
    const result = Strategy.bestMatch(rule.map(r => Interpreter.parseString(r)), items);
    //bestMatch的策略是求最低成本下达成最多优惠，所以规则6条达成，则只计算6条
    expect((121200*2+10000*5)*-0.5)
        .toEqual(result.totalDiscount());
    //rule.discount()在规则达成的情况下，计算整个商品组合，所以算8条，包括最后那条SKU不在规则范围的
    expect((10000*5+121200*3)*-0.5)
        .toEqual(result.matches[0].rule.discount(items));
    //rule.discountFiltered()在规则达成的情况下，计算商品组合匹配规则范围的商品，所以最后一条不计算优惠
    expect((121200 * 2 + 10000 * 5) * -0.5)
        .toEqual(result.matches[0].rule.discountFilteredItems(items));
});

test("4 discounting algorithm", () => {
// 优惠计算有四种计算范围
// 假设总共9个商品，01号商品100块的2张，02号商品121.2块的6张，03号商品0.5块的1张，规则是01，02号商品总共要6张，并且两种商品都要有：
// 1. Strategy.bestMatch()的策略是求最低成本下达成最多优惠，如果是比率折扣，它会取高价商品，否则取低价商品，上例结果是计算1张01和5张02；
// 2. Strategy.bestOfOnlyOnceDiscount()的策略是只允许使用一次优惠规则，所以计算达成规则所需的最少张数，但是是最高价格的商品，上例结果是计算1张01和5张02；
// 3. Rule.discount()，会对所有商品应用优惠，，上例结果是计算所有9个商品；
// 4. Rule.discountFilteredItems()，会对规则指定范围内的所有商品计算优惠，上例结果是计算2张01和6张02，不含03；
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

test("test_oneSKU", () => {
    const ruleString2 = "[#k01#k02].oneSKU(2) -> -50%",
        ruleString6 = "[#k01#k02].oneSKU(6) -> -50%",
        ruleString7 = "$.oneSKU(7) -> -50%";
    const rule2 = Interpreter.parseString(ruleString2),
        rule6 = Interpreter.parseString(ruleString6),
        rule7 = Interpreter.parseString(ruleString7);

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

    expect(true).toEqual(rule2.check(items));
    expect(items.map(t=>t.price).reduce((a,b)=>a+b,0)/-2)
        .toEqual(rule2.discount(items));
    expect((items.map(t=>t.price).reduce((a,b)=>a+b,0)-50)/-2)
        .toEqual(rule2.discountFilteredItems(items));

    expect(true).toEqual(rule6.check(items));
    expect(items.map(t=>t.price).reduce((a,b)=>a+b,0)/-2)
        .toEqual(rule6.discount(items));
    expect((items.map(t=>t.price).reduce((a,b)=>a+b,0)-50)/-2)
        .toEqual(rule6.discountFilteredItems(items));
    let match = Strategy.bestMatch([rule6],items);
    expect((20000+items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());
    match = Strategy.bestOfOnlyOnceDiscount([rule6],items);
    expect((20000+items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());

    expect(false).toEqual(rule7.check(items));

    match = Strategy.bestMatch([rule2],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());

    match = Strategy.bestOfOnlyOnceDiscount([rule2],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());

    const rules = [rule2,rule6,rule7];
    match = Strategy.bestMatch(rules,items);
    expect((10000 * 2 + 121200 * 6) / -2).toEqual(match.totalDiscount());//matches rule2, gets 4 match groups

    match = Strategy.bestOfOnlyOnceDiscount(rules,items);
    expect((20000+121200 * 6) / -2).toEqual( match.totalDiscount());//matches rule6, gets 1 match group
});


test("testOneItemSum", () => {
    const ruleString2 = "[#k01#k02].oneSKUSum(20000) -> -50%",
        ruleString6 = "[#k01#k02].oneSKUSum(727200) -> -50%",
        ruleString7 = "$.oneSKUSum(727201) -> -50%";
    const rule2 = Interpreter.parseString(ruleString2),
        rule6 = Interpreter.parseString(ruleString6),
        rule7 = Interpreter.parseString(ruleString7);

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

    expect(true).toEqual(rule2.check(items));
    expect(items.map(t=>t.price).reduce((a,b)=>a+b,0)/-2)
        .toEqual(rule2.discount(items));
    expect((items.map(t=>t.price).reduce((a,b)=>a+b,0)-50)/-2)
        .toEqual(rule2.discountFilteredItems(items));

    expect(true).toEqual(rule6.check(items));
    expect(items.map(t=>t.price).reduce((a,b)=>a+b,0)/-2)
        .toEqual(rule6.discount(items));
    expect((items.map(t=>t.price).reduce((a,b)=>a+b,0)-50)/-2)
        .toEqual(rule6.discountFilteredItems(items));

    expect(false).toEqual(rule7.check(items));

    //#region deprecated api
    let match = Strategy.bestMatch([rule6],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());
    match = Strategy.bestOfOnlyOnceDiscount([rule6],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());


    match = Strategy.bestMatch([rule2],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());

    match = Strategy.bestOfOnlyOnceDiscount([rule2],items);
    expect((items[0].price * 2 + items[2].price * 6)/-2)
        .toEqual(match.totalDiscount());

    let rules = [rule2,rule6,rule7];
    match = Strategy.bestMatch(rules,items);
    expect((10000 * 2 + 121200 * 6) / -2).toEqual(match.totalDiscount());//matches rule2, gets 4 match groups

    match = Strategy.bestOfOnlyOnceDiscount(rules,items);
    expect((items[0].price * 2 + items[2].price * 6)/-2).toEqual( match.totalDiscount());//matches rule6, gets 1 match group

    //#endregion

    //#region new api
    let best = Strategy.bestChoice([rule6], items, MatchType.OneRule);
    expect((items[0].price * 2 + items[2].price * 6)/-2).toEqual(best.totalDiscount());//02 * 6
    best = Strategy.bestChoice([rule6], items, MatchType.OneTime);
    expect((items[0].price * 2 + items[2].price * 6)/-2).toEqual(best.totalDiscount());//02 * 6

    best = Strategy.bestChoice([rule2], items, MatchType.OneRule);
    expect((10000 * 2 + 121200 * 6) / -2).toEqual( best.totalDiscount());//01 * 2,02 * 6

    best = Strategy.bestChoice([rule2], items, MatchType.OneTime);
    expect((items[0].price * 2 + items[2].price * 6)/-2).toEqual(best.totalDiscount());//02 * 1

    best = Strategy.bestChoice(rules,items, MatchType.OneRule);
    expect((10000 * 2 + 121200 * 6) / -2).toEqual( best.totalDiscount());//matches rule2, gets 7 match groups


    best = Strategy.bestChoice(rules,items, MatchType.OneTime);
    expect((items[0].price * 2 + items[2].price * 6)/-2).toEqual(best.totalDiscount());//matches rule6, gets 1 match group

    best = Strategy.bestChoice(rules,items, MatchType.MultiRule);
    expect((10000 * 2 + 121200 * 6) / -2).toEqual(best.totalDiscount());

    const rule8 = Interpreter.parseString("[#k01#k02#k03].oneSKUSum(727200) -> -60%");
    rules.push(rule8);
    best = Strategy.bestChoice(rules, items, MatchType.MultiRule);
    expect((10000 * 2 + 121200 * 6 + 50)*6/-10).toEqual(best.totalDiscount());

    //#endregion
});


test("testPackage",()=>{
    const items = [
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 }
    ];

    const rule1 = Interpreter.parseString("[#k01].count(1) -> y:f01:1500@0"),
        rule2 = Interpreter.parseString("[#k01].count(2) -> y:f02:4000@0");

    let rules = [rule1,rule2];
    let bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
    console.log(bestMatch.totalDiscount());

    rules = [rule2,rule1];
    bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
    console.log(bestMatch.totalDiscount());
});


test("testRatioRule",()=>{
    const items = [
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 },
        { category: "01", SPU: "01", SKU: "01",price: 3000 }
    ];

    const rule1 = Interpreter.parseString("[#k01].count(3) -> -10%@0"),
        rule2 = Interpreter.parseString("[#k01].sum(9000) -> -200/1000@0");

    let rules = [rule1];
    let bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
    expect(-1200).toEqual(bestMatch.totalDiscount());

    rules = [rule2,rule1];
    bestMatch = Strategy.bestChoice(rules, items,MatchType.MultiRule, MatchGroup.CrossedMatch);
    expect(-2400).toEqual(bestMatch.totalDiscount());
});
