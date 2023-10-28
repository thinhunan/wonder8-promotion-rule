import Rule from "../model/Rule";
import {RuleBuilder} from "../builder";

class RuleImpl extends Rule{
    title;
    description;
    constructor(condition,promotion,title,description) {
        super(condition,promotion);
        this.title = title;
        this.description = description;
    }
}

class RuleImplBuilder extends RuleBuilder{
    _title;
    _description;
    constructor() {
        super(null);
    }

    title(t){
        this._title = t;
        return this;
    }

    description(d){
        this._description = d;
        return this;
    }

    build(){
        let r = super.build();
        r.title = this._title;
        r.description = this._description;
        return r;
    }
}

test('-',()=>{});

export { RuleImpl, RuleImplBuilder };
