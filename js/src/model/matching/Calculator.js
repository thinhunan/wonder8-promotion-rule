/**
 * Calculate best match using a match chain
 * @author tanzhenlin
 * @date 2022/6/29 11:20
 **/
import BestMatch from './BestMatch'
import Match from './Match'
import MatchChain from './MatchChain'
import { MatchGroup, MatchType } from '../enums'
import RuleMatchItem  from './RuleMatchItem'
import Item from '.././Item'

/**
 * Calculate best match using a match chain
 */
export default class Calculator {
    /**
     * Calculate best match using a match chain
     * @param {Rule[]} rules
     * @param {Item[]} items
     * @param {MatchType} type
     * @param {MatchGroup} groupMatch
     * @return {BestMatch}
     */
    static calculate(rules, items, type, groupMatch) {
        let bestMatch = new BestMatch(rules, items, type);
        if(groupMatch === MatchGroup.CrossedMatch){
            this.#calculate(bestMatch);
        }
        else{ // groupMatch = MatchGroup.SequentialMatch
            let ruleGroups = new Map();
            for (const rule of rules) {
                if(!ruleGroups.has(rule.group)){
                    ruleGroups.set(rule.group,[]);
                }
                ruleGroups.get(rule.group).push(rule);
            }
            items = [...items];//avoid function #replacePackage() changes original array
            [...ruleGroups.keys()].sort().forEach(i=>{
                let groupMatch = new BestMatch(ruleGroups.get(i), items, type);
                this.#calculate(groupMatch);
                for (const m of groupMatch.matches) {
                    const matchedRule = m.rule;
                    bestMatch.addMatch(m);
                    if(matchedRule.isPackageRule()){
                        this.#replacePackage(matchedRule,m.items,items);
                    }
                }
            });
        }
        return bestMatch;
    }

    /**
     * 将当前group后续的group中的商品列表中已命中item移除，将组成的套商品加入
     */
    static #replacePackage( rule, matched, itemList) {
        for (const matchedItem  of matched) {
            //itemList = itemList.filter(t=>t!=matchedItem );//stack overflow
            itemList.splice(itemList.indexOf(matchedItem ), 1);
        }
        const firstMatched = matched[0];
        let itemPackage = new Item(
            firstMatched.category,
            firstMatched.SPU,
            rule.getPackageId(),
            matched.map(t=>t.price).reduce((a,b)=>a+b,0) + rule.discount(matched),
            matched.map(t=>t.seat).reduce((s1,s2)=>s1?s1+";"+s2:s2,'')
        );
        itemList.push(itemPackage);
        return itemPackage;
    }

    /**
     * Build match chains and find the best one to fill bestMatch's matches
     * @param {BestMatch} bestMatch
     */
    static #calculate(bestMatch){
        let rules = bestMatch.rules;
        const type = bestMatch.type;
        let items = bestMatch.items;
        let root = new MatchChain(null,null,null,items,rules);
        //以每条规则为起点build chain
        for(const rule of rules){
            //每条开始的chain，需要一个新的集合对象
            let itemList = new Array(...items);
            let chain = this.#buildChain(root,rule,itemList,rules,type);
            if(chain){
                root.next.push(chain);
            }
        }

        //计算每条chain的折扣，得到折扣最多的那条
        if(root.next.length > 0){
            let bestChain = root.next
                .sort((c1, c2) => c1.totalDiscount() - c2.totalDiscount())[0];
            while (bestChain) {
                let m = new Match(bestChain.rule, bestChain.items);
                bestMatch.addMatch(m);
                bestChain = bestChain.bestChain();
            }
        }
    }

    /**
     * build a match chain recursively
     * @param {MatchChain} previous - previous chain node
     * @param {Rule} rule  current rule
     * @param {Item[]} items
     * @param {Rule[]} rules all rule options
     * @param {MatchType} type
     * @return {null|MatchChain}
     */
    static #buildChain(previous, rule, items, rules, type) {
        if (!items || items.length < 1) return null;

        let matcher = new RuleMatchItem (items,rule);
        let matched = matcher.choose(type);

        if (matched.length > 0) {
            let left = items;
            //套商品规则需要将原始商品组合成套商品id，再进行后续运算
            if(rule.isPackageRule()){
                this.#replacePackage(rule,matched,left);
            }
            else{
                left = matcher.more();
            }

            /**
             * 满减满折问题需使用贪婪方式匹配：
             * https://y7bm5epe2b.feishu.cn/wiki/wikcnlF3Tu7yBCqYStaghdD83Xe#fJYY5U
             * */
            if(rule.promotion.indexOf('%') > 0 || rule.promotion.indexOf('/') > 0){
                let moreItem  = rule.filterItem (left);
                if(moreItem .length > 0) {
                    for (let i = 0; i < moreItem .length; i++) {
                        let subList = moreItem .slice(0,i+1);
                        let expandedMatched = new Array(...matched);
                        expandedMatched.push(...subList);
                        let reducedLeft = left.filter(t=>!subList.includes(t));
                        let expandedChain = this.#makeChain(previous,rule,rules,type,expandedMatched,reducedLeft);
                        previous.next.push(expandedChain);
                    }
                }
            }

            return this.#makeChain(previous,rule, rules, type, matched, left);

        }
        return null;
    }

    static #makeChain(previousChain, matchedRule, rules, matchType, matchedItems , leftItems ){
        let chain = new MatchChain(
            previousChain, matchedRule, matchedItems ,
            leftItems , rules);
        //匹配多条规则时，拿所有规则展开
        if (matchType === MatchType.MultiRule) {
            for (const r of rules) {
                let itemList = new Array(...leftItems );
                let c = this.#buildChain(chain, r, itemList, rules, matchType);
                if (c != null) {
                    chain.next.push(c);
                }
            }
        }
        //区配单条规则时，拿同一条规则进行下去
        else if (matchType === MatchType.OneRule) {
            let c = this.#buildChain(chain, matchedRule, leftItems , [matchedRule], matchType);
            if (c != null) {
                chain.next.push(c);
            }
        }
        return chain;
    }
}
