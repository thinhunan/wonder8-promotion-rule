package com.github.thinhunan.wonder8.promotion.rule.model;

/**
 * 和前一条条件规则的范围相同的简化表达法，范围由~表示
 */
public class SameRangeRule extends SimplexRule{

    @Override
    public String toString() {
        return String.format("%s.%s(%d)","~", predict,expected);
    }
}
