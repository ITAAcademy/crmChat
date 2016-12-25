springChatServices.factory('RoomsFactory', ['$injector', '$route', '$routeParams', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', '$q', '$sce', '$rootScope', 'ChannelFactory', function($injector, $route, $routeParams, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, $q, $sce, $rootScope, ChannelFactory) {

    var addRoomWithBot = function(roomName) {
        $http.post(serverPrefix + "/chat/rooms/create/with_bot/", roomName)
        success(function(data, status, headers, config) {
            console.log('room with bot created: ' + roomName)
        }).
        error(function(data, status, headers, config) {
            console.log('creating room with bot failed ')
        });

    };
    var errorMsgTitleNotFound = "Сталася помилка";
    var errorMsgContentNotFound = "Кімната не існує або Ви не є її учасником";
    var lastRoomBindings = [];
    var currentRoom = null;
    var messages = [];
    var participants = [];
    var roomType = -1;
    var ajaxRequestsForRoomLP = [];
    var message_busy = true;
    var rooms = [];

    /*var updateContactsMapFromArray = function(contactsList) {
        for (var key in Object.keys(roomsMap))
            delete roomsMap[key];
        for (var i = 0; i < contactsList.length; i++) {
            var contact = contactsList[i];
            var characterForGroup = contact.string.charAt(0);
            if (roomsMap[characterForGroup] == null) {
                roomsMap[characterForGroup] = [];
            }
            roomsMap[characterForGroup].push(contact);
        }
    }*/

    var findParticipant = function(nickname) {
        for (var c_index in participants)
            if (participants[c_index].username == nickname)
                return participants[c_index];
        return null;
    }

    var setParticipantOnline = function(chatUserId) { //$rootScope.$on("login", function(event, chatUserId)
        for (var index in participants) {
            if (participants[index].chatUserId == chatUserId) {
                participants[index].online = true;
                break;
            }
        }
    };

    var setParticipantOffline = function(chatUserId) {
        for (var index in participants) {
            if (participants[index].chatUserId == chatUserId) {
                participants[index].online = false;
                break;
            }
        }
    };
    var unsubscribeWatch = null;

    var goToRoom = function(roomId) {
        //console.log("roomName:"+roomName);
        if (!$rootScope.isInited) {
            unsubscribeWatch = $rootScope.$watch('isInited', function(newValue, oldValue) {
                if (newValue == true) {
                    goToRoom(roomId);
                }
            });
            return;
        } else {
            if (unsubscribeWatch != undefined)
                unsubscribeWatch();
        }

        if (currentRoom !== undefined && getRoomById(rooms, currentRoom) !== undefined)
            getRoomById(rooms, currentRoom.roomId).date = curentDateInJavaFromat();
        return goToRoomEvn(roomId);
    };

    var goToRoomById = function(roomId) {
        console.log("roomId:" + roomId);
        return goToRoomEvn(roomId).then(function() {
            if (currentRoom !== undefined && getRoomById(rooms, currentRoom) !== undefined)
                getRoomById(rooms, currentRoom.roomId).date = curentDateInJavaFromat();
        });

    };

    function goToRoomEvn(id) {
        console.log("goToRoomEvn(" + id + ")");
        currentRoom = { roomId: id };
        changeRoom();
        var deferred = $q.defer();
        var room = getRoomById(rooms, id);
        if (room != undefined) {
            currentRoom = room;
            room.nums = 0;
        } else {
            deferred.reject();
            return deferred.promise;
        }

        if (ChannelFactory.isSocketSupport() === true) {
            chatSocket.send("/app/chat.go.to.dialog/{0}".format(currentRoom.roomId), {}, JSON.stringify({}));
            deferred.resolve();
            return deferred.promise;
        } else {
            return $http.post(serverPrefix + "/chat.go.to.dialog/{0}".format(currentRoom.roomId));
        }
    }
        var NEXT_MESSAGE_TIME_LIMIT_SECONDS = 10;
    function calcPositionUnshift(msg) {
        if (msg == null)
            return null;
        //     msg.message = msg.message.escapeHtml();//WRAP HTML CODE
        var summarised = false;
        oldMessage = msg;

        if (messages.length > 0) {
             var isActual = differenceInSecondsBetweenDates(new Date(msg.date),new Date(messages[0].date))<NEXT_MESSAGE_TIME_LIMIT_SECONDS;
            if ( messages[0].username == msg.username) {
                if (isActual && msg.attachedFiles.length == 0) {
                    summarised = true;
                    messages[0].message = msg.message + "\n\n" + messages[0].message;
                    //  $scope.messages[0].date = msg.date;
                }
                msg.position = messages[0].position;

            } else {
                msg.position = !messages[0].position;
            }
        } else {
            msg.position = false;
        }

        if (summarised == false)
            messages.unshift(msg);
    }

    function calcPositionPush(msg) {

        if (msg == null)
            return null;
        // msg.message = msg.message.escapeHtml();//WRAP HTML CODE

        var objDiv = document.getElementById("messagesScroll");
        var needScrollDown = Math.round(objDiv.scrollTop + objDiv.clientHeight) >= objDiv.scrollHeight - 100;
        if (needScrollDown){
            $rootScope.$broadcast('MessageAreaScrollDownEvent');
        }
        if (messages.length > 0) {
            if (messages[messages.length - 1].username == msg.username)
                msg.position = messages[messages.length - 1].position;
            else
                msg.position = !messages[messages.length - 1].position;
        } else
            msg.position = false;


        if (messages.length > 0) {
             var isActual = differenceInSecondsBetweenDates(new Date(msg.date),new Date(messages[messages.length - 1].date))<NEXT_MESSAGE_TIME_LIMIT_SECONDS;
            if (isActual && messages[messages.length - 1].username == msg.username && msg.attachedFiles.length == 0 && messages[messages.length - 1].attachedFiles.length == 0) {
                messages[messages.length - 1].date = msg.date;
                messages[messages.length - 1].message += "\n\n" + msg.message;
            } else {
                messages.push(msg);
            }
        } else {
            messages.push(msg);
        }

    }

    /*************************************
     * CHANGE ROOM
     *************************************/
    var changeRoom = function() {
        //alert(16);
        messages = [];
        console.log("roomId:" + currentRoom.roomId);

        if (ChannelFactory.isSocketSupport() === true) {
            lastRoomBindings.push(
                chatSocket.subscribe("/topic/{0}/chat.message".format(currentRoom.roomId), function(message) {
                    calcPositionPush(JSON.parse(message.body)); //POP
                }));

            lastRoomBindings.push(chatSocket.subscribe("/app/{0}/chat.participants/{1}".format(currentRoom.roomId, globalConfig.lang), function(message) {
                if (message.body != "{}") {
                    var o = JSON.parse(message.body);
                    loadSubscribeAndMessage(o);
                } else {
                    $rootScope.goToAuthorize();
                    return;
                }
            }));

            lastRoomBindings.push(chatSocket.subscribe("/topic/{0}/chat.participants".format(currentRoom.roomId, globalConfig.lang), function(message) {
                var o = JSON.parse(message.body);
                participants = o["participants"];
            }));
        } else {
            subscribeMessageLP(); //@LP@
            subscribeParticipantsLP();
            loadSubscribeAndMessageLP();
        }

        lastRoomBindings.push(
            chatSocket.subscribe("/topic/{0}/chat.typing".format(currentRoom.roomId), function(message) {
                var parsed = JSON.parse(message.body);
                if (parsed.username == UserFactory.chatUserId) return;

                for (var index in participants) {
                    var participant = participants[index];

                    if (participant.chatUserId == parsed.username) {
                        participants[index].typing = parsed.typing;
                    }
                }
            }));
        //chatSocket.send("/topic/{0}chat.participants".format(room), {}, JSON.stringify({}));
    }
    var addTenantToRoom = function(id) {
        $http.post(serverPrefix + "/bot_operations/tenant/{0}/askToAddToRoom/{1}".format(id, currentRoom.roomId), {}).
        success(function(data, status, headers, config) {
            userAddedToRoom = true;
        }).
        error(function(data, status, headers, config) {
            userAddedToRoom = true;
        });
    }


    var addUserToRoom = function(email) {
        userAddedToRoom = false;

        if (ChannelFactory.isSocketSupport() === true) {
            chatSocket.send("/app/chat/rooms.{0}/user.add.{1}".format(currentRoom.roomId, email), {}, JSON.stringify({}));
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
            $http.post(serverPrefix + "/chat/rooms.{0}/user.add.{1}".format(currentRoom.roomId, email), {}).
            success(function(data, status, headers, config) {
                chatControllerScope.userAddedToRoom = true;
            }).
            error(function(data, status, headers, config) {
                chatControllerScope.userAddedToRoom = true;
            });
        }
    }

    var removeUserFromRoom = function(userId) {

            $http.post(serverPrefix + "/chat/rooms.{0}/user.remove/{1}".format(currentRoom.roomId, userId), {}).
            success(function(data, status, headers, config) {
                if (data == false) {
                    toaster.pop('error', "Error", "Cant remove user from the room", 1000);
                    return;
                }
            }).
            error(function(data, status, headers, config) {
                toaster.pop('error', "Error", "Cant remove user from the room", 1000);
            });
        }
        /*************************************
         * UPDATE MESSAGE LP
         *************************************/
    var subscribeMessageLP = function() {
        var currentUrl = serverPrefix + "/{0}/chat/message/update".format(currentRoom.roomId);
        ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                mimeType: "text/html; charset=UTF-8",
                url: currentUrl,
                success: function(data) {
                    var parsedData = JSON.parse(data);
                    for (var index = 0; index < parsedData.length; index++) {
                        if (parsedData[index].hasOwnProperty("message")) {
                            calcPositionPush(parsedData[index]); //POP
                        }
                    }
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
                        ChannelFactory.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                    }
                    subscribeMessageLP();
                }
            }));
    }

    var subscribeParticipantsLP = function() {
        var currentUrl = serverPrefix + "/{0}/chat/participants/update".format(currentRoom.roomId)
        ajaxRequestsForRoomLP.push(
            $.ajax({
                type: "POST",
                url: currentUrl,
                mimeType: "text/html; charset=UTF-8",
                success: function(data) {
                    subscribeParticipantsLP();
                    var parsedData = JSON.parse(data);
                    if (parsedData.hasOwnProperty("participants"))
                        participants = parsedData["participants"];
                },
                error: function(xhr, text_status, error_thrown) {
                    if (xhr.status === 0 || xhr.readyState === 0) return;
                    if (xhr.status === 404 || xhr.status === 405) {
                        //alert(14)
                        ChannelFactory.changeLocation("/chatrooms");
                        toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                        toaster.pop('warning', "Сталася помилка", "Кімната не існує або Ви не є її учасником", 5000);
                    }
                    //subscribeParticipantsLP();
                }
            }));
    };

    /*************************************
     * SEND MESSAGE
     *************************************/

    /*************************************
     * LOAD MESSAGE LP
     *************************************/

    function loadSubscribesOnly(message) {
        participants = message["participants"];
        roomType = message["type"];
    }

    function loadMessagesOnly(message) {
        roomType = message["type"];
        for (var i = 0; i < message["messages"].length; i++) {
            calcPositionPush(message["messages"][i]); //POP
            //calcPositionUnshift(JSON.parse(o["messages"][i].text));
        }
    }

    function loadSubscribeAndMessage(message) {
        roomType = message["type"];

        participants = message["participants"];
        if (typeof message["messages"] != 'undefined') {
            $rootScope.$broadcast('messageBusyEvent',true);
            oldMessage = message["messages"][message["messages"].length - 1];

            for (var i = 0; i < message["messages"].length; i++) {
                calcPositionUnshift(message["messages"][i]);
                //calcPositionUnshift(JSON.parse(o["messages"][i].text));
            }
        }
        var bot_params = JSON.parse(message["bot_param"]);
        if (bot_params.length > 0) {

            for (var key in bot_params)
                botParameters[bot_params[key].name] = JSON.parse(bot_params[key].value);
        }
        $timeout(function() {
            var objDiv = document.getElementById("messagesScroll");
            var count = 5;
            objDiv.scrollTop = objDiv.scrollHeight;
               $rootScope.$broadcast('messageBusyEvent',false);
        }, 100);


    }

    function loadSubscribeAndMessageLP() {
        $http.post(serverPrefix + "/{0}/chat/participants_and_messages".format(currentRoom.roomId), {}).
        success(function(data, status, headers, config) {
            loadSubscribeAndMessage(data);
        }).
        error(function(data, status, headers, config) {

        });
    }
    var loadOtherMessages = function() {
        if (message_busy)
            return;
        message_busy = true;
        $http.post(serverPrefix + "/{0}/chat/loadOtherMessage".format(currentRoom.roomId), oldMessage). //  messages[0]). //
        success(function(data, status, headers, config) {
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
        }).
        error(function(data, status, headers, config) {
            if (status == "404" || status == "405") chatControllerScope.ChannelFactory.changeLocation("/chatrooms");
            //messageError("no other message");
        });
    }

    var checkUserAdditionPermission = function() {
        var needPrivilege = USER_COPABILITIES_BY_ROOM.ADD | USER_COPABILITIES_BY_ROOM.REMOVE;
        var havePermitions = UserFactory.chatUserId == currentRoom.roomAuthorId;
        havePermitions = havePermitions || (currentRoom.userPermissions & needPrivilege) == needPrivilege
        if (typeof currentRoom === "undefined") return false;
        var resultOfChecking = currentRoom.active /*&& ($scope.roomType != 1)*/ && havePermitions && chatControllerScope.isMyRoom && authorize;
        return resultOfChecking;
    }

    var unsubscribeCurrentRoom = function(event) {
        var isLastRoomBindingsEmpty = lastRoomBindings == undefined || lastRoomBindings.length == 0;
        if (!isLastRoomBindingsEmpty) {
            while (lastRoomBindings.length > 0) {
                var subscription = lastRoomBindings.pop();
                //if (subscription!=undefined)
                subscription.unsubscribe();
            }
        }
        while (ajaxRequestsForRoomLP.length > 0) {
            var subscription = ajaxRequestsForRoomLP.pop();
            subscription.abort();
        }
        /* var answer = confirm("Are you sure you want to leave this page?")
        if (!answer) {
            event.preventDefault();
        }*/
    }
    tenants = [];
    var subscribeBindings = [];

    function updateTenants(tenants) {
        this.tenants = tenants;
        var itemsToRemove = [];
        if (tenants != null) {
            for (var i = 0; i < tenants.length; i++) {
                if (UserFactory.chatUserId == tenants[i].chatUserId) {
                    itemsToRemove.push(i);
                    continue;
                }
            }
            for (var k = itemsToRemove.length - 1; k >= 0; k--)
                tenants.splice(itemsToRemove[k], 1);
        }
    }

    function initSocketsSubscribes() {
        console.log('initSocketsSubscribes');
        subscribeBindings.push(chatSocket.subscribe("/topic/chat.tenants.remove", function(message) {
            var tenant = JSON.parse(message.body);
            for (var i = 0; i < tenants.length; i++) {
                if (tenant.chatUserId == tenants[i].chatUserId) {
                    tenants.splice(i, 1);
                    break;
                }
            }
            //updateTenants(o);
        }));
        /*   subscribeBindings.push(chatSocket.subscribe("/app/chat.tenants", function(message) {
            var o = JSON.parse(message.body);
            updateTenants(o);
        }));*/
        subscribeBindings.push(chatSocket.subscribe("/topic/chat.tenants.add", function(message) {
            var tenant = JSON.parse(message.body);
            var alreadyExcist = false;
            for (var i = 0; i < tenants.length; i++) {
                if (tenant.chatUserId == tenants[i].chatUserId || UserFactory.chatUserId == tenant.chatUserId) {
                    alreadyExcist = true;
                    break;
                }
            }
            if (!alreadyExcist && tenant.chatUserId != UserFactory.chatUserId) tenants.push(tenant);
            // updateTenants(o);
        }));
    }

    // $watch('isInited', function() {
    function afterIsInited() {
        console.log("try " + currentRoom);
        if (isInited == true) {
            updateTenants(chatControllerScope.tenants);
            var room = getRoomById(rooms, $routeParams.roomId);

            if (room != null && room.type == 2 && controllerName != "ConsultationController") //redirect to consultation
            {
                $http.post(serverPrefix + "/chat/consultation/fromRoom/" + room.roomId)
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

            goToDialog($routeParams.roomId).then(function(data) {

                if (ChannelFactory.isSocketSupport() === true) {
                    initSocketsSubscribes();
                }
                if (data != undefined && data != null) {
                    currentRoom = data.data;
                    dialogName = currentRoom.string;
                }
                pageClass = 'scale-fade-in';
            }, function() {
                $rootScope.goToAuthorize();
                toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                // location.reload();
            });

            $http.post(serverPrefix + "/bot_operations/tenant/did_am_wait_tenant/{0}".format(currentRoom.roomId)).
            success(function(data, status, headers, config) {
                if (data == true)
                    showToasterWaitFreeTenant();
            }).
            error(function(data, status, headers, config) {
                alert("did_am_wait_tenant: server error")
            });

        }

    }

    function subscribeRoomsUpdateLP() {
        console.log("roomsUpdateLP()");
        $http.post(serverPrefix + "/chat/rooms/user/login")
            .success(function(data, status, headers, config) {
                console.log("roomsUpdateLP data:" + data);
                updateRooms(data);
                //console.log("resposnse data received:"+response.data);
                subscribeRoomsUpdateLP();
            }).error(function errorHandler(data, status, headers, config) {
                //console.log("error during http request");
                //$scope.topics = ["error"];
                //console.log("resposnse data error:"+response.data);
                subscribeRoomsUpdateLP();
            });

    }

    var updateRooms = function(message) {
        var parseObj;
        if (ChannelFactory.isSocketSupport() === true) {
            parseObj = JSON.parse(message.body);
        } else {
            parseObj = message;
        }
        var needReplace = parseObj.replace;
        var roomList = parseObj.list;
        if (needReplace) {
            rooms = roomList;
        } else {
            for (var i = 0; i < rooms.length; i++) {
                for (var j = roomList.length - 1; j >= 0; j--) {
                    if (roomList[j].roomId == rooms[i].roomId) {
                        rooms[i] = roomList[j];
                        roomList.splice(j, 1);
                    }
                }
            }
        }
        if (typeof currentRoom != 'undefined') {
            currentRoom = getRoomById(rooms, currentRoom.roomId);
        }
    }


    return {
        goToRoom: goToRoom,
        unsubscribeCurrentRoom: unsubscribeCurrentRoom,
        updateRooms: updateRooms,
        getCurrentRoom: function() {
            return currentRoom;
        },
        setRooms: function(roomsArg) { rooms = roomsArg; },
        getRooms: function() {
            return rooms; },
            getMessages: function(){
                return messages;
            },
        getParticipants: function(){
            return participants;
        },
        getOldMessage: function(){
            return oldMessage;
        },
        calcPositionUnshift:calcPositionUnshift

    };



}]).config(function($routeProvider) {
    /* $routeProvider.when("/chatrooms", {
         templateUrl: "dialogsTemplate.html",
         controller: "DialogsRouteController"
     });*/
    $routeProvider.when("/dialog_view/:roomId/", {
        resolve: {
            load: function($route, RoomsFactory, $routeParams) {
                RoomsFactory.goToRoom($route.current.params.roomId);
            }
        }
    });
    $routeProvider.when("/access_deny", {
        templateUrl: "accessDeny.html",
        controller: "AccessDeny"
    });

    $routeProvider.otherwise({ redirectTo: '/' });
    console.log("scope test");

});
