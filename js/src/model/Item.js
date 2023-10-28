export default class Item {
    /**
     * 一个商品
     * @param {string} category
     * @param {string} SPU
     * @param {string} SKU
     * @param {int} price by cent/分
     * @param {string} seat,如 VIP:A:10 表示VIP区A排10座
     */
    constructor(category, SPU, SKU, price ,seat){
        this.category = category;
        this.SPU = SPU;
        this.SKU = SKU;
        this.price = price;
        if(seat) {
            this.seat = seat.toUpperCase();
        }
    }

}
