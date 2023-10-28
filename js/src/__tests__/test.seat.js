import Item from '../model/./Item';
import Interpreter from "../Interpreter";
import TicketSeatComparator from '../model/comparators/TicketSeatComparator'
import Strategy from '../strategy'
import { MatchType } from '../model/enums'

function prepareItem (){
    let t1 = new Item('','','',100,'VIP:C:5');
    let t2 = new Item('','','',100,'VIP:C:13');
    let t3 = new Item('','','',200,'前台区:C:04');
    let t4 = new Item('','','',100,'VIP:B:13');
    let t5 = new Item('','','',100,'VIP:C:04');
    let items = [t1,t2,t3,t4,t5];
    items.push(new Item('','','',200,'一楼:VIP:C:6'));
    items.push(new Item('','','',200,'一楼:vip:c:08'));
    items.push(new Item('','','',200,'一楼:VIP:C:7'));
    items.push(new Item('','','',200,'一楼:vip:c:09'));
    return items;
}

test('testSeatRange',()=>{
    let t1 = new Item('','','',100,'VIP:C:5');
    let t2 = new Item('','','',100,'VIP:C:13');
    let t3 = new Item('','','',200,'前台区:C:04');
    let t4 = new Item('','','',100,'VIP:B:13');
    let t5 = new Item('','','',100,'VIP:C:04');
    let items = [t1,t2,t3,t4,t5];
    let r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].count(2)->-10%");
    expect(r.discountFilteredItems(items)).toEqual(-20)

    items.push(new Item('','','',200,'一楼:VIP:C:6'));
    expect(r.discountFilteredItems(items)).toEqual(-40);

    items.push(new Item('','','',200,'一楼:vip:c:07'));
    expect(r.discountFilteredItems(items)).toEqual(-60);
});

test('testSeatRangeMatch',()=>{
    let items = prepareItem();
    let r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].adjacentSeat(2)->-10%");
    let result = r.validate(items);
    console.log(result);
});

test("testComparator",()=>{
    let items = prepareItem();
    items.sort((t1,t2)=>{return TicketSeatComparator.compare(t1.seat,t2.seat)});
    for (const t of items){
        console.log(t.seat);
    }
});

test("testAdjacentSeatMath", ()=>{
    const r = Interpreter.parseString("[#zVIP:C:5-VIP:C:15].adjacentSeat(2)->-10%");
    let items = prepareItem();
    let bestMatch = Strategy.bestChoice([r], items, MatchType.MultiRule);
    expect(bestMatch.matches.length).toEqual(2);
    for (let m of bestMatch.matches) {
        console.log("group:"+m.items[0].seat + " & " + m.items[1].seat);
    }
    bestMatch = Strategy.bestChoice([r],items,MatchType.OneTime);
    expect(bestMatch.matches.length).toEqual(1);
    for (let m of bestMatch.matches) {
        console.log("group:"+m.items[0].seat + " & " + m.items[1].seat);
    }
});

