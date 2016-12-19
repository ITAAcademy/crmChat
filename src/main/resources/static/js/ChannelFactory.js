springChatServices.factory('ChannelFactory', ['$http','RoomsFactory','UserFactory','toaster','ChatSocket', function($http,RoomsFactory,UserFactory,toaster,chatSocket) {
var socketSupport = true;

 var initStompClient = function() {

        console.log("serverPrefix");

        function reInitForLP() {
        $http.post(serverPrefix + "/chat/login/" + UserFactory.getChatUserId(), { message: 'true' }).
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
        UserFactory.setChatUserId(frame.headers['user-name']);
        initForWS(false);
        UserFactory.setRealChatUserId(UserFactory.getChatUserId());


    };


        chatSocket.init(serverPrefix + "/wss"); //9999


        chatSocket.connect(onConnect, function(error) {
            /***************************************
             * TRY LONG POLING LOGIN
             **************************************/       
            if ($rootScope.isInited == false) {
                socketSupport = false;

                $http.post(serverPrefix + "/chat/login/" + UserFactory.getChatUserId(), { message:'true' }).
                success(function(data, status, headers, config) {

                    console.log("LOGIN OK " + data);
                    login(data);
                    subscribeRoomsUpdateLP();

                    subscribeInfoUpdateLP();
                    UserFactory.setRealChatUserId(UserFactory.getChatUserId());
                }).
                error(function(data, status, headers, config) {
                    messageError();
                    toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
                });
            } else {
                toaster.pop('error', 'Error', 'Websocket not supportet or server not exist' + error, 99999);
                changeLocation("/");
            }



        });
    };

 function initForWS(reInit) {
        chatSocket.subscribe("/app/chat.login/{0}".format(UserFactory.getChatUserId()), function(message) {
            var mess_obj = JSON.parse(message.body);

            UserFactory.login(mess_obj);

            if (reInit == false)
                $timeout(function() {
                    chatSocket.subscribe("/topic/chat.login".format(room), function(message) {

                        var chatUserId = JSON.parse(message.body).chatUserId;
                        $rootScope.$broadcast("login", chatUserId);
                    });
                    chatSocket.subscribe("/topic/chat.logout".format(room), function(message) {
                        var chatUserId = JSON.parse(message.body).username;
                        $rootScope.$broadcast("logout", chatUserId);

                    });

                    chatSocket.subscribe("/topic/users/must/get.room.num/chat.message", function(message) { // event update
                        console.log("new message in room:" + message.body);
                        var num = JSON.parse(message.body);
                        newMessageEvent(num);
                    });

                    chatSocket.subscribe("/topic/users/{0}/status".format(UserFactory.getChatUserId()), function(message) {
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

                    chatSocket.subscribe("/topic/users/{0}/info".format(UserFactory.getChatUserId()), function(message) {

                        var body = JSON.parse(message.body);
                        askObject = body;
                        showAskWindow();
                    });

                    chatSocket.subscribe("/topic/users/{0}/submitConsultation".format(UserFactory.getChatUserId()), function(message) {
                        var body = JSON.parse(message.body);
                        $rootScope.sendedRoomId = body[0];

                        $rootScope.submitConsultation_processUser($rootScope.sendedRoomId);

                        var sendedConsultantId = body[1];
                        $rootScope.submitConsultation_processTenant(sendedConsultantId, $rootScope.sendedRoomId);
                        // alert("sendedId = " + $rootScope.sendedId + "\n  sendedConsultantId = " + sendedConsultantId)

                    });

                    chatSocket.subscribe("/app/chat/room.private/room_require_trainer".format(UserFactory.getChatUserId()), function(message) {
                        var body = JSON.parse(message.body);
                        roomsRequiredTrainers = body;
                    });
                    chatSocket.subscribe("/topic/chat/room.private/room_require_trainer.add".format(UserFactory.getChatUserId()), function(message) {
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

        chatSocket.subscribe("/topic/chat/rooms/user.{0}".format(UserFactory.getChatUserId()), function(message) { // event update
            console.log("chatUserId:" + UserFactory.getChatUserId());
            updateRooms(message);
        });
    }

  function subscribeInfoUpdateLP() {
        // alert("subscribeInfoUpdateLP()");
        $http.post(serverPrefix + "/chat/global/lp/info")
            .success(function(data, status, headers, config) {
                console.log("infoUpdateLP data:" + data);
                if (data["newMessage"] != null) //new message in room
                {
                    for (var i in data["newMessage"])
                        newMessageEvent(data["newMessage"][i]);
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
                    $scope.askObject = data["newAsk_ToChatUserId"][0];
                    $scope.showAskWindow();
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

        return{
        isSocketSupport: function(){
        	return socketSupport;
        }
        };

}]);