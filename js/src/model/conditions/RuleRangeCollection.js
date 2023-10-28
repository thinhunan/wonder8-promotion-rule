import { R } from '../enums'
import { RuleRange } from '../Condition'

/**
 * 单一条件规则的判断范围设置,for example: [#cPID1#cPID2], #c/#p/#k/$/~
 */
class RuleRangeCollection extends Array {
    static parseString(s){
        if(s == R.ALL) {
            return new RuleRangeCollection(new RuleRange(R.ALL, null));
        }
        else {
            let parts = s.replace(/[\[\]]/g,"").split("#");
            let ranges =  new RuleRangeCollection();
            for(let i = 1; i < parts.length ; i++){
                let range = new RuleRange(parts[i].substring(0,1), parts[i].substring(1));
                ranges.push(range);
            }
            return ranges;
        }
    }

    constructor(...args) {
        super(...args);
    }

    add(e){
        super.push(e);
    }

    toRuleString() {
        let strings = ["["];
        for (let r of this) {
            if(r.type == R.ALL){
                return R.ALL;
            }else {
                strings.push("#");
                strings.push(r.type);
                strings.push(r.id);
            }
        }
        strings.push("]");
        return strings.join('');
    }

    toString() {
        return this.toRuleString();
    }
}

export default RuleRangeCollection;
