package com.github.thinhunan.wonder8.promotion.rule.model.builder;

import com.github.thinhunan.wonder8.promotion.rule.model.RuleComponent;

public abstract class ConditionBuilder {
    public  ConditionBuilder _parentBuilder;

    protected ConditionBuilder(ConditionBuilder parent){
        _parentBuilder = parent;
    }

    //返回上一层设置
    public ConditionBuilder end(){
        if(_parentBuilder == null) {
            return this;
        }else {
            return _parentBuilder;
        }
    }

    //完成条件设置后，返回到rule一级设置
    public RuleBuilder endRule(){
        ConditionBuilder b = this;
        while(b != null){
            if(b instanceof RuleBuilder){
                return (RuleBuilder) b;
            }
            b = b._parentBuilder;
        }
        return null;
    }

    public abstract RuleComponent build();

    public ConditionBuilder _currentSubBuilder;

    public SimplexRuleBuilder simplex() {
        if(this instanceof RuleBuilder){
            if(_currentSubBuilder != null){
                throw new IllegalArgumentException("请先or() 或者 and()");
            }
            else{
                SimplexRuleBuilder builder = new SimplexRuleBuilder(this);
                _currentSubBuilder = builder;
                return builder;
            }
        }
        else if(this instanceof SingleRuleBuilder){
            throw new IllegalArgumentException("请先or() and()");
        }
        else {
            CompositeRuleBuilder compositor = (CompositeRuleBuilder) this;
            SimplexRuleBuilder builder = new SimplexRuleBuilder(this);
            _currentSubBuilder = builder;
            compositor.add(builder);
            return builder;
        }
    }

    public CompositeRuleBuilder or(){
        if(_parentBuilder instanceof OrCompositeRuleBuilder){
            return (OrCompositeRuleBuilder)_parentBuilder;
        }
        else if(this instanceof RuleBuilder){
            OrCompositeRuleBuilder builder = new OrCompositeRuleBuilder(this);
            this._currentSubBuilder = builder;
            return builder;
        }
        else{
            OrCompositeRuleBuilder builder = new OrCompositeRuleBuilder(_parentBuilder);
            _parentBuilder._currentSubBuilder = builder;
            builder.add(this);
            this._parentBuilder = builder;
            return builder;
        }
    }

    public CompositeRuleBuilder and(){
        if(_parentBuilder instanceof AndCompositeRuleBuilder){
            return (AndCompositeRuleBuilder)_parentBuilder;
        }
        else if(this instanceof RuleBuilder){
            AndCompositeRuleBuilder builder = new AndCompositeRuleBuilder(this);
            this._currentSubBuilder = builder;
            return builder;
        }
        else{
            AndCompositeRuleBuilder builder = new AndCompositeRuleBuilder(_parentBuilder);
            _parentBuilder._currentSubBuilder = builder;
            builder.add(this);
            this._parentBuilder = builder;
            return builder;
        }
    }

    public SameRangeRuleBuilder sameRange(){
        if(!(this instanceof CompositeRuleBuilder) ){
            throw new IllegalArgumentException("先simplex() 再 or() and()再samgeRange()");
        }
        else if( _currentSubBuilder == null ||
                !(_currentSubBuilder instanceof SingleRuleBuilder)){
            throw new IllegalArgumentException("需要先创建simplex rule");
        }
        else{
            SameRangeRuleBuilder builder = new SameRangeRuleBuilder(this,((SingleRuleBuilder) _currentSubBuilder).getRanges());
            this._currentSubBuilder = builder;
            ((CompositeRuleBuilder) this).add(builder);
            return builder;
        }
    }


}
