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

    $scope.states = ["Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Dakota", "North Carolina", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"]; //nice.hide();
    $scope.selected = undefined;
    $scope.totalItems = 64;
    $scope.currentPage = 4;

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
        toaster.pop('error', "Error", "server request timeout", 1000);
    }

    function messageError(mess) {
        toaster.pop('error', "Error", mess, 1000);
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

        if (event.keyCode === 9) { // tab was pressed

            // get caret position/selection
            debugger;
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
        var arrowKeyPressed = kk == 38 || kk == 39 || kk == 40 || kk == 37;
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
        //		Don't send notification if we are still typing or we are typing a private message
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
    
    $scope.showCommandListInMessageInput(command) {
    	$scope.show_search_list_in_message_input = true;
        $scope.data_in_message_input = [];
        $timeout.cancel(showCommandListInMessageInputTimer); //888
        
        showCommandListInMessageInputTimer = $timeout(function() {
            $scope.show_search_list_in_message_input = true;
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_commands_like?login=" + command,
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
                url: serverPrefix + "/get_users_emails_like?login=" + email + "&room=" + $scope.currentRoom.roomId
                +"&eliminate_users_of_current_room=false", //'/get_users_emails_like',
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
    	avatar_contaiter.on('load', function(){
    		
    	for (var i = 0; i <  avatar_contaiter.length; i++)
		{
    		var image = avatar_contaiter.eq(i);
    			var width = image.width();
        	var height = image.height();
        	image.css("position",  "static");
        	if (parseInt(width, 10) < parseInt(height, 10)  )
        	{
        		var proportcialHeight = height * 100 / width;
        		image.css("height", proportcialHeight + "%");
        		image.css("width", "100%");
        		image.css("top", "-75px");
        		image.css("overflow", "hidden");
        		image.css("transform", " translateX(-0%) translateY(-5%)");
        	}
        	else
        	if (parseInt(width, 10) > parseInt(height, 10) )
        	{
        		var proportcialWidth = width * 100 / height;
        		image.css("width", proportcialWidth + "%"); 
        		image.css("height", "100%");
        		image.css("transform", " translateX(-"+ (proportcialWidth - 100)/2 + "%)");
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
        resetSpecialInput();
    }

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
    	  var commandStartIndex = message.lastIndexOf('@') + 1;
          var commandPrefix = message.substring(commandStartIndex, carretPosIndex);
          
          $scope.showCommandListInMessageInput(commandPrefix);
    }
    

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

        //Upload the image
        //(Uploader is a service in my application, you will need to create your own)
        if (files) {
            uploadXhr(files, "upload_file/" + $scope.currentRoom.roomId,
                function successCallback(data) {
                    $scope.uploadProgress = 0;
                    $scope.sendMessage("я отправил вам файл", JSON.parse(data));
                    $scope.$apply();
                },
                function(xhr) {
                    $scope.uploadProgress = 0;
                    $scope.$apply();
                    alert("SEND FAILD:" + xhr.response);
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
        $scope.changeRoom();
        var deferred = $q.defer();
        var room = getRoomById($scope.rooms, id);
        if (room != undefined) {
            chatControllerScope.currentRoom = room;
            //$scope.$apply();
            room.nums = 0;
            $scope.dialogName = room.string;
        } else {
            /*	$http.post(serverPrefix + "/chat/rooms/roomInfo/" + $scope.currentRoom.roomId)).
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
        $scope.messages = [];
        console.log("roomId:" + chatControllerScope.currentRoom.roomId);
        room = chatControllerScope.currentRoom.roomId + '/';

        if ($rootScope.socketSupport == true) {
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}chat.message".format(room), function(message) {
                    calcPositionPush(JSON.parse(message.body)); //POP
                }));

            lastRoomBindings.push(chatSocket.subscribe("/app/{0}chat.participants".format(room), function(message) {
                if (message.body != "{}") {
                    var o = JSON.parse(message.body);
                    loadSubscribeAndMessage(o);
                } else {
                    $rootScope.goToAuthorize();
                    return;
                }
            }));

            lastRoomBindings.push(chatSocket.subscribe("/topic/{0}chat.participants".format(room), function(message) {
                var o = JSON.parse(message.body);
                console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!			");
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
            room = $scope.currentRoom.roomId + '/';
            if ($rootScope.socketSupport === true) {
                chatSocket.send("/app/chat/rooms.{0}/user.add.{1}".format($scope.currentRoom.roomId, $scope.searchInputValue.email), {}, JSON.stringify({}));
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
                $http.post(serverPrefix + "/chat/rooms.{0}/user.add.{1}".format($scope.currentRoom.roomId, $scope.searchInputValue.email), {}).
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
        /*************************************
         * UPDATE MESSAGE LP
         *************************************/
    function subscribeMessageLP() {
        var currentUrl = serverPrefix + "/{0}/chat/message/update".format($scope.currentRoom.roomId);
        console.log("subscribeMessageLP()");
        $scope.ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
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
                        chatControllerScope.changeLocation("/chatrooms");
                    }
                    subscribeMessageLP();
                }
            }));
    }

    function subscribeParticipantsLP() {
        var currentUrl = serverPrefix + "/{0}/chat/participants/update".format($scope.currentRoom.roomId)
        $scope.ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                url: currentUrl,
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
                        chatControllerScope.changeLocation("/chatrooms");
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

        var summarised = false;
        $scope.oldMessage = msg;
        if ($scope.messages.length > 0) {
            if ($scope.messages[0].username == msg.username) {
                if (msg.attachedFiles.length == 0) {
                    summarised = true;
                    $scope.messages[0].message = msg.message + "\n\n" + $scope.messages[0].message;
                    //	$scope.messages[0].date = msg.date;
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
            if ($scope.messages[$scope.messages.length - 1].username == msg.username && msg.attachedFiles.length == 0) {
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
                objDiv.scrollTop = objDiv.scrollHeight;
        });

    }

    function loadSubscribeAndMessage(message) {
        $scope.roomType = message["type"];
        if ($scope.roomType == 2 && $route.current.scope.name != "ConsultationController") //redirect to consultation
        {
            $http.post(serverPrefix + "/chat/consultation/fromRoom/" + chatControllerScope.currentRoom.roomId)
                .success(function(data, status, headers, config) {
                    if (data == "" || data == undefined)
                        $rootScope.goToAuthorize(); //not found => go out
                    else
                        $location.path("consultation_view/" + data);
                }).error(function errorHandler(data, status, headers, config) {
                    $rootScope.goToAuthorize(); //not found => go out
                });
            return;
        }

        $scope.participants = message["participants"];
        if (typeof message["messages"] != 'undefined') {
            //	$scope.message_busy = true;
            $scope.oldMessage = message["messages"][message["messages"].length - 1];

            for (var i = 0; i < message["messages"].length; i++) {
                calcPositionUnshift(message["messages"][i]);
                //calcPositionUnshift(JSON.parse(o["messages"][i].text));
            }
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
            uploadXhr(files, "upload_file/" + $scope.currentRoom.roomId,
                function successCallback(data) {
                    $scope.uploadProgress = 0;
                    $scope.sendMessage("я отправил вам файл", JSON.parse(data));
                    $('#myfile').fileinput('clear');
                    $scope.$apply();
                },
                function(xhr) {
                    $scope.uploadProgress = 0;
                    $scope.$apply();
                    alert("SEND FAILD:" + xhr);
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
        var fileInput = $("#myfile").fileinput({ language: "uk", showCaption: false, initialPreviewShowDelete: true, browseLabel: "", browseClass: " btn btn-primary load-btn", uploadExtraData: { kvId: '10' } });
    })

}]);
