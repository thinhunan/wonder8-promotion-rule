import Item from '../model/./Item'
import Interpreter from '../Interpreter'
import Strategy from '../strategy'
import { MatchGroup, MatchType } from '../model/enums'

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

test('benchmark',()=> {
    let seatedItems = getSeatedItems();
    //二楼:A:1:1-5
    //rule1 -2000 rule2 -1800 rule1+rule2 -3800 rule3 -4000
    const rule1 = Interpreter.parseString("[#z二楼:A:1:1-二楼:A:1:5].adjacentSeat(2)->y:APackage2:18000");
    rule1.group = 0;
    const rule2 = Interpreter.parseString("[#kAPackage2].count(1)->-10%@1");
    const rule3 = Interpreter.parseString("[#k02].count(3)->-4000@1");
    const rules = [rule1, rule2, rule3];

    let start = new Date();
    for (let i = 0; i < 10000; i++) {
        const crossedGroupMatch = Strategy.bestChoice(rules, seatedItems,
            MatchType.MultiRule, MatchGroup.CrossedMatch);
        expect(crossedGroupMatch.totalDiscount()).toEqual(-3800 - 4000);
    }
    let end = new Date();
    console.log("10000 crossed group match ms:" + (end - start));

    start = new Date();
    for (let i = 0; i < 10000; i++) {
        const sequentialGroupMatch = Strategy.bestChoice(rules, seatedItems,
            MatchType.MultiRule, MatchGroup.SequentialMatch);
        expect(sequentialGroupMatch.totalDiscount()).toEqual(-3800 * 2);
    }
    end = new Date();
    console.log("10000 sequential group match ms:" + (end - start));
});
