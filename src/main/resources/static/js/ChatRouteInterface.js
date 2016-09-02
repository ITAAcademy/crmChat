String.prototype.HTMLEncode = function(str) {
    var result = "";
    var str = (arguments.length === 1) ? str : this;
    for (var i = 0; i < str.length; i++) {
        var chrcode = str.charCodeAt(i);
        result += (chrcode > 128) ? "&#" + chrcode + ";" : str.substr(i, 1)
    }
    return result;
}
String.prototype.insertAt = function(index, string) {
    return this.substr(0, index) + string + this.substr(index);
}
springChatControllers.controller('ChatRouteInterface', ['$route', '$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$sce', function($route, $routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $sce) {
    var INPUT_MODE = {
        STANDART_MODE: 0,
        DOG_MODE: 1,
        TILDA_MODE: 2,
        COMMAND_MODE: 3
    };

    var errorMsgTitleNotFound = "Сталася помилка";
    var errorMsgContentNotFound = "Кімната не існує або Ви не є її учасником";


    $scope.BOT_ID = 0; // need read from config

    $scope.selected = undefined;
    $scope.totalItems = 64;
    $scope.currentPage = 4;
    $scope.botParameters = new Map();
    $scope.botContainers = new Array();

    $scope.show_search_list_in_message_input = false;
    var isSpecialInput = false;
    var specialInputPositionInText;
    var specialInputMode = INPUT_MODE.STANDART_MODE;
    var enableInputMode = function(input_mode, positionInText) {
        if (isSpecialInput) {
            //special input is already enabled, so cancel further actions
            return;
        }
        isSpecialInput = true;
        specialInputPositionInText = positionInText;
        specialInputMode = input_mode;
    }
    var resetSpecialInput = function() {
        isSpecialInput = false;
        specialInputMode = INPUT_MODE.STANDART_MODE;
    }


    var showListInMessageInputTimer;



    function messageError() {
        toaster.pop('error', "Error", "server request timeout", 0);
    }

    function messageError(mess) {
        toaster.pop('error', "Error", mess, 0);
    }

    function getCaretPosition(oField) {

        // Initialize
        var iCaretPos = 0;

        // IE Support
        if (document.selection) {

            // Set focus on the element
            oField.focus();

            // To get cursor position, get empty selection range
            var oSel = document.selection.createRange();

            // Move selection start to 0 position
            oSel.moveStart('character', -oField.value.length);

            // The caret position is selection length
            iCaretPos = oSel.text.length;
        }

        // Firefox support
        else if (oField.selectionStart || oField.selectionStart == '0')
            iCaretPos = oField.selectionStart;

        // Return results
        return iCaretPos;
    };
    var typing = undefined;

    $scope.onKeyMessageKeyReleaseEvent = function(event) {
        if (event.keyCode == 13) {
            if (event.shiftKey) {
                $scope.startTyping(event);
            } else
            if (!$scope.show_search_list_in_message_input)
                $scope.sendMessage();
        } else
            $scope.startTyping(event);
    }

    $scope.oldMessage; // = $scope.messages[0];

    $scope.onKeyMessageKeyPressEvent = function(event) {

        var keyCode = event.which || event.keyCode;
        var typedChar = String.fromCharCode(event.keyCode);
        var shiftPressed = event.shiftKey;
        var ctrlPressed = event.ctrlKey || event.metaKey;
        var selectAllHotKeyPressed = typedChar == 'A' && ctrlPressed;
        var kk = keyCode;
        var arrowKeyPressed = kk == 39 || kk == 37;
        var enterPressed = keyCode == 13;

        if (enterPressed && !$scope.show_search_list_in_message_input) {
            $scope.onMessageInputClick();
            return;
        }



        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);
        switch (typedChar) {
            case '@':
                enableInputMode(INPUT_MODE.DOG_MODE, carretPosIndex);
                return;
            case '~':
                enableInputMode(INPUT_MODE.TILDA_MODE, carretPosIndex);
                return;
            case '/':
                enableInputMode(INPUT_MODE.COMMAND_MODE, carretPosIndex);
                return;
            case ' ':
                $scope.onMessageInputClick();
                break;

        }

        if (selectAllHotKeyPressed || arrowKeyPressed) {
            $scope.onMessageInputClick();
        }


    }
    $scope.beforeMessageInputKeyPress = function(event) {

        var escapePressed = event.keyCode == 27;
        if (escapePressed) {
            data_in_message_input = [];
            $scope.show_search_list_in_message_input = false;
            resetSpecialInput();
            return;
        }

        if (event.keyCode === 9) { // tab was pressed

            // get caret position/selection
           // debugger;
            var val = event.target.value,
                start = event.target.selectionStart,
                end = event.target.selectionEnd;

            // set textarea value to: text before caret + tab + text after caret
            event.target.value = val.substring(0, start) + '\t' + val.substring(end);

            // put caret at right position again
            event.target.selectionStart = event.target.selectionEnd = start + 1;

            // prevent the focus lose
            event.preventDefault();

        }
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);
        var keyCode = event.which || event.keyCode;
        var backSpacePressed = keyCode == 8;
        if (backSpacePressed && carretPosIndex <= specialInputPositionInText + 1) {
            $scope.onMessageInputClick();
            return;
        }
        var kk = keyCode;
        var arrowKeyPressed = kk == 39 || kk == 37;
        if (arrowKeyPressed) $scope.onMessageInputClick();
    }


    $scope.startTyping = function(event) {
        //var keyCode = event.which || event.keyCode;
        //var typedChar = String.fromCharCode(keyCode);
        //if(typedChar==' ')$scope.onMessageInputClick();       
        switch (specialInputMode) {
            case INPUT_MODE.DOG_MODE:
                processDogInput();
                break;
            case INPUT_MODE.COMMAND_MODE:
                processCommandInput();
                break;
            case INPUT_MODE.TILDA_MODE:
                processTildaInput();
                break;

        }
        //      Don't send notification if we are still typing or we are typing a private message
        if (angular.isDefined(typing) || $scope.sendTo != "everyone") return;

        typing = $interval(function() {
            $scope.stopTyping();
        }, 500);

        chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({ username: $scope.chatUserId, typing: true }));
    };

    $scope.stopTyping = function() {
        if (angular.isDefined(typing)) {
            $interval.cancel(typing);
            typing = undefined;

            chatSocket.send("/topic/{0}chat.typing".format(room), {}, JSON.stringify({ username: $scope.chatUserId, typing: false }));

        }
    };

    $scope.showCommandListInMessageInput = function(command) {

        $scope.data_in_message_input = [];
        if ($scope.showCommandListInMessageInputTimer != null && $scope.showCommandListInMessageInputTimer != undefined)
            $timeout.cancel($scope.showCommandListInMessageInputTimer); //888

        $scope.showCommandListInMessageInputTimer = $timeout(function() {
            $scope.show_search_list_in_message_input = true;
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_commands_like?command=" + command,
                data: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            request.success(function(data) {
                $scope.data_in_message_input = data;
            });
        }, 500); //for click event
    };

    $scope.showUsersListInMessageInput = function(email) {
        $scope.show_search_list_in_message_input = true;
        $scope.data_in_message_input = [];
        $timeout.cancel(showListInMessageInputTimer);

        showListInMessageInputTimer = $timeout(function() {
            $scope.show_search_list_in_message_input = true;
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_users_emails_like?login=" + email + "&room=" + chatControllerScope.currentRoom.roomId + "&eliminate_users_of_current_room=false", //'/get_users_emails_like',
                data: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            request.success(function(data) {
                $scope.data_in_message_input = data;
            });
        }, 500); //for click event
    };

    $scope.showCoursesListInMessageInput = function(coursePrefix, courseLang) {

        $scope.show_search_list_in_message_input = true;
        $scope.data_in_message_input = [];
        $timeout.cancel(showListInMessageInputTimer);

        showListInMessageInputTimer = $timeout(function() {
            $scope.show_search_list_in_message_input = true;
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_courses_like?prefix=" + coursePrefix + "&lang=" + courseLang, //'/get_users_emails_like',
                data: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            request.success(function(data) {
                $scope.data_in_message_input = data;
            });
        }, 500); //for click event
    };

    $scope.scaleCenterIconCircle = function() {

        var avatar_contaiter = $('.centered').children();
        avatar_contaiter.on('load', function() {

            for (var i = 0; i < avatar_contaiter.length; i++) {
                var image = avatar_contaiter.eq(i);
                var width = image.width();
                var height = image.height();
                image.css("position", "static");
                if (parseInt(width, 10) < parseInt(height, 10)) {
                    var proportcialHeight = height * 100 / width;
                    image.css("height", proportcialHeight + "%");
                    image.css("width", "100%");
                    image.css("top", "-75px");
                    image.css("overflow", "hidden");
                    image.css("transform", " translateX(-0%) translateY(-5%)");
                } else
                if (parseInt(width, 10) > parseInt(height, 10)) {
                    var proportcialWidth = width * 100 / height;
                    image.css("width", proportcialWidth + "%");
                    image.css("height", "100%");
                    image.css("transform", " translateX(-" + (proportcialWidth - 100) / 2 + "%)");
                }

            }
        });
    };

    $scope.onMessageInputClick = function() {
        if (!$scope.open_search_list_in_message_input)
            resetSpecialInput();

        $timeout(function() {
            $scope.show_search_list_in_message_input = false;
        }, 500);


    }
    $scope.appendToSearchInput_in_message_input = function(value) {
        console.log("searchInputValue:" + $scope.searchInputValue.email);
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);
        $scope.newMessage = $scope.newMessage.insertAt(carretPosIndex - 1, value);
        $timeout(function() {
            $scope.show_search_list_in_message_input = false;
        }, 500);
        resetSpecialInput();
    }

    $scope.appendDifferenceToSearchInput_in_message_input = function(value) {
        //alert(value)
        var functionalChar;
        var prefix = "";
        var suffix = "";
        switch (specialInputMode) {
            case INPUT_MODE.DOG_MODE:
                functionalChar = "@";
                break;
            case INPUT_MODE.TILDA_MODE:
                functionalChar = "~";
                prefix = '"';
                suffix = '"';
                break;
            case INPUT_MODE.COMMAND_MODE:
                functionalChar = "/";
                break;
        }
        var message = $scope.newMessage;
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);
        var userNameStartIndex = message.lastIndexOf(functionalChar) + 1;
        //var userNamePrefix = message.substring(userNameStartIndex,carretPosIndex);
        var charactersAlreadyKnownCount = carretPosIndex - userNameStartIndex;
        var differenceValue = value.substring(charactersAlreadyKnownCount);
        $scope.newMessage = $scope.newMessage.insertAt(carretPosIndex, differenceValue).insertAt(userNameStartIndex, prefix) + suffix;
        $timeout(function() {
            $scope.show_search_list_in_message_input = false;
        }, 500);


        if (specialInputMode == INPUT_MODE.COMMAND_MODE) {
            var funcCall = value + "()";
            //alert(funcCall);
            //Call the function
            var ret = eval(funcCall);
        }
        resetSpecialInput();
    }

    function createDialogWithBot() {
        $('#wndTitle').text("Створити діалог з ботом");
        $('#roomNameInput').attr("placeholder", "Назва діалогу");

        $scope.dialogNameBackup = $scope.dialogName;

        $scope.dialogName = ""
        $scope.toggleNewRoomModal(); //999

    }

    function getCurrentTime(hour_shift) {
        var currentdate = new Date();
        return (currentdate.getHours() + hour_shift) + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
    }

    function getTimeString(time) {
        return time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds();
    };

    var validate_create_consultation_form = true;

    function getDateString(time) {
        var year = time.getUTCFullYear();

        var month = time.getUTCMonth() + 1;
        var day = time.getUTCDate();

        var date = year + "-";

        if (month > 9)
            date += month + "-";
        else
            date += "0" + month + "-";

        if (day > 9)
            date += day;
        else
            date += "0" + day;

        return date;
    };

    function getTimeInInputFormat(hour, minutes, sec) {
        var currentdate = new Date();
        currentdate.setHours(hour);
        currentdate.setMinutes(minutes);
        currentdate.setSeconds(sec);
        currentdate.setMilliseconds(0);
        //  currentdate.setMiliseconds(0);
        return currentdate;
    }


    $scope.consultationDate
    $scope.consultationTimeBeginInput
    $scope.is_teacher_Nick_valid = false;
    $scope.is_lectionTitle_valid = false;



    function createConsultation() {

        $('#create_consultation_wndTitle').text("Створити консультацію");
        $scope.consultationDate = new Date()
        $scope.teacher_Nick = "red2015in@gmail.com"
        $scope.lectionTitle = "r4fregv"

        var time = new Date();
        $scope.consultationTimeBegin = getTimeInInputFormat(time.getHours() + 1, time.getMinutes(), time.getSeconds());
        $scope.consultationTimeEnd = getTimeInInputFormat(time.getHours() + 2, time.getMinutes(), time.getSeconds());
        $scope.toggleNewConsultationModal();
    };

    $scope.createSmtVisible = false;

    $scope.toggleNewRoomModal = function() {
        $('#new_room_modal').modal('toggle');

        if ($scope.createSmtVisible == true)
            $scope.dialogName = $scope.dialogNameBackup;

        $scope.createSmtVisible = !$scope.createSmtVisible;
    };

    var consultationWindowVisible = false;

    $scope.toggleNewConsultationModal = function() {
        $('#new_consultation_modal').modal('toggle');

        if (consultationWindowVisible == true) {
            $scope.teacher_Nick = "";
            $scope.lectionTitle = "";
        }
        consultationWindowVisible = !consultationWindowVisible;
    };

    $scope.addDialog = function() {
        var dialName = $scope.dialogName;
        $scope.toggleNewRoomModal();
        $http.post(serverPrefix + "/chat/rooms/create/with_bot/", dialName) // + $scope.dialogName).
        success(function(data, status, headers, config) {
            console.log('room with bot created: ' + $scope.dialogName)
        }).
        error(function(data, status, headers, config) {
            console.log('creating room with bot failed ')
        });

    };

    $scope.teachersList = []
    $scope.lectionsList = []

    $scope.teacher_Nick = ""
    $scope.lectionTitle = ""

    var getTeachersEmailsTimer;



    $scope.teacherNickInputKeyDown = function(event) {
        var enterPressed = event.keyCode == 13;
        if (enterPressed) {
            $('#lectionTitleInput').focus();
            return;
        }
    };


    $scope.lectionTitleInputKeyDown = function(event) {
        var enterPressed = event.keyCode == 13;
        if (enterPressed) {
            $('#consultationDateInput').focus();
            return
        }
    };


    $scope.consultationDateInputKeyDown = function(event) {
        var enterPressed = event.keyCode == 13;
        if (enterPressed) {
            $('#consultationTimeBeginInput').focus();
        }
    };

    $scope.consultationTimeBeginInputKeyDown = function(event) {
        var enterPressed = event.keyCode == 13;
        if (enterPressed) {
            $('#consultationTimeEndInput').focus();
        }
    };

    $scope.consultationTimeEndInputKeyDown = function(event) {
        var enterPressed = event.keyCode == 13;
        if (enterPressed) {
            //submit
        }
    };

    $scope.lectionTitleInputBorder = "";
    $scope.teacher_NickInputBorder = "";
    $scope.consultationDateInputBorder = "";
    $scope.consultationTimeBeginInputBorder = "";
    $scope.consultationTimeEndInputBorder = "";

    function isFirstTimeLessThanSecond(time1, time2) {
        var consultationTimeBegin_hour = time1.getHours();
        var consultationTimeBegin_minute = time1.getMinutes();
        var consultationTimeBegin_second = time1.getSeconds();

        var consultationTimeEnd_hour = time2.getHours();
        var consultationTimeEnd_minute = time2.getMinutes();
        var consultationTimeEnd_second = time2.getSeconds();

        var is_begin_time_less_end_time = false;
        if (consultationTimeBegin_hour < consultationTimeEnd_hour)
            is_begin_time_less_end_time = true;
        else
        if (consultationTimeBegin_hour == consultationTimeEnd_hour) {
            if (consultationTimeBegin_minute < consultationTimeEnd_minute)
                is_begin_time_less_end_time = true;
            else
            if (consultationTimeBegin_minute == consultationTimeEnd_minute) {
                if (consultationTimeBegin_second < consultationTimeEnd_second)
                    is_begin_time_less_end_time = true;
            }
        }
        return is_begin_time_less_end_time;
    };

    function isDatesEquials(date1, date2) {
        var curDay = date1.getUTCDate();
        var curYear = date1.getUTCFullYear();
        var curMonth = date1.getUTCMonth() + 1;

        var consultationDate_day = date2.getUTCDate();
        var consultationDate_Year = date2.getUTCFullYear();
        var consultationDate_Month = date2.getUTCMonth() + 1;

        var is_equials = (consultationDate_day == curDay && consultationDate_Year == curYear && consultationDate_Month == curMonth);
        return is_equials;
    };

    function isFirstDateLessEquialThanSecond(date1, date2) {
        var curDay = date1.getUTCDate();
        var curYear = date1.getUTCFullYear();
        var curMonth = date1.getUTCMonth() + 1;

        var consultationDate_day = date2.getUTCDate();
        var consultationDate_Year = date2.getUTCFullYear();
        var consultationDate_Month = date2.getUTCMonth() + 1;

        var isconsultationDateValid = false;
        if (consultationDate_Year > curYear)
            isconsultationDateValid = true;
        else
        if (consultationDate_Year == curYear) {
            if (consultationDate_Month > curMonth)
                isconsultationDateValid = true;
            else
            if (consultationDate_Month == curMonth) {
                if (consultationDate_day >= curDay)
                    isconsultationDateValid = true;
            }
        }
        return isconsultationDateValid;
    };


    $scope.createConsultationButtonPressed = function(event) {

        var all_ok;
        if (validate_create_consultation_form == true) {
            if ($scope.is_lectionTitle_valid == false)
                $scope.lectionTitleInputBorder = "solid 1px red";
            else
                $scope.lectionTitleInputBorder = "";

            if ($scope.is_teacher_Nick_valid == false)
                $scope.teacher_NickInputBorder = "solid 1px red";
            else
                $scope.teacher_NickInputBorder = "";

            var cur_time = new Date();

            var curHour = cur_time.getHours();
            var curMinute = cur_time.getMinutes();
            var curSecond = cur_time.getSeconds();


            var isconsultationDateValid = isFirstDateLessEquialThanSecond(cur_time, $scope.consultationDate);

            if (isconsultationDateValid == false)
                $scope.consultationDateInputBorder = "solid 1px red";
            else
                $scope.consultationDateInputBorder = "";

            var is_begin_time_less_end_time = isFirstTimeLessThanSecond($scope.consultationTimeBegin, $scope.consultationTimeEnd);

            if (is_begin_time_less_end_time == false)
                $scope.consultationTimeEndInputBorder = "solid 1px red";
            else
                $scope.consultationTimeEndInputBorder = "";

            all_ok = ($scope.is_lectionTitle_valid && $scope.is_teacher_Nick_valid && isconsultationDateValid && is_begin_time_less_end_time);

            if (isDatesEquials($scope.consultationDate, cur_time)) {
                var is_cur_time_less_begin_time = isFirstTimeLessThanSecond(cur_time, $scope.consultationTimeBegin);

                if (is_cur_time_less_begin_time == false) {
                    $scope.consultationTimeBeginInputBorder = "solid 1px red";
                    all_ok = false;
                } else
                    $scope.consultationTimeBeginInputBorder = "";
            }
        } else
            all_ok = true;

        if (all_ok) {
            //console.log("dddddddddddddd   " + getDateString($scope.consultationDate)  + " " + $scope.consultationDate)
            //send kson
            $http.post(serverPrefix + "/chat/rooms/create/consultation/", {
                "email": $scope.teacher_Nick,
                "lection": $scope.lectionTitle,
                "date": getDateString($scope.consultationDate),
                "begin": getTimeString($scope.consultationTimeBegin),
                "end": getTimeString($scope.consultationTimeEnd)
            }).
            success(function(data, status, headers, config) {
                console.log('consultation created: ')
                $scope.toggleNewConsultationModal();
            }).
            error(function(data, status, headers, config) {
                console.log('creating consultation failed ')
            });
        }

    };


    $scope.$watch('teacher_Nick', function() {
        $scope.showTeachersList();
    }, true);

    $scope.$watch('lectionTitle', function() {
        $scope.showLectionsList();
    }, true);


    $scope.showTeachersList = function() {
        $scope.is_teacher_Nick_valid = false;
        $scope.teachersList = [];
        $timeout.cancel(getTeachersEmailsTimer);
        getTeachersEmailsTimer = $timeout(function() {
            $http.get(serverPrefix + "/get_all_users_emails_like?email=" + $scope.teacher_Nick).
            success(function(data, status, headers, config) {
                $scope.teachersList = data;
                //$scope.$apply();                      

                var list_len = data.length;
                if (list_len == 1)
                    if ($scope.teacher_Nick == data[0]) {
                        $scope.is_teacher_Nick_valid = true;
                        $scope.teacher_NickInputBorder = "";
                    }
            }).
            error(function(data, status, headers, config) {
                // log error
            });

        }, 500);
    };

    var getLectionsListTimer;

    $scope.onSelectFromLectionsList = function(value) {
        $scope.lectionTitle = value;
        $scope.is_lectionTitle_valid = true;
    };

    $scope.onSelectFromTeachersList = function(value) {
        $scope.teacher_Nick = value;
        $scope.is_teacher_Nick_valid = true;
    };

    $scope.showLectionsList = function() {
        $scope.is_lectionTitle_valid = false;
        $scope.lectionsList = [];
        $timeout.cancel(getLectionsListTimer);

        getLectionsListTimer = $timeout(function() {
            $http.get(serverPrefix + "/chat/lectures/get_five_titles_like/?title=" + $scope.lectionTitle).
            success(function(data, status, headers, config) {
                $scope.lectionsList = data;
                // $scope.$apply();

                var list_len = data.length;
                if (list_len == 1)
                    if ($scope.lectionTitle == data[0]) {
                        $scope.is_lectionTitle_valid = true;
                        $scope.lectionTitleInputBorder = "";
                    }
            }).
            error(function(data, status, headers, config) {

            });
        }, 500); //for click event
    };


    function processDogInput() {
        var message = $scope.newMessage;
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);

        //if (!isSpecialInput)return;//return if @ is not present in word
        var userNameStartIndex = message.lastIndexOf('@') + 1;
        var userNamePrefix = message.substring(userNameStartIndex, carretPosIndex);

        $scope.showUsersListInMessageInput(userNamePrefix);
    }

    function processCommandInput() {
        var message = $scope.newMessage;
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);
        var commandStartIndex = message.lastIndexOf('/') + 1;
        var commandPrefix = message.substring(commandStartIndex, carretPosIndex);

        $scope.showCommandListInMessageInput(commandPrefix);
    };


    function processTildaInput() {
        var message = $scope.newMessage;
        var msgInputElm = document.getElementById("newMessageInput");
        var carretPosIndex = getCaretPosition(msgInputElm);

        //if (!isSpecialInput)return;//return if @ is not present in word
        var userNameStartIndex = message.lastIndexOf('~') + 1;
        var userNamePrefix = message.substring(userNameStartIndex, carretPosIndex);

        $scope.showCoursesListInMessageInput(userNamePrefix, "ua");
    }

    $scope.fileDropped = function() {
        //Get the file
        var files = $scope.uploadedFiles;
        var totalSize = 0;
        for (var i = 0; i < files.length; i++) {
            totalSize += files[i].size;
        }
        if (totalSize > MAX_UPLOAD_FILE_SIZE_BYTES) {
            var noteStr = fileUploadLocal.fileSizeOverflowLimit + ":" + Math.round(totalSize / 1024) + "/" + MAX_UPLOAD_FILE_SIZE_BYTES / 1024 + "Kb";
            toaster.pop('error', "Failed", noteStr, 5000);
            return;
        }

        //Upload the image
        //(Uploader is a service in my application, you will need to create your own)
        if (files) {
            uploadXhr(files, "upload_file/" + chatControllerScope.currentRoom.roomId,
                function successCallback(data) {
                    $scope.uploadProgress = 0;
                    $scope.sendMessage("я отправил вам файл", JSON.parse(data));
                    $scope.$apply();
                },
                function(xhr) {
                    $scope.uploadProgress = 0;
                    $scope.$apply();
                    alert("SEND FAILED:" + JSON.parse(xhr.response).message);
                },
                function(event, loaded) {
                    console.log(event.loaded + ' / ' + event.totalSize);
                    $scope.uploadProgress = Math.floor((event.loaded / event.total) * 100);
                    $scope.$apply();

                });
        }

        //Clear the uploaded file
        $scope.uploadedFile = null;
    };

    Scopes.store('ChatRouteInterface', $scope);
    var chatControllerScope = Scopes.get('ChatController');
    var lastRoomBindings = [];
    chatControllerScope.$watch('currentRoom', function() {
        $scope.currentRoom = chatControllerScope.currentRoom;
    });
    $scope.messages = [];
    $scope.participants = [];
    $scope.roomType = -1;
    $scope.ajaxRequestsForRoomLP = [];
    $scope.newMessage = '';
    $scope.uploadProgress = 0;
    $scope.message_busy = true;

    $scope.findParticipant = function(nickname) {
        for (var c_index in $scope.participants)
            if ($scope.participants[c_index].username == nickname)
                return $scope.participants[c_index];
        return null;
    }

    $scope.bool_rightLeft = false;

    $scope.addPhrase = function(text) {
        $scope.newMessage += text;
    }

    $scope.goToEmail = function(email) {
        console.log(email);
    }


    $rootScope.$on("login", function(event, chatUserId) {
        for (var index in $scope.participants) {
            if ($scope.participants[index].chatUserId == chatUserId) {
                $scope.participants[index].online = true;
                break;
            }
        }
    });

    $rootScope.$on("logout", function(event, chatUserId) {
        for (var index in $scope.participants) {
            if ($scope.participants[index].chatUserId == chatUserId) {
                $scope.participants[index].online = false;
                break;
            }
        }
    });


    $scope.goToDialog = function(roomId) {
        //console.log("roomName:"+roomName);
        if (chatControllerScope.currentRoom !== undefined && getRoomById($scope.rooms, chatControllerScope.currentRoom) !== undefined)
            getRoomById($scope.rooms, chatControllerScope.currentRoom.roomId).date = curentDateInJavaFromat();
        return goToDialogEvn(roomId);
    };

    $scope.goToDialogById = function(roomId) {
        console.log("roomId:" + roomId);
        return goToDialogEvn(roomId).then(function() {
            if (chatControllerScope.currentRoom !== undefined && getRoomById($scope.rooms, chatControllerScope.currentRoom) !== undefined)
                getRoomById($scope.rooms, chatControllerScope.currentRoom.roomId).date = curentDateInJavaFromat();
        });
        //$scope.templateName = 'chatTemplate.html';
        //$scope.dialogName = "private";

    };

    function goToDialogEvn(id) {
        console.log("goToDialogEvn(" + id + ")");
        chatControllerScope.currentRoom = { roomId: id };
        
        $rootScope.currentRoomId = id;
        
        $scope.changeRoom();
        var deferred = $q.defer();
        var room = getRoomById($scope.rooms, id);
        if (room != undefined) {
            chatControllerScope.currentRoom = room;
            //$scope.$apply();
            room.nums = 0;
            $scope.dialogName = room.string;
        } else {
            /*  $http.post(serverPrefix + "/chat/rooms/roomInfo/" + chatControllerScope.currentRoom.roomId)).
            success(function(data, status, headers, config) {

                $scope.goToDialog();
                chatControllerScope.rooms.push("");
            }).
            error(function(data, status, headers, config) {
                $rootScope.goToAuthorize();//not found => go out
            });*/
            deferred.reject();
            return deferred.promise;
        }

        if ($rootScope.socketSupport) {
            chatSocket.send("/app/chat.go.to.dialog/{0}".format(chatControllerScope.currentRoom.roomId), {}, JSON.stringify({}));
            deferred.resolve(true);
            return deferred.promise;
        } else {
            deferred = $http.post(serverPrefix + "/chat.go.to.dialog/{0}".format(chatControllerScope.currentRoom.roomId));
            return deferred;
        }

    }

    /*************************************
     * CHANGE ROOM
     *************************************/
    $scope.changeRoom = function() {
    	//alert(16);
        $scope.messages = [];
        console.log("roomId:" + chatControllerScope.currentRoom.roomId);
        room = chatControllerScope.currentRoom.roomId + '/';

        if ($rootScope.socketSupport == true) {
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}chat.message".format(room), function(message) {
                    calcPositionPush(JSON.parse(message.body)); //POP
                }));

            lastRoomBindings.push(chatSocket.subscribe("/app/{0}chat.participants/{1}".format(room, globalConfig.lang), function(message) {
                if (message.body != "{}") {
                    var o = JSON.parse(message.body);
                    loadSubscribeAndMessage(o);
                } else {
                    $rootScope.goToAuthorize();
                    return;
                }
            }));

            lastRoomBindings.push(chatSocket.subscribe("/topic/{0}chat.participants".format(room, globalConfig.lang), function(message) {
                var o = JSON.parse(message.body);
                console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!            ");
                console.log(o);
                $scope.participants = o["participants"];
            }));
        } else {
            console.log('subscribeMessageAndParticipants');
            subscribeMessageLP(); //@LP@
            subscribeParticipantsLP();
            loadSubscribeAndMessageLP();
        }

        lastRoomBindings.push(
            chatSocket.subscribe("/topic/{0}chat.typing".format(room), function(message) {
                var parsed = JSON.parse(message.body);
                if (parsed.username == $scope.chatUserId) return;
                //$scope.participants[parsed.username].typing = parsed.typing;
                for (var index in $scope.participants) {
                    var participant = $scope.participants[index];

                    if (participant.chatUserId == parsed.username) {
                        $scope.participants[index].typing = parsed.typing;
                        //break;
                    }
                }
            }));


        //chatSocket.send("/topic/{0}chat.participants".format(room), {}, JSON.stringify({}));
    }

    $scope.addUserToRoom = function() {
            chatControllerScope.userAddedToRoom = false;
            room = chatControllerScope.currentRoom.roomId + '/';
            if ($rootScope.socketSupport === true) {
                chatSocket.send("/app/chat/rooms.{0}/user.add.{1}".format(chatControllerScope.currentRoom.roomId, $scope.searchInputValue.email), {}, JSON.stringify({}));
                var myFunc = function() {
                    if (angular.isDefined(addingUserToRoom)) {
                        $timeout.cancel(addingUserToRoom);
                        addingUserToRoom = undefined;
                    }
                    if (chatControllerScope.userAddedToRoom) return;
                    toaster.pop('error', "Error", "server request timeout", 1000);
                    chatControllerScope.userAddedToRoom = true;

                };
                addingUserToRoom = $timeout(myFunc, 6000);
            } else {
                console.log("$scope.searchInputValue:" + $scope.searchInputValue);
                $http.post(serverPrefix + "/chat/rooms.{0}/user.add.{1}".format(chatControllerScope.currentRoom.roomId, $scope.searchInputValue.email), {}).
                success(function(data, status, headers, config) {
                    console.log("ADD USER OK " + data);
                    chatControllerScope.userAddedToRoom = true;
                }).
                error(function(data, status, headers, config) {
                    chatControllerScope.userAddedToRoom = true;
                });
            }
            $scope.searchInputValue.email = '';
        }
        
        $scope.removeUserFromRoom = function(userId) {
            debugger;
            $http.post(serverPrefix + "/chat/rooms.{0}/user.remove/{1}".format(chatControllerScope.currentRoom.roomId, userId), {}).
                success(function(data, status, headers, config) {
                    if(data == false)
                    {
                        toaster.pop('error', "Error", "Cant remove user from the room", 1000);
                        return;    
                    }
                    console.log("REMOVE USER OK " + data);
                }).
                error(function(data, status, headers, config) {
                    toaster.pop('error', "Error", "Cant remove user from the room", 1000);
                });
        }
        /*************************************
         * UPDATE MESSAGE LP
         *************************************/
    function subscribeMessageLP() {
        var currentUrl = serverPrefix + "/{0}/chat/message/update".format(chatControllerScope.currentRoom.roomId);
        console.log("subscribeMessageLP()");
        $scope.ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                 mimeType:"text/html; charset=UTF-8",
                url: currentUrl,
                success: function(data) {
                    var parsedData = JSON.parse(data);
                    for (var index = 0; index < parsedData.length; index++) {
                        if (parsedData[index].hasOwnProperty("message")) {
                            calcPositionPush(parsedData[index]); //POP
                            console.log("subscribeMessageLP success:" + parsedData[index]);
                        }
                    }
                    $scope.$apply();
                    subscribeMessageLP();
                },
                error: function(xhr, text_status, error_thrown) {
                    //if (text_status == "abort")return;

                    if (xhr.status === 0 || xhr.readyState === 0) {
                        //alert("discardMsg");
                        return;
                    }
                    if (xhr.status === 404 || xhr.status === 405) {
                    	//alert("13")
                        chatControllerScope.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                    }
                    subscribeMessageLP();
                }
            }));
    }

    function subscribeParticipantsLP() {
        var currentUrl = serverPrefix + "/{0}/chat/participants/update".format(chatControllerScope.currentRoom.roomId)
        $scope.ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                url: currentUrl,
                mimeType:"text/html; charset=UTF-8",
                success: function(data) {
                    console.log("subscribeParticipantsLP:" + data)
                    subscribeParticipantsLP();
                    var parsedData = JSON.parse(data);
                    if (parsedData.hasOwnProperty("participants"))
                        $scope.participants = parsedData["participants"];
                    $scope.$apply();

                },
                error: function(xhr, text_status, error_thrown) {
                    if (xhr.status === 0 || xhr.readyState === 0) return;
                    if (xhr.status === 404 || xhr.status === 405) {
                    	//alert(14)
                        chatControllerScope.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                        toaster.pop('warning', "Сталася помилка", "Кімната не існує або Ви не є її учасником", 5000);
                    }
                    subscribeParticipantsLP();

                }


            }));
    };

    /*************************************
     * SEND MESSAGE
     *************************************/
    $scope.sendMessage = function(message, attaches) {
            if (!chatControllerScope.messageSended)
                return;
            var textOfMessage;
            if (message === undefined) textOfMessage = $scope.newMessage;
            else textOfMessage = message;
            if (textOfMessage.length < 1) {
                $scope.newMessage = '';
                $("#newMessageInput")[0].value  = '';
                return;
            }
            var destination = "/app/{0}/chat.message".format(chatControllerScope.currentRoom.roomId);
            chatControllerScope.messageSended = false;
            if ($rootScope.socketSupport == true) {
                if ($scope.sendTo != "everyone") {
                    destination = "/app/{0}chat.private.".format(chatControllerScope.currentRoom.roomId) + $scope.sendTo;
                    calcPositionPush({ message: textOfMessage, username: 'you', priv: true, to: $scope.sendTo }); //POP
                }
                chatSocket.send(destination, {}, JSON.stringify({ message: textOfMessage, username: chatControllerScope.chatUserNickname, attachedFiles: attaches }));


                var myFunc = function() {
                    if (angular.isDefined(sendingMessage)) {
                        $timeout.cancel(sendingMessage);
                        sendingMessage = undefined;
                    }
                    if (chatControllerScope.messageSended) return;
                    messageError();
                    chatControllerScope.messageSended = true;

                };
                sendingMessage = $timeout(myFunc, 2000);
            } else {

                $http.post(serverPrefix + "/{0}/chat/message".format(chatControllerScope.currentRoom.roomId), { message: textOfMessage, username: chatControllerScope.chatUserNickname, attachedFiles: attaches }).
                success(function(data, status, headers, config) {
                    console.log("MESSAGE SEND OK " + data);
                    chatControllerScope.messageSended = true;
                }).
                error(function(data, status, headers, config) {
                    messageError();
                    chatControllerScope.messageSended = true;
                });
            };
            if (message === undefined)
                $scope.newMessage = '';

        }
        /*************************************
         * LOAD MESSAGE LP
         *************************************/

    function calcPositionUnshift(msg) {
        if (msg == null)
            return null;
        //     msg.message = msg.message.escapeHtml();//WRAP HTML CODE

        var summarised = false;
        $scope.oldMessage = msg;
        if ($scope.messages.length > 0) {
            if ($scope.messages[0].username == msg.username) {
                if (msg.attachedFiles.length == 0) {
                    summarised = true;
                    $scope.messages[0].message = msg.message + "\n\n" + $scope.messages[0].message;
                    //  $scope.messages[0].date = msg.date;
                }
                msg.position = $scope.messages[0].position;

            } else {
                msg.position = !$scope.messages[0].position;
            }
        } else {
            msg.position = false;
        }

        if (summarised == false)
            $scope.messages.unshift(msg);
    }

    function calcPositionPush(msg) {
        if (msg == null)
            return null;
        // msg.message = msg.message.escapeHtml();//WRAP HTML CODE

        var objDiv = document.getElementById("messagesScroll");
        var needScrollDown = Math.round(objDiv.scrollTop + objDiv.clientHeight) >= objDiv.scrollHeight - 100;

        if ($scope.messages.length > 0) {
            if ($scope.messages[$scope.messages.length - 1].username == msg.username)
                msg.position = $scope.messages[$scope.messages.length - 1].position;
            else
                msg.position = !$scope.messages[$scope.messages.length - 1].position;
        } else
            msg.position = false;


        if ($scope.messages.length > 0) {
            if ($scope.messages[$scope.messages.length - 1].username == msg.username && msg.attachedFiles.length == 0 && $scope.messages[$scope.messages.length - 1].attachedFiles.length == 0) {
                $scope.messages[$scope.messages.length - 1].date = msg.date;
                $scope.messages[$scope.messages.length - 1].message += "\n\n" + msg.message;
            } else {
                $scope.messages.push(msg);
            }
        } else {
            $scope.messages.push(msg);
        }

        $scope.$$postDigest(function() {
            var objDiv = document.getElementById("messagesScroll");
            if (needScrollDown)
                objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
        });

    }

    function loadSubscribeAndMessage(message) {
        $scope.roomType = message["type"];

        $scope.participants = message["participants"];
        if (typeof message["messages"] != 'undefined') {
            //  $scope.message_busy = true;
            $scope.oldMessage = message["messages"][message["messages"].length - 1];

            for (var i = 0; i < message["messages"].length; i++) {
                calcPositionUnshift(message["messages"][i]);
                //calcPositionUnshift(JSON.parse(o["messages"][i].text));
            }
        }
        var bot_params = JSON.parse(message["bot_param"]);
        if (bot_params.length > 0) {

            for (var key in bot_params)
                $scope.botParameters[bot_params[key].name] = JSON.parse(bot_params[key].value);
        }

        $timeout(function() {
            var objDiv = document.getElementById("messagesScroll");
            var count = 5;
            objDiv.scrollTop = objDiv.scrollHeight;
            $scope.message_busy = false;
        }, 100);


    }

    function loadSubscribesOnly(message) {
        $scope.participants = message["participants"];
        $scope.roomType = message["type"];
    }

    function loadMessagesOnly(message) {
        $scope.roomType = message["type"];
        for (var i = 0; i < message["messages"].length; i++) {
            calcPositionPush(message["messages"][i]); //POP
            //calcPositionUnshift(JSON.parse(o["messages"][i].text));
        }
    }

    function loadSubscribeAndMessageLP() {
        $http.post(serverPrefix + "/{0}/chat/participants_and_messages".format(chatControllerScope.currentRoom.roomId), {}).
        success(function(data, status, headers, config) {
            console.log("MESSAGE SEND OK " + data);
            loadSubscribeAndMessage(data);
        }).
        error(function(data, status, headers, config) {

        });
    }
    $scope.loadOtherMessages = function() {
        if ($scope.message_busy)
            return;
        $scope.message_busy = true;
        console.log("TRY " + $scope.messages.length);
        $http.post(serverPrefix + "/{0}/chat/loadOtherMessage".format(chatControllerScope.currentRoom.roomId), $scope.oldMessage). //  messages[0]). //
        success(function(data, status, headers, config) {
            console.log("MESSAGE onLOAD OK " + data);

            var objDiv = document.getElementById("messagesScroll");
            var lastHeight = objDiv.scrollHeight;
            if (data == "")
                return;



            for (var index = 0; index < data.length; index++) {
                if (data[index].hasOwnProperty("message")) {
                    calcPositionUnshift(data[index]);
                }
            }
            //restore scrole
            $scope.$$postDigest(function() {
                var objDiv = document.getElementById("messagesScroll");
                objDiv.scrollTop = objDiv.scrollHeight - lastHeight;
                $scope.message_busy = false;
                $scope.$apply();
            });
        }).
        error(function(data, status, headers, config) {
            console.log('TEST');
            if (status == "404" || status == "405") chatControllerScope.changeLocation("/chatrooms");
            //messageError("no other message");
        });
    }


    $scope.getNameFromUrl = function getNameFromUrl(url) {
        var fileNameSignaturePrefix = "file_name=";
        var startPos = url.lastIndexOf(fileNameSignaturePrefix) + fileNameSignaturePrefix.length;
        var endPos = url.length - DEFAULT_FILE_PREFIX_LENGTH;
        return url.substring(startPos, endPos);
    }

    $scope.checkUserAdditionPermission = function() {
        if (typeof chatControllerScope.currentRoom === "undefined") return false;
        var resultOfChecking = chatControllerScope.currentRoom.active && ($scope.roomType != 1) && (chatControllerScope.chatUserId == chatControllerScope.currentRoom.roomAuthorId) && chatControllerScope.isMyRoom;
        return resultOfChecking;
    }

    // file upload button click reaction
    angular.element(document.querySelector('#upload_file_form')).context.onsubmit = function() {
        var input = this.elements.myfile;
        var files = [];
        for (var i = 0; i < input.files.length; i++) files.push(input.files[i]);
        if (files) {
            uploadXhr(files, "upload_file/" + chatControllerScope.currentRoom.roomId,
                function successCallback(data) {
                    $scope.uploadProgress = 0;
                    $scope.sendMessage("я отправил вам файл", JSON.parse(data));
                    $('#myfile').fileinput('clear');
                    $scope.$apply();
                },
                function(xhr) {
                    $scope.uploadProgress = 0;
                    $scope.$apply();
                    alert("SEND FAILED:" + JSON.parse(xhr.response).message);
                },
                function(event, loaded) {
                    console.log(event.loaded + ' / ' + event.totalSize);
                    $scope.uploadProgress = Math.floor((event.loaded / event.totalSize) * 100);
                    $scope.$apply();

                });
        }
        return false;
    }


    /*
     * close event
     */
    function unsubscribeCurrentRoom(event) {
        var isLastRoomBindingsEmpty = lastRoomBindings == undefined || lastRoomBindings.length == 0;
        if (!isLastRoomBindingsEmpty) {

            while (lastRoomBindings.length > 0) {
                var subscription = lastRoomBindings.pop();
                //if (subscription!=undefined)
                subscription.unsubscribe();
            }
        }


        while ($scope.ajaxRequestsForRoomLP.length > 0) {

            var subscription = $scope.ajaxRequestsForRoomLP.pop();
            console.log("cancel ajaxRequestsForRoomLP:" + subscription);
            subscription.abort();
        }
        /* var answer = confirm("Are you sure you want to leave this page?")
        if (!answer) {
            event.preventDefault();
        }*/
    }
    $scope.$on('$locationChangeStart', unsubscribeCurrentRoom);


    $scope.$$postDigest(function() {
        var nice = $(".scroll").niceScroll();
        var lang = globalConfig.lang;
        if (lang=="ua")lang="uk";
        var fileInput = $("#myfile").fileinput({ language: "uk", maxFileSize: MAX_UPLOAD_FILE_SIZE_BYTES / 1000, minFileSize: 1, showCaption: false, initialPreviewShowDelete: true, browseLabel: "", browseClass: " btn btn-primary load-btn", uploadExtraData: { kvId: '10' } });
        $('#myfile').on('change', function(event, numFiles, label) {
            var totalFilesLength = 0;
            for (var i = 0; i < this.files.length; i++) {
                totalFilesLength += this.files[i].size;
            }
            if (totalFilesLength > MAX_UPLOAD_FILE_SIZE_BYTES) {
                $('#myfile').fileinput('lock');
                var noteStr = fileUploadLocal.fileSizeOverflowLimit + ":" + Math.round(totalFilesLength / 1024) + "/" + MAX_UPLOAD_FILE_SIZE_BYTES / 1024 + "Kb";
                $scope.$apply(function() {
                    toaster.pop('error', "Failed", noteStr, 5000);
                });

                //  alert(noteStr);
            }
        });
        $('#myfile').on('fileclear', function(event, numFiles, label) {
            $('#myfile').fileinput('unlock');
        });

    });


}]);
