package com.github.thinhunan.wonder8.promotion.rule.model;

// RangeType 规则应用的范围
public enum R {
    /**
     * 所有选商品进入运算范围-$
     */
    ALL,
    /**
     * 计算指定分类-c
     */
    CATEGORY,
    /**
     * 计算指定SPU-p
     */
    SPU,
    /**
     * 计算指定SKU-k
     */
    SKU,
    /**
     * 计算指定座位
     */
    SEAT,
    /**
     * 与上一条规则的范围相同-~
     */
    SAME;

    @Override
    public String toString() {
        switch (this){
            case SKU:return "k";
            case CATEGORY:return "c";
            case SPU:return "p";
            case SEAT:return "z";
            case ALL:return "$";
            case SAME:return "~";
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
    
    public static R parseString(String s){
        switch (s){
            case "P":
            case "p":
                return R.SPU;
            case "C":
            case "c":
                return R.CATEGORY;
            case "K":
            case "k":
                return R.SKU;
            case "Z":
            case "z":
                return R.SEAT;
            case "$":
                return R.ALL;
            case "~":
                return R.SAME;
            default:
                throw new IllegalStateException("expected $ or ~ or c or p or k or z");
        }
    }
}
