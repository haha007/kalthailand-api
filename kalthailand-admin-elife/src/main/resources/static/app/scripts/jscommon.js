//////////////////////////////////////////////////////////////////////////////////////////////////
//
// This common file has nothing to do with other JavaScript framework, just pure JavaScript function.
//
///////////////////////////////////////////////////////////////////////////////////////////////////

Array.prototype.remove = function (item) {
    var j = 0;
    while (j < this.length) {
        // alert(originalArray[j]);
        if (this[j] == item) {
            this.splice(j, 1);
        } else {
            j++;
        }
    }
};

Array.prototype.sortByField = function (fieldName, asc) {

    var compareFn = function (a, b) {
        var valA = getField(a, fieldName);
        var valB = getField(b, fieldName);
        var result = 0;
        if (hasValue(valA)) {
            if (hasValue(valB)) {
                if (valA < valB) {
                    result = -1;
                } else if (valA > valB) {
                    result = 1;
                }
            } else {
                result = 1;
            }
        } else {
            if (hasValue(valB)) {
                result = -1;
            } else {
                result = 0;
            }
        }

        if (asc == -1) {
            result = result * asc;
        }
        return result;
    };
    this.sort(compareFn);
}
/**
 * http://stackoverflow.com/questions/6491463/accessing-nested-javascript-objects-with-string-key
 * @param object
 * @param fieldExpression
 * Example:
 * You have an object like this.
 * <pre>
 * var someObject = {
 *   'part3' : [
 *       {
 *           'name': 'Part 3A',
 *           'size': '10',
 *           'qty' : '20'
 *       }, {
 *           'name': 'Part 3B',
 *           'size': '5',
 *           'qty' : '20'
 *       }, {
 *           'name': 'Part 3C',
 *           'size': '7.5',
 *           'qty' : '20'
 *       }
 *   ]
 * };
 * </pre>
 * So getField(someObject, "part3[0].name") will return "Part 3A".
 * @returns
 */
function getField(object, fieldExpression) {
    fieldExpression = fieldExpression.replace(/\[(\w+)\]/g, '.$1'); // convert indexes to properties
    fieldExpression = fieldExpression.replace(/^\./, '');           // strip a leading dot
    var a = fieldExpression.split('.');
    for (var i = 0, n = a.length; i < n; ++i) {
        var fieldName = a[i];
        if (hasValue(object) && fieldName in object) {
            object = object[fieldName];
        } else {
            return;
        }
    }
    return object;
}

String.prototype.beginWithRegExp = function (regExp) {
    var rs = false;
    if (pathname.match(regExp)) {
        rs = true;
    }
    return rs;
    //return(this.indexOf(needle) == 0);
};
/**
 * @param s This method check whether the string start with @param s or not.
 * @returns {Boolean}
 */
String.prototype.beginWith = function (s) {
    var rs = (this.substr(0, s.length) == s);
    return rs;
};

function inheritPrototype(prototype) {
    function F() {
    }; // Dummy constructor
    F.prototype = prototype;
    return new F();
}
function inherit(parentClazz, childClazz) {
    childClazz.prototype = inheritPrototype(parentClazz.prototype);
    childClazz.prototype.constructor = childClazz;
}
function isArray(obj) {
    return ( Object.prototype.toString.call(obj) === '[object Array]' );
}
/**
 * @param variable
 * @returns {Boolean} Note: if variable is an empty string (""), it still return true;
 */
function hasValue(variable) {
    return (typeof variable !== 'undefined') && (variable !== null);
}
function hasValueNotEmpty(variable) {
    return (typeof variable !== 'undefined') && (variable !== null) && (variable.length !== 0);
}
function isUndefined(value) {
    return typeof value == 'undefined';
}
/**
 * Just for testing
 * @param msecs
 */
function wait(msecs) {
    var start = new Date().getTime();
    var cur = start;
    while (cur - start < msecs) {
        cur = new Date().getTime();
    }
}

function numericOnly(field) {
    var num = field.value;
    var len = num.length;
    var string = num.substring(len - 1, len);

    if (string == " ")
        field.value = num.replace(string, "");

    if (isNaN(num))
        field.value = num.replace(string, "");
}

function hasValue(value) {
    return (value !== undefined && value != null);
}
