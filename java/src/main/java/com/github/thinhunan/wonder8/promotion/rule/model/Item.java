package com.github.thinhunan.wonder8.promotion.rule.model;

/*
    千万不能加@Data，因为它生成的equals是比对所有属性相不相等，但是一个订单中有几张相同的商品是常见的
 */
public class Item {
    String category;
    String SPU;
    String SKU;
    String seat;

    /// 单位：分 (by cent)
    int price;

    public Item(){}

    /*
     * @param price by cent(分)
     */
    public Item(String category, String spu, String sku,int price){
        this.category = category;
        this.SPU = spu;
        this.SKU = sku;
        this.price = price;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        if(seat != null) {
            this.seat = seat.toUpperCase();
        }
        else{
            this.seat = null;
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSPU() {
        return SPU;
    }

    public void setSPU(String SPU) {
        this.SPU = SPU;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    /*
     * 单位：分 (by cent)
     */
    public int getPrice() {
        return price;
    }

    /*
     * 单位：分 (by cent)
     */
    public void setPrice(int price) {
        this.price = price;
    }


    /*
     Don't use @Data，because auto-generated equals method compares object's all properties are equal or not.
        public boolean equals(Object o){
            return this == o;
        }
    */
}

