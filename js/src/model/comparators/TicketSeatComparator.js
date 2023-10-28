/**
 * TicketSeat examples:VIP:C01:08 VIP:C1:7 VIP:C01:15
 * 可以看到有字符有数字，数字可能填充了0，可能没有填充,字符部分按字符方式比较，数字部分按数字方式比较
 */
export default class TicketSeatComparator {

    static #splitSeat(seat) {
        let seatParts = [];
        let numberBegin = -1;
        for (let i = 0; i < seat.length; i++) {
            const c = seat.charAt(i);
            if (c < '0' || c > '9') {
                if (numberBegin > -1) {
                    seatParts.push(seat.substring(numberBegin, i + 1));
                }
                numberBegin = -1;
                seatParts.push(c);
            } else {
                if (numberBegin === -1) {
                    numberBegin = i;
                }
            }
            if (i === seat.length - 1) {
                if (numberBegin > -1) {
                    seatParts.push(seat.substring(numberBegin, i + 1));
                }
            }
        }
        return seatParts;
    }

    static compare(seat1,seat2){
        if(!seat2 || seat2.length < 1){
            return -1;
        }
        if(!seat1 || seat1.length < 1){
            return 1;
        }
        const seat1Parts = this.#splitSeat(seat1);
        const seat2Parts = this.#splitSeat(seat2);
        for (let i = 0; i < seat1Parts.length; i++) {
            if(i === seat2Parts.length){
                return 1;
            }
            const p1 = seat1Parts[i];
            const p2 = seat2Parts[i];
            if(/^\d+$/.test(p1) && /^\d+$/.test(p2)){
                const compare =  parseInt(p1) - parseInt(p2);
                if(compare != 0){
                    return compare;
                }
            }
            else {
                const compared = p1.localeCompare(p2);
                if(compared != 0){
                    return compared;
                }
            }
        }

        if(seat2Parts.length > seat1Parts.length){
            return -1;
        }

        return 0;
    }

    static #stringAddOne(s){
        for(let i = s.length - 1; i > -1 ; i--){
            const c = s.charAt(i);
            if((c >= '0' && c < '9') || c >= 'A' && c < 'Z'){
                let chars = s.split('');
                chars[i] = String.fromCharCode(s.charCodeAt(i) + 1);
                s = chars.join('');
                break;
            }
            else if(c == '9'){
                let chars = s.split('');
                chars[i] = '0';
                if( i === 0 || !/\d/.test(chars[i-1])){
                    chars.splice(i,0,"1");
                }
                s = chars.join('');
            }
            else{
                break;
            }
        }
        return s;
    }

    static isNextSeat(seat1, seat2) {
        const addOne = this.#stringAddOne(seat1);
        return this.compare(seat2, addOne) == 0;
    }
}
