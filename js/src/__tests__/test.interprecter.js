import Interpreter from "../Interpreter";

test("test validateCondition",()=>{
    let ruleStr = "($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10)&~.countSPU(2))|$.sum(100)";
    expect(Interpreter.validateCondition(ruleStr)).toBe(true);
});

test("test parseString",()=>{
    let ruleStr = "($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10)&~.countSPU(2))|$.sum(100)";
    let rule = Interpreter.parseString(ruleStr);
    console.log(rule.toString());

    ruleStr = "($.count(5)|([#cCATEGORYID1#cCATEGORYID2].sum(10)&~.countSPU(2)))|$.sum(100)";
    rule = Interpreter.parseString(ruleStr);
    expect(rule.toString()).toEqual(ruleStr);


    ruleStr = "(($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|([#cCATEGORYID1#cCATEGORYID2].sum(10)&~.countSPU(2)))|$.sum(100)";
    rule = Interpreter.parseString(ruleStr);
    expect(rule.toString()).toEqual(ruleStr);

    ruleStr = "(($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|[#cCATEGORYID1#cCATEGORYID2].sum(10))|$.sum(100)";
    rule = Interpreter.parseString(ruleStr);
    expect(rule.toString()).toEqual(ruleStr);

    ruleStr = "(($.count(5)&[#cCATEGORYID1#cCATEGORYID2].sum(10))|[#cCATEGORYID1#cCATEGORYID2].sum(10))|($.sum(100)&~.countCate(2))";
    rule = Interpreter.parseString(ruleStr);
    expect(rule.toString()).toEqual(ruleStr);

});

test("foldRuleString",()=> {

    let rule = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|[#c01#c02#c03].count(10)&[#c01].sum(10)";
    let expected = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|~.count(10)&[#c01].sum(10)";
    let actual = Interpreter.foldRuleString(rule);
    expect(actual).toEqual(expected);

    let rule2 = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
    let expected2 = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
    let actual2 = Interpreter.foldRuleString(rule2);
    expect(actual2).toEqual(expected2);
});

test("unfoldRuleString",()=> {

    let rule = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|~.count(10)&[#c01].sum(10)";
    let expected = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|[#c01#c02#c03].count(10)&[#c01].sum(10)";
    let actual = Interpreter.unfoldRuleString(rule);
    expect(actual).toEqual(expected);

    let expected2 = "[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
    let rule2 = "[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))";
    let actual2 = Interpreter.unfoldRuleString(rule2);
    expect(actual2).toEqual(expected2);
});

test("a rule", ()=>{
    const ruleString = "[#kk01].count(2)&[#kk02].count(3) -> 1000@0";
    const rule = Interpreter.parseString(ruleString);

    expect("1000").toEqual(rule.promotion);
    expect(ruleString).toEqual(rule.toString());
});

