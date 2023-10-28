import TicketSeatComparator from '../comparators/TicketSeatComparator'
/**
 * 条件规则的判断谓词
 */
const P = Object.freeze({
    SUM: {
        name: "sum",
        handler:function(items){
            return items.map(t=>t.price).reduce((a,b)=>a+b,0);
        },
        toString: function (){
            return this.name;
        }
    },
    COUNT_CATEGORY: {
        name: "countCate",
        handler: function(items){
            return [...new Set(items.map(t=>t.category))].length;
        },
        toString: function (){
            return this.name;
        }
    },
    COUNT_SPU: {
        name: "countSPU",
        handler: function(items){
            return [...new Set(items.map(t=>t.SPU))].length;
        },
        toString: function (){
            return this.name;
        }
    },
    COUNT_SKU: {
        name: "countSKU",
        handler: function(items){
            return [...new Set(items.map(t=>t.SKU))].length;
        },
        toString: function (){
            return this.name;
        }
    },
    COUNT: {
        name: "count",
        handler: function(items){
            return items.length;
        },
        toString: function (){
            return this.name;
        }
    },
    ONE_SKU_COUNT: {
        name: "oneSKU",
        handler: function(items){
            if(items.length < 1){
                return 0;
            }
            let map = new Map();
            for (const item of items) {
                let count = map.get(item.SKU);
                if(count){
                    map.set(item.SKU,count + 1);
                }
                else{
                    map.set(item.SKU,1);
                }
            }
            return [...map.values()].sort().reverse()[0];
        },
        toString: function (){
            return this.name;
        }
    },
    ONE_SKU_SUM: {
        name: "oneSKUSum",
        handler: function(items){
            if(items.length < 1){
                return 0;
            }
            let map = new Map();
            for (const item of items) {
                let sum = map.get(item.SKU);
                if(sum){
                    map.set(item.SKU,sum + item.price);
                }
                else{
                    map.set(item.SKU,item.price);
                }
            }
            return [...map.values()].sort().reverse()[0];
        },
        toString: function (){
            return this.name;
        }
    },
    ADJACENT_SEAT: {
        name: "adjacentSeat",
        handler: function(items){
            let count = 0, maxCount = 0;
            let lastSeat = "";

            const sorted = items.sort((a,b)=>TicketSeatComparator.compare(a.seat,b.seat));
            for (const t of sorted) {
                const seat = t.seat;
                if(lastSeat === ""){
                    count = maxCount = 1;
                }
                else{
                    if(TicketSeatComparator.isNextSeat(lastSeat,seat)){
                        count ++;
                        if(count > maxCount){
                            maxCount = count;
                        }
                    }
                    else{
                        count = 1;
                    }
                }
                lastSeat = seat;
            }
            return maxCount;
        },
        toString: function (){
            return this.name;
        }
    },

    parseString: function(s){
        switch (s){
            case this.COUNT_CATEGORY.name:
                return this.COUNT_CATEGORY;
            case this.SUM.name:
                return this.SUM;
            case this.COUNT.name:
                return this.COUNT;
            case this.COUNT_SKU.name:
                return this.COUNT_SKU;
            case this.ONE_SKU_COUNT.name:
                return this.ONE_SKU_COUNT;
            case this.COUNT_SPU.name:
                return this.COUNT_SPU;
            case this.ONE_SKU_SUM.name:
                return this.ONE_SKU_SUM;
            case this.ADJACENT_SEAT.name:
                return this.ADJACENT_SEAT;
            default:
                throw "Illegal predicate string: " + s;
        }
    }
});

export default P;
