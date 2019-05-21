function foo(a) {
    var b = 42;
    function bar(c) {
        return a + b + c;
    }
    return bar(24);
}
foo(-1);