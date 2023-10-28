package com.github.thinhunan.wonder8.promotion.rule.model.validate;

import com.github.thinhunan.wonder8.promotion.rule.model.RuleComponent;

import java.util.ArrayList;
import java.util.List;

public class RuleValidateResult {
    public RuleValidateResult() {
    }

    public RuleValidateResult(boolean valid, RuleComponent rule, int expected, int actual, List<RuleValidateResult> clauseResults) {
        this.valid = valid;
        this.rule = rule;
        this.expected = expected;
        this.actual = actual;
        this.clauseResults = clauseResults;
    }

    boolean valid;
    RuleComponent rule;
    int expected;
    int actual;
    List<RuleValidateResult> clauseResults;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public RuleComponent getRule() {
        return rule;
    }

    public void setRule(RuleComponent rule) {
        this.rule = rule;
    }

    public int getExpected() {
        return expected;
    }

    public void setExpected(int expected) {
        this.expected = expected;
    }

    public int getActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    public List<RuleValidateResult> getClauseResults() {
        return clauseResults;
    }

    public void setClauseResults(List<RuleValidateResult> clauseResults) {
        this.clauseResults = clauseResults;
    }

    public RuleValidateResult addRuleResult(RuleValidateResult r){
        if(clauseResults== null){
            clauseResults = new ArrayList<>();
            clauseResults.add(r);
        }
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{\n\trule:\"");
        sb.append(rule.toRuleString());
        sb.append("\",\n\tvalid:");
        sb.append(valid);
        if(clauseResults == null || clauseResults.size() == 0){
            sb.append(",\n\texpected:");
            sb.append(expected);
            sb.append(",\n\tactual:");
            sb.append(actual);
        }
        else{
            sb.append(",\n\tclauses:[\n\t\t");
            for(RuleValidateResult r :clauseResults){
                sb.append(r.toString());
                sb.append(",");
            }
            sb.append("\n\t]");
        }
        sb.append("}");
        return sb.toString();
    }

    public static RuleValidateResultBuilder builder(){
        return new RuleValidateResultBuilder();
    }

    public static class RuleValidateResultBuilder {
        RuleValidateResult _result;
        public RuleValidateResultBuilder(){
            _result = new RuleValidateResult();
        }
        public RuleValidateResult build(){
            return _result;
        }
        public RuleValidateResultBuilder valid(boolean v){
            _result.setValid(v);
            return this;
        }

        public RuleValidateResultBuilder rule(RuleComponent r){
            _result.setRule(r);
            return this;
        }

        public RuleValidateResultBuilder expected(int e){
            _result.setExpected(e);
            return this;
        }

        public RuleValidateResultBuilder actual(int a){
            _result.setActual(a);
            return this;
        }

        public RuleValidateResultBuilder clauseResults(List<RuleValidateResult> c){
            _result.clauseResults = c;
            return this;
        }


    }
}
