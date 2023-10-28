import {P,R} from '../model/enums';


test('Predict defines',()=>{
    expect(P.COUNT_SKU.name).toBe("countSKU");
});

test('RuleType defindes',()=>{
    expect(R.ALL).toBeDefined();
    expect(R.ALL).toBe("$");
});

test("enum",()=>{
    expect(R.ALL).toEqual("$");
    expect(R.ALL).toEqual(R.ALL);
    expect(R.ALL.toString()).toEqual("$");
    console.log(R.ALL);
    console.log(R.ALL.toString());
});

