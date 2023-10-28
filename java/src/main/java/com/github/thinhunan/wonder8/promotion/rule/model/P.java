package com.github.thinhunan.wonder8.promotion.rule.model;

//predict 判断动词
public enum P {
    /**
     * 计算总价
     */
    SUM,

    /**
     * 计算有几个类别
     */
    COUNT_CATEGORY,

    /**
     * 计算有几种SPU
     */
    COUNT_SPU,

    /*
     * 计算有几种SKU
     */
    COUNT_SKU,

    /**
     * 计算item的数量
     */
    COUNT,

    /**
     * 任一SKU的数量达标
     */
    ONE_SKU_COUNT,

    /**
     * 任一SKU的总价达标
     */
    ONE_SKU_SUM,

    /**
     * 相邻座位的套商品
     */
    ADJACENT_SEAT;

    static String errorMessage = "expected sum or count or countCate/SPU or oneSKU or oneSKUSum or adjacentSeat";

    @Override
    public String toString() {
        switch (this){
            case SUM:return "sum";
            case COUNT:return "count";
            case COUNT_CATEGORY:return "countCate";
            case COUNT_SPU:return "countSPU";
            case COUNT_SKU:return "countSKU";
            case ONE_SKU_COUNT:return "oneSKU";
            case ONE_SKU_SUM:return "oneSKUSum";
            case ADJACENT_SEAT:return "adjacentSeat";
            default:
                throw new IllegalStateException(errorMessage);
        }
    }

    public static P parseString(String s){
        switch (s){
            case "sum":return P.SUM;
            case "count": return P.COUNT;
            case "countSPU": return P.COUNT_SPU;
            case "countSKU": return P.COUNT_SKU;
            case "countCate": return P.COUNT_CATEGORY;
            case "oneSKU": return P.ONE_SKU_COUNT;
            case "oneSKUSum": return P.ONE_SKU_SUM;
            case "adjacentSeat": return P.ADJACENT_SEAT;
            default:
                throw new IllegalStateException(errorMessage);
        }
    }
}

