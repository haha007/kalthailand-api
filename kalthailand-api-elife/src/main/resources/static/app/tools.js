(function () {
    "use strict";
    var tools = {
        renderDateAndTime : function(datetime) {
            console.log(datetime);
            var date = new Date(Date.parse(datetime));
            var strDate = date.toDateString().substring(4, date.length);
            var strTime = formatTime(datetime);
            strDate += " " + strTime;
            return strDate;
        },

        formatTime : function(datetime) {
            var date = new Date(Date.parse(datetime));
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var ampm = hours >= 12 ? 'pm' : 'am';
            hours = hours % 12;
            hours = hours ? hours : 12; // the hour '0' should be '12'
            minutes = minutes < 10 ? '0' + minutes : minutes;
            return hours + ':' + minutes + ampm;
        }
    };

    (function expose() {
        tools = tools;
    })();
})();
