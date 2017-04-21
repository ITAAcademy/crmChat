/**
 * @author nicolas
 */
(function() {
    'use strict';

    angular.module('Intita.monitor.servises', [])
        .service('UserMonitorService', userMonitor);

    /** @ngInject */
    function userMonitor($http) {
        var _isTenant = false;
        var _isTrainer = false;
        var isStudent = false;
        var _isAdmin = false;
        var studentTrainerList = [];
        var chatUserNickname, chatUserRole, chatUserAvatar = null;//= "noname.png";
        var realChatUserId;
        var chatUserId = null;
        var isUserTenantInited = false;
        var chatUserRole;

        var tenants = [];
        var friends;
        var onlineUsersIds = [];
        var messageSended = true;

        this.getChatUserAvatar = function() {
            return chatUserAvatar;
        }

        

        this.getChatUserWithNickNameLike = function(like) {
            return $http.get(serverPrefix + "/get_users_nicknames_like_without_room?nickName=" + like);
        }
        this.getRoomsWithNameLike = function(like) {
            return $http.get(serverPrefix + "/chat/rooms/find?name=" + like);
        }


        function reInitForLP() {
            $http.post(serverPrefix + "/chat/login/-1", { message: 'true' }).success(function(data, status, headers, config) {
                login(data);
            }).error(function(data, status, headers, config) {
                $rootScope.messageError();
                toaster.pop('error', "Authentication err", "...Try later", { 'position-class': 'toast-top-full-width' });
            });
        }

        function login(mess_obj) {

            chatUserId = mess_obj.chatUser.id;
                _isTenant = mess_obj.chatUser.roles.indexOf("TENANT") != -1;
        _isTrainer =mess_obj.chatUser.roles.indexOf("TRAINER") != -1;
        isStudent = mess_obj.chatUser.roles.indexOf("STUDENT") != -1;
        _isAdmin = mess_obj.chatUser.roles.indexOf("ADMIN") != -1;

            if (isStudent && mess_obj.trainer != undefined) {
                studentTrainerList.push(mess_obj.trainer);
            }

        chatUserNickname = mess_obj.chatUser.nickName;
        chatUserRole = mess_obj.chatUser.role;
        chatUserAvatar = mess_obj.chatUser.avatar;

            var onlineUserIds = mess_obj.activeUsers;

            var rooms = mess_obj.roomModels;
            tenants = typeof mess_obj["tenants"] == "undefined" ? [] : mess_obj["tenants"];
            friends = typeof mess_obj["friends"] == "undefined" ? [] : mess_obj["friends"];
        }
        reInitForLP();
        this.findTenants = function(info) {
            return [
                { label: 'Hot Dog, Fries and a Soda', value: 1 },
                { label: 'Burger, Shake and a Smile', value: 2 },
                { label: 'Sugar, Spice and all things nice', value: 3 },
                { label: 'Baby Back Ribs', value: 4 }
            ];
        }
    }
})();
