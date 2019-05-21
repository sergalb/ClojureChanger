function a() {
    return 1;
}

function b() {
    return a();
}

function c(inp) {

    function cNested(x) {
        return x * x;
    }
    function other() {
        return inp / cNested(10);
    }
    other();
    return inp + b();
}

c(5);

