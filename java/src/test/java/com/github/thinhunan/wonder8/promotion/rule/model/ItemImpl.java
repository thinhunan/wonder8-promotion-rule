package com.github.thinhunan.wonder8.promotion.rule.model;

/*
    千万不能加@Data，因为它生成的equals是比对所有属性相不相等，但是一个订单中有几张相同的商品是常见的
 */
public class ItemImpl extends Item {
    String categoryName;
    String spuName;
    String skuName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSpuName() {
        return spuName;
    }

    public void setSpuName(String spuName) {
        this.spuName = spuName;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }


    public ItemImpl(String categoryId, String categoryName,
                    String spuId, String spuName,
                    String skuId, String skuName,
                    int price){
        super(categoryId,spuId,skuId,price);
        this.categoryName = categoryName;
        this.spuName = spuName;
        this.skuName = skuName;
        this.price = price;
    }

    public ItemImpl(String categoryId, String categoryName,
                    String spuId, String spuName,
                    String skuId, String skuName,
                    int price, String seat){
        super(categoryId,spuId,skuId,price);
        this.categoryName = categoryName;
        this.spuName = spuName;
        this.skuName = skuName;
        this.seat = seat;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("ItemImpl(categoryId=");
        sb.append(this.category);
        sb.append(",programName=");
        sb.append(this.categoryName);
        sb.append(",spuId=");
        sb.append(this.SPU);
        sb.append(",sessionName=");
        sb.append(this.spuName);
        sb.append(",skuId=");
        sb.append(this.SKU);
        sb.append(",ticketName=");
        sb.append(this.skuName);
        sb.append(",price=");
        sb.append(this.price);
        sb.append(",seat=");
        sb.append(this.seat);
        sb.append(")");
        return sb.toString();
    }

    //千万不能加@Data，因为它生成的equals是比对所有属性相不相等，但是一个订单中有几张相同的商品是常见的
//    public boolean equals(Object o){
//        return this == o;
//    }
}