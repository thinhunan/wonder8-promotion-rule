/**
 * 条件规则范围的类型
 * @type {Readonly<{SAME: string, ALL: string, SKU: string, SPU: string, CATEGORY: string, SEAT: string}>}
 */
const R = Object.freeze({
    SKU: "k",
    CATEGORY: "c",
    SPU: "p",
    SEAT: "z",
    ALL: "$",
    SAME: "~"
});

export default R;
