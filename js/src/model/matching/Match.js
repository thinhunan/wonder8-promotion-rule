/**
 * Match result presentation
 * @author tanzhenlin
 * @date 2022/6/29 11:22
 **/
export default class Match {
    rule;
    items;

    constructor (rule, items) {
        this.rule = rule;
        this.items = items;
    }

    totalDiscount() {
        if (!this.items || this.items.length === 0 || !this.rule) return 0;
        return this.rule.discount(this.items);
    }

    count() {
        if (!this.items) return 0;
        return this.items.length;
    }

    totalPrice() {
        if (!this.items) return 0;
        return this.items.map(t => t.price).reduce((a, b) => a + b, 0);
    }
}
