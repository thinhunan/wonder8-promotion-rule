package com.github.thinhunan.wonder8.promotion.rule.model.comparator;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TicketSeat examples:VIP:C01:08 VIP:C1:7 VIP:C01:15
 * 可以看到有字符有数字，数字可能填充了0，可能没有填充,字符部分按字符方式比较，数字部分按数字方式比较
 */
public class TicketSeatComparator implements Comparator<Item> {

    @Override
    public int compare(Item o1, Item o2) {
        String seat1 = o1.getSeat();
        String seat2 = o2.getSeat();
        return compareSeat(seat1,seat2);
    }

    private List<String> splitSeat(String seat) {
        List<String> seatParts = new ArrayList<>();
        int numberBegin = -1;
        for (int i = 0; i < seat.length(); i++) {
            char c = seat.charAt(i);
            if(c<'0' || c > '9'){
                if(numberBegin > -1){
                    seatParts.add(seat.substring(numberBegin,i+1));
                }
                numberBegin = -1;
                seatParts.add(String.valueOf(c));
            }
            else{
                if(numberBegin == -1) {
                    numberBegin = i;
                }
            }
            if(i == seat.length() -1){
                if(numberBegin > -1){
                    seatParts.add(seat.substring(numberBegin,i+1));
                }
            }
        }
        return seatParts;
    }

    public int compareSeat(String seat1,String seat2){
        if(seat2 == null || seat2.isEmpty()){
            return -1;
        }
        if(seat1 == null || seat1.isEmpty()){
            return 1;
        }
        List<String> seat1Parts = splitSeat(seat1);
        List<String> seat2Parts = splitSeat(seat2);
        for (int i = 0; i < seat1Parts.size(); i++) {
            if(i == seat2Parts.size()){
                return 1;
            }
            String p1 = seat1Parts.get(i);
            String p2 = seat2Parts.get(i);
            if(p1.matches("^\\d+$")
                    && p2.matches("^\\d+$")){
                int compare =  Integer.parseInt(p1) - Integer.parseInt(p2);
                if(compare != 0){
                    return compare;
                }
            }
            else {
                int compared = p1.compareTo(p2);
                if(compared != 0){
                    return compared;
                }
            }
        }

        if(seat2Parts.size() > seat1Parts.size()){
            return -1;
        }

        return 0;
    }

    public boolean isNextSeat(String seat1, String seat2) {
        if(seat1 == null || seat2 == null || seat1.isEmpty() || seat2.isEmpty()){
            return false;
        }
        String addOne = stringAddOne(seat1);
        return this.compareSeat(seat2, addOne) == 0;
    }

    private String stringAddOne(String s){
        StringBuilder sb = new StringBuilder(s);
        for(int i = s.length() - 1; i > -1 ; i--){
            char c = s.charAt(i);
            if((c >= '0' && c < '9') || c >= 'A' && c < 'Z'){
                sb.replace(i,i+1,String.valueOf((char)(c+1)));
                break;
            }
            else if(c == '9'){
                sb.replace(i,i+1,String.valueOf('0'));

                if(i == 0 || s.charAt(i-1) <'0' || s.charAt(i-1) > '9') {
                    sb.insert(i, "1");
                }
            }
            else{
                break;
            }
        }
        return sb.toString();
    }
}
