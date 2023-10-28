import {P,R} from '../model/enums';
import Item from '../model/./Item';
import { RuleRange, RuleRangeCollection, SimplexRule, SameRangeRule, AndCompositeRule, OrCompositeRule } from '../model/Condition';
import Rule from "../model/rule";
import {Builder} from "../builder";

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

test('test get predict handler',()=>{
    let actual = P.COUNT_CATEGORY.handler(items);
    expect(actual).toBe(8);
});

test("test range collection",()=>{
    let ranges = RuleRangeCollection.parseString("[#cc01#cc02]");
    console.log(ranges);
    ranges = RuleRangeCollection.parseString("$");
    console.log(ranges);
});

test("test promotion validate",()=>{
    let p = ["-10","10","-10%","-10/100"];
    for (let promotion of p) {
        if(!Rule.validatePromotion(promotion)){
            console.log(promotion);
        }
    }
});

test("test simplex",()=>{
    let simplex = new SimplexRule();
    let range = RuleRangeCollection.parseString("$");
    simplex.range = range;
    simplex.predict = P.COUNT_CATEGORY;
    simplex.expected = 3;
    expect(simplex.check(items)).toBe(true);
    console.log(simplex.toString());
    simplex.expected = 10;
    expect(simplex.check(items)).not.toBe(true);
    console.log(simplex.toString());
    range = new RuleRangeCollection();
    range.push(new RuleRange(R.CATEGORY,"c01"));
    range.push(new RuleRange(R.CATEGORY,"c02"));
    simplex.range = range;
    simplex.expected = 3;
    expect(simplex.check(items)).not.toBe(true);
    console.log(simplex.toString());
    simplex.predict = P.COUNT_SPU;
    expect(simplex.check(items)).toBe(true);
    console.log(simplex.toString());
});

test("test composite rule",()=>{
    let simplex = new SimplexRule();
    let range = RuleRangeCollection.parseString("$");
    simplex.range = range;
    simplex.predict = P.COUNT_CATEGORY;
    simplex.expected = 3;
    let simplex2 = new SimplexRule();
    simplex2.range = new RuleRangeCollection();
    simplex2.range.push(new RuleRange(R.CATEGORY,"c01"));
    simplex2.range.push(new RuleRange(R.CATEGORY,"c02"));
    simplex2.expected = 3;
    simplex2.predict = P.COUNT_SPU;
    let sameRange = new SameRangeRule(P.COUNT_CATEGORY,2);
    sameRange.range = simplex2.range;
    console.log(simplex.toString());
    console.log(simplex2.toString());
    console.log(sameRange.toString());
    expect(simplex.check(items)).toBe(true);
    expect(simplex2.check(items)).toBe(true);
    expect(sameRange.check(items)).toBe(true);

    let and = new AndCompositeRule([simplex,simplex2]);
    expect(and.check(items)).toBe(true);
    let bigmoney = new SimplexRule(R.ALL,P.SUM,50000);
    let and2 = new AndCompositeRule([and]);
    and2.addRule(bigmoney);
    expect(and2.check(items)).not.toBe(true);

    let or = new OrCompositeRule([and]);
    or.addRule(bigmoney);
    expect(or.check(items)).toBe(true);
    console.log(and.toString());
    console.log(and2.toString());
    console.log(or.toString());

});


test('test filter', ()=>{
    const rule1 = Builder.simplex()
        .range("[#cc01#cc02]")
        .predict(P.COUNT).expected(3)
        .build();
    let filtered = rule1.filterItem (items);
    expect([items[0],items[1],items[2],items[3]])
        .toEqual(expect.arrayContaining(filtered));

    const rule2 = Builder.simplex()
        .range("[#cc03]")
        .predict(P.COUNT).expected(3)
        .build();

    const ruleOr = Builder.or().addRule(rule1).addRule(rule2).build();
    filtered = ruleOr.filterItem (items);
    expect(5).toEqual(filtered.length);
    expect([items[0],items[1],items[2],items[3],items[4]])
        .toEqual(expect.arrayContaining(filtered));

    const ruleAnd = Builder.and().addRule(rule1).addRule(rule2).build();
    filtered = ruleAnd.filterItem (items);
    expect(5).toEqual(filtered.length);//不管是and还是or，都用并集
});

