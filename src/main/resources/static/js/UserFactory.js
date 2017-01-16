springChatServices.factory('UserFactory', ['$timeout', '$rootScope', '$location', '$http', 'toaster', '$injector', 'ChannelFactory', 'ChatSocket', 'AskWindow', function($timeout, $rootScope, $location, $http, toaster, $injector, ChannelFactory, chatSocket, AskWindow) {
    $rootScope.authorize = false;
    var isTenant = false;
    var isTrainer = false;
    var isStudent = false;
    var chatUserNickname, chatUserRole, chatUserAvatar;
    var realChatUserId;
    var chatUserId = -1;
    var isUserTenantInited = false;
    var isTenantFree;
    var chatUserRole;
    var isInited = false;

    var tenants;
    var friends;
    var onlineUsersIds = [];

    var checkRole = function() {
        if (chatUserRole & 256)
            return true;
        return false;
    }
    var isUserOnline = function(chatUserId){
        return onlineUsersIds.indexOf(chatUserId)==-1 ? false : true;
    }
    var setOnlineUsersIds = function(ids){
        onlineUsersIds = ids;
    }

    var socketSupport = true;

    var initStompClient = function() {

        function reInitForLP() {
            $http.post(serverPrefix + "/chat/login/" + getChatUserId(), { message: 'true' }).
            success(function(data, status, headers, config) {
                console.log("LOGIN OK " + data);
                login(data);
            }).
            error(function(data, status, headers, config) {
                messageError();
                toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            });
        }
        var onConnect = function(frame) {
            console.log("onconnect");
            setChatUserId(frame.headers['user-name']);
            initForWS(false);
            setRealChatUserId(getChatUserId());


        };

        ChannelFactory.subscribeToConnect(function(socketSupport, frame) {
            if (socketSupport) {
                onConnect(frame);
            } else {
                $http.post(serverPrefix + "/chat/login/" + getChatUserId(), { message: 'true' }).
                success(function(data, status, headers, config) {
                    var RoomsFactory = $injector.get('RoomsFactory');
                    console.log("LOGIN OK " + data);
                    login(data);
                    RoomsFactory.subscribeRoomsUpdateLP();

                    subscribeInfoUpdateLP();
                    setRealChatUserId(getChatUserId());
                }).
                error(function(data, status, headers, config) {
                    messageError();
                    toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
                });
            }

        })
    };
    $rootScope.$on("login", function(event, chatUserId){
        onlineUsersIds.push(chatUserId);
    });
    $rootScope.$on("logout",function(event,chatUserId){
        var index = onlineUsersIds.indexOf(chatUserId);
        onlineUsersIds.splice(index,1);
    });

    function initForWS(reInit) {
        chatSocket.subscribe("/app/chat.login/{0}".format(getChatUserId()), function(message) {
            var mess_obj = JSON.parse(message.body);

            login(mess_obj);

            if (reInit == false)
                $timeout(function() {
                    chatSocket.subscribe("/topic/chat.login", function(message) {

                        var chatUserId = JSON.parse(message.body).chatUserId;
                        $rootScope.$broadcast("login", chatUserId);
                    });
                    chatSocket.subscribe("/topic/chat.logout", function(message) {
                        var chatUserId = JSON.parse(message.body).username;
                        $rootScope.$broadcast("logout", chatUserId);

                    });

                    chatSocket.subscribe("/topic/users/must/get.room.num/chat.message", function(message) { // event update
                        console.log("new message in room:" + message.body);
                        var num = JSON.parse(message.body);
                        $rootScope.$broadcast('newMessageEvent', num);
                    });

                    chatSocket.subscribe("/topic/users/{0}/status".format(getChatUserId()), function(message) {
                        var operationStatus = JSON.parse(message.body);
                        switch (operationStatus.type) {
                            case Operations.send_message_to_all:
                            case Operations.send_message_to_user:
                                messageSended = true;
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", message.body);
                                break;
                            case Operations.add_user_to_room:
                                userAddedToRoom = true;
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", "user wasn't added to room");
                                break;
                            case Operations.add_room:
                                roomAdded = true;
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", "room wasn't added");
                                break;
                            case Operations.add_room_on_login:
                                $timeout(function() {
                                    changeLocation("dialog_view/" + operationStatus.description);
                                }, 1000);
                                break;
                            case Operations.add_room_from_tenant:
                                //changeLocation("dialog_view/"+operationStatus.description);
                                break;
                            case "updateRoom":
                                // 
                                break;

                        }
                        //                      ZIGZAG OPS
                    });

                    chatSocket.subscribe("/topic/users/{0}/info".format(getChatUserId()), function(message) {
                        var body = JSON.parse(message.body);
                        AskWindow.askObject = body;
                        AskWindow.showAskWindow();
                    });

                    chatSocket.subscribe("/topic/users/{0}/submitConsultation".format(getChatUserId()), function(message) {
                        var body = JSON.parse(message.body);
                        $rootScope.sendedRoomId = body[0];

                        $rootScope.submitConsultation_processUser($rootScope.sendedRoomId);

                        var sendedConsultantId = body[1];
                        $rootScope.submitConsultation_processTenant(sendedConsultantId, $rootScope.sendedRoomId);
                        // alert("sendedId = " + $rootScope.sendedId + "\n  sendedConsultantId = " + sendedConsultantId)

                    });

                    chatSocket.subscribe("/app/chat/room.private/room_require_trainer".format(getChatUserId()), function(message) {
                        var body = JSON.parse(message.body);
                        roomsRequiredTrainers = body;
                    });
                    chatSocket.subscribe("/topic/chat/room.private/room_require_trainer.add".format(getChatUserId()), function(message) {
                        var roomsMap = JSON.parse(message.body);
                        for (var key in roomsMap) {
                            if (roomsMap.hasOwnProperty(key)) {
                                roomsRequiredTrainers[key] = roomsMap[key];
                            }
                        }

                    });
                    chatSocket.subscribe("/topic/chat/room.private/room_require_trainer.remove", function(message) {
                        var idToRemove = JSON.parse(message.body);
                        delete roomsRequiredTrainers[idToRemove];

                    });


                    chatSocket.subscribe("/topic/users/info", function(message) {
                        var RoomsFactory = $injector.get('RoomsFactory');
                        var operationStatus = JSON.parse(message.body);
                        //operationStatus = JSON.parse(operationStatus);
                        if (operationStatus["updateRoom"].roomId == RoomsFactory.currentRoom.roomId) {
                            RoomsFactory.currentRoom = operationStatus["updateRoom"];
                        }
                        //
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
                        toaster.pop('error', "Error", message.body);
                    });
                }, 1500);

        });

        chatSocket.subscribe("/topic/chat/rooms/user.{0}".format(getChatUserId()), function(message) { // event update
            console.log("chatUserId:" + getChatUserId());
            var RoomsFactory = $injector.get('RoomsFactory');
            RoomsFactory.updateRooms(message);
        });
    }

    function subscribeInfoUpdateLP() {
        // alert("subscribeInfoUpdateLP()");
        $http.post(serverPrefix + "/chat/global/lp/info")
            .success(function(data, status, headers, config) {
                console.log("infoUpdateLP data:" + data);
                if (data["newMessage"] != null) //new message in room
                {
                    $rootScope.$broadcast('newMessageArrayEvent', data["newMessage"]);
                }
                /*if (data["newGuestRoom"] != null) {
                    if ($scope.currentRoom == undefined)
                        $scope.currentRoom = { roomId: data["newGuestRoom"] };
                    else
                        $scope.currentRoom.roomId = data["newGuestRoom"];
                    changeLocation("/dialog_view/" + data["newGuestRoom"]);
                }*/
                if (data["newAsk_ToChatUserId"] != null) {
                    /*SHOW*/
                    AskWindow.askObject = data["newAsk_ToChatUserId"][0];
                    AskWindow.showAskWindow();
                }
                if (data["newConsultationWithTenant"] != null) {
                    var roomId = data["newConsultationWithTenant"][0][0];

                    $rootScope.submitConsultation_processUser(roomId);

                    var sendedConsultantId = data["newConsultationWithTenant"][0][1];

                    $rootScope.submitConsultation_processTenant(sendedConsultantId, roomId);
                }
                if (data["tenants.add"] != null) {
                    //TODO tenant addition to list
                    var tenantObjs = data["tenants.add"];
                    for (var i = 0; i < tenantObjs.length; i++) {
                        addTenantToList(tenantObjs[i]);
                    }


                }
                if (data["tenants.remove"] != null) {
                    //TODO renant removing from list
                    var tenantObjs = data["tenants.remove"];
                    for (var i = 0; i < tenantObjs.length; i++) {
                        removeTenantFromList((tenantObjs[i]))
                    }

                }
                /* if (data["updateRoom"] != null && data["updateRoom"][0]["updateRoom"].roomId == $scope.currentRoom.roomId) {
                     
                     $scope.currentRoom = data["updateRoom"][0]["updateRoom"];;
                 }*/
                subscribeInfoUpdateLP();
            }).error(function errorHandler(data, status, headers, config) {
                subscribeInfoUpdateLP();
            });

    }
    initStompClient();

    function login(mess_obj) {
        var RoomsFactory = $injector.get('RoomsFactory');
        chatUserId = mess_obj.chat_id;
        isTenant = Boolean(mess_obj.isTenant);
        isTrainer = Boolean(mess_obj.isTrainer);
        isStudent = Boolean(mess_obj.isStudent);

        console.log("isTenant:" + isTenant + " isTrainer:" + isTrainer + " isStudent:" + isStudent);
        chatUserNickname = mess_obj.chat_user_nickname;
        chatUserRole = mess_obj.chat_user_role;
        chatUserAvatar = mess_obj.chat_user_avatar
        $rootScope.isInited = true;

        var onlineUserIds = JSON.parse(mess_obj.onlineUsersIdsJson);
        setOnlineUsersIds(onlineUserIds);

        if (mess_obj.nextWindow == -1) {
            toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            return;
        }
        if (ChannelFactory.isSocketSupport() == false) {
            RoomsFactory.setRooms(JSON.parse(mess_obj.chat_rooms).list);
        } else {
            initIsUserTenant();
            RoomsFactory.setRooms(JSON.parse(mess_obj.chat_rooms).list);
        }
        tenants = typeof mess_obj["tenants"] == "undefined" ? undefined : JSON.parse(mess_obj["tenants"]);
        friends = typeof mess_obj["friends"] == "undefined" ? undefined : JSON.parse(mess_obj["friends"]);
        isInited = true;

        if (mess_obj.nextWindow == 0) {

            // if ($scope.currentRoom.roomId != undefined)
            /* if ($scope.currentRoom != undefined)
                 if ($scope.currentRoom.roomId != undefined && $scope.currentRoom.roomId != '' && $scope.currentRoom.roomId != -1) {
                     //mess_obj.nextWindow=$scope.currentRoom.roomId;
                     //  goToDialogEvn($scope.currentRoom.roomId);
                     console.log("currentRoom");
                     ChannelFactory.changeLocation("/dialog_view/" + $scope.currentRoom.roomId);
                     $scope.showDialogListButton = true;
                     return;
                 }*/
            $rootScope.authorize = true;
            if ($location.path() == "/")
                ChannelFactory.changeLocation("/chatrooms");
        } else {
            $rootScope.authorize = false;

            if ($location.path() != "/") {
                //    $rootScope.goToAuthorize();
                return;
            }

            ChannelFactory.changeLocation("/dialog_view/" + mess_obj.nextWindow);
            // toaster.pop('note', "Wait for teacher connect", "...thank", { 'position-class': 'toast-top-full-width' });
            //  $rootScope.showToasterWaitFreeTenant();
        }
    }

    var initIsUserTenant = function() {
        if (isUserTenantInited == false) {
            $http.post(serverPrefix + "/bot_operations/tenant/did_am_busy_tenant").
            success(function(data, status, headers, config) {

                isTenant = data[0];
                if (data[0])
                    isTenantFree = !data[1];
                isUserTenantInited = true;
            }).
            error(function(data, status, headers, config) {
                alert("did_am_wait_tenant: server error")
            });
        }
    };

    var setChatUserId = function(id) { chatUserId = id; };
    var getChatUserId = function() {
        return chatUserId; };
    var setRealChatUserId = function(id) { realChatUserId = id };

    return {
        setChatUserId: setChatUserId,
        getChatUserId: getChatUserId,
        setRealChatUserId: setRealChatUserId,
        login: login,
        getChatUserNickname: function() {
            return chatUserNickname;
        },
        getChatuserAvatar: function() {
            return chatUserAvatar;
        },
        getTenantsList : function(){
            return tenants;
        },
        isUserOnline : isUserOnline,
        setOnlineUsersIds : setOnlineUsersIds

    };



}]);
