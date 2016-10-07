'use strict';



/* Controllers */
springChatControllers.config(function($routeProvider) {
    $routeProvider.when("/chatrooms", {
        templateUrl: "dialogsTemplate.html",
        controller: "DialogsRouteController"
    });
    $routeProvider.when("/dialog_view/:roomId/", {
        templateUrl: "chatTemplate.html",
        controller: "ChatRouteController"
    });

    $routeProvider.when("/builder", {
        templateUrl: "builderTemplateJSTemp.html",
        controller: "ChatBotViewBuilderController"
    });
    $routeProvider.when("/builderForm", {
        templateUrl: "builderTemplateJSTemp.html",
        controller: "ChatBotFormBuilderController"
    });

    $routeProvider.when("/teachers_list", {
        templateUrl: "teachersTemplate.html",
        controller: "TeachersListRouteController"
    });
    $routeProvider.when("/private_dialog_view/:chatUserId", {
        templateUrl: "redirectPage.html",
        controller: "StrictedDialogRouteController"
    });
    $routeProvider.when("/access_deny", {
        templateUrl: "accessDeny.html",
        controller: "AccessDeny"
    });

    $routeProvider.when("/consultation_view/:consultationId", {
        templateUrl: "consultationTemplate.html",
        controller: "ConsultationController"
    });



    $routeProvider.otherwise({ redirectTo: '/' });
    console.log("scope test");

});

var chatControllerScope;

springChatControllers.controller('TeachersListRouteController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes) {
    Scopes.store('TeachersListRouteController', $scope);
    chatControllerScope = Scopes.get('ChatController');
    //  while (!chatControllerScope.$rootScope.isInited);//chatControllerScope.initStompClient();
    //  typeof chatControllerScope.socketSupport!=='undefined'
    chatControllerScope.goToTeachersList();
    $scope.pageClass = 'page-contact';
}]);


springChatControllers.controller('AccessDeny', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes) {
    Scopes.store('AccessDeny', $scope);
    var chatControllerScope = Scopes.get('ChatController');
    $timeout(function() {
        //history.back(2);
        $scope.changeLocation("/chatrooms");
    }, 2000);
    //maybe add button
}]);

springChatControllers.controller('StrictedDialogRouteController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes) {
    Scopes.store('StrictedDialogRouteController', $scope);
    var chatControllerScope = Scopes.get('ChatController');
    //if($rootScope.isInited == true)
    chatControllerScope.goToPrivateDialog($routeParams.chatUserId);

}]);


var chatController = springChatControllers.controller('ChatController', ['$q', '$rootScope', '$scope', '$http', '$route', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', function($q, $rootScope, $scope, $http, $route, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes) {
    Scopes.store('ChatController', $scope);
    $rootScope.isInited = false;
    $rootScope.baseurl = globalConfig["baseUrl"];
    $rootScope.goToUserPage = function(username) {
        var request = $http({
            method: "get",
            url: serverPrefix + "/get_id_by_username?intitaUsername=" + username,
            data: null,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        request.success(function(data) {
            if (data != null) {
                window.open(generateUrlToUserPage(data), '_blank');
                //window.top.location.href =generateUrlToUserPage(data);
                return true;
            }
        });
        return false;
    }

    $scope.isTenantFree = true;
    $scope.isUserTenant = false;
    $scope.isUserTenantInited = false;


    $scope.clickSetTenantFree = function() {
        $scope.isTenantFree = !$scope.isTenantFree;
        /*console.log("$scope.isTenantFree = " + $scope.isTenantFree)
        return;*/
        if ($scope.isTenantFree) {
            $http.post(serverPrefix + "/bot_operations/tenant/becomeFree"). //$scope.chatUserId)        
            success(function(data, status, headers, config) {

            }).
            error(function(data, status, headers, config) {
                alert("error")
            });
        } else {
            $http.post(serverPrefix + "/bot_operations/tenant/becomeBusy"). //$scope.chatUserId)        
            success(function(data, status, headers, config) {

            }).
            error(function(data, status, headers, config) {
                alert("error")
            });
        }
    }

    var isAskTenantToTakeConsultationVisible = false;

    $scope.needReloadPage = true;

    $scope.askTenantToTakeConsultationTogle = function() {
        $('#askTenantToTakeConsultation').modal('toggle');
        isAskTenantToTakeConsultationVisible = !isAskTenantToTakeConsultationVisible;
    };

    $scope.showAskWindow = function() {
        if (isAskTenantToTakeConsultationVisible == false) {

            $scope.isTenantFree = false;
            $scope.askTenantToTakeConsultationTogle();

            $scope.hideAskTenantToTakeConsultation_tenantNotRespond =
                $timeout(function() {
                    $scope.hideAskTenantToTakeConsultation();
                }, 30000);
        }
    }

    $scope.hideAskTenantToTakeConsultation = function() {
        if (isAskTenantToTakeConsultationVisible == true) {
            $scope.askTenantToTakeConsultationTogle();
        }
    }

    $scope.answerToFinishConsultation = function() {
        $scope.needReloadPage = false;
        $http.post(serverPrefix + "/bot_operations/tenant/becomeFree"). //$scope.chatUserId)        
        success(function(data, status, headers, config) {

        }).
        error(function(data, status, headers, config) {
            alert("error : " + status)
        });
    }

    $scope.answerToTakeConsultation = function(value) {
        $timeout.cancel($scope.hideAskTenantToTakeConsultation_tenantNotRespond);
        debugger;
        if (value) {
            //alert($rootScope.sendedId + "  " +  $scope.askConsultation_roomId)
            //alert($scope.askConsultation_roomId);
            $http.post(serverPrefix + $scope.askObject.yesLink). //$scope.chatUserId)
            success(function(data, status, headers, config) {
            }).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        } else {
            $scope.hideAskTenantToTakeConsultation();
            $http.post(serverPrefix+ $scope.askObject.noLink).
            success(function(data, status, headers, config) {

            }).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        }
    };

    $scope.toggleAskForDeleteMe = function(event, room) {
        if (event != undefined && event != null)
            event.stopPropagation();
        if (room != undefined && room != null)
            $rootScope.askForDeleteMe = { "room": room, isAuthor: $scope.chatUserId == room.roomAuthorId }
        $('#askForDeleteMe').modal('toggle');
    }

    $scope.deleteMeFromRoom = function() {
        var showERR = function() { $rootScope.askForDeleteMe.error = "Не вдалося видалити Вас з розмови!!!"; }
        $http.post(serverPrefix + "/chat/rooms/{0}/remove".format($rootScope.askForDeleteMe.room.roomId)).
        success(function(data, status, headers, config) {
            if (data == true)
                $scope.toggleAskForDeleteMe();
            else
                showERR();
        }).
        error(function(data, status, headers, config) {
            showERR();
        });
    };

    $scope.toggleNewRoomModal = function() {
        $('#new_room_modal').modal('toggle');

        $timeout.cancel($scope.setFocusToRoomNameInput); //888
        isCreateDialogWndVisible = !isCreateDialogWndVisible;

        $scope.setFocusToRoomNameInput = $timeout(function() {
            if (isCreateDialogWndVisible == true) {
                $('#roomNameInput').focus();
            }
        }, 300);
    };

    var isCreateDialogWndVisible = false;

    $scope.addDialog = function() {};
    $rootScope.formatDateWithLast = formatDateWithLast;
    $rootScope.formatDate = formatDate;

    function generateUrlToUserPage(user_id) {
        var baseurl = globalConfig["baseUrl"];
        return baseurl + "/profile/" + user_id + "/";
    }

    function generateUrlToCourse(course_alias, lang) {
        var baseurl = globalConfig["baseUrl"];
        return baseurl + "/course/" + lang + "/" + course_alias + "/";
    }
    $rootScope.goToCourseByTitle = function(title, lang) {
        var request = $http({
            method: "get",
            url: serverPrefix + "/get_course_alias_by_title?title=" + encodeURIComponent(title) + "&lang=" + encodeURIComponent(lang),
            data: null,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        request.success(function(data) {
            if (data != null) {
                window.open(generateUrlToCourse(data, lang), '_blank');
                //window.top.location.href =generateUrlToCourse(data,lang);
                return true;
            }
        });

    }
    $rootScope.parseMsg = function(msg) {
        if (msg == null) return null;
        msg = $scope.parseMain(msg, '\\B@\\w+@\\w+[.]\\w+', 'goToUserPage(#)', 1);
        msg = $scope.parseMain(msg, '\\B~["].+["]', 'goToCourseByTitle(#,&quot;ua&quot;)', 2, 3);

        //msg = msg.HTMLEncode();
        return msg; //.replace(' ','&#32;');
    }
    $rootScope.parseMain = function(msg, reg, callback, trimLeft, trimRight) {
        if (msg == null) return null;
        trimLeft = trimLeft || 0;
        trimRight = trimRight || 0;
        var expr = new RegExp(reg, 'g');
        msg = msg.replace(expr, function(str) {
            return ' <a ng-click="' + callback.replace(new RegExp('#', 'g'), "'" + str.substr(trimLeft, str.length - trimRight).escapeHtml() + "'") + '">' + str.substr(trimLeft, str.length - trimRight) + "</a>";
        })

        //$interval($route.reload(), 200);
        return msg;
    }


    $scope.changeLocation = function changeLocation(url) {
        //alert(url);
        toaster.clear();
        $location.path(url);
        console.log("Change location:" + $location.path());
        // $scope.$apply();
    };

    function changeLocation(url) {
        $location.path(url);
        console.log("Change location:" + $location.path());
        // $scope.$apply();
    };
    //$scope.templateName = null;
    $rootScope.socketSupport = true;
    $rootScope.authorize = false;


    $scope.disableAddDlgButton = false;

    $rootScope.$watch('authorize', function() {
        $scope.disableAddDlgButton = !$rootScope.authorize;
    });

    $rootScope.roomForUpdate = new Map();

    $rootScope.redirectzToAuthorizePage = function() {
        window.top.location.href = globalConfig["baseUrl"] + '/site/authorize';
    }
    $rootScope.goToAuthorize = function(func) {
        if ($rootScope.authorize || $rootScope.isInited == false) {
            if (func == null || func == undefined) {
                $location.path("/access_deny");
            } else
                func();
        } else
            $rootScope.redirectzToAuthorizePage();
        //window.top.location.href = 'http://localhost/IntITA/site/authorize';
    }


    var addingUserToRoom = undefined;
    var sendingMessage = undefined;
    var addingRoom = undefined;
    var changeLastRoomCanceler = $q.defer();

    var room = "default_room/";

    var room = "1/";

    //  Format string
    if (!String.prototype.format) {
        String.prototype.format = function() {
            var args = arguments;
            return this.replace(/{(\d+)}/g, function(match, number) {
                return typeof args[number] != 'undefined' ? args[number] : match;
            });
        };
    }

    $scope.emails = [];
    $scope.show_search_list_admin = false;

    var getEmailsTimer;

    $scope.searchInputValue = { email: "" };

    $scope.hideSearchList = function() {
        $scope.show_search_list = false;
        $scope.show_search_list_admin = false;
    }

    $scope.showSearchList = function() {

        $scope.show_search_list = true;
        $scope.emails = [];
        $timeout.cancel(getEmailsTimer);

        getEmailsTimer = $timeout(function() {
            $scope.show_search_list = true;
            // debugger;
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_users_like?login=" + $scope.searchInputValue.email + "&room=" + $scope.currentRoom.roomId + "&eliminate_users_of_current_room=true", //'//get_users_like',
                data: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            request.success(function(data) {
                $scope.emails = data;
                //$scope.$apply();
            });
        }, 500); //for click event
    };

    $scope.appendToSearchInput = function(value) {
        console.log("searchInputValue:" + $scope.searchInputValue.email);
        $scope.searchInputValue.email = value;
        $scope.show_search_list = false;
    }


    $scope.showSearchListAdmin = function() {

        $scope.emails = [];
        $timeout.cancel(getEmailsTimer);

        getEmailsTimer = $timeout(function() {
            var request = $http({
                method: "get",
                url: serverPrefix + "/get_users_nicknames_like_without_room?nickName=" + $scope.searchResultAdmin,
                data: null,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });

            request.success(function(data) {
                $scope.show_search_list_admin = true;
                $scope.emails = data;
            });
        }, 500); //for click event
    };

    $scope.returnToRealUser = function() {
        $scope.changeUser($scope.realChatUserId, $scope.realChatUserId);
        $scope.isMyRoom = true;
    }

    $scope.changeUser = function(chatUserId, chatUserNickName) {

        $scope.emails = [];

        $scope.chatUserId = chatUserId;
        $scope.chatUserNickname = chatUserNickName;
        $scope.searchResultAdmin = "";
        var temp_role = $scope.chatUserRole

        if ($rootScope.socketSupport)
            initForWS(true);
        else
            reInitForLP();

        $timeout(function() {
            $scope.chatUserRole = temp_role;
        }, 2000);


        $scope.isMyRoom = false;
    }


    $scope.searchUserName = "";
    $scope.chatUserId = -1;
    $scope.realChatUserId = -1;
    $scope.chatUserRole = 0;
    $scope.chatUserNickname = "";
    $scope.sendTo = 'everyone';
    $scope.dialogs = [];
    $scope.rooms = [];
    $scope.roomsCount = 0;
    $scope.currentRoom = { roomId: '' };
    $scope.dialogShow = false;
    $scope.roomAdded = true;
    $scope.showDialogListButton = false;
    $scope.searchResultAdmin;
    $scope.isMyRoom = true;
    $scope.messageSended = true;
    $scope.userAddedToRoom = true;
    $rootScope.isConectedWithFreeTenant = false;

    $scope.goToTeachersList = function() {

        $http.post(serverPrefix + "/chat/users").

        success(function(data, status, headers, config) {
            console.log("USERS GET OK ");
            $scope.seachersTeachers = data;
        }).
        error(function(data, status, headers, config) {
            $scope.seachersTeachers = [];
        });
    }
    var goToPrivateDialogErr = function(data, status, headers, config) {
        toaster.pop('error', "PRIVATE ROOM CREATE FAILD", "", 3000);
        console.log("PRIVATE ROOM CREATE FAILD ");
        $rootScope.goToAuthorize(function() { changeLocation("/chatrooms"); });
    }
    $scope.goToPrivateDialog = function(intitaUserId) {
        $http.post(serverPrefix + "/chat/rooms/private/" + intitaUserId).
        success(function(data, status, headers, config) {
            console.log("PRIVATE ROOM CREATE OK ");

            //$scope.goToDialogById(data);
            if ($scope.currentRoom == null)
                $scope.currentRoom = {};
            if (data != null && data != undefined) {
                //    $scope.currentRoom.roomId = data;
                changeLocation("/dialog_view/" + data);
            } else {
                goToPrivateDialogErr();
            }


        }).
        error(goToPrivateDialogErr);
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
                    $rootScope.sendedId = data["newAsk_ToChatUserId"][0][0];
                    if ($rootScope.sendedId == $scope.chatUserId) {
                        $scope.askObject = data["newAsk_ToChatUserId"];
                        $scope.showAskWindow();
                    }
                }
                if (data["newConsultationWithTenant"] != null) {
                    var roomId = data["newConsultationWithTenant"][0][0];

                    $rootScope.submitConsultation_processUser(roomId);

                    var sendedConsultantId = data["newConsultationWithTenant"][0][1];

                    $rootScope.submitConsultation_processTenant(sendedConsultantId);
                }
                /* if (data["updateRoom"] != null && data["updateRoom"][0]["updateRoom"].roomId == $scope.currentRoom.roomId) {
                     debugger;
                     $scope.currentRoom = data["updateRoom"][0]["updateRoom"];;
                 }*/
                subscribeInfoUpdateLP();
            }).error(function errorHandler(data, status, headers, config) {
                subscribeInfoUpdateLP();
            });

    }

    /*************************************
     * UPDATE ROOM LP
     **************************************/
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

    $scope.privateSending = function(username) {
        $scope.sendTo = (username != $scope.sendTo) ? username : 'everyone';
    };

    $scope.checkRole = function() {
        if ($scope.chatUserRole & 256)
            return true;
        return false;
    }

    $rootScope.initIsUserTenant = function() {
        if ($scope.isUserTenantInited == false) {

            $http.post(serverPrefix + "/bot_operations/tenant/did_am_busy_tenant").
            success(function(data, status, headers, config) {
                debugger;
                $scope.isUserTenant = data[0];
                if (data[0])
                    $scope.isTenantFree = !data[1];
                $scope.isUserTenantInited = true;
            }).
            error(function(data, status, headers, config) {
                alert("did_am_wait_tenant: server error")
            });
        }
    };
    $rootScope.isWaiFreeTenatn = false;

    $rootScope.showToasterWaitFreeTenant = function() {
        if (!$rootScope.isWaiFreeTenatn) {
            toaster.pop({
                type: 'wait',
                body: 'Wait for free consultant',
                timeout: 0,
                onHideCallback: function() {
                    if (!$rootScope.isConectedWithFreeTenant && !$rootScope.authorize) {
                        $rootScope.isWaiFreeTenatn = false;
                        $rootScope.showToasterWaitFreeTenant();
                    }
                },
                showCloseButton: false
            });
            $rootScope.isWaiFreeTenatn = true;
        }
    }

    function login(mess_obj) {
        $scope.chatUserId = mess_obj.chat_id;
        $scope.chatUserNickname = mess_obj.chat_user_nickname;
        $scope.chatUserRole = mess_obj.chat_user_role;

        if (mess_obj.nextWindow == -1) {
            toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            return;
        }

        if ($rootScope.socketSupport == false) {
            updateRooms(JSON.parse(mess_obj.chat_rooms));
        } else {
            $rootScope.initIsUserTenant();
            $scope.rooms = JSON.parse(mess_obj.chat_rooms).list;
            $scope.roomsCount = $scope.rooms.length;
        }

        $rootScope.isInited = true;

        if (mess_obj.nextWindow == 0) {
            $rootScope.authorize = true;
            // if ($scope.currentRoom.roomId != undefined)
            if ($scope.currentRoom != undefined)
                if ($scope.currentRoom.roomId != undefined && $scope.currentRoom.roomId != '' && $scope.currentRoom.roomId != -1) {
                    //mess_obj.nextWindow=$scope.currentRoom.roomId;
                    //  goToDialogEvn($scope.currentRoom.roomId);
                    console.log("currentRoom");
                    changeLocation("/dialog_view/" + $scope.currentRoom.roomId);
                    $scope.showDialogListButton = true;
                    return;
                }
            $scope.showDialogListButton = true;

            if ($location.path() == "/")
                changeLocation("/chatrooms");
        } else {
            $rootScope.authorize = false;

            if ($location.path() != "/") {
                //    $rootScope.goToAuthorize();
                return;
            }

            changeLocation("/dialog_view/" + mess_obj.nextWindow);
            // toaster.pop('note', "Wait for teacher connect", "...thank", { 'position-class': 'toast-top-full-width' });
            //  $rootScope.showToasterWaitFreeTenant();
        }
    }

    function updateRooms(message) {
        var parseObj;

        if ($rootScope.socketSupport) {
            parseObj = JSON.parse(message.body);
        } else {
            parseObj = message;
        }
        var needReplace = parseObj.replace;
        var roomList = parseObj.list;
        if (needReplace) {
            $scope.rooms = roomList;
        } else {
            for (var i = 0; i < $scope.rooms.length; i++) {
                for (var j = roomList.length - 1; j >= 0; j--) {
                    if (roomList[j].roomId == $scope.rooms[i].roomId) {
                        $scope.rooms[i] = roomList[j];
                        roomList.splice(j, 1);
                    }
                }
            }
        }
        $scope.roomsCount = $scope.rooms.length;
        if (typeof $scope.currentRoom != 'undefined') {
            $scope.currentRoom = getRoomById($scope.rooms, $scope.currentRoom.roomId);
        }
    }


    function newMessageEvent(roomId) {
        for (var roomIndex = 0; roomIndex < $scope.rooms.length; roomIndex++) {
            var room = $scope.rooms[roomIndex];


            if (room.roomId == roomId) {
                $rootScope.roomForUpdate[room.roomId] = true;
                if ($scope.currentRoom == undefined || $scope.currentRoom.roomId != room.roomId) {
                    room.nums++;
                    // console.log("room " + room.roomId + "==" + roomId + " currentRoom=" + $scope.currentRoom.roomId);
                    room.date = curentDateInJavaFromat();
                    new Audio('data/new_mess.mp3').play();
                    toaster.pop('note', "NewMessage in " + room.string, "", 2000);
                    break; // stop loop
                }
            }
        }
        var updateDialogListTimeout = null;
        //Update if user see rooms
        if ($scope.currentRoom != undefined)
            if ($scope.currentRoom.roomId == "" && $rootScope.roomForUpdate.keys().next() != undefined) {
                if (updateDialogListTimeout != null)
                    updateDialogListTimeout.cancel();

                updateDialogListTimeout = $timeout(function() {
                    $http.post(serverPrefix + "/chat/update/dialog_list", { "roomForUpdate": $rootScope.roomForUpdate });
                    $rootScope.roomForUpdate = new Map();
                    updateDialogListTimeout = null;
                }, 2500);
            }
    }

    $scope.getKeys = function(obj) {
        var keys = Object.keys(obj);
        for (var i = 0; i < keys.length; i++) {
            var value = undefined;
            if (obj[keys[i]] != undefined)
                value = obj[keys[i]];

            console.log(i + ") " + keys[i] + " = " + value);
        }
    }

    function initForWS(reInit) {
        chatSocket.subscribe("/app/chat.login/{0}".format($scope.chatUserId), function(message) {
            var mess_obj = JSON.parse(message.body);

            login(mess_obj);

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

                    chatSocket.subscribe("/topic/users/{0}/status".format($scope.chatUserId), function(message) {
                        var operationStatus = JSON.parse(message.body);
                        switch (operationStatus.type) {
                            case Operations.send_message_to_all:
                            case Operations.send_message_to_user:
                                $scope.messageSended = true;
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", message.body);
                                break;
                            case Operations.add_user_to_room:
                                $scope.userAddedToRoom = true;
                                if (!operationStatus.success)
                                    toaster.pop("error", "Error", "user wasn't added to room");
                                break;
                            case Operations.add_room:
                                $scope.roomAdded = true;
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
                                // debugger;
                                //$scope.currentRoom = 
                                break;

                        }
                        //                      ZIGZAG OPS

                        console.log("SERVER MESSAGE OPERATION STATUS:" + operationStatus.success + operationStatus.description);
                    });

                    chatSocket.subscribe("/topic/users/{0}/info".format($scope.chatUserId), function(message) {

                        var body = JSON.parse(message.body);
                        $scope.askObject = body;
                        $scope.showAskWindow();
                    });

                    chatSocket.subscribe("/topic/users/{0}/submitConsultation".format($scope.chatUserId), function(message) {
                        var body = JSON.parse(message.body);
                        $rootScope.sendedRoomId = body[0];

                        $rootScope.submitConsultation_processUser($rootScope.sendedRoomId);

                        var sendedConsultantId = body[1];
                        $rootScope.submitConsultation_processTenant(sendedConsultantId);
                        // alert("sendedId = " + $rootScope.sendedId + "\n  sendedConsultantId = " + sendedConsultantId)

                    });




                    chatSocket.subscribe("/topic/users/info", function(message) {
                        var operationStatus = JSON.parse(message.body);
                        //operationStatus = JSON.parse(operationStatus);
                        if (operationStatus["updateRoom"].roomId == $scope.currentRoom.roomId) {
                            $scope.currentRoom = operationStatus["updateRoom"];
                        }
                        //debugger;
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/errors", function(message) {
                        toaster.pop('error', "Error", message.body);
                    });
                }, 1500);

        });

        /*
         * 
         *Room
         *
         */
        console.log("chatUserId:" + $scope.chatUserId);
        /*chatSocket.subscribe("/app/chat/rooms/user.{0}".format($scope.chatUserId), function(message) { // event update
            console.log("chatUserId:" + $scope.chatUserId);
            updateRooms(message);
        });*/
        chatSocket.subscribe("/topic/chat/rooms/user.{0}".format($scope.chatUserId), function(message) { // event update
            console.log("chatUserId:" + $scope.chatUserId);
            updateRooms(message);
        });
    }

    $rootScope.submitConsultation_processUser = function(roomId) {
        if (roomId == $rootScope.currentRoomId) {
            if (chatControllerScope == undefined)
                chatControllerScope = Scopes.get('ChatController');
            chatControllerScope.userAddedToRoom = true;
            $rootScope.isConectedWithFreeTenant = true;

            toaster.clear();
            $rootScope.isConectedWithFreeTenant = false;
        }

    };

    $rootScope.submitConsultation_processTenant = function(tenantId) {
        if (tenantId == $scope.chatUserId) {
            $scope.hideAskTenantToTakeConsultation();

            if (chatControllerScope == undefined)
                chatControllerScope = Scopes.get('ChatController');

            $timeout(function() {
                toaster.clear();
                chatControllerScope.changeLocation('/dialog_view/' + $scope.askConsultation_roomId);
            }, 1000);
            //chatControllerScope.changeLocation('/dialog_view/' + $scope.askConsultation_roomId);
        }

    };

    function reInitForLP() {
        $http.post(serverPrefix + "/chat/login/" + $scope.chatUserId, { message: $scope.newMessage }).
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
        $scope.chatUserId = frame.headers['user-name'];
        initForWS(false);
        $scope.realChatUserId = $scope.chatUserId;


    };

    var initStompClient = function() {

        console.log("serverPrefix");

        chatSocket.init(serverPrefix + "/wss"); //9999


        chatSocket.connect(onConnect, function(error) {
            /***************************************
             * TRY LONG POLING LOGIN
             **************************************/
            //$scope.chatUserId = frame.headers['user-name'];          
            if ($rootScope.isInited == false) {
                $rootScope.socketSupport = false;

                $http.post(serverPrefix + "/chat/login/" + $scope.chatUserId, { message: $scope.newMessage }).
                success(function(data, status, headers, config) {

                    console.log("LOGIN OK " + data);
                    login(data);
                    subscribeRoomsUpdateLP();

                    subscribeInfoUpdateLP();
                    $scope.realChatUserId = $scope.chatUserId;
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
        if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
            xmlhttp = new XMLHttpRequest();
        }
        return xmlhttp;
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
    initStompClient();

}]);



springChatControllers.factory('Scopes', function($rootScope) {
    var mem = {};

    return {
        store: function(key, value) {
            mem[key] = value;
        },
        get: function(key) {
            return mem[key];
        }
    };
});
