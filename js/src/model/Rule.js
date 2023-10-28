/**
 * 营销规则
 */
class Rule {
    /**
     * 条件规则
     * @type {SimplexRule|SameRangeRule|AndCompositeRule|OrCompositeRule}
     */
    condition;
    /**
     * 优惠规则 -10,-10%,10,-10/100,目前支持四种:固定扣减，打折，一口价，逢多少减
     */
    promotion;

    /**
     * 规则分组，不同group的规则可以叠加
     * 在MatchGroup.SequentialMatch模式下，按group的值从低到高一组一组分别运算
     */
    group;

    static promotionPattern = /-?\d+(\.\d+)?(%|\/\d+)?/;
    static packagePattern = /y:(?<id>\w+):(?<price>\-?\d+)/i;

    static validatePromotion (rules) {
        //组成套商品的规则
        if(this.#isPackageRule(rules)) return true;
        return this.promotionPattern.test(rules);
    }

    static #removeWhitespaces (s) {
        return s.replaceAll(/\s+/g, '');
    }

    static #isPackageRule(str){
        return this.packagePattern.test(str);
    }

    isPackageRule(){
        return Rule.#isPackageRule(this.promotion);
    }

    getPackageId(){
        let m = Rule.packagePattern.exec(this.promotion);
        if(m){
            return m.groups["id"];
        }
        return null;
    }

    /**
     * 营销规则
     * @param {SimplexRule|SameRangeRule|AndCompositeRule|OrCompositeRule} condition
     * @param {string} promotion
     */
    constructor (condition, promotion, group=0) {
        this.promotion = promotion;
        this.condition = condition;
        this.group = group;
    }

    get promotion () {
        return this.promotion;
    }

    set promotion (p) {
        p = Rule.#removeWhitespaces(p);
        if (!Rule.validatePromotion(p)) {
            throw "营销规则格式错误";
        }
        this.promotion = p;
    }

    get group(){
        return this.group;
    }

    set group(g){
        this.group = g;
    }

    toRuleString () {
        return `${this.condition.toString()} -> ${this.promotion}@${this.group}`;
    }

    toString () {
        return this.toRuleString();
    }

    /**
     * 检查items是否满足当前规则
     * @param {Item []} items
     * @returns {boolean}
     */
    check (items) {
        return this.condition.check(items);
    }

    /**
     * 检查items是否满足当前规则,并且返回详细的核对结果
     * @param {Item []} items
     * @returns {RuleValidateResult}
     */
    validate (items) {
        return this.condition.validate(items);
    }

    /**
     * 过滤出在当前规则指定范围内的item，
     *   如果是And组合，则求子规则过滤的交集，
     *   如果是Or组合，则求子规则过滤的并集
     * @param {Item []} items
     * @returns {Item []}
     */
    filterItem (items) {
        return this.condition.filterItem (items);
    }

    /**
     * 计算打折金额，注意此方法只管对给定商品计算折扣值，不管规则是否匹配成功
     * @param {Item []} items
     * @return {int} 负值,为扣除的分数
     */
    discount (items) {
        if (!items || items.length < 1
            || !this.promotion) {
            return 0;
        }
        let promo = this.promotion;
        let m = Rule.packagePattern.exec(promo);
        if(m){
            promo = m.groups["price"];
        }
        let total = items.map(t => t.price).reduce((a, b) => a + b, 0);
        if (promo.indexOf('/') > 0) {
            let parts = promo.split("/"),
                factor = parseInt(parts[0]),
                divider = parseInt(parts[1]);
            return Math.floor(total / divider) * factor;
        } else if (promo.indexOf('%') > 0) {
            //return total * parseInt(this.promotion.substring(0, this.promotion.length - 1)) / 100;
            return Math.ceil(total * parseFloat(promo.substring(0, promo.length - 1)) / 100);
        } else {
            //fix parseInt("-0") = 0 ambiguous meanning
            if( promo === "-0"){
                return 0;
            }
            let p = parseInt(promo);
            if (p >= 0) {
                return total > p ? p - total : 0;
            } else {
                return p;
            }
        }
    }

    /**
     * 计算规则范围内部分商品的打折金额，注意此方法只管计算折扣值，不管规则是否匹配成功
     * @param {Item []} items
     * @return {int} 负值,为扣除的分数
     */
    discountFilteredItems (items){
        return this.discount(this.filterItem (items));
    }
}

export default Rule;
