var d = 0;
function foo(a) {
    var b = 42;
    function bar() {
        var c = a % 2;
        function baz(d) {
            return a * (d + c);
        }
        return baz(a + (-0));
    }
    return bar();
}

foo(1);