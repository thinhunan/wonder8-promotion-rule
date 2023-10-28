package com.github.thinhunan.wonder8.promotion.rule.model;

import com.github.thinhunan.wonder8.promotion.rule.model.comparator.TicketSeatComparator;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Validator {
    //@1 有新的玩法只需在这里加谓词和对应的含义
    private static HashMap<P, Function<Stream<Item>, Integer>> validators
            = new HashMap<P, Function<Stream<Item>, Integer>>(){
        {
            put(P.COUNT_CATEGORY, (items) -> (int)items.map(Item::getCategory).distinct().count());
            put(P.COUNT_SPU,(items) -> (int)items.map(Item::getSPU).distinct().count());
            put(P.COUNT_SKU,(items) -> (int)items.map(Item::getSKU).distinct().count());
            put(P.COUNT,(items) -> (int)items.count());
            put(P.SUM,(items) -> items.map(Item::getPrice).reduce(0,Integer::sum));
            put(P.ONE_SKU_COUNT,(items) ->  items.collect(
                    Collectors.groupingBy(
                        Item::getSKU,Collectors.counting()))
                    .values().stream()
                    .max(Long::compare)
                    .orElse(0L).intValue());
            put(P.ONE_SKU_SUM,(items)-> items.collect(
                    Collectors.groupingBy(
                        Item::getSKU,Collectors.summingInt(Item::getPrice)))
                    .values().stream()
                    .max(Integer::compare)
                    .orElse(0));
            put(P.ADJACENT_SEAT,(items)->{
                int count = 0, maxCount = 0;
                TicketSeatComparator comparator = new TicketSeatComparator();
                List<Item> sorted = items.sorted(comparator).collect(Collectors.toList());
                String lastSeat = "";
                for (Item t :sorted) {
                    String seat = t.getSeat();
                    if(lastSeat.equals("")){
                        count = maxCount = 1;
                    }
                    else{
                        if(comparator.isNextSeat(lastSeat,seat)){
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
            });
        }
    };

    public static Function<Stream<Item>,Integer> getValidator(P p){
        return validators.get(p);
    }

}
