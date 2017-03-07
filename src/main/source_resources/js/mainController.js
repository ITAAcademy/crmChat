'use strict';
/* Controllers */
springChatControllers.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when("/dialog_view/:roomId/", {
        resolve: {
            load: ['$route', 'RoomsFactory', 'ChannelFactory', '$routeParams','$rootScope', function($route, RoomsFactory, ChannelFactory, $routeParams,$rootScope) {
                if (!ChannelFactory.getIsInited()) return;
                RoomsFactory.goToRoom($route.current.params.roomId);
                 $rootScope.$broadcast('dialog_view_route');
            }]
        }
    });
    $routeProvider.when("/access_deny", {
        templateUrl: "accessDeny.html",
        controller: "AccessDeny"
    });
    $routeProvider.when("/private_dialog_view/:chatUserId", {
        resolve: {
            load: ['$route', 'RoomsFactory', '$routeParams', function($route, RoomsFactory, $routeParams) {
                RoomsFactory.goToPrivateDialog($route.current.params.chatUserId);
            }]
        }
    });

    $routeProvider.otherwise({ redirectTo: '/' });
}]);

var chatControllerScope;

springChatControllers.controller('AccessDeny', ['$locationProvider', '$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore',
    function($locationProvider, $routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, $cookieStore) {
        //maybe add button
    }
]);

var chatController = springChatControllers.controller('ChatController', ['$sce', 'ngDialog', '$q', '$rootScope', '$scope', '$http', '$route', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore', 'RoomsFactory', 'UserFactory', 'ChannelFactory',
    function($sce, ngDialog, $q, $rootScope, $scope, $http, $route, $location, $interval, $cookies, $timeout, toaster, $cookieStore, RoomsFactory, UserFactory, ChannelFactory) {
        //Imports from Services
        //Imports/>

        ChannelFactory.setIsInited(false);
        $rootScope.baseurl = globalConfig["baseUrl"];
        $rootScope.imagesPath = globalConfig["imagesPath"];
        $scope.baseurl = globalConfig["baseUrl"];
        $rootScope.firstLetter = firstLetter;
        $rootScope.checkIfToday = checkIfToday;
        $rootScope.checkIfYesterday = checkIfYesterday;
        $rootScope.isRoomPrivate = RoomsFactory.isRoomPrivate;
        $rootScope.isRoomGroup = RoomsFactory.isRoomGroup;
        $rootScope.needShowDayDivided = function(curr, prev) {
            if (checkIfToday(curr) || checkIfYesterday(curr))
                return true;
            return isSameDay(curr, prev);
        };
        $rootScope.getNameFromUrl = getNameFromUrl;
        $rootScope.getNameFromRandomizedUrl = getNameFromRandomizedUrl;
        $scope.state = 2;
        $scope.loadOnlyFilesInfiniteScrollMode = false;
        $scope.toggleAskForDeleteMeFromCurrentRoom = function() {
            //TODO leave current room
            toggleAskForDeleteMe(RoomsFactory.getCurrentRoom());
        }

        $scope.mouseMoveEvent = function(event) {
            if (event.buttons == 1) {

            }
        };

        $scope.newMessage = {value:""};

        var roomElement = angular.element(document.getElementById("panel-body"));
        $scope.resizeRoomElement = function(oldSize, newSize) {

            roomElement.css('height', 'calc(100% - ' + newSize + 'px)');
        }

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


        $scope.isUserTenant = UserFactory.isTenant;;
        $scope.isUserTenantInited = false;
        $scope.blocksNames = ['first', 'second'];
        $scope.blocksItems = [
            [new BlockItem("", "Людмила Журавская")],
            [new BlockItem("", "Василій Пупкін", true), new BlockItem("", "Микола Петряк")]
        ];

        /*FILE FORM INIT*/

        /*END*/


        function BlockItem(avatar, name) {
            this.avatar = avatar;
            this.name = name;
        }
        $scope.clickSetTenantFree = function() {
            UserFactory.setTenantFree(true);
            $http.post(serverPrefix + "/bot_operations/tenant/becomeFree");
        }
        $scope.clickSetTenantBusy = function() {
            UserFactory.setTenantBusy();
            $http.post(serverPrefix + "/bot_operations/tenant/becomeBusy");
        }
        $scope.answerToFinishConsultation = function() {
            var needReloadPage = false;
            $http.post(serverPrefix + "/bot_operations/tenant/becomeFree");
        };

        $scope.needReloadPage = true;

        var toggleAskForDeleteMe = function(room) {

            // if (event != undefined && event != null)
            //    event.stopPropagation();

            if (room != undefined && room != null)
                $rootScope.askForDeleteMe = { "room": room, isAuthor: UserFactory.getChatUserId() == room.roomAuthorId }
            ngDialog.open({
                template: 'askForDeleteMe.html',
                scope: $scope
            });
            //$('#askForDeleteMe').modal('toggle');
        };

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
            if ($rootScope.authorize || ChannelFactory.getIsInited() == false) {
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
            if (user.chatUserId == undefined || user.chatUserId == null) return null;
            $http.get(serverPrefix + '/chat/get/rooms/private/' + user.chatUserId + '?isChatId=true', {}).
            success(function(data, status, headers, config) {
                ChannelFactory.changeLocation("/dialog_view/" + data);
            }).
            error(function(data, status, headers, config) {

            });

          /*  if (user.chatUserId != undefined && user.chatUserId != null)
                ChannelFactory.changeLocation('/chat/go/rooms/private/' + user.chatUserId + '?isChatId=true');
            else
            ChannelFactory.changeLocation('#/chat/go/rooms/private/' + user.id + '?isChatId=false');*/
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
        // $rootScope.messageSended = true;
        $scope.userAddedToRoom = true;
        $rootScope.isConectedWithFreeTenant = false;


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
            var urlTemplate = "/{0}/chat/loadOtherMessage";
            if ($scope.loadOnlyFilesInfiniteScrollMode) {
                urlTemplate = "/{0}/chat/loadOtherMessageWithFiles";
            }
            $rootScope.message_busy = true;
            var date = (RoomsFactory.getOldMessage() == null) ? null : RoomsFactory.getOldMessage().date;
            var payload = { 'date': date };
            if ($scope.messageSearchEnabled)
                payload['searchQuery'] = $scope.messageSearchQuery.value;
            $http.post(serverPrefix + urlTemplate.format(RoomsFactory.getCurrentRoom().roomId), payload). //  messages[0]). //
            success(function(data, status, headers, config) {

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
                if (status == "404" || status == "405") ChannelFactory.changeLocation("/");
                //messageError("no other message");
            });
        }


        $rootScope.$on('MessageAreaScrollDownEvent', function() {
            var objDiv = document.getElementById("messagesScroll");
            objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
        });

        function newMessageMapEventHandler(event, messagesMap) {
            if (messagesMap == null) return;
            var roomIds = Object.keys(messagesMap);
            
            for (var roomIndex = 0; roomIndex < RoomsFactory.getRooms().length; roomIndex++) {
                var room = RoomsFactory.getRooms()[roomIndex];
                var newMessageInThisRoom = ($.inArray("" + room.roomId, roomIds));
                if (newMessageInThisRoom != -1) {
                    $rootScope.roomForUpdate[room.roomId] = true;
                    room.lastMessage = messagesMap[room.roomId];
                    room.lastMessageDate = (new Date()).getTime();
                    if (RoomsFactory.getCurrentRoom() == undefined || RoomsFactory.getCurrentRoom().roomId != room.roomId) {
                        room.nums++;
                        RoomsFactory.updateNewMsgNumber(1);
                        //room.date = curentDateInJavaFromat();

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

        $rootScope.$on('newMessageEvent', newMessageMapEventHandler);


        $scope.getKeys = function(obj) {
            var keys = Object.keys(obj);
            for (var i = 0; i < keys.length; i++) {
                var value = undefined;
                if (obj[keys[i]] != undefined)
                    value = obj[keys[i]];
            }
        }
        $scope.getLength = function(obj) {
            return Object.keys(obj).length;
        }

        function messageError() {
            toaster.pop('error', "Error", "server request timeout", 0);
        }
        $rootScope.messageError = messageError;

        $rootScope.submitConsultation_processUser = function(roomId) {
            if (roomId == RoomsFactory.getCurrentRoom().roomId) {
                $rootScope.isConectedWithFreeTenant = true;

                toaster.clear();
                $rootScope.isConectedWithFreeTenant = false;
            }

        };

        $rootScope.submitConsultation_processTenant = function(tenantId, roomId) {
            if (tenantId == UserFactory.getChatUserId()) {
                $timeout(function() {;
                    ChannelFactory.changeLocation('/dialog_view/' + roomId);
                }, 100);
            }
        };
        $scope.messageSearchEnabled = false;
        $scope.enableMessagesSearch = function(event) {
            
            event.stopPropagation();
            event.preventDefault();
            $scope.messageSearchQuery = { 'value': '' };
            $scope.messageSearchEnabled = true;
            setTimeout(function() { $("#searchInput").focus(); }, 500);


        }
        $scope.showMenu = false;
        $scope.toggleMenu = function() {
            $scope.showMenu = !$scope.showMenu;
            return $scope.showMenu;
        }
        $rootScope.hideMenu = function() {
            $scope.showMenu = false;
            
            $rootScope.__modaleToggle['menu'].restart();
            return $scope.showMenu;
        }

        $scope.getNewMsgNumber = RoomsFactory.getNewMsgNumber;

        /* $interval(function(){
             if($scope.getNewMsgNumber() > 0)
             {
                 $("message_notification").addClass("animation-play-state: running;");
             }
             else
             {
                 $("message_notification").removeClass("animation-play-state: running;");
             }
         }, 1)*/
        var updateMessagesSearchTimeout;
        $scope.messagesSearching = false;
        $scope.updateMessagesSearch = function() {
            if (updateMessagesSearchTimeout != undefined)
                $timeout.cancel(updateMessagesSearchTimeout);
              $scope.messagesSearching = true;
            updateMessagesSearchTimeout = $timeout(function() {
                RoomsFactory.clearMessages();
                var deffered = RoomsFactory.loadMessagesContains($scope.messageSearchQuery.value);
                deffered.finally(function(){
                     $scope.messagesSearching = false;
                });
                $rootScope.message_busy = false;
                var objDiv = document.getElementById("messagesScroll");
                objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
            }, 500);
        }
        $scope.focusMessagesSearchChange = function() {
            if ($scope.messageSearchQuery.value == '')
                $scope.disableMessagesSearch(false);

        }
        $rootScope.$on('RoomChanged', function(event, isBusy) {
            $scope.disableMessagesSearch(false);
            $scope.disableRoomNameChangeMode();
        });
        $scope.disableMessagesSearch = function(reloadMessages) {
            if (reloadMessages === true) {
                RoomsFactory.clearMessages();
                RoomsFactory.loadMessagesContains('');
                $timeout(function() {
                    $rootScope.message_busy = false;
                }, 500);

            }
            $scope.messageSearchEnabled = false;
            var objDiv = document.getElementById("messagesScroll");
            if (objDiv != null)
                objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
        }
        $scope.isMessageInputAvailable = RoomsFactory.checkMessageAdditionPermission;
        $scope.currentRoomIsNull = function() {
            if (RoomsFactory.getCurrentRoom() == null) return true;
            return RoomsFactory.getCurrentRoom().roomId == null;
        }
        $scope.getCurrentRoom = RoomsFactory.getCurrentRoom;
        $scope.isRoomChangeModeEnabled = false;
        $scope.newRoomName = {'value':''};

        $scope.toggleRoomNameChangeMode = function(){
            if (!$scope.isRoomChangeModeEnabled){
                $scope.newRoomName.value = $scope.getCurrentRoom().string;
                setTimeout(function() { $("#changeRoomNameInput").focus(); }, 500);
            }else{
                RoomsFactory.changeCurrentRoomName($scope.newRoomName.value);         
            }
            $scope.isRoomChangeModeEnabled = !$scope.isRoomChangeModeEnabled;
        }
        $scope.disableRoomNameChangeMode = function(){
        $scope.isRoomChangeModeEnabled = false;
        }
         $scope.focusNewRoomNameChange = function(){
            if ($scope.newRoomName.value == $scope.getCurrentRoom().string)
             $scope.disableRoomNameChangeMode();
         }

        $scope.$on('$routeChangeStart', $scope.disableMessagesSearch(false));
        var savedDistanceToBottom;
        var savedPaddingHeight;

        /**
         * Store distance between bottom of container and bottom of scroll
         */
        function saveScrollBottom() {
            var messagesScroll = $('#messagesScroll');
            var messagesScrollTop = messagesScroll.scrollTop();
            savedDistanceToBottom = (typeof messagesScroll === "undefined" ||
                    typeof messagesScroll[0] === "undefined") ? 0 : messagesScroll[0].scrollHeight -
                messagesScroll.scrollTop() - messagesScroll[0].clientHeight
        }

        function getScrollTopToPreserveScroll(futureHeight) {
            var messagesScroll = $('#messagesScroll');
            var currentHeight = messagesScroll[0].clientHeight;
            var heightDelta = currentHeight - futureHeight;
            if (heightDelta < 0) return messagesScroll.scrollTop();
            var scrollHeight = messagesScroll[0].scrollHeight + heightDelta;
            if (typeof savedDistanceToBottom === "undefined") return;
            var scrollTop = scrollHeight - savedDistanceToBottom -
                messagesScroll[0].clientHeight;
            // $('#messagesScroll').scrollTop(scrollTop);
            return scrollTop
                //$('#messagesScroll').animate({scrollTop: ""+scrollTop+"px"}, 1000);
        }

        var delay = null;

        function messageAreaResizer(e) {
            //  $('#messagesScroll').stop();
            if (delay != null)
                $timeout.cancel(delay);
            delay = $timeout(function() {
                var containerHeight = $('.right_panel').height();
                var toolsAreaHeight = $('.tools_area').height();
                var messagesInputHeight = $('.messages_input_area').height();
                var messagesOutputHeight = containerHeight - toolsAreaHeight - messagesInputHeight - 10; //pading
                /*if (messagesInputHeight > messagesOutputHeight)
                    return;*/
                saveScrollBottom();
                //$('#messagesScroll').height(messagesOutputHeight);
                //$('#messagesScroll').scrollTop(getScrollTopToPreserveScroll(messagesOutputHeight));

                $('#messagesScroll').stop(true).animate({
                    height: messagesOutputHeight,
                    scrollTop: getScrollTopToPreserveScroll(messagesOutputHeight)
                }, 1000);
            }, 200);
        }

        $rootScope.stripHtml = function(html) {
            var tmp = document.createElement("DIV");
            tmp.innerHTML = html;
            return tmp.textContent || tmp.innerText || "";
        }
    

        $scope.tools_dropdown_click = function() {
            $('#tools_dropdown').toggleClass('shown');
        }
        $scope.attaches_dropdown_click = function() {
            $('#attaches_dropdown').toggleClass('shown');
        }
        $scope.help_dropdown_click = function() {
            $('#help_dropdown').toggleClass('shown');
        }

        messageAreaResizer();

        $timeout(function() {
            //DOM has finished rendering
            $(".messages_input_area").resize(messageAreaResizer);
            $(window).resize(messageAreaResizer);
        });

        $scope.$$postDigest(function() {
            /*****************************
             ************CONFIG************
             *****************************/
            //get sound
            if (localStorage.getItem('soundEnable') == undefined) {
                localStorage.setItem('soundEnable', true);
                $scope.soundEnable = true;
            }
            $scope.soundEnable = localStorage.getItem('soundEnable') == 'true';

            $scope.togleSoundEnable = function() {
                $scope.soundEnable = !$scope.soundEnable;
                localStorage.setItem('soundEnable', $scope.soundEnable);
            }
            $scope.showAttaches = function() {
                RoomsFactory.clearMessages();
                $scope.loadOnlyFilesInfiniteScrollMode = true;
                $rootScope.loadOtherMessages();
                $rootScope.message_busy = false;
                var objDiv = document.getElementById("messagesScroll");
                objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
            }
            $scope.clearHistory = function(){
                RoomsFactory.clearHistory();
                 RoomsFactory.clearMessages();
                }

            $scope.showAllMessages = function(reloadMessages) {
                if (reloadMessages === true) {
                    RoomsFactory.clearMessages();
                    RoomsFactory.loadMessagesContains('');
                    $timeout(function() {
                        $rootScope.message_busy = false;
                    }, 500);

                }
                $scope.loadOnlyFilesInfiniteScrollMode = false;
                var objDiv = document.getElementById("messagesScroll");
                if (objDiv != null)
                    objDiv.scrollTop = 99999999999 //objDiv.scrollHeight;
            }

            $scope.canLeaveCurrentRoom = function() {
                if (RoomsFactory.getCurrentRoom() == null || RoomsFactory.getCurrentRoom().type == 1)
                    return false;
                return true;
            }
            $scope.getTenantIsFree = UserFactory.getTenantIsFree;

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
    }
]);
