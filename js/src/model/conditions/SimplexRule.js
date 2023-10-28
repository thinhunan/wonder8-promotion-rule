import RuleValidateResult from '../matching/RuleValidateResult'
import { R } from '../enums'
import { RuleRange } from '../Condition'

/**
 * 单一条件规则, [#cPID1#cPID2].sum(4000)
 */
class SimplexRule {
    range;
    predict;
    expected;
    static calculatorPattern = /^(?<range>[\[\]#\w:\-\u4e00-\u9fa5]+|[\$~])\.(?<predict>\w+)_(?<threshold>\d+)$/;
    static rulePattern = /^([|&(]?(\[(#[cpk]\w+|#z[^#\]]+)+\]|[\$~])\.\w+\(\d+\)\)?)+$/;

    /**
     * 单一条件规则, [#cPID1#cPID2].sum(4000)
     * @param {RuleRangeCollection} range
     * @param {P} predict
     * @param {int} expected
     */
    constructor(range, predict, expected){
        this.range = range;
        this.predict = predict;
        this.expected = expected;
    }

    /**
     * 字符串格式化
     * @returns {string}
     */
    toRuleString() {
        return `${this.range.toString()}.${this.predict.toString()}(${this.expected})`;
    }

    /**
     * 字符串格式化
     * @returns {string}
     */
    toString() {
        return this.toRuleString();
    }

    /**
     * 检查items是否满足当前规则
     * @param {Item []} items
     * @returns {boolean}
     */
    check(items) {
        return this._getActual(items)>=this.expected;
    }

    /**
     * 检查items是否满足当前规则,并且返回详细的核对结果
     * @param {Item []} items
     * @returns {RuleValidateResult}
     */
    validate(items) {
        let actual = this._getActual(items);
        return new RuleValidateResult(actual>=this.expected,
            this,this.expected,actual);
    }

    _getActual(items){
        items =  this.filterItem (items);
        return this.predict.handler(items);
    }

    /**
     * 根据range过滤出当前规则所匹配的item
     * @param {Item []} items
     * @returns {Item []}
     */
    filterItem (items){
        if(this.range == R.ALL){
            return items;
        }
        else{
            let wheres = [];
            for(const match of this.range.toString().matchAll(RuleRange.rangePattern)){
                let cate = match.groups["cate"];
                let item = match.groups["item"];
                switch(cate){
                    case 'c':
                        let f1 = function(cate){
                            return function(item){
                                return item.category === cate;
                            }
                        }(item);
                        wheres.push(f1);
                        break;
                    case 'p':
                        let f2 = function(spu){
                            return function(item){
                                return item.SPU === spu;
                            }
                        }(item);
                        wheres.push(f2);
                        break;
                    case 'k':
                        let f3 = function(sku){
                            return function(item){
                                return item.SKU === sku;
                            }
                        }(item);
                        wheres.push(f3);
                        break;
                    case 'z':
                        let f4 = function(seatRange){
                            return function(item){
                                return new RuleRange('z',seatRange).isSeatInRange(item);
                            }
                        }(item)
                        wheres.push(f4);
                        break;
                    default:
                        throw "不支持的规则判断范围";
                }
            }
            return items.filter(t=>{
                for(let f of wheres){
                    if(f(t)){
                        return true;
                    }
                }
                return false;
            });
        }
    }
}

export default SimplexRule;
