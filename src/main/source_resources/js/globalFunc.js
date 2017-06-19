  if (!String.prototype.format) {
            String.prototype.format = function() {
                var args = arguments;
                return this.replace(/{(\d+)}/g, function(match, number) {
                    return typeof args[number] != 'undefined' ? args[number] : match;
                });
            };
        }
        
function inIframe() {
    try {
        return window.self !== window.top;
    } catch (e) {
        return true;
    }
    return false;
}

if (inIframe())
    $("body").addClass("frame");

function toArray(object) {
    return angular.isArray(object) ? object : Object.keys(object).map(function(key) {
        return object[key];
    });
}
var MAX_UPLOAD_FILE_SIZE_BYTES = 100 * 1000 * 1024;
var TIME_FOR_WAITING_ANSWER_FROM_TENANT = 30000;
var daysName = {},
    hoursName = {},
    minutesName = {};
var dayName = {},
    hourName = {},
    minuteName = {};
var daysNameShort = {},
    hoursNameShort = {},
    minutesNameShort = {};
var dayNameShort = {},
    hourNameShort = {},
    minuteNameShort = {};
var endName = {};

daysName['ua'] = ' днів';
dayName['ua'] = ' день';
daysName['en'] = ' day';
dayName['en'] = ' days';
daysName['ru'] = ' дней';
dayName['ru'] = ' день';

hoursName['ua'] = ' годин';
hourName['ua'] = ' годину';
hoursName['en'] = ' hour';
hourName['en'] = ' hours';
hoursName['ru'] = ' часов';
hourName['ru'] = 'час';

minutesName['ua'] = ' хвилин ';
minuteName['ua'] = ' хвилину ';
minutesName['en'] = ' minutes ';
minuteName['en'] = ' minute ';
minutesName['ru'] = ' минут ';
minuteName['ru'] = ' минуту ';

daysNameShort['ua'] = ' дн.';
daysNameShort['en'] = ' d.';
daysNameShort['ru'] = ' дн.';

hoursNameShort['ua'] = ' год.';
hoursNameShort['en'] = ' h';
hoursNameShort['ru'] = ' час.';

minutesNameShort['ua'] = ' хв. ';
minutesNameShort['en'] = ' min ';
minutesNameShort['ru'] = ' мин. ';

endName['ua'] = " тому";
endName['en'] = " ago";
endName['ru'] = " спустя";

var ROOM_PERMISSIONS = {
    ADD_USER: 1,
    REMOVE_USER: 2
};

var checkIfToday = function(inputDateLong) {
    var inputDate = new Date(inputDateLong);
    var todaysDate = new Date();
    return inputDate.setHours(0, 0, 0, 0) == todaysDate.setHours(0, 0, 0, 0);
}
var checkIfYesterday = function(inputDateLong) {
    var inputDate = new Date(inputDateLong);
    var yesterdayDate = new Date();
    yesterdayDate.setDate(yesterdayDate.getDate() - 1);
    return (yesterdayDate.getDate() == inputDate.getDate() && yesterdayDate.getMonth() == inputDate.getMonth() && yesterdayDate.getFullYear() == inputDate.getFullYear());
}

var isSameDay = function(currentdate, previus) {
    if (currentdate == undefined && previus == undefined)
        return true;
    var dateToCheck = new Date(currentdate);
    var actualDate = new Date(previus);
    return (dateToCheck.getDate() == actualDate.getDate() && dateToCheck.getMonth() == actualDate.getMonth() && dateToCheck.getFullYear() == actualDate.getFullYear())
}

var getNameFromRandomizedUrl = function(url) {
    var fileNameSignaturePrefix = "file_name=";
    var startPos = url.lastIndexOf(fileNameSignaturePrefix) + fileNameSignaturePrefix.length;
    var endPos = url.length - DEFAULT_FILE_PREFIX_LENGTH;
    return url.substring(startPos, endPos);
}
var getNameFromUrl = function(url) {
    var fileNameSignaturePrefix = "file_name=";
    var startPos = url.lastIndexOf(fileNameSignaturePrefix) + fileNameSignaturePrefix.length;
    var endPos = url.length;
    return url.substring(startPos, endPos);
}
var firstLetter = function(name) {
    if (undefined != name)
        return name.toUpperCase().charAt(0);
}

function isEquivalent(a, b) {
    // Create arrays of property names
    var aProps = Object.getOwnPropertyNames(a);
    var bProps = Object.getOwnPropertyNames(b);

    // If number of properties is different,
    // objects are not equivalent
    if (aProps.length != bProps.length) {
        return false;
    }

    for (var i = 0; i < aProps.length; i++) {
        var propName = aProps[i];

        // If values of same property are not equal,
        // objects are not equivalent
        if ((a[propName]!=null && typeof a[propName] !== 'object') && a[propName] !== b[propName]) {
            return false;
        }
    }

    // If we made it this far, objects
    // are considered equivalent
    return true;
}

var formatDateWithLast = function(date, short, withoutTime, max_time) {
    if (short == undefined)
        short = false;
    if (withoutTime == undefined)
        withoutTime = false;
    if (max_time == undefined)
        max_time = 60;


    if (date == null || date == undefined)
        return "";

    // need translate and move to global to config map
    var dateObj = new Date(date);

    if (dateObj == null || dateObj == undefined || isNaN(dateObj))
        return "";

    var delta = new Date().getTime() - dateObj.getTime();
    if (delta > 60000 * max_time)
        return formatDate(date, short, withoutTime);
    else
    if (Math.round(delta / 60000) == 0)
        return "щойно";

    var minutesStr = Math.round(delta / 60000);
    if (short) {
        if (minutesStr < 60)
            return minutesStr + minutesNameShort[globalConfig.lang];
        else
        if (Math.ceil(minutesStr / 60) < 24)
            return Math.ceil(minutesStr / 60) + hoursNameShort[globalConfig.lang];
        else
            return Math.ceil(minutesStr / (60 * 24)) + daysNameShort[globalConfig.lang];

    } else {
        if (minutesStr > 1) {
            if (minutesStr < 60)
                return minutesStr + minutesName[globalConfig.lang] + endName[globalConfig.lang];
            else {
                var hours = Math.ceil(minutesStr / 60);
                if (hours < 24) {
                    if (hours > 1)
                        return hours + hoursName[globalConfig.lang] + endName[globalConfig.lang];
                    else
                        return hours + hourName[globalConfig.lang] + endName[globalConfig.lang];
                } else {
                    var days = Math.ceil(hours / 24);
                    var lastDigitOfDay = days % 10;
                    //1 день, 2-4,22-24 дні,32-34 5-20 днів,21 день, 
                    if (days > 1){
                        if (lastDigitOfDay>=2 && lastDigitOfDay <= 4 && globalConfig.lang=="ua")
                        return days + " дні" + endName[globalConfig.lang];
                        else
                        return days + daysName[globalConfig.lang] + endName[globalConfig.lang];
                    }
                    else
                        return days + dayName[globalConfig.lang] + endName[globalConfig.lang];
                }
            }
        } else
            return minutesStr + minuteName[globalConfig.lang] + endName[globalConfig.lang];
    }
}

var differenceInSecondsBetweenDates = function(t1, t2) {
    var dif = t1.getTime() - t2.getTime();
    var Seconds_from_T1_to_T2 = dif / 1000;
    var Seconds_Between_Dates = Math.abs(Seconds_from_T1_to_T2);
    return Seconds_Between_Dates;
}
var formatDate = function(date, short, withoutTime) {
    // need translate and move to global to config map
    if (short == undefined)
        short = false;

    var monthNames = {};
    monthNames['ua'] = [
        "Січеня", "Лютого", "Березеня ",
        "Квітня", "Травня ", "Червня ", "Липня",
        "Серпня", "Вересеня", "Жовтеня",
        "Листопада", "Груденя"
    ];
    monthNames['en'] = [
        "January", "February", "March",
        "April", "May", "June", "July",
        "August", "September", "October",
        "November", "December"
    ];
    monthNames['ru'] = [
        "Января", "Февраля", "Марта",
        "Апреля", "Мая", "Июня", "Июля",
        "Августа", "Сентября", "Октября",
        "Ноября", "Декабря"
    ];
    var dateObj = new Date(date);
    var day = dateObj.getDate();
    var monthIndex = dateObj.getMonth();
    var year = dateObj.getFullYear();
    var minutes = dateObj.getMinutes();
    if (minutes < 10)
        minutes = '0' + minutes;
    if (short) {
        if (monthIndex + 1 < 10)
            return day + ".0" + (monthIndex + 1);
        else
            return day + "." + (monthIndex + 1);
    }
    var res = day + " " + monthNames[globalConfig.lang][monthIndex];
    if (!withoutTime)
        res += " " + dateObj.getHours() + ":" + minutes;
    return res;
}

function getCurrentTime() {
    var currentdate = new Date();
    var h = currentdate.getHours();
    var m = currentdate.getMinutes();
    var s = currentdate.getSeconds();
    return h + ":" + m + ":" + s;
}

function formatDateToTime(date) {
    var h = date.getHours();
    var m = date.getMinutes();
    var s = date.getSeconds();
    return h + ":" + m + ":" + s;
}



function getPropertyByValue(obj, value) {
    for (var prop in obj) {
        if (obj.hasOwnProperty(prop)) {
            if (obj[prop] === value)
                return prop;
        }
    }
}

var curentDateInJavaFromat = function() {
    var currentdate = new Date();
    var day = currentdate.getDate();
    if (day < "10")
        day = "0" + day;

    var mouth = (currentdate.getMonth() + 1);
    if (mouth < "10")
        mouth = "0" + mouth;

    var datetime = currentdate.getFullYear() + "-" + mouth + "-" +
        day + " " + currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds() + ".0";
    return datetime;
};

function getIdInArrayFromObjectsMap(roomNameMap, propertyName, valueToFind) {

    for (var item in roomNameMap)
        if (roomNameMap[item][propertyName] == valueToFind) return item;
    return undefined;
}

function getRoomById(rooms, id) {

    for (var i = 0; i < rooms.length; i++) {
        if (rooms[i].roomId == id) return rooms[i];
    }
    return undefined;
}


/*
 * FILE UPLOAD
 */
function uploadXhr(files, urlpath, successCallback, errorCallback, onProgress) {

    var xhr = getXmlHttp();

    //  обработчик для закачки
    xhr.upload.onprogress = function(event) {
        onProgress(event, xhr.upload.loaded);
    }

    //  обработчики успеха и ошибки
    //  если status == 200, то это успех, иначе ошибка
    xhr.onload = xhr.onerror = function() {
        if (this.status == 200) {
            successCallback(xhr.responseText);
        } else {
            errorCallback(xhr);
        }
    };

    xhr.open("POST", urlpath);
    var boundary = String(Math.random()).slice(2);
    //  xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
    var formData = new FormData();

    for (var i = 0; i < files.length; i++) {
        formData.append("file" + i, files[i]);
    }
    xhr.send(formData);

}

function upload($http, files, urlpath) {
    var formData = new FormData();
    for (var i = 0; i < files.length; i++) {
        formData.append("file" + i, files[i]);
    }

    return $http.post(urlpath, formData, {
        transformRequest: function(data, headersGetterFunction) {
            return data;
        },
        headers: { 'Content-Type': undefined }
    }).error(function(data, status) {});
}


function getXmlHttp() {
    var xmlhttp;
    try {
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
        try {
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (E) {
            xmlhttp = false;
        }
    }
    if ((!xmlhttp || !xmlhttp.upload) && typeof XMLHttpRequest != 'undefined') {
        xmlhttp = new XMLHttpRequest();
    }
    return xmlhttp;
}

function replacer(str, offset, s) {
    return "{{call(" + str.substring(2, str.length - 4) + ")}}";
}

function call(str) {
    return "{{" + str + "}}";
}

/*function upload(file,urlpath) {

var xhr = new XMLHttpRequest();

// обработчик для закачки
xhr.upload.onprogress = function(event) {
  log(event.loaded + ' / ' + event.total);
}

// обработчики успеха и ошибки
// если status == 200, то это успех, иначе ошибка
xhr.onload = xhr.onerror = function() {
  if (this.status == 200) {
    log("success");
  } else {
    log("error " + this.status);
  }
};

xhr.open("POST", urlpath, true);
xhr.setRequestHeader('Content-Type', 'multipart/form-data')
xhr.send(file);

}*/

/*
 * CONST
 */
var Operations = Object.freeze({
    "send_message_to_all": "SEND_MESSAGE_TO_ALL",
    "send_message_to_user": "SEND_MESSAGE_TO_USER",
    "add_user_to_room": "ADD_USER_TO_ROOM",
    "add_room": "ADD_ROOM",
    "add_room_from_tenant": "ADD_ROOM_FROM_TENANT",
    "add_room_on_login": "ADD_ROOM_ON_LOGIN"
});

var serverPrefix = "/crmChat";
var DEFAULT_FILE_PREFIX_LENGTH = 15;

var substringMatcher = function(strs) {
    return function findMatches(q, cb) {
        var matches, substringRegex;

        // an array that will be populated with substring matches
        matches = [];

        // regex used to determine if a string contains the substring `q`
        substrRegex = new RegExp(q, 'i');

        // iterate through the pool of strings and for any string that
        // contains the substring `q`, add it to the `matches` array
        $.each(strs, function(i, str) {
            if (substrRegex.test(str)) {
                matches.push(str);
            }
        });
        cb(matches);
    };
};

function getKeyByValue(value, object) {
    for (var prop in object) {
        if (object.hasOwnProperty(prop)) {
            if (object[prop] === value)
                return prop;
        }
    }
}

var isDate = function(date) {
    return (date instanceof Date); //&& (!Number.isInteger(parseInt(date)) && ((new Date(date) !== "Invalid Date" && !isNaN(new Date(date)) )));
}

function getType(value) {
    if (value === true || value === false || value == 'true' || value == 'false')
        return "bool";

    if (Array.isArray(value))
        return "array";

    if (isDate(value))
        return "date";

    return "string";
}

function parseBoolean(value) {
    if (value == "true")
        return true;
    else
        return false;
}

function Color(val) {
    this.val = val;
}

function send(destination, data, ok_funk, err_funk) {
    var xhr = getXmlHttp();
    xhr.open("POST", serverPrefix + destination, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 || xhr.readyState == "complete") {
            ok_funk();
        } else
            err_funk();

    }
    xhr.send(data);
}

function htmlEscape(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// I needed the opposite function today, so adding here too:
function htmlUnescape(str) {
    return str
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'")
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&');
}


function addOrRemove(array, value) {
    var index = array.indexOf(value);

    if (index === -1) {
        array.push(value);
    } else {
        array.splice(index, 1);
    }
}

function readTextFileAndCallFunc(file,callback)
{
    var rawFile = getXmlHttp();
    rawFile.open("GET", file, true);
    rawFile.onreadystatechange = function ()
    {
        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                callback(rawFile.responseText);
            }
        }
    }
    rawFile.send(null);
}

        function pasteHtmlAtCaret(html, selectPastedContent) {
            var sel, range;
            if (window.getSelection) {
                // IE9 and non-IE
                sel = window.getSelection();
                if (sel.getRangeAt && sel.rangeCount) {
                    range = sel.getRangeAt(0);
                    range.deleteContents();

                    // Range.createContextualFragment() would be useful here but is
                    // only relatively recently standardized and is not supported in
                    // some browsers (IE9, for one)
                    var el = document.createElement("div");
                    el.innerHTML = html;
                    var frag = document.createDocumentFragment(),
                        node, lastNode;
                    while ((node = el.firstChild)) {
                        lastNode = frag.appendChild(node);
                    }
                    var firstNode = frag.firstChild;
                    range.insertNode(frag);

                    // Preserve the selection
                    if (lastNode) {
                        range = range.cloneRange();
                        range.setStartAfter(lastNode);
                        if (selectPastedContent) {
                            range.setStartBefore(firstNode);
                        } else {
                            range.collapse(true);
                        }
                        sel.removeAllRanges();
                        sel.addRange(range);
                    }
                }
            } else if ((sel = document.selection) && sel.type != "Control") {
                // IE < 9
                var originalRange = sel.createRange();
                originalRange.collapse(true);
                sel.createRange().pasteHTML(html);
                if (selectPastedContent) {
                    range = sel.createRange();
                    range.setEndPoint("StartToStart", originalRange);
                    range.select();
                }
            }
        }