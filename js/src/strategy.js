import { SimplexRule } from "./model/Condition";
import {P,MatchType,MatchGroup} from './model/enums'
import Calculator from './model/matching/Calculator'

export default class Strategy {

    /**
     * 找出一组商品和和一堆规则的最佳匹配，即优惠力度最大的匹配结果
     * type{OneTime:匹配一次，OneRule:匹配一个规则（可以多次），MultiRules：匹配多个规则（且可多次）}
     * @param {Rule[]} rules
     * @param {Item []} items
     * @param {MatchType} matchType
     * @param {MatchGroup} matchGroup
     * @return {BestMatch}
     */
    static bestChoice(rules,items,matchType=MatchType.MultiRule, matchGroup= MatchGroup.CrossedMatch){
        let best = Calculator.calculate(rules,items,matchType,matchGroup);
        this.#bindSuggestion(best);
        return best;
    }

    static #bindSuggestion(best) {
        if (best != null) {
            const ss = this.suggestion(best.rules, best.left());
            if (ss != null && ss.length > 0) {
                best.suggestion = ss[0];
            }
        }
    }

    /**
     * 找出一组商品和和一堆规则的最佳匹配，即优惠力度最大的匹配结果
     * （目前只实现了一个组合中只能适用一个优惠规则的一次或多次匹配，没有实现多个规则共同适配取得真正的最大优惠
     * @param {Rule[]} rules
     * @param {Item []} items
     * @return {MatchResult}
     * @deprecated
     * @see Strategy.bestMatch(rules,items,MatchType.OneRule)
     */
    static bestMatch(rules, items) {
        return this.bestChoice(rules,items,MatchType.OneRule);
    }

    /**
     * 找出一组商品和和一堆规则的最佳匹配，即优惠力度最大的匹配结果
     * 与bestMatch()的区别在于，不管当前选的商品可以拆成匹配多少次，都只按一次匹配来算优惠
     * 匹配的张数是规则的最低要求，价值取最高价格
     * @param {Rule[]} rules
     * @param {Item []} items
     * @return {MatchResult}
     * @deprecated
     * @see Strategy.bestMatch(rules,items,MatchType.OneTime)
     */
    static bestOfOnlyOnceDiscount(rules, items) {
        return this.bestChoice(rules,items,MatchType.OneTime);
    }


    /**
     * 获取所有未匹配但是比当前匹配更好的优惠组合建议
     * 最好不直接调用，而通过调用bestMatch()再调用返回结果的suggestion属性来获取拼单建议，
     * 因为会过滤当前商品集合已满足的优惠条件，可能造成没有更多的规则可供建议，而bestMatch的是匹配完后基于剩下的商品来建议
     * @param {Rule[]} rules
     * @param {Item []} items
     * @return {RuleValidateResult[]}
     * * */
    static suggestions(rules, items) {
        //remember discount is negative number
        let minDiscount = 1;
        let dontMatchedRules = [];
        for (const r of rules) {
            if (r.check(items)) {
                const discountValue = r.discount(items);
                if (discountValue < minDiscount) {
                    minDiscount = discountValue;
                }
            } else {
                dontMatchedRules.push(r);
            }
        }
        let results = [];
        dontMatchedRules.filter(r => r.discount(items) < minDiscount)
            .forEach(r => {
                results.push(r.validate(items));
            });
        return results;
    }

    /*
     * 获取当前未匹配，但是最接近的优惠组合拼单建议
     * 最好不直接调用，而通过调用bestMatch()再调用返回结果的suggestion属性来获取拼单建议，
     * 因为会过滤当前商品集合已满足的优惠条件，可能造成没有更多的规则可供建议，而bestMatch的是匹配完后基于剩下的商品来建议
     * @param {Rule[]} rules
     * @param {Item[]} items
     * @return {RuleValidateResult}
     * * */
    static suggestion(rules, items) {
        if(!items || items.length === 0 ||!rules || rules.length === 0){
            return null;
        }
        let results = Strategy.suggestions(rules, items);

        //缺张数优先级高还是差总价优先级高，咱讲道理，按每个商品的平均价来
        const averagePrice = items.map(t => t.price).reduce((a, b) => a + b, 0) / items.length;
        let best;
        let moreCount = 10000, moreSum = 100000000;
        for (const r of results) {
            let needs = this.#validateNeeds(r);
            if (moreCount * averagePrice + moreSum > needs[0] * averagePrice + needs[1]) {
                moreCount = needs[0];
                moreSum = needs[1];
                best = r;
            }
        }
        return best;
    }


    /*
        @Returns int[0] = count,int[1] = sum;
     */
    static #validateNeeds(result) {
        let values = [0,0];
        const clauses = result.clauseResults;
        if (!clauses || clauses.length === 0) {
            if (result.rule instanceof SimplexRule) {
                const simplex = result.rule;
                if (simplex.predict === P.SUM) {
                    values[1] += result.expected - result.actual;
                } else {
                    values[0] += result.expected - result.actual;
                }
            }
        } else {
            for (const sub of result.clauseResults) {
                let subNeeds = this.#validateNeeds(sub);
                values[0] += subNeeds[0];
                values[1] += subNeeds[1];
            }
        }
        return values;
    }

}



