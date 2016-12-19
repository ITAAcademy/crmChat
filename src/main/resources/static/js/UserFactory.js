springChatServices.factory('UserFactory', ['$http','toaster','$injector', function($http,toaster,$injector) {

var isTenant = false;
var isTrainer = false;
var isStudent = false;
var chatUserNickname,chatUserRole,chatUserAvatar;
var realChatUserId,chatUserId;
var isUserTenantInited=false;
var chatUserRole;
var isInited = false;
var authorize;

    var checkRole = function() {
        if (chatUserRole & 256)
            return true;
        return false;
    }

    function login(mess_obj) {
    	var ChannelFactory = $injector.get('ChannelFactory');
    	var RoomsFactory = $injector.get('RoomsFactory');
        chatUserId = mess_obj.chat_id;
        isTenant = Boolean(mess_obj.isTenant);
        isTrainer = Boolean(mess_obj.isTrainer);
        isStudent = Boolean(mess_obj.isStudent);

        console.log("isTenant:"+isTenant+" isTrainer:"+isTrainer + " isStudent:"+isStudent);
        chatUserNickname = mess_obj.chat_user_nickname;
        chatUserRole = mess_obj.chat_user_role;
        chatUserAvatar = mess_obj.chat_user_avatar

        if (mess_obj.nextWindow == -1) {
            toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            return;
        }

        if (ChannelFactory.isSocketSupport() == false) {
            RoomsFactory.updateRooms(JSON.parse(mess_obj.chat_rooms));
        } else {
           initIsUserTenant();
           RoomsFactory.setRooms(JSON.parse(mess_obj.chat_rooms).list);
        }
        $scope.tenants = typeof mess_obj["tenants"] == "undefined" ? undefined : JSON.parse(mess_obj["tenants"]);
        $scope.friends = typeof mess_obj["friends"] == "undefined" ? undefined : JSON.parse(mess_obj["friends"]);
        isInited = true;

        if (mess_obj.nextWindow == 0) {
            $rootScope.authorize = true;
            // if ($scope.currentRoom.roomId != undefined)
            /* if ($scope.currentRoom != undefined)
                 if ($scope.currentRoom.roomId != undefined && $scope.currentRoom.roomId != '' && $scope.currentRoom.roomId != -1) {
                     //mess_obj.nextWindow=$scope.currentRoom.roomId;
                     //  goToDialogEvn($scope.currentRoom.roomId);
                     console.log("currentRoom");
                     changeLocation("/dialog_view/" + $scope.currentRoom.roomId);
                     $scope.showDialogListButton = true;
                     return;
                 }*/
            $scope.showDialogListButton = true;

            if ($location.path() == "/")
                changeLocation("/chatrooms");
        } else {
            authorize = false;

            if ($location.path() != "/") {
                //    $rootScope.goToAuthorize();
                return;
            }

            changeLocation("/dialog_view/" + mess_obj.nextWindow);
            // toaster.pop('note', "Wait for teacher connect", "...thank", { 'position-class': 'toast-top-full-width' });
            //  $rootScope.showToasterWaitFreeTenant();
        }
    }

    var initIsUserTenant = function() {
        if (isUserTenantInited == false) {

            $http.post(serverPrefix + "/bot_operations/tenant/did_am_busy_tenant").
            success(function(data, status, headers, config) {

                $scope.isUserTenant = data[0];
                if (data[0])
                    $scope.isTenantFree = !data[1];
                isUserTenantInited = true;
            }).
            error(function(data, status, headers, config) {
                alert("did_am_wait_tenant: server error")
            });
        }
    };

    return {
    	setChatUserId: function(id){chatUserId=id;},
    	getChatUserId: function(){return chatUserId;},

    	setRealChatUserId: function(id){realChatUserId = id},

    	login: login
    };



}]);