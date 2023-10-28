package com.github.thinhunan.wonder8.promotion.rule.model;

public class RuleRange{
    public RuleRange(R type, String id) {
        this.type = type;
        this.id = id;
        if(type == R.SEAT){
            disassembleSeatRange();
        }
    }


    public RuleRange() {
    }

    public R getType() {
        return type;
    }

    public void setType(R type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    R type;
    String id;

    String toRuleString() {
        if(type == R.ALL){
            return type.toString();
        }
        else{
            return type.toString() + id;
        }
    }

    String[] rangeFrom;
    String[] rangeTo;

    void disassembleSeatRange(){
        String range = this.id.toUpperCase();
        String[] fromTo = range.split("-");
        rangeFrom = fromTo[0].split(":");
        if(fromTo.length > 1){
            rangeTo = fromTo[1].split(":");
            if(rangeFrom.length != rangeTo.length){
                throw new IllegalArgumentException("座位范围规则的超始两部分格式需要一致");
            }
        }
    }

    public boolean isSeatInRange(Item item){
        String seat = item.getSeat();
        if(seat == null || seat.isEmpty()){
            return false;
        }

        seat = seat.toUpperCase();
        String[] seatParts = seat.split(":");
        //具体商品的座位信息要比规则的座位信息精度高才行
        if(seatParts.length < rangeFrom.length){
            return false;
        }

        for (int i = 1; i <= rangeFrom.length; i++) {
            //因为规则不一定要区、排、座都写全，所以从后往前比
            String rangeFromPart = rangeFrom[rangeFrom.length - i];
            String seatPart = seatParts[seatParts.length - i];
            //规则不是范围，只是一个固定座位,或者不是可计算范围的A-Z0-9
            if (rangeTo == null || rangeTo.length == 0
                    || !rangeFromPart.matches("^[A-Z0-9]+$") ) {

                if (!rangeFromPart.equals(seatPart)) {
                    return false;
                }
            }
            else{
                String rangeToPart = rangeTo[rangeTo.length - i];

                int rangeFromNumber = 0,
                        rangeToNumber = 0,
                        seatNumber = 0;
                int length = rangeFromPart.length();
                for(int j = 0 ; j < length ; j++){
                    rangeFromNumber += (rangeFromPart.charAt(length - j -1 )-48) * (int)Math.pow(42,j);
                }
                length = rangeToPart.length();
                for(int j = 0 ; j < length ; j++){
                    rangeToNumber += (rangeToPart.charAt(length - j - 1) - 48) * (int) Math.pow(42, j);
                }
                length = seatPart.length();
                for(int j = 0 ; j < length ; j++){
                    seatNumber += (seatPart.charAt(length -j -1) -48) *(int)Math.pow(42,j);
                }

                if(seatNumber < rangeFromNumber || seatNumber > rangeToNumber){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString(){
        return this.toRuleString();
    }
}
