import { R } from '../enums'
import { SimplexRule } from '../Condition'

/**
 * 和前一条条件规则的范围相同的简化表达法，范围由~表示
 */
class SameRangeRule extends SimplexRule{
    /**
     * 和前一条条件规则的范围相同的简化表达法，范围由~表示
     * @param {P} predict
     * @param {int} expected
     */
    constructor(predict,expected) {
        super(R.SAME, predict, expected);
    }

    /**
     * 字符串格式化
     * @returns {string}
     */
    toString(){
        return `~.${this.predict.toString()}(${this.expected})`;
    }
}

export default SameRangeRule;