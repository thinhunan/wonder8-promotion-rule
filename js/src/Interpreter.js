import {P,R} from './model/enums'
import { AndCompositeRule, OrCompositeRule, SameRangeRule, SimplexRule } from './model/Condition'
import Rule from './model/Rule'

export default class Interpreter {
    ruleString;
    _previousRuleRange;
    static rulePattern = /^([|&(]?(\[(#[cpk]\w+|#z[^#\]]+)+]|[$~])\.\w+\(\d+\)\)?)+$/;
    static calculatorPattern = /^(?<range>[\[\]#\w:\-\u4e00-\u9fa5]+|[$~])\.(?<predict>\w+)_(?<threshold>\d+)$/;

    constructor(ruleString) {
        this.ruleString = ruleString;
    }

    static _removeWhiteSpaces(str) {
        return str.replace(/\s+/g, '');
    }

    static _replaceBracketsToUnderline(s) {
        return s.replace(/\((\d+)\)/g, "_$1");
    }

    static _replaceUnderlineToBrackets(s) {
        return s.replace(/_(\d+)/g, "\($1\)");
    }

    static validateCondition(rules) {
        rules = this._removeWhiteSpaces(rules);
        return this.rulePattern.test(rules);
    }

    /**
     * 如果是condition -> promotion 的格式，则解释为Rule
     * 否则解释为RuleComponent，即仅condition部分
     * @param {string} ruleString
     * @return {Rule|SimplexRule|SameRangeRule|AndCompositeRule|OrCompositeRule}
     */
    static parseString(ruleString) {
        if (ruleString && ruleString.length > 5) {
            let group = "0";
            if (ruleString.indexOf("@") > 0) {
                const parts = ruleString.split("@");
                ruleString = parts[0];
                if (/^\d+$/.test(parts[1]))
                    group = parts[1];
            }

            const g = parseInt(group);

            if (ruleString.indexOf("->") > 0) {
                const parts = ruleString.split("->");
                const condition = new Interpreter(
                    this._replaceBracketsToUnderline(
                        this._removeWhiteSpaces(parts[0]))).parse();
                const promotion = this._removeWhiteSpaces(parts[1]);

                if (Rule.validatePromotion(promotion)) {
                    return new Rule(condition, promotion, g);
                } else {
                    return condition;
                }
            }
            const r = this._replaceBracketsToUnderline(
                this._removeWhiteSpaces(ruleString));
            return new Interpreter(r).parse();
        }
    }

    /**
     * 自动将同一组规则中相邻的同样范围替换为~,
     * 比如将：[#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     * 替换为: [#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     */
    static foldRuleString(s) {
        s = this._removeWhiteSpaces(s);
        s = this._replaceBracketsToUnderline(s);
        let rangeStart = -1,
            index = 0,
            lastRange = null,
            sb = [];
        while (index < s.length) {
            let c = s.charAt(index);
            switch (c) {
                case '[':
                    rangeStart = index;
                    break;
                case ']':
                    let currentRange = s.substring(rangeStart, index + 1);
                    if (currentRange == lastRange) {
                        sb.push('~');
                    } else {
                        sb.push(currentRange);
                        lastRange = currentRange;
                    }
                    rangeStart = -1;
                    break;
                case '(':
                case ')':
                    lastRange = null;
                default:
                    if (rangeStart === -1) {
                        sb.push(c);
                    }
                    break;
            }
            index++;
        }
        let re = sb.join('');
        re = this._replaceUnderlineToBrackets(re);
        return re;
    }

    /**
     * 自动将规则中范围标记为~的SameRangeRule的range替换为所引用的range表达式,
     * 比如将：[#c01#c02#c03].countCate(2)&~.countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     * 替换为: [#c01#c02#c03].countCate(2)&[#c01#c02#c03].countSPU(5)|([#c01#c02#c03].count(10)&[#c01].sum(10))
     */
    static unfoldRuleString(s) {
        s = this._removeWhiteSpaces(s);
        s = this._replaceBracketsToUnderline(s);
        let rangeStart = -1,
            index = 0,
            lastRange = null,
            sb = [];
        while (index < s.length) {
            let c = s.charAt(index);
            switch (c) {
                case '~':
                    sb.push(lastRange);
                    break;
                case '[':
                    rangeStart = index;
                    sb.push(c);
                    break;
                case ']':
                    lastRange = s.substring(rangeStart, index + 1);
                    rangeStart = -1;
                    sb.push(c);
                    break;
                default:
                    sb.push(c);
            }
            index++;
        }
        let re = sb.join('');
        re = this._replaceUnderlineToBrackets(re);
        return re;
    }

    parse() {
        return this._parsePart(this.ruleString);
    }

    _parsePart(ruleStr) {
        let length = ruleStr.length;
        let m = Interpreter.calculatorPattern.exec(ruleStr);
        if (m) { //已是SingleRule
            let range = m.groups["range"],
                predict = m.groups["predict"],
                threshold = parseInt(m.groups["threshold"]);
            if (range == R.SAME) {
                if (!this._previousRuleRange) {
                    throw "SameRangeRule必须紧跟SimplexRule:" + ruleStr;
                }
                let rule = new SameRangeRule();
                rule.range = this._previousRuleRange;
                rule.predict = P.parseString(predict);
                rule.expected = threshold;
                return rule;
            } else {
                let rule = new SimplexRule(range, P.parseString(predict), threshold);
                this._previousRuleRange = rule.range;
                return rule;
            }
        } else { //拆解组合语句
            let or = true,
                index = 0,
                startIndex = 0,
                brackets = 0,
                result = null,
                ruleInBracket = null;
            while (index < length) {
                let c = ruleStr.charAt(index);
                //括号分组处理，括号必须成对，括号内子括号的收，不能终结外面括号的起
                if ('(' === c) {
                    if (brackets++ === 0) {
                        startIndex = index;
                    }
                }
                if (')' === c) {
                    if (--brackets === 0) {
                        let subCondition = ruleStr.substring(startIndex + 1, index);
                        ruleInBracket = this._parsePart(subCondition);
                        if (result != null) {
                            result.addRule(ruleInBracket);
                        }
                        startIndex = index + 1;
                    }
                }

                if (brackets === 0 && ('|' === c || '&' === c)) {
                    or = '|' === c;
                    if (or) {
                        if (!result) {
                            result = new OrCompositeRule([ruleInBracket]);
                        } else if (!(result instanceof OrCompositeRule)) {
                            result = new OrCompositeRule([result]);
                        }
                    } else {
                        if (!result) {
                            result = new AndCompositeRule([ruleInBracket]);
                        } else if (!(result instanceof AndCompositeRule)) {
                            result = new AndCompositeRule([result]);
                        }
                    }
                    if (index - startIndex > 3) { //前半截
                        let subCondition = ruleStr.substring(startIndex, index);
                        let left = this._parsePart(subCondition);
                        result.addRule(left);
                    }
                    startIndex = index + 1;
                }

                if (index === length - 1) {
                    let subCondition = ruleStr.substring(startIndex, index + 1);
                    let right = this._parsePart(subCondition);
                    if (!result) {
                        return right;
                    } else {
                        result.addRule(right);
                    }
                }

                index++;
            }
            if (result != null) {
                return result;
            } else {
                return ruleInBracket;
            }
        }
    }
}
