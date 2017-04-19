springChatServices.factory('UserFactory', ['$routeParams', '$timeout', '$rootScope', '$location', '$http', 'toaster', '$injector', 'ChannelFactory', 'ChatSocket', 'AskWindow', function($routeParams, $timeout, $rootScope, $location, $http, toaster, $injector, ChannelFactory, chatSocket, AskWindow) {
    $rootScope.authorize = false;
    var isTenant = false;
    var isTrainer = false;
    var isStudent = false;
    var isAdmin = false;
    var studentTrainerList = [];
    var chatUserNickname, chatUserRole, chatUserAvatar;
    var realChatUserId;
    var chatUserId = null;
    var isUserTenantInited = false;
    var chatUserRole;

    var tenants = [];
    var friends;
    var onlineUsersIds = [];
    var messageSended = true;

    var roomsRequiredTrainers = new Map();

    var getRoomsRequiredTrainers = function() {
        return roomsRequiredTrainers;
    }
    var getRoomsRequiredTrainersLength = function() {
        return roomsRequiredTrainers == null ? 0 : Object.keys(roomsRequiredTrainers).length;
    }
    var isMessageSended = function() {
        return messageSended;
    }
    var setMessageSended = function(val) {
        messageSended = val;
    }

    var isTenantFree = true;
    var setTenantFree = function() {
        isTenantFree = true;
    }
    var setTenantBusy = function() {
        isTenantFree = false;
    }
    var getTenantIsFree = function() {
        return isTenantFree;
    }

    var checkRole = function() {
        if (chatUserRole & 256)
            return true;
        return false;
    }
    var isUserOnline = function(chatUserId) {
        return onlineUsersIds.indexOf(chatUserId) == -1 ? false : true;
    }
    var setOnlineUsersIds = function(ids) {
        onlineUsersIds = ids;
    }

    var socketSupport = true;

    function initSocketsSubscribes() {
        chatSocket.subscribe("/topic/chat.tenants.remove", function(message) {
            var tenant = JSON.parse(message.body);
            for (var i = 0; i < tenants.length; i++) {
                if (tenant.chatUserId == tenants[i].chatUserId) {
                    tenants.splice(i, 1);
                    break;
                }
            }
            //updateTenants(o);
        });
        /*   subscribeBindings.push(chatSocket.subscribe("/app/chat.tenants", function(message) {
         var o = JSON.parse(message.body);
         updateTenants(o);
         }));*/
        chatSocket.subscribe("/topic/chat.tenants.add", function(message) {
            var tenant = JSON.parse(message.body);
            var alreadyExcist = false;
            for (var i = 0; i < tenants.length; i++) {
                if (tenant.chatUserId == tenants[i].chatUserId || getChatUserId() == tenant.chatUserId) {
                    alreadyExcist = true;
                    break;
                }
            }
            if (!alreadyExcist && tenant.chatUserId != getChatUserId()) tenants.push(tenant);
            // updateTenants(o);
        });
    }

    var initStompClient = function() {

        function reInitForLP() {
            $http.post(serverPrefix + "/chat/login/" + getChatUserId(), { message: 'true' }).
            success(function(data, status, headers, config) {
                login(data);
            }).
            error(function(data, status, headers, config) {
                $rootScope.messageError();
                toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            });
        }
        var onConnect = function(frame) {
            if (frame.headers['user-name'] == undefined)
                location.reload();
            setChatUserId(frame.headers['user-name']);
            initForWS(false);
            setRealChatUserId(getChatUserId());
        };

        ChannelFactory.subscribeToConnect(function(socketSupport, frame) {
            if (socketSupport) {
                onConnect(frame);
            } else {
                $http.post(serverPrefix + "/chat/login/-1", { message: 'true' }).
                success(function(data, status, headers, config) {
                    var RoomsFactory = $injector.get('RoomsFactory');
                    login(data);
                    RoomsFactory.subscribeRoomsUpdateLP();

                    subscribeInfoUpdateLP();
                    setRealChatUserId(getChatUserId());
                }).
                error(function(data, status, headers, config) {
                    $rootScope.messageError();
                    toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
                });
            }

        })
    };

    function initForWS(reInit) {
        chatSocket.subscribe("/app/chat.login/{0}".format(getChatUserId()), function(message) {
            var mess_obj = JSON.parse(message.body);

            login(mess_obj);

            if (reInit == false)
                $timeout(function() {
                    chatSocket.subscribe("/topic/chat.login", function(message) {

                        var chatUserId = JSON.parse(message.body).chatUserId;
                        $rootScope.$broadcast("login", chatUserId);
                        onlineUsersIds.push(chatUserId);
                    });
                    chatSocket.subscribe("/topic/chat.logout", function(message) {
                        var chatUserId = JSON.parse(message.body).username;
                        $rootScope.$broadcast("logout", chatUserId);
                        var index = onlineUsersIds.indexOf(chatUserId);
                        onlineUsersIds.splice(index, 1);

                    });
                    //TODO make channel private
                    chatSocket.subscribe("/topic/{0}/must/get.room.num/chat.message".format(getChatUserId()), function(message) { // event update
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
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", "room wasn't added");
                                break;
                            case Operations.add_room_on_login:
                                $timeout(function() {
                                    ChannelFactory.changeLocation("dialog_view/" + operationStatus.description);
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
                        //AskWindow.setLinks(body.yesLink, body.noLink);
                        AskWindow.setAskObject(body);
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
                        var waitingUsers = JSON.parse(message.body);
                        //TODO add room where waiting users
                        notifyAboutUserDemandingRoom(waitingUsers);

                    });
                    chatSocket.subscribe("/topic/chat/room.private/room_require_trainer.remove", function(message) {
                        var idToRemove = JSON.parse(message.body);
                        //TODO removing trainers from room
                        removeNotificationAboutUserDemandingRoom(idToRemove);

                    });

                    if (isTrainer)
                        initSocketsSubscribes();


                    chatSocket.subscribe("/topic/users/info", function(message) {
                        var RoomsFactory = $injector.get('RoomsFactory');
                        var operationStatus = JSON.parse(message.body);
                        //operationStatus = JSON.parse(operationStatus);
                        if (operationStatus["updateRoom"] != undefined && operationStatus["updateRoom"].roomId == RoomsFactory.currentRoom.roomId) {
                            RoomsFactory.currentRoom = operationStatus["updateRoom"];
                        }
                        if (operationStatus["type"] == "roomRead") {
                            $rootScope.$broadcast('roomRead', operationStatus);
                        }
                        //
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
                        toaster.pop('error', "Error", message.body);
                    });
                }, 1500);

        });

        chatSocket.subscribe("/topic/chat/rooms/user.{0}".format(getChatUserId()), function(message) { // event update
            var RoomsFactory = $injector.get('RoomsFactory');
            RoomsFactory.updateRooms(message);
        });
    }

    function updateTenants(tenants) {
        this.tenants = tenants;
        var itemsToRemove = [];
        if (tenants != null) {
            for (var i = 0; i < tenants.length; i++) {
                if (getChatUserId() == tenants[i].chatUserId) {
                    itemsToRemove.push(i);
                    continue;
                }
            }
            for (var k = itemsToRemove.length - 1; k >= 0; k--)
                tenants.splice(itemsToRemove[k], 1);
        }
    }

    function subscribeInfoUpdateLP() {
        // alert("subscribeInfoUpdateLP()");
        $http.post(serverPrefix + "/chat/global/lp/info")
            .success(function(data, status, headers, config) {
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
                    //                    AskWindow.setLinks(data["newAsk_ToChatUserId"][0].yesLink, data["newAsk_ToChatUserId"][0].noLink);
                    AskWindow.setAskObject(data["newAsk_ToChatUserId"][0]);
                    AskWindow.showAskWindow();
                }
                if (data["newConsultationWithTenant"] != null) {
                    var roomId = data["newConsultationWithTenant"][0][0];

                    $rootScope.submitConsultation_processUser(roomId);

                    var sendedConsultantId = data["newConsultationWithTenant"][0][1];

                    $rootScope.submitConsultation_processTenant(sendedConsultantId, roomId);
                }
                if (data["roomRead"] != null) {
                    $rootScope.$broadcast('roomRead', data["roomRead"]);
                }
                if (isTrainer) {
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

    function addTenantToList(tenantObj) {
        for (var i = 0; i < tenants.length; i++) {
            if (tenantObj != null && tenants[i] != null && tenantObj.id == tenants[i].id) return; //tenant is already excist in list
        }
        tenants.push(tenantObj);
    }

    function removeTenantFromList(tenantObj) {
        for (var i = 0; i < tenants.length; i++) {
            if (tenantObj != null && tenants[i] != null && tenantObj.id == tenants[i].id) tenants.splice(i, 1); //tenant is already excist in list
        }
    }


    function login(mess_obj) {
        $('body').addClass('loaded');
        var RoomsFactory = $injector.get('RoomsFactory');
        chatUserId = mess_obj.chatUser.id;
        isTenant = mess_obj.chatUser.roles.indexOf("TENANT") != -1;
        isTrainer =mess_obj.chatUser.roles.indexOf("TRAINER") != -1;
        isStudent = mess_obj.chatUser.roles.indexOf("STUDENT") != -1;
        isAdmin = mess_obj.chatUser.roles.indexOf("ADMIN") != -1;
        if (isStudent && mess_obj.trainer != undefined) {

            studentTrainerList.push(mess_obj.trainer);
        }

        chatUserNickname = mess_obj.chatUser.nickName;
        chatUserRole = mess_obj.chatUser.role;
        chatUserAvatar = mess_obj.chatUser.avatar;

        var onlineUserIds = mess_obj.activeUsers;
        setOnlineUsersIds(onlineUserIds);

        if (mess_obj.nextWindow == -1) {
            toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            return;
        }
        if (ChannelFactory.isSocketSupport() == false) {
            RoomsFactory.setRooms(mess_obj.roomModels);
        } else {
            initIsUserTenant();
            var rooms = mess_obj.roomModels;
            RoomsFactory.setRooms(rooms);
            if ($routeParams.roomId == null && rooms.length > 0) {
                if (isStudent && mess_obj.trainer != undefined) {
                    RoomsFactory.goToPrivateDialog(studentTrainerList[0].intitaUserId);
                } else {
                    rooms.sort(function(obj1, obj2) {
                        return new Date(obj1.date) - new Date(obj2.date);
                    })
                    ChannelFactory.changeLocation("/dialog_view/" + rooms[rooms.length - 1].roomId);
                }
            }
        }
        tenants = typeof mess_obj["tenants"] == "undefined" ? [] : mess_obj["tenants"];
        friends = typeof mess_obj["friends"] == "undefined" ? [] : mess_obj["friends"];

        ChannelFactory.setIsInited(true);


        if (mess_obj.nextWindow == 0) {

            // if ($scope.currentRoom.roomId != undefined)
            /* if ($scope.currentRoom != undefined)
                 if ($scope.currentRoom.roomId != undefined && $scope.currentRoom.roomId != '' && $scope.currentRoom.roomId != -1) {
                     //mess_obj.nextWindow=$scope.currentRoom.roomId;
                     //  goToDialogEvn($scope.currentRoom.roomId);
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
        rescrollToRoom($routeParams.roomId);
    }
    var confirmToHelp = function(roomId) {
        $http.post(serverPrefix + "/bot_operations/triner/confirmToHelp/" + roomId, {}).
        success(function(data, status, headers, config) {
            ChannelFactory.changeLocation("/dialog_view/" + roomId);
        }).
        error(function(data, status, headers, config) {

        });
    }

    var notifications = [];
    var getNotifications = function() {
        return notifications;
    }

    function removeNotificationByValue(value) {
        var index = notifications.indexOf(value);
        if (index != -1)
            notifications.splice(index, 1);
    }
    var notifyAboutUserDemandingRoom = function(demandingUser) {
        var currentType = 'user_wait_tenant';
        var generateAvatarSrc = function(avatar) {
            return $rootScope.imagesPath + '/avatars/' + avatar
        }
        var notificationObject = {
                'type': currentType,
                'avatar': generateAvatarSrc(demandingUser.avatar),
                'title': demandingUser.name,
                'details': demandingUser.lastMessage,
                'chatUserId': demandingUser.chatUserId,
                'roomId': demandingUser.roomId
            }
            //check if user or room already present in white list
        for (var i = 0; i < notifications.length; i++) {
            if (notifications[i].type != currentType) continue;
            if (notifications[i].chatUserId === demandingUser.chatUserId ||
                notifications[i].roomId === demandingUser.roomId) return;
        }

        notifications.push(notificationObject);
    }

    var removeNotificationAboutUserDemandingRoom = function(chatUserId) {
        var currentType = 'user_wait_tenant';
        var notificationIndexToRemove = null;
        for (var i = 0; i < notifications.length; i++) {
            if (notifications[i].type != currentType) continue;
            if (notifications[i].chatUserId === chatUserId) {
                notificationIndexToRemove = i;
                break;
            }
        }
        if (notificationIndexToRemove != null) notifications.split(notificationIndexToRemove, 1);
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
        return chatUserId;
    };
    var setRealChatUserId = function(id) { realChatUserId = id };

    var participantsSort = function(participant) {
        if (participant == null) return '';
        var isOnline = isUserOnline(participant.chatUserId);
        return isOnline ? 'a' + participant.username : 'b' + participant.username;
    }

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
        getTenantsList: function() {
            return tenants;
        },
        getStudentTrainerList: function() {
            return studentTrainerList;
        },

        isUserOnline: isUserOnline,
        setOnlineUsersIds: setOnlineUsersIds,
        participantsSort: participantsSort,
        setTenantFree: setTenantFree,
        setTenantBusy: setTenantBusy,
        getTenantIsFree: getTenantIsFree,
        isMessageSended: isMessageSended,
        setMessageSended: setMessageSended,
        getRoomsRequiredTrainers: getRoomsRequiredTrainers,
        getRoomsRequiredTrainersLength: getRoomsRequiredTrainersLength,
        confirmToHelp: confirmToHelp,
        notifyAboutUserDemandingRoom: notifyAboutUserDemandingRoom,
        getNotifications: getNotifications,
        removeNotificationByValue: removeNotificationByValue,
        isAdmin: function() {
            return isAdmin;
        },
        isTenant: function() {
            return isTenant;
        },
        isTrainer: function() {
            return isTrainer;
        },
        getUser: function() {
            return {
                'id': chatUserId,
                'nickName': chatUserNickname,
                'avatar': chatUserAvatar

            }
        }

    };



}]);
