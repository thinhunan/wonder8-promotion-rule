import CompositeRule from './CompositeRule'
/**
 * 以"并且"为关系组织的条件规则组合
 */
class AndCompositeRule extends CompositeRule{
    /**
     * 以"并且"为关系组织的条件规则组合
     * @param {(SimplexRule|SameRangeRule|AndCompositeRule|OrCompositeRule)[]} components
     */
    constructor(components) {
        super(components);
        this.combinator = "&";
    }

    combineResult(b1, b2) {
        return b1&&b2;
    }

    /**
     * 检查items是否满足当前规则
     * @param {Item []} items
     * @returns {boolean}
     */
    check(items) {
        for(let rule of this.components) {
            if(rule) {
                let r = rule.check(items);
                if (!r) {
                    return false;
                }
            }
        }
        return true;
    }
}

export default AndCompositeRule;
