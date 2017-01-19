springChatControllers.controller('DialogsRouteController', ['$q', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', function($q, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes) {
    Scopes.store('DialogsRouteController', $scope);
    var chatControllerScope = Scopes.get('ChatController');

    /*
     * 
     */
    //$location.path("http://localhost/IntITA/site/authorize");//not found => go out
    $scope.dialogName = '';

    $scope.showNewRoomModal = false;
    /*$scope.toggleNewRoomModal = function() {
        $('#new_room_modal').modal('toggle');
    }*/
    $scope.addRoomButtonKeyPress = function(event) {
        if (event.keyCode == 13) {
            $scope.addDialog();
            // $scope.toggleNewRoomModal();
        }
    }

    $scope.calcImage = function(count, index) {
        var size = 80;
        var border = 2;

        var k = 'right';
        var clip = "rect(auto, auto, auto,  auto)";
        if (index % 2) {
            k = 'left';
            clip = "rect(auto, auto, auto, " + size / 4 + "px)";
        }

        size -= border * 2;
        var obj = { 'position': 'absolute', 'width': size, 'height': size };
        if (count == 1)
            return obj;
        if (count == 2) {
            obj["clip"] = clip;
            obj[k] = "calc(50% - " + size / 4 + "px)";

            return obj;
        }
        return { 'width': size / 2, 'height': size / 2 };


    }
    $scope.updateFriends = function() {
        $http({
            method: 'POST',
            url: serverPrefix + "/chat/user/friends"
        }).
        then(function(data, status, headers, config) {
            $scope.friends = data.data;
        }, function(data, status, headers, config) {
            toaster.pop('error', "Error", "server request timeout", 1000);
        });
    }
    $scope.onFriendClick = function(user) {
        if (user.chatUserId != undefined && user.chatUserId != null)
            window.location.replace(serverPrefix + '/chat/go/rooms/private/' + user.chatUserId + '?isChatId=true');
        else
            window.location.replace(serverPrefix + '/chat/go/rooms/private/' + user.id + '?isChatId=false');
    }

    $scope.onRoomItemClick = function(e, roomId) {
        var isLink = e.target.id == "roomItemLink";
        if (!isLink) { //skip redirection to chatroom from block onclick event
            //if click occured in <a> element
            doGoToRoom(roomId);
        }
    }

    function doGoToRoom(roomId) {
        //
        if ($scope.mouseBusy == false)
            chatControllerScope.changeLocation('/dialog_view/' + roomId);
    }



    $scope.addDialog = function() {
        $scope.toggleNewRoomModal();
        if ($rootScope.socketSupport) {
            chatControllerScope.roomAdded = false;
            chatSocket.send("/app/chat/rooms/add." + "{0}".format($scope.dialogName), {}, JSON.stringify({}));

            var myFunc = function() {
                if (angular.isDefined(addingRoom)) {
                    $timeout.cancel(addingRoom);
                    addingRoom = undefined;
                }
                if ($scope.roomAdded)
                    return;
                toaster.pop('error', "Error", "server request timeout", 1000);
                $scope.roomAdded = true;
                console.log("!!!!!!!!!!!!!!!!!!!!!Room added");

            };
            addingRoom = $timeout(myFunc, 6000);
        }
        //SOcket don't support case
        else {
            $http.post(serverPrefix + "/chat/rooms/add", $scope.dialogName).
            success(function(data, status, headers, config) {
                console.log("ADD USER OK " + data);
                chatControllerScope.userAddedToRoom = true;
            }).
            error(function(data, status, headers, config) {
                chatControllerScope.userAddedToRoom = true;
                toaster.pop('error', "Error", "server request timeout", 1000);
            });
        }
        $scope.dialogName = '';
    };


    $scope.goToDialogList = function() {

        if (chatControllerScope.currentRoom !== undefined && getRoomById(chatControllerScope.rooms, chatControllerScope.currentRoom.date) !== undefined)
            getRoomById(chatControllerScope.rooms, chatControllerScope.currentRoom.roomId).date = curentDateInJavaFromat();

        //$scope.templateName = 'dialogsTemplate.html';
        //changeLocation("/chatrooms")
        $scope.dialogName = '';
        if ($rootScope.roomForUpdate == undefined)
            $rootScope.roomForUpdate = new Map();
        if (chatControllerScope.currentRoom !== undefined && chatControllerScope.currentRoom != null) {
            if ($rootScope.socketSupport) {
                chatSocket.send("/app/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId), {}, JSON.stringify({ "roomForUpdate": $rootScope.roomForUpdate }));
            } else {
                $http.post(serverPrefix + "/chat.go.to.dialog.list/{0}".format(chatControllerScope.currentRoom.roomId), { "roomForUpdate": $rootScope.roomForUpdate });
            }
        }
        $rootScope.roomForUpdate = new Map(); //clear list
        chatControllerScope.currentRoom = { roomId: '' };

        $rootScope.initIsUserTenant();
    }
    $scope.mouseBusy = false;

    $('.multiple-select-wrapper').bind('click', function(e) {
        $scope.mouseBusy = true;
        e.stopPropagation();
        $('.multiple-select-wrapper .list').slideUp();
        $(e.currentTarget).find('div.list').toggle('10');
    });

    $('.multiple-select-wrapper .list').bind('click', function(e) {
        e.stopPropagation();
    });

    $(document).bind('click', function() {
        $scope.mouseBusy = false;
        $('.multiple-select-wrapper .list').slideUp();
    });

    /* need rename */
    $scope.Airlines = [
        { selected: true, name: filters['anonim'], img: 'https://cdn0.iconfinder.com/data/icons/users-android-l-lollipop-icon-pack/24/user-128.png' },
        { selected: true, name: filters['private'], img: 'http://megaicons.net/static/img/icons_title/40/110/title/lock-icon.png' },
        { selected: true, name: filters['consultation'], img: 'https://cdn4.iconfinder.com/data/icons/staff-management-soft/512/family_users_forum_consultation_friends_people_group_social_connection-128.png' }
    ];

    $scope.getSelectedItemsOnly = function(item) {
        return item.selected;
    };

    $scope.filterDialogsByType = function(value) {
        if ($scope.Airlines[value.type].selected)
            return true;
        else
            return false;
    };




    /*
     * 
     */
    if ($rootScope.isInited == true)
        $scope.goToDialogList();
    console.log("initing:" + chatControllerScope.socketSupport);
    $scope.pageClass = 'scale-fade';
    $scope.$$postDigest(function() {
        //   var nice = $(".chat-box").niceScroll();

    })

}]);
