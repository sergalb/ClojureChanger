function foo(a) {
    function bar(b) {
        return b + a;
    }
    return bar(11 - 5);
}
foo(1);