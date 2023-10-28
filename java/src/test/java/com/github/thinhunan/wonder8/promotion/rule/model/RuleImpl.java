package com.github.thinhunan.wonder8.promotion.rule.model;


public class RuleImpl extends Rule {

    public static RuleImplBuilder myBuilder(){
        return new RuleImplBuilder();
    }

    String description;

    String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
