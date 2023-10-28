import { AndCompositeRule, OrCompositeRule, SimplexRule } from '../Condition'
import { MatchType, P, R } from '../enums'
import TicketSeatComparator from '../comparators/TicketSeatComparator'

/**
 * for Strategy.bestMatch()'s calculation
 */
export default class RuleMatchItem  {
    items;
    rule;
    chosenItems;
    //有哪些商品可匹配某条规则
    matchGroup;
    //一个商品与多少条规则匹配
    matchCount;

    constructor(items, rule){
        if(items == null || items.length < 1
            || rule == null || rule.condition == null ) {
            throw "参数不能为空,Rule的条件和优惠必须设置好";
        }
        this.items = items;
        this.rule = rule;
    }

    /*
      calculate item to match condition's expectation
   */
    choose(type = MatchType.OneRule){
        if(this.rule.condition instanceof SimplexRule){
            if(this.rule.condition.predict === P.ADJACENT_SEAT){
                this.chosenItems = this.#adjacentSeatMatch(this.rule.condition.expected);
                return this.chosenItems;
            }
        }
        //只匹配一次或者优惠方式是优格的比率，则优先把大商品价的匹配出来
        if(type === MatchType.OneTime || this.#isRatio()){
            this.#maxPriceMatch();
        }
        else {
            this.#minPriceMatch();
        }
        return this.chosenItems;
    }

    /**
     * items remained after match
    */
    more(){
        return this.items.filter(t=> !this.chosenItems.includes(t));
    }


    //等比例折扣
    #isRatio(){
        const promotion = this.rule.promotion;
        if(!promotion) return false;
        return promotion.indexOf('%') > 0 || promotion.indexOf('/') > 0;
    }

    /**
     * 求expected个相邻座位的ticket
     * @param expected 相邻座位所需的个数
     * @return 匹配出来的座位列表
     */
    #adjacentSeatMatch(expected){
        let r = this.rule;
        let filtered = r.filterItem (this.items);
        let sorted = filtered.sort((t1,t2)=>TicketSeatComparator.compare(t1.seat,t2.seat));
        let lastSeat = "";
        let chosen = [];
        for (const t of sorted) {
            const seat = t.seat;
            if (!TicketSeatComparator.isNextSeat(lastSeat, seat)) {
                chosen = [];
            }
            chosen.push(t);
            if (chosen.length >= expected) {
                return chosen;
            }

            lastSeat = seat;
        }
        return [];
    }

    /**
     * 最低代价匹配集
     */
    #minPriceMatch(){
        this.chosenItems = [];
        this.matchCount = new Map();
        this.matchGroup = new Map();
        for (let item of this.items) {
            this.matchCount.set(item, 0);
        }
        let c = this.rule.condition;
        //初始化匹配计数
        this.#matchItemForCondition(c);
        this.#chooseItemForCondition(c,null);
        return this.chosenItems;
    }

    /**
     * 最高价格匹配集
     * @return
     */
    #maxPriceMatch() {
        const r = this.rule;
        let items = this.items;
        if (!r.check(items)) {
            this.chosenItems = [];
            return;
        }

        //all candidate items which matched condition range
        let filtered = r.filterItem (items);

        //for iterating
        let sorted = new Array(...filtered);

        sorted.sort((t1, t2) => {
            //price then seat
            const compared = (t1.price - t2.price) * (this.#isRatio() ? 1 : -1);
            if(compared === 0){
                return TicketSeatComparator.compare(t1,t2) * -1;
            }
            return compared;
        });
        //尝试每个商品拿掉看是否还匹配，如果不匹配，则放回，so easy!
        for (let i = 0; i < sorted.length; i++) {
            const t = sorted[i];
            //filtered.splice(filtered.indexOf(t), 1);//splice会把关联的array里的对象都去掉
            filtered = filtered.filter(item=>item!=t);
            if (!r.check(filtered)) {
                filtered.push(t);
            }
        }
        this.chosenItems = filtered;
    }

    /*
        Match items and rules, for picking item strategy:
        1. each item matched which conditions' range;
        2.each condition's range contains which items;
    */
    #matchItemForCondition(c) {
        if(c instanceof SimplexRule){
            let simplex = c,
                matchedItems ;
            if(simplex.range === R.ALL || simplex.range[0].type === R.ALL){
                matchedItems  = this.items;
            }
            else {
                matchedItems  = simplex.filterItem (this.items);
            }
            this.matchGroup.set(c, matchedItems );
            for (const t of matchedItems ) {
                this.matchCount.set(t,this.matchCount.get(t)+1);
            }
        }
        else{
            let subConditions = c.components;
            for (const subCondition of subConditions) {
                this.#matchItemForCondition(subCondition);
            }
        }
    }

    /*
        calculate the lowest cost set of item to match condition's expectation,
        then merge the set to this.chosenItems
     */
    #chooseItemForCondition(c, parentOr){
        if(!c.check(this.items)){
            //add all when cannot match condition with all items
            return new Array(...this.items);
        }

        let chosenItemForCondition = [];
        chosenItemForCondition.distinctPush = function(x){
            if(!this.includes(x)){
                this.push(x);
            }
            return this.length;
        }

        //因为同一个商品可区配多条规则，所以基于现在已选的基础上添加候选item来完足当前condition的要求
        //如果不在一个or组合中，则每次选出的商品立刻加入全局chosen
        //否则先不加，等到parentOr这一级考量需求最少的那条子规则的匹配集加入全局chosen，因为or只需有一条子规则成立即可
        if( c instanceof SimplexRule){
            let simplex = c;
            //all items match current condition
            let allCandidates = this.matchGroup.get(c);
            // SUM items' price
            if(simplex.predict === P.SUM){
                //candidates in chosen items already reached an aggregated price
                let chosenItemSum = allCandidates
                    .filter(t=> this.chosenItems.includes(t))
                    .map(t=>t.price)
                    .reduce((a,b)=>a+b,0);
                let expected = simplex.expected;
                if( chosenItemSum < expected ){// not enough
                    let candidates = allCandidates
                        .filter(t=>!this.chosenItems.includes(t))//pick candidate which not chosen
                        .sort((t1,t2) => {// from cheap to expensive
                            if(t1.price < t2.price) return -1;
                            return 1;
                            const compared = t1.price - t2.price;
                            if(compared === 0){
                                //按座位倒序取商品，不容易打散相邻座位
                                return TicketSeatComparator.compare(t1,t2) * -1;
                            }
                            return compared;
                        });
                    let need = expected - chosenItemSum;
                    //has an item perfect match price needs?
                    let perfectItem =  candidates.find(t=>t.price === need);
                    if(perfectItem){
                        chosenItemForCondition.distinctPush(perfectItem);
                    }
                    else{
                        //no perfect, then combine items to match
                        let count = candidates.length;
                        chosenItemForCondition.distinctPush(candidates[count-1]);//pick the biggest one of candidates
                        chosenItemSum += candidates[count -1].price;
                        //continuous add cheap candidates until reach condition's expectation
                        for(let i = 0 ; i < count -1 && chosenItemSum < expected; i++) {
                            chosenItemSum += candidates[i].price;
                            chosenItemForCondition.distinctPush(candidates[i]);
                            if(chosenItemSum >= expected){
                                //adjust chosen items to fix overflowing
                                //比如，A+B超了，A+C更接近需求，那么就要A+B+C-B，得到A+C的组合
                                let exceeding = chosenItemSum - expected;
                                let lastPosition = chosenItemForCondition.length - 1;
                                for(let j = lastPosition; j >= 0; j--) {
                                    if(chosenItemForCondition[j].price === exceeding){
                                        chosenItemForCondition.splice(j,1);
                                        exceeding = 0;
                                    }
                                    else if(chosenItemForCondition[j].price < exceeding){
                                        chosenItemForCondition.splice(j,1);
                                        exceeding -= chosenItemForCondition[j].price;
                                    }
                                    if(exceeding === 0){
                                        break;
                                    }
                                }
                            }
                        }

                    }
                }
            }
            else {
                //COUNTs
                if(!simplex.check(this.chosenItems)){
                    let candidates = allCandidates
                        .filter(t => !this.chosenItems.includes(t))//pick candidate which not chosen
                        .sort((t1,t2)=>{
                            const t1Count = this.matchCount.get(t1),
                                t2Count = this.matchCount.get(t2);
                            if(t1Count === t2Count){
                                if(t1.price === t2.price){
                                    return TicketSeatComparator.compare(t2.seat,t1.seat);//3th seat sort
                                }
                                return t2.price - t1.price ;//second order：from expensive to cheap
                            }
                            return t2Count - t1Count; //from wide-matched to narrow-matched
                        });

                    //已选商品 + 候选商品 --> 新集合
                    //尝试将候选商品中的各个商品从新集合中移除
                    //如果移除后check通过，则移除，否则加回
                    //以此来找到最少需要哪些商品加入，才能完成check
                    let joined = this.chosenItems.concat(candidates);
                    let removed = [];
                    for (let i = 0; i < candidates.length; i++) {
                        const t = candidates[i];
                        joined.splice(joined.indexOf(t),1);
                        if(simplex.check(joined)){
                            removed.push(t);
                        }
                        else{
                            joined.push(t);
                        }
                    }
                    chosenItemForCondition = chosenItemForCondition
                        .concat(candidates.filter(t=>!removed.includes(t)));
                }
            }
            if(!parentOr) {
                this.#mergeToChosen(chosenItemForCondition);
            }
        }
        else if(c instanceof AndCompositeRule){
            for (const sub of c.components) {
                //calculates sub-conditions
                let chosenForSubCondition = this.#chooseItemForCondition(sub,parentOr);
                if(!parentOr) {
                    //if not under "or", merge items to rule-scope-chosen immediately
                    this.#mergeToChosen(chosenForSubCondition);
                }
                //merge items to current condition-scope-chosen
                for (const t of chosenForSubCondition) {
                    chosenItemForCondition.distinctPush(t);
                }
            }
        }
        else if(c instanceof  OrCompositeRule){
            let or = c;
            if(!parentOr){
                parentOr = or;//is the most top "or"
            }
            for(const sub of or.components){
                //calculates sub-conditions
                let chosenForSubCondition = this.#chooseItemForCondition(sub,parentOr);
                if(chosenItemForCondition.length === 0){//first matched sub-condition
                    chosenItemForCondition = chosenForSubCondition;
                }
                else{
                    //we need the lowest cost matched condition
                    if(chosenForSubCondition.length
                        < chosenItemForCondition.length){
                        chosenItemForCondition = chosenForSubCondition;
                    }
                }
            }
            if(parentOr === or) {//is the most top "or"
                this.#mergeToChosen(chosenItemForCondition);
            }
        }

        return chosenItemForCondition;
    }

    #mergeToChosen(itemList) {
        for (const t of itemList) {
            this.#distinctPush(this.chosenItems,t);
        }
    }

    #distinctPush(array,item){
        if(!array.includes(item)){
            array.push(item);
        }
    }
}
