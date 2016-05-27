var formatDateWithLast = function(date) {

    var daysName = {},
        hoursName = {},
        minutesName = {};
    var dayName = {},
        hourName = {},
        minuteName = {};
    var endName = {};
    daysName['ua'] = 'днів';
    dayName['ua'] = 'день';
    daysName['en'] = 'day';
    dayName['en'] = 'days';
    daysName['ru'] = 'дней';
    dayName['ru'] = 'день';

    hoursName['ua'] = 'годин';
    hourName['ua'] = 'годину';
    hoursName['en'] = 'hour';
    hourName['en'] = 'hours';
    hoursName['ru'] = 'часов';
    hourName['ru'] = 'час';

    minutesName['ua'] = ' хвилин ';
    minuteName['ua'] = ' хвилину ';
    minutesName['en'] = ' minutes ';
    minuteName['en'] = ' minute ';
    minutesName['ru'] = ' минут ';
    minuteName['ru'] = ' минуту ';

    endName['ua'] = "тому";
    endName['en'] = "ago";
    endName['ru'] = "спустя";

    // need translate and move to global to config map
    var dateObj = new Date(date);

    var delta = new Date().getTime() - dateObj.getTime();
    if (delta > 60000 * 59)
        return formatDate(date);
    else
    if (Math.round(delta / 60000) == 0)
        return null;

    var minutesStr = Math.round(delta / 60000);
    if (minutesStr > 1)
        return minutesStr + minutesName[globalConfig.lang] + endName[globalConfig.lang];
    else
        return minutesStr + minuteName[globalConfig.lang] + endName[globalConfig.lang];
}
var formatDate = function(date) {
    // need translate and move to global to config map
    var monthNames = {};
    monthNames['ua'] = [
        "Січеня", "Лютого", "Березеня ",
        "Квітня", "Травня ", "Червеня ", "Липеня",
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

    return day + " " + monthNames[globalConfig.lang][monthIndex] + " " + dateObj.getHours() + ":" + minutes;
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
    //console.log("------------------ " + datetime)
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
        //console.log(event.loaded + ' / ' + event.total);
        onProgress(event, xhr.upload.loaded);
    }

    //  обработчики успеха и ошибки
    //  если status == 200, то это успех, иначе ошибка
    xhr.onload = xhr.onerror = function() {
        if (this.status == 200) {
            console.log("SUCCESS:" + xhr.responseText);
            successCallback(xhr.responseText);
        } else {
            console.log("error " + this.status);
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
    }).error(function(data, status) {
        console.log("Error ... " + status);
    });
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

function getType(value) {
    if (value === true || value === false)
        return "bool";

    if (Array.isArray(value))
        return "array";

    return "string";
}

function parseBoolean(value) {
    if (value == "true")
        return true;
    else
        return false;
}
