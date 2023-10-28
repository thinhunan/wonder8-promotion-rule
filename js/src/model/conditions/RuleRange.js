import { R } from '../enums'

class RuleRange {
    static rangePattern = /#(?<cate>[cpkz])(?<item>[^#\]]+)/g;

    constructor (type, id) {
        this.type = type;
        this.id = id;
        if (this.type === R.SEAT) {
            this._disassembleSeatRange();
        }
    }

    _rangeFrom;
    _rangeTo;

    _disassembleSeatRange = function () {
        const range = this.id.toUpperCase();
        const fromTo = range.split("-");
        this._rangeFrom = fromTo[0].split(":");
        if (fromTo.length > 1) {
            this._rangeTo = fromTo[1].split(":");
            if (this._rangeFrom.length !== this._rangeTo.length) {
                throw "座位范围规则的超始两部分格式需要一致";
            }
        }
    }

    isSeatInRange = function (item) {
        let seat = item.seat;
        if (seat == null || seat.length < 1) {
            return false;
        }

        seat = seat.toUpperCase();
        const seatParts = seat.split(":");
        //具体商品的座位信息要比规则的座位信息精度高才行
        if (seatParts.length < this._rangeFrom.length) {
            return false;
        }

        for (let i = 1; i <= this._rangeFrom.length; i++) {
            //因为规则不一定要区、排、座都写全，所以从后往前比
            const rangeFromPart = this._rangeFrom[this._rangeFrom.length - i];
            const seatPart = seatParts[seatParts.length - i];
            //规则不是范围，只是一个固定座位,或者不是可计算范围的A-Z0-9
            if (this._rangeTo == null || this._rangeTo.length == 0
                || !/^[A-Z0-9]+$/.test(rangeFromPart)) {

                if (rangeFromPart !== seatPart) {
                    return false;
                }
            } else {
                let rangeToPart = this._rangeTo[this._rangeTo.length - i];

                let rangeFromNumber = 0,
                    rangeToNumber = 0,
                    seatNumber = 0,
                    length = rangeFromPart.length;
                for (let j = 0; j < length; j++) {
                    rangeFromNumber += (rangeFromPart.charCodeAt(length - j - 1) - 48) * Math.pow(42, j);
                }
                length = rangeToPart.length;
                for (let j = 0; j < length; j++) {
                    rangeToNumber += (rangeToPart.charCodeAt(length - j - 1) - 48) * Math.pow(42, j);
                }
                length = seatPart.length;
                for (let j = 0; j < length; j++) {
                    seatNumber += (seatPart.charCodeAt(length - j - 1) - 48) * Math.pow(42, j);
                }

                if (seatNumber < rangeFromNumber || seatNumber > rangeToNumber) {
                    return false;
                }
            }
        }

        return true;
    }

    toRuleString () {
        if (this.type == R.ALL) {
            return this.type;
        } else {
            return this.type + this.id;
        }
    }

    toString () {
        return this.toRuleString();
    }
}

export default RuleRange;
