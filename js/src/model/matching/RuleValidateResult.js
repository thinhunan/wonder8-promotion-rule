/**
 * 规则匹配结果
 */
export default class RuleValidateResult{
    /**
     * 规则匹配结果
     * @param {boolean} valid
     * @param {Rule|SimplexRule|SameRangeRule|CompositeRule|AndCompositeRule|OrCompositeRule} rule
     * @param {int} expected
     * @param {int} actual
     * @param {RuleValidateResult[]} clauseResults
     */
    constructor(valid, rule, expected, actual, clauseResults=[]) {
        this.valid = valid;
        this.rule = rule;
        this.expected = expected;
        this.actual = actual;
        this.clauseResults = clauseResults;
    }

    addRuleResult(result) {
        this.clauseResults.push(result);
        return this;
    }

    toString() {
        let strings = [];
        strings.push("{rule:\"");
        strings.push(this.rule.toRuleString());
        strings.push("\",valid:");
        strings.push(this.valid);
        if(this.clauseResults.length === 0){
            strings.push(",expected:");
            strings.push(this.expected);
            strings.push(",actual:");
            strings.push(this.actual);
        }
        else{
            strings.push(",clauses:[");
            for(let r of this.clauseResults){
                strings.push(r.toString());
                strings.push(",");
            }
            strings.push("]");
        }
        strings.push("}");
        return strings.join('');
    }
}
