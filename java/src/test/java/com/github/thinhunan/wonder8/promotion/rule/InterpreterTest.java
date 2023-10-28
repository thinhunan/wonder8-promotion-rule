package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.RuleComponent;
import org.junit.Test;

import static org.junit.Assert.*;

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