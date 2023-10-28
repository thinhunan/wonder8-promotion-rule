/**
 * 分组匹配方式
 */
const MatchGroup = Object.freeze({
    CrossedMatch:"C", //各组交叉一起匹配最大优惠组合
    SequentialMatch:"S" //各组按组号从小到大分批匹配，后一批基于前一批结果，最终求最大优惠
});

export default MatchGroup;
