function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}
function validateNumber(numString, digit) {
    var re;
    if (hasValue(digit)) {
        re = new RegExp("^[0-9]{" + digit + "}$");
    } else {
        re = new RegExp("^[0-9]+$");
    }
    return re.test(numString);
}
