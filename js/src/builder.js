import R from './model/enums/R';
import {
    SimplexRule,
    SameRangeRule,
    AndCompositeRule,
    OrCompositeRule,
    RuleRangeCollection,
    RuleRange
} from './model/Condition';
import Rule  from './model/Rule'
import Interpreter from "./Interpreter";

class ConditionBuilder{
    _parentBuilder;
    _currentSubBuilder;

    constructor(parent) {
        this._parentBuilder = parent;
    }

    end(){
        if(!this._parentBuilder){
            return this;
        }
        else{
            return this._parentBuilder;
        }
    }

    //完成条件设置后，返回到rule一级设置
    endRule(){
        let b = this;
        while(b != null){
            if(b instanceof RuleBuilder){
                return b;
            }
            b = b._parentBuilder;
        }
        return null;
    }

    build(){};

    simplex() {
        if(this instanceof RuleBuilder){
            if(this._currentSubBuilder != null){
                throw "请先or() 或者 and()";
            }
            else{
                let builder = new SimplexRuleBuilder(this);
                this._currentSubBuilder = builder;
                return builder;
            }
        }
        else if(this instanceof SingleRuleBuilder){
            throw "请先or() and()";
        }
        else {
            let compositor = this;
            let builder = new SimplexRuleBuilder(this);
            this._currentSubBuilder = builder;
            compositor.add(builder);
            return builder;
        }
    }

    or(){
        if(this._parentBuilder instanceof OrCompositeRuleBuilder){
            return this._parentBuilder;
        }
        else if(this instanceof RuleBuilder){
            let builder = new OrCompositeRuleBuilder(this);
            this._currentSubBuilder = builder;
            return builder;
        }
        else{
            let builder = new OrCompositeRuleBuilder(this._parentBuilder);
            this._parentBuilder._currentSubBuilder = builder;
            builder.add(this);
            this._parentBuilder = builder;
            return builder;
        }
    }

    and(){
        if(this._parentBuilder instanceof AndCompositeRuleBuilder){
            return this._parentBuilder;
        }
        else if(this instanceof RuleBuilder){
            let builder = new AndCompositeRuleBuilder(this);
            this._currentSubBuilder = builder;
            return builder;
        }
        else{
            let builder = new AndCompositeRuleBuilder(this._parentBuilder);
            this._parentBuilder._currentSubBuilder = builder;
            builder.add(this);
            this._parentBuilder = builder;
            return builder;
        }
    }

    sameRange(){
        if(!(this instanceof CompositeRuleBuilder) ){
            throw "先simplex() 再 or() and()再sameRange()";
        }
        else if( !this._currentSubBuilder ||
            !(this._currentSubBuilder instanceof SingleRuleBuilder)){
            throw "需要先创建simplex rule";
        }
        else{
            let builder = new SameRangeRuleBuilder(this,this._currentSubBuilder._ruleRanges);
            this._currentSubBuilder = builder;
            this.add(builder);
            return builder;
        }
    }
}

class SingleRuleBuilder extends ConditionBuilder {
    _ruleRanges;
    _predict;
    _expected;
    constructor (ruleBuilder){
        super(ruleBuilder);
    }
}

class SimplexRuleBuilder extends SingleRuleBuilder {
    constructor(ruleBuilder) {
        super(ruleBuilder);
    }

    addRange(type,id){
        if(!this._ruleRanges){
            this._ruleRanges = new RuleRangeCollection();
        }
        this._ruleRanges.add(new RuleRange(type,id));
        return new SimplexRangePart(this);
    }

    addRanges(type, ids){
        if(!this._ruleRanges){
            this._ruleRanges = new RuleRangeCollection();
        }
        if(ids != null){
            for (let id of ids) {
                this._ruleRanges.add(new RuleRange(type,id));
            }
        }
        return new SimplexRangePart(this);
    }

    addRangeAll(){
        this.addRange(R.ALL,null);
        return new SimplexRangePart(this);
    }

    range(rangeString){
        this._ruleRanges = RuleRangeCollection.parseString(rangeString);
        return new SimplexRangePart(this);
    }

    build() {
        return new SimplexRule(this._ruleRanges,this._predict,this._expected);
    }
}

class SameRangeRuleBuilder extends SingleRuleBuilder{
    refRange;
    constructor( ruleBuilder,  refRanges) {
        super(ruleBuilder);
        this.refRange = refRanges;
    }

    predict(predict){
        this._predict = predict;
        return new SameRangePredictPart(this);
    }

    build() {
        let rule = new SameRangeRule(this._predict,this._expected);
        rule.range = this.refRange;
        return rule;
    }
}

class CompositeRuleBuilder extends ConditionBuilder {
    components;
    builders;
    constructor (builder){
        super(builder);
    }

    addRule(rule){
        if(!rule){
            return this;
        }
        if(!this.components){
            this.components = [];
        }
        this.components.push(rule);
        return this;
    }

    add(builder){
        if(!this.builders){
            this.builders = [];
        }
        this.builders.push(builder);
        this._currentSubBuilder = builder;
        return this;
    }

    buildComponent(){
        if(!this.components){
            this.components = [];
        }
        if(this.builders && this.builders.length > 0) {
            for (let b of this.builders) {
                this.components.push(b.build());
            }
        }
    }
}

class AndCompositeRuleBuilder extends CompositeRuleBuilder {
    constructor (builder) {
        super(builder);
    }

    build() {
        this.buildComponent();
        return new AndCompositeRule(this.components);
    }
}

class OrCompositeRuleBuilder extends CompositeRuleBuilder {
    constructor (builder) {
        super(builder);
    }

    build() {
        this.buildComponent();
        return new OrCompositeRule(this.components);
    }
}

class RuleBuilder extends ConditionBuilder {
    _condition;
    _promotion;
    _group;

    constructor (parent) {
        super(parent);
    }

    condition(condition){
        this._condition = condition;
        return this;
    }

    promotion(promotion){
        this._promotion = promotion;
        return this;
    }

    group(g){
        this._group = g;
        return this;
    }

    build(){
        let r = new Rule();
        if(this._promotion != null) {
            r.promotion = this._promotion;
        }
        if(this._currentSubBuilder != null) {
            r.condition = this._currentSubBuilder.build();
        }
        else if( this._condition!=null ){
            r.condition = (Interpreter.parseString(this._condition));
        }
        if(this._group === undefined || this._group === null){
            this._group = 0;
        }
        r.group = this._group;

        return r;
    }
}

class SimplexRangePart {
    _builder;
    constructor (builder) {
        this._builder = builder;
    }

    addRange(type, id) {
        this._builder.addRange(type, id);
        return this;
    }

    addRanges(type, ids){
        this._builder.addRanges(type,ids);
        return this;
    }

    predict(predict) {
        this._builder._predict = predict
        return new SimplexPredictPart(this._builder);
    }
}

class SimplexPredictPart {
    _builder;
    constructor (builder) {
        this._builder = builder;
    }

    expected(expected) {
        this._builder._expected = expected;
        return this._builder;
    }
}

class SameRangePredictPart {
    _builder;
    constructor (builder) {
        this._builder = builder;
    }
    expected(expected) {
        this._builder._expected = expected;
        return this._builder;
    }
}

class Builder {
    static rule(){
        return new RuleBuilder();
    }

    static simplex(){
        return new SimplexRuleBuilder();
    }

    static and(){
        return new AndCompositeRuleBuilder();
    }

    static or(){
        return new OrCompositeRuleBuilder();
    }
}

export { SimplexRuleBuilder, SameRangeRuleBuilder, AndCompositeRuleBuilder, OrCompositeRuleBuilder, RuleBuilder, Builder };
