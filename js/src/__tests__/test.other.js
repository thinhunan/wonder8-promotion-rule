
//#region extend objects
import Strategy from '../strategy'
import Interpreter from '../Interpreter'

class A{
    a;
    b;
    constructor(a,b) {
        this.a = a;
        this.b = b;
    }
}

test('extend object',()=>{
    let a1 = new A('this is a','this is b');
    let a2 = new A('this is a2\'s a','this is a2\'s b');

    // Object.assign( A.prototype,{
    //     getc:function(){return this.a;},
    //     setc:function(v){this.a = v}
    //   });
    // let a3 = new A('3','3');
    // console.log(a1.getc());
    // console.log(a3.getc());

    // Object.defineProperties(A.prototype,{ //这个写法和下面这个写法效果一样
    Object.defineProperties(a1.__proto__,{
        "c":{
            get:function (){return this.a;},
            set:function (v){this.a = v;}
        }
    });
    let a3 = new A('3','3');//注意a1,a2是扩展前的对象，a3是扩展后的对象
    console.log(a1.c);
    expect(a1.a).toEqual(a1.c);
    console.log(a3.c);
    expect(a3.a).toEqual(a3.c);
});

//下面演示的是从业务接口拿到了数据，但是数据的模型并不匹配引擎接口定义，这个时候我们有两种选择：
//1.创建新的一批Item 结构对象，把原数据中的值拷过来，这种方式造成的问题是多了一倍的对象开销，同时相同意义的数据多份拷贝，当数据变化时需要维护多份数据的同步
//2.利用动态语言的特性，扩展已有业务模型数据对象，添加引擎所需的属性，即可直接使用原有数据对象
//下面演示了两种搞法:
//1.为每个对象扩展属性；
test("extend each biz object for engine", () => {
    const rule = ["[#k62400cec34fa961d487cbf26].count(1)&[#k62400cec34fa961d487cbf2a].count(1)&[#k62400e0934fa961d487cc209].count(1)&[#k62400e0934fa961d487cc20f].count(1) -> 0"];
    const items = [
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc20f", "pricef": 1.99 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc20f", "pricef": 1.99 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc209", "pricef": 1.00 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc209", "pricef": 1.00 },

        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf26", "pricef": 1.00 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf26", "pricef": 1.00 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf2a", "pricef": 1.99 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf2a", "pricef": 1.99 }];

    items.forEach(t=>{
        Object.defineProperties(t,{
            'category':{
                get:function (){return t.cate;}
            },
            'SPU':{
                get:function(){return t.spu;}
            },
            'SKU':{
                get:function (){return t.sku;}
            },
            'price':{
                get:function (){return t.pricef * 100<<0;}
            }
        });
    });
    const result = Strategy.bestMatch(rule.map(r => Interpreter.parseString(r)), items);
    expect(-1196).toEqual(result.totalDiscount());
});

//2.基于prototype为一批相同对象，做一次原型扩展即可
test("extend biz object on prototype", () => {
    const rule = ["[#k62400cec34fa961d487cbf26].count(1)&[#k62400cec34fa961d487cbf2a].count(1)&[#k62400e0934fa961d487cc209].count(1)&[#k62400e0934fa961d487cc20f].count(1) -> 0"];
    const items = [
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc20f", "pricef": 1.99 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc20f", "pricef": 1.99 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc209", "pricef": 1.00 },
        { "cate": "62400d5c34fa961d487cc058", "spu": "62400d66d1812e6a99faee63", "sku": "62400e0934fa961d487cc209", "pricef": 1.00 },

        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf26", "pricef": 1.00 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf26", "pricef": 1.00 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf2a", "pricef": 1.99 },
        { "cate": "6240075e34fa961d487cb312", "spu": "62400784d1812e6a99fae18c", "sku": "62400cec34fa961d487cbf2a", "pricef": 1.99 }];


    Object.defineProperties(items[0].__proto__,{
        'category':{
            get:function (){return this.cate;}
        },
        'SPU':{
            get:function(){return this.spu;}
        },
        'SKU':{
            get:function (){return this.sku;}
        },
        'price':{
            get:function (){return this.pricef * 100<<0;}
        }
    });
    const result = Strategy.bestMatch(rule.map(r => Interpreter.parseString(r)), items);
    console.log(result.totalDiscount());
    expect(-1196).toEqual(result.totalDiscount());
});

//#endregion


