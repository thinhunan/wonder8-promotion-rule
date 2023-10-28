import RuleValidateResult from '../matching/RuleValidateResult'
import { SimplexRule } from '../Condition'

class CompositeRule {
    components;
    combinator;

    constructor(components) {
        this.components = new Array(0);
        if(components && components.length > 0){
            for (const component of components) {
                if(component){
                    this.components.push(component);
                }
            }
        }
    }

    get combinator(){
        return this.combinator;
    }

    combineResult(b1,b2){
        return b1 && b2;
    }

    /**
     * 添加一条条件规则到组合条件中
     * @param {SimplexRule|SameRangeRule|AndCompositeRule|OrCompositeRule} rule
     * @returns {CompositeRule}
     */
    addRule(rule){
        if(!rule){
            return this;
        }
        if(!this.components){
            this.components = new Array(0);
        }
        this.components.push(rule);
        return this;
    }

    /**
     * 字符串格式化
     * @returns {string}
     */
    toRuleString() {
        if (!this.components) {
            return "";
        } else {
            let sb = [],
                first = true;
            for (let rule of this.components) {
                if (!rule){
                    continue;
                }
                if (!first) {
                    sb.push(this.combinator);
                }
                if (rule instanceof SimplexRule) {
                    sb.push(rule.toString());
                } else {
                    sb.push("(");
                    sb.push(rule.toString());
                    sb.push(")");
                }
                first = false;
            }
            return sb.join('');
        }
    }

    /**
     * 字符串格式化
     * @returns {string}
     */
    toString(){
        return this.toRuleString();
    }

    /**
     * 检查items是否满足当前规则,并且返回详细的核对结果
     * @param {Item []} items
     * @returns {RuleValidateResult}
     */
    validate(items) {
        let results = [],
            first = true,
            result = true;
        for(let rule of this.components) {
            if(rule) {
                let r = rule.validate(items);
                if (first) {
                    result = r.valid;
                    first = false;
                } else {
                    result = this.combineResult(result, r.valid);
                }
                results.push(r);
            }
        }
        return new RuleValidateResult(result,this,0,0,results);
    }


    /**
     * 过滤出在当前规则指定范围内的item，
     *   //如果是And组合，则求子规则过滤的交集，
     *   //如果是Or组合，则求子规则过滤的并集
     *  不管And Or，都是返回并集
     * @param {Item []} items
     * @returns {Item []}
     */
    filterItem (items){
        let result = null;
        for(let rule of this.components) {
            if(rule) {
                const filtered = rule.filterItem (items);
                if(!result){
                    result = filtered;
                }
                //if(this.combinator === "&"){
                if(false) { //不管And Or，都是返回并集
                    result = result.filter(t=>filtered.includes(t));
                }
                else{
                    result = [...new Set([...result,...filtered])];
                }
            }
        }
        return result;
    }
}

export default CompositeRule;
