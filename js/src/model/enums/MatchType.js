/**
 * BestMatch匹配方式：匹配1次，匹配其中一个规则（可多次），匹配规则组合（可多次）
 */
const MatchType = Object.freeze({
    OneTime:"OneTime",
    OneRule:"OneRule",
    MultiRule:"MultiRule"
});

export default MatchType;