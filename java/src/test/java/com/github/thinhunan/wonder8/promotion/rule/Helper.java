package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.ItemImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author tanzhenlin
 * @Date 2022/7/13 17:16
 **/

public class Helper {
    /**
     *
     * @return c01 * 5, c02 * 3; k02*5, k03*2, k04*1, 10000 * 5, 121200 * 3
     */
    public static List<Item> getTestItems() {
        return Arrays.asList(
                new ItemImpl("01", "spuId", "01", "skuId", "02", "price", 10000),
                new ItemImpl("01", "spuId", "01", "skuId", "02", "price", 10000),
                new ItemImpl("01", "spuId", "01", "skuId", "02", "price", 10000),
                new ItemImpl("01", "spuId", "01", "skuId", "02", "price", 10000),
                new ItemImpl("01", "spuId", "01", "skuId", "02", "price", 10000),
                new ItemImpl("02", "spuId", "02", "skuId", "03", "price", 121200),
                new ItemImpl("02", "spuId", "02", "skuId", "03", "price", 121200),
                new ItemImpl("02", "spuId", "02", "skuId", "04", "price", 121200));
    }

    public static List<Item> getSeatedTickets(){
        return new ArrayList<Item>(){{
            add(new ItemImpl("01", "", "01", "", "02", "", 10000,"二楼:A:1:1"));
            add(new ItemImpl("01", "", "01", "", "02", "", 10000,"二楼:A:1:3"));
            add(new ItemImpl("01", "", "01", "", "02", "", 10000,"二楼:A:1:2"));
            add(new ItemImpl("01", "", "01", "", "02", "", 10000,"二楼:A:1:5"));
            add(new ItemImpl("01", "", "01", "", "02", "", 10000,"二楼:A:1:4"));
            add(new ItemImpl("02", "", "02", "", "03", "", 121200,"VIP:A:1:4"));
            add(new ItemImpl("02", "", "02", "", "03", "", 121200,"VIP:A:1:2"));
            add(new ItemImpl("02", "", "02", "", "04", "", 121200,"VIP:A:1:3"));
            add(new ItemImpl("02", "", "02", "", "04", "", 121200,null));
            add(new ItemImpl("02", "", "02", "", "04", "", 121200,""));
        }};
    }
}
