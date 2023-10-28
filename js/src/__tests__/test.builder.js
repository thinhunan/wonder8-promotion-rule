import { SimplexRuleBuilder, Builder } from '../builder'
import {P,R} from '../model/enums';
import Item from '../model/./Item';
import { OrCompositeRule } from '../model/Condition';
import Interpreter from "../Interpreter";
import { RuleImplBuilder } from './RuleImpl'

let items = [];

beforeAll(()=>{
    let t1 = new Item("c01","p01","k01",3000);
    let t2 = new Item("c01","p01","k02",2000);
    let t3 = new Item("c01","p02","k03",2000);
    let t4 = new Item("c02","p03","k04",2000);
    let t5 = new Item("c03","p04","k05",2000);
    let t6 = new Item("c04","p05","k06",2000);
    let t7 = new Item("c05","p06","k07",2000);
    let t8 = new Item("c06","p07","k08",2000);
    let t9 = new Item("c07","p08","k09",2000);
    let t10 = new Item("c08","p09","t10",2000);
    items = [t1,t2,t3,t4,t5,t6,t7,t8,t9,t10];
});

function getSingleRule1(){
    return new SimplexRuleBuilder()
        .addRangeAll()
        .predict(P.COUNT)
        .expected(5).build();
}
//
function getSingleRule2(){
    return Builder.simplex()
        .addRange(R.CATEGORY,"CATEGORYID1")
        .addRange(R.CATEGORY,"CATEGORYID2")
        .predict(P.SUM)
        .expected(10).build();

}
//
function getSingleRule3(){
    return Builder
        .simplex()
        .addRangeAll()
        .predict(P.SUM)
        .expected(100).build();
}

test('-',()=>{

});

test('testSingleRule',()=>{

    let rule = getSingleRule1();
    let ruleFromString = Interpreter.parseString(rule.toString());
    expect(rule.toString()).toEqual(ruleFromString.toString());
    rule = getSingleRule2();
    ruleFromString = Interpreter.parseString(rule.toString());
    expect(rule.toString()).toEqual(ruleFromString.toString());
// }
});

test('testCompositRule',()=>{
    let rule1 = getSingleRule1();
    let rule2 = getSingleRule2();

    let rules1 = Builder.and()// same as => new AndCompositRule()
        .addRule(rule1)
        .addRule(rule2).build();
    expect(rules1.toString()).toEqual("$.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10)");

    let rules2 = new OrCompositeRule()
        .addRule(rules1)
        .addRule(getSingleRule3());
    expect(rules2.toString()).toEqual("($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|$.sum(100)");

    rules2 = new OrCompositeRule([rules1,getSingleRule3()]);
    expect(rules2.toString()).toEqual("($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|$.sum(100)");

    rules2 = Builder.or().addRule(rules1).addRule(getSingleRule3()).build();
    expect(rules2.toString()).toEqual("($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|$.sum(100)");

});

test('testCompositRule2',()=>{

    let ids = ["CATEGORYID1","CATEGORYID2"];
    let rules1 = Builder.and()
        .simplex().addRangeAll().predict(P.COUNT).expected(5).end()
        .simplex().addRanges(R.CATEGORY,ids).predict(P.SUM).expected(10)
        .end()
        .build();
    let expected = "$.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10)";
    let actual = rules1.toString();
    expect(actual).toEqual(expected);

    let rules2 = Builder.or()
        .addRule(rules1)
        .addRule(getSingleRule3())
        .build();
    expected = "($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|$.sum(100)";
    actual = rules2.toString();
    expect(actual).toEqual(expected);
});


test('benchmark',()=>{
    let selectedItems  =  items;
    var c,p,t,d;
    //满折满减是最简单的规则
    //下面是一些复杂的规则组合
    c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000)) & $.sum(50000)";
    p = "-100/10000";
    t = "虎年大礼包500";
    d = "条件：（购买c01c02c02这3个项目中至少2个项目,并且总共有3个以上的场次的多于5种商品并且商品价总价多于100块） 同时 （总价多于500块）每满100优惠1块";
    let r = new RuleImplBuilder().condition(c).promotion(p).title(t).description(d).build();

    let startTime = new Date();
    for(let i = 0 ; i < 10000; i++){
        if(r.check(selectedItems )){
            r.discount(selectedItems );
        }
    }
    let endTime = new Date();
    let elapsedTime = (endTime - startTime);
    console.log("10k次共计耗时(ms):"+elapsedTime);
});

function testRule( condition,  promotion,  title,  description,  expectedValid,  expectedDiscount) {
    let r = new RuleImplBuilder()
        .condition(condition)
        .promotion(promotion)
        .title(title)
        .description(description)
        .build();
    let selectedItems  = items;

    let result = r.validate(selectedItems );
    expect(result.valid).toEqual(expectedValid);
    let canDiscount = 0;
    if(result.valid) {
        canDiscount = r.discount(selectedItems );
        expect(canDiscount).toEqual(expectedDiscount);
    }

    console.log(`${r.title}(${r.condition.toRuleString()})\n${r.description}\n匹配结果：${result.valid},可优惠${canDiscount}分钱\n${result.toString()}\n---------\n`)
}

test('testRules',()=>{

    // c01有3个商品70块，其中p01有2张50块，p02有1张20块,
    // 其它都只有1张20块，10个商品210块
    // k01 30块（3000分），其它都是20块

    let c,p,t,d;
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
    testRule(c,p,t,d,true,-2100);

    //一口价
    c = "$.sum(20000)";
    p = "16000";
    t = "一口价160块";
    d = "满200块一口价160";
    testRule(c,p,t,d,true,-5000);


    //下面是一些复杂的规则组合
    c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000)) & $.sum(50000)";
    p = "-100/10000";
    t = "虎年大礼包500";
    d = "条件：（购买c01c02c02这3个项目中至少2个项目,并且总共有3个以上的场次的多于5种商品并且商品价总价多于100块） 同时 （总价多于500块）每满100优惠1块";
    testRule(c,p,t,d,false,0);

    c = "([#cc01#cc02#cc03].countCate(2) & $.countSPU(3) & $.count(5) & $.sum(10000))|$.sum(50000)";
    p = "-100/10000";
    t = "虎年大礼包500";
    d = "条件：（购买c01c02c02这3个项目中至少2个项目，并且总共有3个以上的场次的多于5种商品并且商品价总价多于100块） 或者（总价多于500块）每满100优惠1块";
    testRule(c,p,t,d,true,-200);

    c = "([#cc01#cc02#cc03].countCate(2) & [#cc01#cc02#cc03].countSPU(5) & $.count(5) & $.sum(10000)) | $.sum(50000)";
    p = "-100/10000";
    t = "虎年大礼包";
    d = "条件：购买3个项目中至少2个项目的5个以上的场次的多于5种商品，并且总价多于100块 或者 总价多于500 每满100优惠1块";
    testRule(c,p,t,d,false,0);

    c = "([#cc01#cc02#cc03].countCate(2) & ~.countSPU(5) & $.count(5) & ~.sum(10000)) | $.sum(50000)";
    p = "-100/10000";
    t = "虎年大礼包";
    d = "条件：购买3个项目中至少2个项目的5个以上的场次的多于5种商品，并且总价多于100块 或者 总价多于500 每满100优惠1块";
    testRule(c,p,t,d,false,0);

    c = "[#cc01].count(4)&$.sum(10000)";
    p = "-100";
    t = "虎年大礼包2";
    d = "条件：购买c01项目中至少4个商品，并且总价多于100块 优惠1块";
    testRule(c,p,t,d,false,0);

    c = "[#cc01].count(4)|$.sum(10000)";
    p = "-10%";
    t = "虎年大礼包3";
    d = "条件：购买c01项目中至少4个商品，或者总价多于100块 优惠10%";
    testRule(c,p,t,d,true,-2100);

    c = "[#pp01#pp02].count(3)&[#pp01#pp02].sum(9000)";
    p = "-10%";
    t = "虎年大礼包4";
    d = "条件：购买p01,p02两个场次中至少3个商品，且总价多于90块 优惠10%";
    testRule(c,p,t,d,false,0);
});

test('countSKU',()=>{
    const r = Builder.rule()
        .simplex().addRangeAll().predict(P.COUNT).expected(11).endRule()
        .promotion("-100").build();
    expect(-100).toEqual( r.discount(items));

    const ruleCountSKU = Interpreter.parseString("$.countSKU(10)->-90");
    expect(true).toEqual(ruleCountSKU.check(items));

    const ruleCountSKU2 = Interpreter.parseString("$.countSKU(11)->-90");
    expect(false).toEqual(ruleCountSKU2.check(items));
});

test('testZeroPromotionRule',()=>{

    // c01有3个商品70块，其中p01有2张50块，p02有1张20块,
    // 其它都只有1张20块，10个商品210块
    // k01 30块（3000分），其它都是20块

    let c = "[#pp01#pp02].count(3)";
    let p = "-0";
    let t = "虎年大礼包4";
    let d = "条件：购买p01,p02两个场次中至少3个商品 优惠0";
    testRule(c,p,t,d,true,0);
});

test("perdiscount",()=>{
    const rule = Builder.rule()
        .simplex().addRangeAll().predict(P.SUM).expected(10000)
        .endRule()
        .promotion("-1000/10000")
        .build();
    const testItems  = [new Item("c01","p01","k01",15000)];
    expect(-1000).toEqual( rule.discount(testItems ));
})

test("promotionWithDecimal", ()=> {
    const promotion = "-0.5%";
    let total = 31000;
    let i = Math.ceil(parseFloat(promotion.substring(0,promotion.length-1))/100 * total);
    let expected = Math.ceil(31000 * -0.005);
    expect(i).toEqual(expected);

    total = 310;
    i = Math.ceil(parseFloat(promotion.substring(0,promotion.length-1))/100 * total);
    expected = Math.ceil(310 * -0.005);
    expect(i).toEqual(expected);

    const r = Builder.rule()
        .simplex().addRangeAll().predict(P.SUM).expected(10000).endRule()
        .promotion("-0.5%")
        .build();
    expected = Math.ceil(21000 * -0.005);
    let actual = r.discount(items);
    expect(actual).toEqual(expected);
});

