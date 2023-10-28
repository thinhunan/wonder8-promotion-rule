package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.model.validate.RuleValidateResult;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 营销规则
 */
public class Rule extends RuleComponent {
    public Rule(RuleComponent condition, String promotion) {
        this.condition = condition;
        this.promotion = promotion;
        this.group = 0;
    }

    public Rule(RuleComponent condition, String promotion, int group){
        this.condition = condition;
        this.promotion = promotion;
        this.group = group;
    }

    public Rule() {
    }

    /**
     * 规则分组，不同group的规则可以叠加
     * 在MatchGroup.SequentialMatch模式下，按group的值从低到高一组一组分别运算
     */
    private int group = 0;
    public int getGroup(){
        return this.group;
    }

    public void setGroup(int group){
        this.group = group;
    }

    private RuleComponent condition;

    public RuleComponent getCondition() {
        return condition;
    }

    public void setCondition(RuleComponent condition) {
        this.condition = condition;
    }

    //region promotion
    private final static Pattern promotionPattern = Pattern.compile("\\-?\\d+(\\.\\d+)?(%|/\\d+)?");
    private final static Pattern packagePattern = Pattern.compile("y:(?<id>\\w+):(?<price>\\-?\\d+)");

    //验证优惠规则是否合法
    public static boolean validatePromotion(String promotionRule){
        promotionRule = promotionRule.replaceAll("\\s+","");
        //组成套商品的规则
        if(_isPackageRule(promotionRule)){
            return true;
        }
        return promotionRule.matches(promotionPattern.pattern());
    }

    private static boolean _isPackageRule(String promotionRule){
        return promotionRule.matches(packagePattern.pattern());
    }

    public boolean isPackageRule(){
        return _isPackageRule(this.promotion);
    }

    private String promotion;

    public String getPromotion(){
        return promotion;
    }

    /**
    * -50 固定优惠50
    * -10% 固定9折 支持小数比如 -0.5%
    * -10/100 每100元优惠10块
    * 80 固定多少钱，一口价
    * y:套商品id:套SKU格或折扣
    * */
    public void setPromotion(String promotion){
        String rule = promotion.replaceAll("\\s+","");
        if(!validatePromotion(rule)){
            throw new IllegalArgumentException("营销规则格式错误");
        }
        this.promotion = rule;
    }

    //endregion

    //region formatter

    @Override
    public String toRuleString() {
        return String.format("%s -> %s@%s",condition.toString(),promotion,group);
    }

    @Override
    public String toString() {
        return toRuleString();
    }

    //endregion

    //region functions

    /**
     * 快速检查所选商品组合是否满足本条规则的条件
     */
    public boolean check(List<Item> selectedItems){
        return condition.check(selectedItems);
    }

    /**
     * 检查所选商品组合是否满足本条规则的条件,并返回详细的匹配结果
     * @param selectedItems
     * @return
     */
    public RuleValidateResult validate(List<Item> selectedItems){
        return condition.validate(selectedItems);
    }

    @Override
    public Predicate<Item> getFilter() {
        return this.condition.getFilter();
    }

    public String getPackageId(){
        Matcher m = packagePattern.matcher(this.promotion);
        if(m.find()){
            return m.group("id");
        }
        return null;
    }

    //计算打折金额
    public int discount(List<Item> items) {
        if (items == null || items.isEmpty()
                || promotion == null || promotion.isEmpty()) {
            return 0;
        }

        String p = this.promotion;
        Matcher m = packagePattern.matcher(p);
        if(m.find()){
            p = m.group("price");
        }
        int total =  items.stream().map(Item::getPrice).reduce(0,Integer::sum);
        if(p.indexOf('/')>0){
            String[] parts = p.split("/");

            int factor = Integer.parseInt(parts[0]);
            int divider = Integer.parseInt(parts[1]);
            return total / divider * factor;
        }
        else if(p.indexOf('%')>0){
            //return total * Integer.parseInt(promotion.substring(0,promotion.length()-1))/100;
            return (int)(Float.parseFloat(p.substring(0,p.length()-1))/100 * total);
        }
        else{
            //fix Integer.parseInt("-0") = 0 的用法分歧
            if(p.equals("-0")){
                return 0;
            }
            int i =  Integer.parseInt(p);
            if( i>=0 ){
                return total>i ? i - total : 0;
            }
            else {
                return i;
            }
        }
    }

    /**
     * 仅计算应用范围过滤后商品优惠额
     */
    public int discountFilteredItems(List<Item> items){
        return this.discount(items.stream().filter(this.getFilter()).collect(Collectors.toList()));
    }

    //endregion
}
