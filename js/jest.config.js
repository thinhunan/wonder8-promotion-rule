module.exports = {
    roots: [
        "src/__tests__"
    ],
    testRegex: '__tests__/(.+)$',
    transform: {
        "^.+\\.js?$": "babel-jest"
    },
    moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
};
