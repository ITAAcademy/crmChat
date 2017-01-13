'use strict';
/* Controllers */
springChatControllers.config(function($routeProvider) {
    $routeProvider.when("/builder", {
        templateUrl: "builderTemplateJSTemp.html",
        controller: "ChatBotViewBuilderController"
    });
    $routeProvider.when("/builderForm", {
        templateUrl: "builderTemplateJSTemp.html",
        controller: "ChatBotFormBuilderController"
    });
    $routeProvider.otherwise({ redirectTo: '/' });
    console.log("scope test");

});

var chatControllerScope;

springChatControllers.controller('AccessDeny', ['$locationProvider', '$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore',  function($locationProvider, $routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, $cookieStore) {
    //maybe add button
}]);

var chatController = springChatControllers.controller('ChatController', ['ngDialog', '$q', '$rootScope', '$scope', '$http', '$route', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore', 'RoomsFactory', 'UserFactory', 'ChannelFactory', function(ngDialog, $q, $rootScope, $scope, $http, $route, $location, $interval, $cookies, $timeout, toaster, $cookieStore, RoomsFactory, UserFactory, ChannelFactory) {
    $rootScope.isInited = false;
    $rootScope.baseurl = globalConfig["baseUrl"];
    $rootScope.imagesPath = globalConfig["imagesPath"];
    $scope.baseurl = globalConfig["baseUrl"];
    $rootScope.firstLetter = firstLetter;
    $rootScope.checkIfToday = checkIfToday;
    $rootScope.checkIfYesterday = checkIfYesterday;
    $rootScope.getNameFromUrl = getNameFromUrl;
    $scope.state = 2;
    var loadOnlyFilesInfiniteScrollMode = false;

    $scope.mouseMoveEvent = function(event) {
        if (event.buttons == 1) {

        }
    };
    $scope.dragOptions = {
        start: function(e) {
            console.log("STARTING");
        },
        drag: function(e) {
            console.log("DRAGGING");
        },
        stop: function(e) {
            console.log("STOPPING");
        },
        container: 'body',
        dragElement: 'consultant_wrapper'
    };
    $scope.newMessage = "";

    $scope.getStateClass = function() {
            switch ($scope.state) {
                case 0:
                    $(".consultant_wrapper").removeClass("drag-disable");
                    return "normal";
                    break;
                case 1:
                    $(".consultant_wrapper").removeClass("drag-disable");
                    return "minimize";
                    break;
                case 2:
                    $(".consultant_wrapper").removeAttr("style");
                    $(".consultant_wrapper").addClass("drag-disable");
                    return "fullScreen";
                    break;
                case -1:
                    return "closed";
                    break
            }
        }
        /***************
        ***********STATE
        *
        0 - normal
        1 - mini
        2 - full
        -1 - close

        */


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
    $scope.$on('$routeChangeStart', RoomsFactory.unsubscribeCurrentRoom);


    $scope.isTenantFree = true;
    $scope.isUserTenant = false;
    $scope.isUserTenantInited = false;
    $scope.blocksNames = ['first', 'second'];
    $scope.blocksItems = [
        [new BlockItem("", "Людмила Журавская", true)],
        [new BlockItem("", "Василій Пупкін", true), new BlockItem("", "Микола Петряк", true)]
    ];

    /*FILE FORM INIT*/
    $scope.uploadFiles = function(files) {
            $scope.files = files;
            if (files) {
                uploadXhr(files, "upload_file/" + RoomsFactory.getCurrentRoom().roomId,
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
                        $scope.uploadProgress = Math.floor((event.loaded / event.totalSize) * 100);
                        $scope.$apply();

                    });
            }
            return false;
        }
        /*END*/


    function BlockItem(avatar, name, online) {
        this.avatar = avatar;
        this.name = name;
        this.online = online;
    }
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

    /* $scope.askTenantToTakeConsultationTogle = function() {
         $('#askTenantToTakeConsultation').modal('toggle');
         isAskTenantToTakeConsultationVisible = !isAskTenantToTakeConsultationVisible;
     };
     $scope.askTenantToTakeConsultationHide = function() {
         $('#askTenantToTakeConsultation').modal('hide');
         isAskTenantToTakeConsultationVisible = false;
     };*/

    $scope.showAskWindow = function() {
        if (isAskTenantToTakeConsultationVisible == false) {

            $scope.isTenantFree = false;
            $scope.askTenantToTakeConsultationTogle();

            $scope.hideAskTenantToTakeConsultation_tenantNotRespond =
                $timeout(function() {
                    $scope.hideAskTenantToTakeConsultation();
                }, TIME_FOR_WAITING_ANSWER_FROM_TENANT);
        }
    }

    $scope.hideAskTenantToTakeConsultation = function() {
        if (isAskTenantToTakeConsultationVisible == true) {
            $scope.askTenantToTakeConsultationHide();
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
        $scope.hideAskTenantToTakeConsultation();

        if (value) {
            //alert($rootScope.sendedId + "  " +  $scope.askConsultation_roomId)
            //alert($scope.askConsultation_roomId);
            $http.post(serverPrefix + $scope.askObject.yesLink). //$scope.chatUserId)
            success(function(data, status, headers, config) {}).
            error(function(data, status, headers, config) {
                alert("error : " + status)
            });
        } else {
            $http.post(serverPrefix + $scope.askObject.noLink).
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
            $rootScope.askForDeleteMe = { "room": room, isAuthor: UserFactory.getChatUserId() == room.roomAuthorId }
        ngDialog.open({
            template: 'askForDeleteMe.html',
            scope: $scope
        });
        //$('#askForDeleteMe').modal('toggle');
    }

    $scope.deleteMeFromRoom = function() {
        var showERR = function() { $rootScope.askForDeleteMe.error = "Не вдалося видалити Вас з розмови!!!"; }
        $http.post(serverPrefix + "/chat/rooms/{0}/remove".format($rootScope.askForDeleteMe.room.roomId)).
        success(function(data, status, headers, config) {
            if (data == true)
                ngDialog.closeAll();
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
        // msg = htmlEscape(msg);
        msg = $scope.parseMain(msg, '\\B@\\w+@\\w+[.]\\w+', 'goToUserPage(#)', 1);
        msg = $scope.parseMain(msg, '\\B~["].+["]', 'goToCourseByTitle(#,&quot;ua&quot;)', 2, 3);


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

    var changeLocation = $scope.changeLocation;
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
        return; //@BAG@
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
    $scope.onFriendClick = function(user) {
        if(user.chatUserId != undefined && user.chatUserId != null)
            window.location.replace(serverPrefix +'/chat/go/rooms/private/' + user.chatUserId + '?isChatId=true');
        else
            window.location.replace(serverPrefix +'/chat/go/rooms/private/' + user.id + '?isChatId=false');
    }

    $rootScope.getUserSearchIndetify = function(item, searchInputValue) {
        if (item == undefined || item == '')
            return '';
        var name = ''; // = item.firstName + ' ' + item.secondName;
        if (item.firstName != null)
            name += item.firstName + ' ';
        if (item.secondName != null)
            name += item.secondName;

        if ((searchInputValue != null && item.email != null && item.email.indexOf(searchInputValue) != -1) || name == null || name == ' ')
            return item.email;
        else
            return name;
    }


    $scope.showSearchList = function(ignore) {

        $scope.show_search_list = true;
        $scope.emails = [];
        $timeout.cancel(getEmailsTimer);
        var url;
        if (ignore == true) {
            url = serverPrefix + "/get_users_like?login=" + $scope.searchInputValue.email;
        } else {
            url = serverPrefix + "/get_users_like?login=" + $scope.searchInputValue.email + "&room=" + RoomsFactory.getCurrentRoom().roomId + "&eliminate_users_of_current_room=true"; //'//get_users_like',
        }

        getEmailsTimer = $timeout(function() {
            $scope.show_search_list = true;
            // 
            var request = $http({
                method: "get",
                url: url,
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
        $rootScope.isMyRoom = true;
    }

    $scope.changeUser = function(chatUserId, chatUserNickName) {

        $scope.emails = [];
        $scope.chatUserId = chatUserId;
        $scope.chatUserNickname = chatUserNickName;
        $scope.searchResultAdmin = "";
        var temp_role = $scope.chatUserRole

        if ($rootScope.socketSupport)
            ChannelFactory.initForWS(true);
        else
            ChannelFactory.reInitForLP();

        $timeout(function() {
            $scope.chatUserRole = temp_role;
        }, 2000);


        $rootScope.isMyRoom = false;
    }


    $scope.searchUserName = "";
    $scope.chatUserId = -1;
    $scope.realChatUserId = -1;
    $scope.chatUserRole = 0;
    $scope.chatUserNickname = "";
    $scope.sendTo = 'everyone';
    $scope.dialogShow = false;
    $scope.roomAdded = true;
    $scope.showDialogListButton = false;
    $scope.searchResultAdmin;
    $rootScope.isMyRoom = true;
    $scope.messageSended = true;
    $scope.userAddedToRoom = true;
    $rootScope.isConectedWithFreeTenant = false;


    function addTenantToList(tenantObj) {
        for (var i = 0; i < $scope.tenants.length; i++) {
            if (tenantObj != null && $scope.tenants[i] != null && tenantObj.id == $scope.tenants[i].id) return; //tenant is already excist in list
        }
        $scope.tenants.push(tenantObj);
    }

    function removeTenantFromList(tenantObj) {
        for (var i = 0; i < $scope.tenants.length; i++) {
            if (tenantObj != null && $scope.tenants[i] != null && tenantObj.id == $scope.tenants[i].id) $scope.tenants.splice(i, 1); //tenant is already excist in list
        }
    }

    /*************************************
     * UPDATE ROOM LP
     **************************************/

    var isWaiFreeTenatn = false;

    $rootScope.showToasterWaitFreeTenant = function() {
        if (isWaiFreeTenatn) {
            toaster.pop({
                type: 'wait',
                body: 'Wait for free consultant',
                timeout: 0,
                onHideCallback: function() {
                    if (!$rootScope.isConectedWithFreeTenant && !$rootScope.authorize) {
                        isWaiFreeTenatn = false;
                        $rootScope.showToasterWaitFreeTenant();
                    }
                },
                showCloseButton: false
            });
            isWaiFreeTenatn = true;
        }
    }

    function updateStudents() {

    }

    function updateTenants() {

    }
    $rootScope.message_busy = false;
    $rootScope.loadOtherMessages = function() {
        if ($rootScope.message_busy || RoomsFactory.getCurrentRoom() == null)
            return;
        $rootScope.message_busy = true;
        console.log("TRY " + $scope.messages.length);
        var payload = { 'date': RoomsFactory.getOldMessage().date };
        if ($scope.messageSearchEnabled)
            payload['searchQuery'] = $scope.messageSearchQuery;
        $http.post(serverPrefix + "/{0}/chat/loadOtherMessage".format(RoomsFactory.getCurrentRoom().roomId), payload). //  messages[0]). //
        success(function(data, status, headers, config) {
            console.log("MESSAGE onLOAD OK " + data);

            var objDiv = document.getElementById("messagesScroll");
            var lastHeight = objDiv.scrollHeight;

            if (data == "")
                return;



            for (var index = 0; index < data.length; index++) {
                if (data[index].hasOwnProperty("message")) {
                    RoomsFactory.calcPositionUnshift(data[index]);
                }
            }
            //restore scrole
            $scope.$$postDigest(function() {
                var objDiv = document.getElementById("messagesScroll");
                objDiv.scrollTop = objDiv.scrollHeight - lastHeight;
                $rootScope.message_busy = false;
                $scope.$apply();
            });
        }).
        error(function(data, status, headers, config) {
            console.log('TEST');
            if (status == "404" || status == "405") chatControllerScope.changeLocation("/chatrooms");
            //messageError("no other message");
        });
    }


    $rootScope.$on('MessageAreaScrollDownEvent', function() {
        var objDiv = document.getElementById("messagesScroll");
        objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
    });
    $rootScope.$on('MessageBusyEvent', function(event, isBusy) {
        $rootScope.message_busy = isBusy;
    });

    function newMessageArrayEventHandler(event, roomIds) {
        debugger;
        for (var roomIndex = 0; roomIndex < RoomsFactory.getRooms().length; roomIndex++) {
            var room = RoomsFactory.getRooms()[roomIndex];

            //$.inArray(value, array)
            var newMessageInThisRoom = ($.inArray(room.roomId, roomIds));
            if (newMessageInThisRoom != -1) {
                $rootScope.roomForUpdate[room.roomId] = true;
                if (RoomsFactory.getCurrentRoom() == undefined || RoomsFactory.getCurrentRoom().roomId != room.roomId) {
                    room.nums++;
                    // console.log("room " + room.roomId + "==" + roomId + " currentRoom=" + $scope.currentRoom.roomId);
                    room.date = curentDateInJavaFromat();
                    if ($scope.soundEnable)
                        new Audio('data/new_mess.mp3').play();
                    toaster.pop('note', "NewMessage in " + room.string, "", 2000);
                    break; // stop loop
                }
            }
        }
        var updateDialogListTimeout = null;
        //Update if user see rooms
        if (RoomsFactory.getCurrentRoom() != undefined)
            if (RoomsFactory.getCurrentRoom().roomId == "" && $rootScope.roomForUpdate.keys().next() != undefined) {
                if (updateDialogListTimeout != null)
                    updateDialogListTimeout.cancel();

                updateDialogListTimeout = $timeout(function() {
                    $http.post(serverPrefix + "/chat/update/dialog_list", { "roomForUpdate": $rootScope.roomForUpdate });
                    $rootScope.roomForUpdate = new Map();
                    updateDialogListTimeout = null;
                }, 2500);
            }
    }

    function newMessageEventHandler(event, message) {
        var messageArr = [];
        messageArr.push(message);
        newMessageArrayEventHandler(event, messageArr);
    }

    $rootScope.$on('newMessageEvent', newMessageEventHandler);
    $rootScope.$on('newMessageArray', newMessageArrayEventHandler);


    $scope.getKeys = function(obj) {
        var keys = Object.keys(obj);
        for (var i = 0; i < keys.length; i++) {
            var value = undefined;
            if (obj[keys[i]] != undefined)
                value = obj[keys[i]];

            console.log(i + ") " + keys[i] + " = " + value);
        }
    }
    $scope.getLength = function(obj) {
        return Object.keys(obj).length;
    }
    $scope.roomsRequiredTrainers = new Map();
    $scope.$watch('roomsRequiredTrainers', function(value) {
        $scope.roomsRequiredTrainersLength = $scope.getLength($scope.roomsRequiredTrainers);
    }, true);

    $scope.confirmToHelp = function(roomId) {
        $http.post(serverPrefix + "/bot_operations/triner/confirmToHelp/" + roomId, {}).
        success(function(data, status, headers, config) {
            changeLocation("/dialog_view/" + roomId);
        }).
        error(function(data, status, headers, config) {

        });
    }
    var messageSended = true;

    function messageError() {
        toaster.pop('error', "Error", "server request timeout", 0);
    }
    $rootScope.messageError = messageError;

    $scope.sendMessage = function(message, attaches) {
        if (!messageSended)
            return;
        var textOfMessage;
        if (typeof message === "undefined") textOfMessage = $scope.newMessage;
        else textOfMessage = message;
        if (typeof textOfMessage === "undefined" || textOfMessage.length < 1) {
            $scope.newMessage = '';
            //$("#newMessageInput")[0].value  = '';
            return;
        }
        var destination = "/app/{0}/chat.message".format(RoomsFactory.getCurrentRoom().roomId);
        messageSended = false;
        if (attaches == null)
            attaches = [];

        var msgObj = { message: textOfMessage, username: UserFactory.getChatUserNickname(), attachedFiles: attaches, chatUserAvatar: UserFactory.getChatuserAvatar() };
        if (ChannelFactory.socketSupport == true) {

            chatSocket.send(destination, {}, JSON.stringify(msgObj));
            var myFunc = function() {
                if (angular.isDefined(sendingMessage)) {
                    $timeout.cancel(sendingMessage);
                    sendingMessage = undefined;
                }
                if (messageSended) return;
                messageError();
                messageSended = true;

            };
            sendingMessage = $timeout(myFunc, 2000);
        } else {

            $http.post(serverPrefix + "/{0}/chat/message".format(RoomsFactory.getCurrentRoom().roomId), msgObj).
            success(function(data, status, headers, config) {
                console.log("MESSAGE SEND OK " + data);
                messageSended = true;
            }).
            error(function(data, status, headers, config) {
                messageError();
                messageSended = true;
            });
        };
        if (message === undefined)
            $scope.newMessage = '';

    }



    $rootScope.submitConsultation_processUser = function(roomId) {
        if (roomId == RoomsFactory.getCurrentRoom().roomId) {

            chatControllerScope.userAddedToRoom = true;
            $rootScope.isConectedWithFreeTenant = true;

            toaster.clear();
            $rootScope.isConectedWithFreeTenant = false;
        }

    };

    $rootScope.submitConsultation_processTenant = function(tenantId, roomId) {
        if (tenantId == $scope.chatUserId) {
            $scope.hideAskTenantToTakeConsultation();
            $timeout(function() {;
                chatControllerScope.changeLocation('/dialog_view/' + roomId);
            }, 100);
        }
    };
    $scope.messageSearchEnabled = false;
    $scope.enableMessagesSearch = function() {
        debugger;
        $scope.messageSearchEnabled = true;
        RoomsFactory.clearMessages();
        RoomsFactory.loadMessagesContains($scope.messageSearchQuery);
        $rootScope.message_busy = false;
        var objDiv = document.getElementById("messagesScroll");
        objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
    }
    $scope.disableMessagesSearch = function(reloadMessages) {
        if(reloadMessages===true) {
            RoomsFactory.clearMessages();
            RoomsFactory.loadMessagesContains('');
            $timeout(function() {
                $rootScope.message_busy = false;
            }, 500);

        }
        $scope.messageSearchEnabled = false;
        var objDiv = document.getElementById("messagesScroll");
        if (objDiv!=null)
        objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
    }
    $scope.currentRoomIsNull = function() {
        return RoomsFactory.getCurrentRoom().roomId == null;
    }
    $scope.$on('$routeChangeStart', $scope.disableMessagesSearch(false));
    var savedDistanceToBottom;
    var savedPaddingHeight; 
    function saveScrollBottom(){
        var messagesScroll = $('#messagesScroll');
        var messagesScrollTop = messagesScroll.scrollTop();
        savedPaddingHeight = (messagesScroll.outerHeight() - messagesScroll.height());
         savedDistanceToBottom = (typeof messagesScroll === "undefined" ||
        typeof messagesScroll[0] === "undefined" ) ? undefined : messagesScroll.outerHeight()
        - messagesScroll[0].scrollHeight +
         messagesScroll.scrollTop();
    }
    function getScrollTopToPreserveScroll(futureHeight){
        var messagesScroll = $('#messagesScroll');
        var currentHeight = $('#messagesScroll').height();
        var outerHeight = savedPaddingHeight + futureHeight;
        var heightDelta = currentHeight - futureHeight;
        if (heightDelta<0) return messagesScroll.scrollTop();
        var scrollHeight = messagesScroll[0].scrollHeight + heightDelta - savedPaddingHeight;
        console.log('heightDelta:'+heightDelta);
        console.log('savedPaddingHeight:'+savedPaddingHeight);
        console.log('scrollHeight:'+scrollHeight);
        if (typeof savedDistanceToBottom === "undefined") return;
        var messagesScrollOuterHeight = outerHeight;//$('#messagesScroll').outerHeight();
        var messagesScrollElementScrollHeight = scrollHeight;// $('#messagesScroll')[0].scrollHeight;
        var scrollTop =  savedDistanceToBottom - messagesScrollOuterHeight +
            messagesScrollElementScrollHeight;
       // $('#messagesScroll').scrollTop(scrollTop);
        return scrollTop
        //$('#messagesScroll').animate({scrollTop: ""+scrollTop+"px"}, 1000);
    }

    function messageAreaResizer(e) {
      //  $('#messagesScroll').stop();
        var containerHeight = $('.right_panel').height();
        var toolsAreaHeight = $('.tools_area').height();
        var messagesInputHeight = $('.message_input').height();
        var messagesOutputHeight = containerHeight - toolsAreaHeight - messagesInputHeight - 100;
        if (messagesInputHeight > messagesOutputHeight) return;
        saveScrollBottom();
        //$('#messagesScroll').height(messagesOutputHeight);

        $('#messagesScroll').stop(true).animate({height: messagesOutputHeight,
            scrollTop:getScrollTopToPreserveScroll(messagesOutputHeight)}, 1000);
    }


    $scope.tools_dropdown_click = function() {
        $('#tools_dropdown').toggleClass('shown');
    }
    $scope.attaches_dropdown_click = function() {
        $('#attaches_dropdown').toggleClass('shown');
    }
    $timeout(function() {
        messageAreaResizer();
    }, 1000);

    $scope.$$postDigest(function() {
        $(document).ready(function() {
            $(".message_input").resize(messageAreaResizer);
            $(window).resize(messageAreaResizer);
        });

        /*****************************
         ************CONFIG************
         *****************************/
        //get sound
        $scope.soundEnable = localStorage.getItem('soundEnable') == 'true';
        if ($scope.soundEnable == undefined) {
            localStorage.setItem('soundEnable', true);
            $scope.soundEnable = true;
        }
        $scope.togleSoundEnable = function() {
            $scope.soundEnable = !$scope.soundEnable;
            localStorage.setItem('soundEnable', $scope.soundEnable);
        }
        $scope.showAttaches = function(){
            var messagesWithFiles = RoomsFactory.getMessagesWithFiles();
            loadOtherMessagesWithFiles();
            loadOnlyFilesInfiniteScrollMode = true;
        }
        function loadOtherMessagesWithFiles(){
                // /chat/room/{roomId}/get_messages_contains
                if (currentRoom == null){
                    console.warn('loadMessagesContains failed duing to current room is not selected');
                    return;
                }
                $http.post(serverPrefix + "/chat/room/{0}/loadOtherMessageWithFiles".format(currentRoom.roomId), searchQuery).
                success(function(data, status, headers, config) {
                    loadMessagesFromArrayList(data);
                }).
                error(function(data, status, headers, config) {

                });
        }


        /*****************************
         ************CONFIG************
         *****************************/
        /*var lang = globalConfig.lang;
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
        });*/

    });
}]);
