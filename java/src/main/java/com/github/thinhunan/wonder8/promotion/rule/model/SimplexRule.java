package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.List;
import java.util.function.Predicate;

/**
 * 单一条件规则, [#cPID1#cPID2].sum(4000)
 */
public class SimplexRule extends RuleComponent {
    //判断范围
    RuleRangeCollection range;
    //判断方式
    P predict;
    //判断标准
    int expected;

    public RuleRangeCollection getRange() {
        return range;
    }

    public void setRange(RuleRangeCollection range) {
        this.range = range;
    }

    public P getPredict() {
        return predict;
    }

    public void setPredict(P predict) {
        this.predict = predict;
    }

    public int getExpected() {
        return expected;
    }

    public void setExpected(int expected) {
        this.expected = expected;
    }

    boolean _selfCheck(){
        return !(this.range == null || this.range.size() < 1 || expected < 1);
    }

    @Override
    public boolean check(List<Item> items) {
        if(!_selfCheck()){
            return false;
        }
        return _getActual(items) >= expected;
    }

    @Override
    public RuleValidateResult validate(List<Item> items) {
        if(!_selfCheck()){
            return new RuleValidateResult(false,null,0,0,null );
        }
        int actual = _getActual(items);
        return RuleValidateResult.builder()
                .rule(this)
                .actual(actual)
                .expected(expected)
                .valid(actual >= expected)
                .build();
    }

    private int _getActual(List<Item> items) {

        Predicate<Item> fitted = getFilter();
        return Validator.getValidator(this.getPredict()).apply(items.stream().filter(fitted));
    }

    /**
     * 根据range过滤当前规则所匹配的那部分ticket
     */
     public Predicate<Item> getFilter() {
        Predicate<Item> filter = t->false;
        for(RuleRange r : this.range) {
            switch (r.getType()) {
                case CATEGORY:
                    filter = filter.or(t -> t.getCategory().equals(r.getId()));
                    break;
                case SPU:
                    filter = filter.or(t -> t.getSPU().equals(r.getId()));
                    break;
                case SKU:
                    filter = filter.or(t -> t.getSKU().equals(r.getId()));
                    break;
                case SEAT:
                    filter = filter.or(t-> r.isSeatInRange(t));
                    break;
                default:
                    filter = t -> true;
                    break;
            }
        }
        return filter;
    }


    @Override
    public String toRuleString() {
        return String.format("%s.%s(%d)",range.toRuleString(), predict,expected);
    }

    @Override
    public String toString(){
        return this.toRuleString();
    }
}
