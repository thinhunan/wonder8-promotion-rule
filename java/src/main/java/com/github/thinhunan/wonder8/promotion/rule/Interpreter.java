package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {

    private final static Pattern rulePattern = Pattern.compile("^([|&\\(]?(\\[(#[cpk]\\w+|#z[^#\\]]+)+\\]|[\\$~])\\.\\w+\\(\\d+\\)\\)?)+$");
    // range: []#\w-:汉字
    private final static Pattern calculatorPattern = Pattern.compile("^(?<range>[\\[\\]#\\w:\\-\\u4e00-\\u9fa5]+|[\\$~])\\.(?<predict>\\w+)\\_(?<threshold>\\d+)$");

    //验证条件规则是否合法
    public static boolean validateCondition(String rules){
        rules = _removeWhitespaces(rules);
        return rules.matches(rulePattern.pattern());
    }

    /*
       如果是condition -> promotion 的格式，则解释为Rule
       否则解释为RuleComponent，即仅condition部分
    * */
    public static RuleComponent parseString(String ruleString) throws IllegalArgumentException{
        if(ruleString == null || ruleString.isEmpty()){
            return null;
        }
        String group = "0";
        if(ruleString.indexOf("@")>0){
            String[] parts = ruleString.split("@");
            ruleString = parts[0];
            if(parts[1].matches("^\\d+$")) {
                group = parts[1];
            }
        }
        int g = Integer.parseInt(group);

        if(ruleString.indexOf("->") > 0){
            String[] parts = ruleString.split("->");
            String promotion = Interpreter._removeWhitespaces(parts[1]);
            String condition = Interpreter._removeWhitespaces(parts[0]);
            if(Rule.validatePromotion(promotion)){
                String r = _replaceBracketsToUnderline(condition);
                RuleComponent c = new Interpreter(r).parse();
                return new Rule(c,promotion,g);
            }
            else{
                return new Interpreter(Interpreter._replaceBracketsToUnderline(condition)).parse();
            }
        }

        String r = Interpreter._replaceBracketsToUnderline(
                Interpreter._removeWhitespaces(ruleString));
        return new Interpreter(r).parse();
    }

    /**
     * 自动将同一组规则中相邻的同样范围替换为~,
     * 比如将：[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     * 替换为: [#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     */
    public static String foldRuleString(String s){
        s = _removeWhitespaces(s);
        s = _replaceBracketsToUnderline(s);
        int rangeStart = -1;
        int index = 0;
        String lastRange=null;
        StringBuilder sb = new StringBuilder();
        while(index < s.length()){
            char c = s.charAt(index);
            switch (c){
                case '[':
                    rangeStart = index;
                    break;
                case ']':
                    String currentRange = s.substring(rangeStart,index+1);
                    if(currentRange.equals(lastRange)){
                        sb.append('~');
                    }
                    else{
                        sb.append(currentRange);
                        lastRange = currentRange;
                    }
                    rangeStart = -1;
                    break;
                case '(':
                case ')':
                    lastRange = null;
                default:
                    if(rangeStart == -1) {
                        sb.append(c);
                    }
            }
            index++;
        }
        String re = sb.toString();
        re = _replaceUnderlineToBrackets(re);
        return re;
    }
    /**
     * 自动将规则中范围标记为~的SameRangeRule的range替换为所引用的range表达式,
     * 比如将：[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     * 替换为: [#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     */
    public static String unfoldRuleString(String s){
        s = _removeWhitespaces(s);
        s = _replaceBracketsToUnderline(s);
        int rangeStart = -1;
        int index = 0;
        String lastRange=null;
        StringBuilder sb = new StringBuilder();
        while(index < s.length()){
            char c = s.charAt(index);
            switch (c){
                case '~':
                    sb.append(lastRange);
                    break;
                case '[':
                    rangeStart = index;
                    sb.append(c);
                    break;
                case ']':
                    lastRange = s.substring(rangeStart,index+1);
                    rangeStart = -1;
                    sb.append(c);
                    break;
                default:
                    sb.append(c);
            }
            index++;
        }
        String re = sb.toString();
        re = _replaceUnderlineToBrackets(re);
        return re;
    }

    String ruleString;
    RuleRangeCollection previousRuleRange;

    private Interpreter(String ruleString){
        this.ruleString = ruleString;
    }
    private RuleComponent parse(){
        return _parsePart(ruleString);
    }

    private RuleComponent _parsePart(String ruleStr)
            throws IllegalArgumentException{
        int length = ruleStr.length();
        Matcher m = calculatorPattern.matcher(ruleStr);
        if(m.find()){ //已是SingleRule
            String range = m.group("range");
            String predict = m.group("predict");
            int threshold = Integer.parseInt(m.group("threshold"));
            if(range.equals(R.SAME.toString())){
                if(previousRuleRange == null){
                    throw new IllegalArgumentException("SameRangeRule必须紧跟SimplexRule:"+ruleStr);
                }
                SameRangeRule rule = new SameRangeRule();
                rule.setRange(previousRuleRange);
                rule.setPredict(P.parseString(predict));
                rule.setExpected(threshold);
                return rule;
            }
            else {
                SimplexRule rule = Builder.simplex()
                        .range(range)
                        .predict(P.parseString(predict))
                        .expected(threshold)
                        .build();
                previousRuleRange =  rule.getRange();
                return rule;
            }
        }
        else{ //拆解组合语句
            boolean or = true;
            int index = 0;
            int startIndex = 0;
            int brackets = 0;
            CompositeRule result = null;
            RuleComponent ruleInBracket = null;
            while(index < length){
                char c = ruleStr.charAt(index);
                //括号分组处理，括号必须成对，括号内子括号的收，不能终结外面括号的起
                if('(' == c ){
                    if(brackets++ == 0) {
                        startIndex = index;
                    }
                }
                if(')' == c){
                    if( --brackets == 0){
                        String subCondition = ruleStr.substring(startIndex+1, index);
                        ruleInBracket = _parsePart(subCondition);
                        if(result != null ){
                            result.addRule(ruleInBracket);
                        }
                        startIndex = index + 1;
                    }
                }

                if(brackets == 0 &&( '|' == c || '&' == c)){
                    or = '|' == c;
                    if(or){
                        if(result == null){
                            result = new OrCompositeRule().addRule(ruleInBracket);
                        }
                        else if(!(result instanceof OrCompositeRule)){
                            OrCompositeRule orRule = new OrCompositeRule();
                            orRule.addRule(result);
                            result = orRule;
                        }
                    }
                    else {
                        if(result == null){
                            result = new AndCompositeRule().addRule(ruleInBracket);
                        }
                        else if(!(result instanceof AndCompositeRule)){
                            AndCompositeRule andRule = new AndCompositeRule();
                            andRule.addRule(result);
                            result = andRule;
                        }
                    }
                    if( index - startIndex > 3) { //前半截
                        String subCondition = ruleStr.substring(startIndex, index);
                        RuleComponent left = _parsePart(subCondition);
                        result.addRule(left);
                    }
                    startIndex = index + 1;
                }

                if(index == length-1) {
                    String subCondition = ruleStr.substring(startIndex, index+1);
                    RuleComponent right = _parsePart(subCondition);
                    if(result == null){
                        return right;
                    }
                    else {
                        result.addRule(right);
                    }
                }

                index ++;
            }
            if (result != null){
                return result;
            }
            else {
                return ruleInBracket;
            }
        }
    }


    //region utils
    private static String _removeWhitespaces(String s){
        return s.replaceAll("\\s+","");
    }
    private static String _replaceBracketsToUnderline(String s){
        return s.replaceAll("\\((\\d+)\\)","_$1");
    }
    private static String _replaceUnderlineToBrackets(String s){
        return s.replaceAll("_(\\d+)","\\($1\\)");
    }
    //endregion

}
