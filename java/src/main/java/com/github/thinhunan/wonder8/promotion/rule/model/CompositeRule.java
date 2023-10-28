package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class CompositeRule extends RuleComponent{

    List<RuleComponent> components;
    public List<RuleComponent> getComponents(){
        return components;
    }

    abstract String getCombinator();
    abstract boolean CombineResult(boolean r1,boolean r2);

    public CompositeRule addRule(RuleComponent rule){
        if(rule == null){
            return this;
        }
        if(components == null){
            components = new ArrayList<>();
        }
        components.add(rule);
        return this;
    }

    public CompositeRule clearRules(){
        if(components != null){
            components.clear();
        }
        return this;
    }

    public CompositeRule removeRule(RuleComponent rule){
        if(rule == null){
            return this;
        }
        if(components != null){
            components.remove(rule);
        }
        return this;
    }

    @Override
    public String toRuleString() {
        if(components == null){
            return "";
        }
        else{
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(RuleComponent rule : components){
                if(!first){
                    sb.append(getCombinator());
                }
                if(rule instanceof SimplexRule){
                    sb.append(rule);
                }else{
                    sb.append("(");
                    sb.append(rule);
                    sb.append(")");
                }
                first = false;
            }
            return sb.toString();
        }
    }

    @Override
    public String toString(){
        return this.toRuleString();
    }


    @Override
    public RuleValidateResult validate(List<Item> items) {
        List<RuleValidateResult> results = new ArrayList<>();
        boolean first = true;
        boolean result = true;
        for(RuleComponent rule : components) {
            RuleValidateResult r = rule.validate(items);
            if (first) {
                result = r.isValid();
                first = false;
            } else {
                result = CombineResult(result, r.isValid());
            }
            //results.add(rule.validate(tickets).ruleResults.get(0));
            results.add(r);
        }
        return RuleValidateResult.builder()
                .valid(result)
                .rule(this)
                .clauseResults(results).build();
    }

    /**
     * 过滤出在当前规则指定范围内的ticket，
     *  如果是And组合，则求子规则过滤的交集，
     *  如果是Or组合，则求子规则过滤的并集
     */
    public Predicate<Item> getFilter() {
        //boolean and = this.getCombinator() == "&";
        boolean and = false; // 组合条件要把所有条件的范围放一块来处理
        Predicate<Item> filter = and ? t->true : t-> false;
        for(RuleComponent r : this.components) {
            if(and){
                filter = filter.and(r.getFilter());
            }
            else{
                filter = filter.or(r.getFilter());
            }
        }
        return filter;
    }

}
