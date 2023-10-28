/**
 * a match result chain, for calculating best match combination
 * @author tanzhenlin
 * @date 2022/6/28 13:38
 **/
import Match from './Match'

export default class MatchChain extends Match {

    previousChain;
    /**
     * @type{[MatchChain]}
     */
    next;
    rules;
    left;


    constructor(previousChain, matchRule, matchItems , left, rules){
        super(matchRule,matchItems );
        this.previousChain = previousChain;
        this.left = left;
        this.rules = rules;
        this.next = [];
    }

    /**
     * 将链路中每一环折得最低的汇总，就是最低折扣
     * @return
     */
    totalDiscount() {
        let d = 0;
        for (let i = 0; i < this.next.length; i++) {
            const one = this.next[i];
            const oneDiscount = one.totalDiscount();
            d = Math.min(d, oneDiscount);
        }
        return super.totalDiscount() + d;
    }

    bestChain(){
        if(this.next.length === 0) return null;
        return this.next.sort((t1,t2) => {
            return t1.totalDiscount() < t2.totalDiscount()?-1:1;
        })[0];
    }
}
