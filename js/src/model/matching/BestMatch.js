/**
 * Strategy.bestMatch result called by Calculator
 * @author tanzhenlin
 * @date 2022/6/29 11:20
 **/
export default class BestMatch {
    rules;
    items;
    type;
    matches;
    suggestion;

    constructor (rules,items,type) {
        this.rules = rules;
        this.items = items;
        this.type = type;
        this.matches = [];
    }

    /**
     * 添加一条匹配结果
     * @param {Match} match
     */
    addMatch(match) {
        if (match != null) {
            this.matches.push(match);
        }
    }

    /**
     * 被规则命中的所有商品的集合
     * @return {Item []}
     */
    chosen() {
        let chosen = [];
        for (const m of this.matches) {
            for (const t of m.items)
                chosen.push(t);
        }
        return chosen;
    }

    /**
     * 没有被命中的商品的集合
     * @return {Item []}
     */
    left() {
        let chosen = this.chosen();
        return this.items
            .filter(t => !chosen.includes(t));
    }

    /**
     * 所有命中的商品的价格总和
     * @return {number}
     */
    totalPrice() {
        if (this.matches.length === 0) {
            return 0;
        }
        return this.matches
            .map(m => m.totalPrice())
            .reduce((a, b) => a + b, 0);
    }

    /**
     * 总的优惠额
     * @return {number}
     */
    totalDiscount() {
        if (this.matches.length === 0) {
            return 0;
        }
        return this.matches
            .map(m => m.totalDiscount())
            .reduce((a, b) => a + b, 0);
    }
}
