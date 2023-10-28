package com.github.thinhunan.wonder8.promotion.rule.model.strategy;

/*
 * @Author tanzhenlin
 * @Date 2022/8/30 12:10
**/

/**
 * 分组匹配方式
 */
public enum MatchGroup {
    CrossedMatch,//各组交叉一起匹配最大优惠组合
    SequentialMatch //各组按组号从小到大分批匹配，后一批基于前一批结果，最终求最大优惠
}
