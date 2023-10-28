package com.github.thinhunan.wonder8.promotion.rule;

import com.github.thinhunan.wonder8.promotion.rule.model.Item;
import com.github.thinhunan.wonder8.promotion.rule.model.comparator.TicketSeatComparator;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author tanzhenlin
 * @Date 2022/7/13 17:18
 **/

public class OtherTest {

    @Test
    public void testSummarizing(){
        List<Item> items = Helper.getTestItems();
        items.stream()
                .collect(
                        Collectors.groupingBy(Item::getSKU,Collectors.summingInt(Item::getPrice))
                )
                .forEach((id,sum)-> System.out.println(id + " summing: "+ sum ));
        items.stream()
                .collect(Collectors.groupingBy(Item::getSKU,Collectors.summarizingInt(Item::getPrice)))
                .forEach((id,statistics)-> System.out.println(id + "summarizing:" + statistics));
    }

    @Test
    public void testChar(){
        for (int i = 48; i < 123; i++) {
            System.out.println(i+":" + (char)i);
        }
    }

    @Test
    public void testRegex(){
//        Pattern pattern = Pattern.compile("[^\\]#]+");
//        Matcher m = pattern.matcher("#z贵宾区:a:10#z平民区:b:1");
//        while(m.find()){
//            System.out.println(m.group(0));
//        }
//
//        Pattern p2 = Pattern.compile("[0-9]+[a-z]+|[a-z]+[0-9]+");
//        Matcher m2 = p2.matcher("abc123,123abc");
//        while(m2.find()){
//            System.out.println(m2.group(0));
//        }
//
//        String testString = "aa-bb";
//        for (String s :
//                testString.split("-"))   {
//            System.out.println(s);
//        }
//        testString = "aaa";
//        String[] parts = testString.split("-");
//        System.out.println(parts.length);
//
//        System.out.println("A01".matches("^[A-Z0-9]+$"));
        String from = "A01";
        String to = "B10";
        String test = "A09";
        boolean in = stringInRange(from, to, test);
        System.out.println(in);

        from = "A";
        to = "Z";
        test = "C";
        in = stringInRange(from,to,test);
        System.out.println(in);

        System.out.println(stringInRange("B10","Z20","C03"));
    }

    private boolean stringInRange(String from, String to, String test) {
        int length = from.length();
        Integer rangeFromNumber = 0;
        Integer rangeToNumber = 0;
        Integer seatNumber = 0;
        for(int j = 0; j < from.length() ; j++){
            rangeFromNumber += (from.charAt(length - j -1 )-48) * (int)Math.pow(42,j);
            rangeToNumber += (to.charAt(length -j -1)-48) * (int)Math.pow(42,j);
            seatNumber += (test.charAt(length -j -1) -48) *(int)Math.pow(42,j);
        }
        return seatNumber>=rangeFromNumber && seatNumber <= rangeToNumber;
    }


    @Test
    public  void testReplaceZero(){
        String test= "VIP:C01:08";
        String expect = "VIP:C1:8";
        test = test.replaceAll("(?<=\\D)0+(?=\\d)","");
        System.out.println(test);
        System.out.println("VIP:C01:0108".replaceAll("(?<=\\D)0+(?=\\d)",""));
        System.out.println("VIP:C01:01080".replaceAll("(?<=\\D)0+(?=\\d)",""));
    }

    @Test
    public void testHashMapClone(){
        List<Item> testItems = Helper.getTestItems();
        Map<Integer,List<Item>> map1 = new HashMap<>();
        map1.put(1, testItems);
        Map<Integer,List<Item>> map2 = new HashMap<>(map1);
        map2.put(1, testItems.stream().filter(t->t.getPrice()>10000).collect(Collectors.toList()));
        System.out.println(map1.size());
        System.out.println(map2);
        System.out.println(map2.size());
    }

    @Test
    public void testTicketSort(){
        List<Item> filtered = Helper.getSeatedTickets();
        Comparator<Item> comparator = Comparator.comparingInt(Item::getPrice);
        comparator = comparator.reversed();
        Comparator<Item> seatComparator = new TicketSeatComparator().reversed();
        comparator = comparator.thenComparing(seatComparator);
        List<Item> sorted = filtered.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        for (int i = 0; i <filtered.size(); i++) {
            System.out.println(sorted.get(i).getSeat());
        }
    }

}
