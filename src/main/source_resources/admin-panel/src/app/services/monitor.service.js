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
        var chatUserNickname, chatUserRole, chatUserAvatar;
        var realChatUserId;
        var chatUserId = null;
        var isUserTenantInited = false;
        var chatUserRole;

        var tenants = [];
        var friends;
        var onlineUsersIds = [];
        var messageSended = true;

        this.getChatUserAvatar = function(){
        	return chatUserAvatar;
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

            chatUserId = mess_obj.chat_id;
            _isTenant = mess_obj.isTenant == 'true';
            _isTrainer = mess_obj.isTrainer == 'true';
            isStudent = mess_obj.isStudent == 'true';
            _isAdmin = mess_obj.isAdmin == 'true';
            if (isStudent && mess_obj.trainer != undefined) {
                studentTrainerList.push(JSON.parse(mess_obj.trainer));
            }

            chatUserNickname = mess_obj.chat_user_nickname;
            chatUserRole = mess_obj.chat_user_role;
            chatUserAvatar = mess_obj.chat_user_avatar

            var onlineUserIds = JSON.parse(mess_obj.onlineUsersIdsJson);

            var rooms = JSON.parse(mess_obj.chat_rooms).list;
            tenants = typeof mess_obj["tenants"] == "undefined" ? [] : JSON.parse(mess_obj["tenants"]);
            friends = typeof mess_obj["friends"] == "undefined" ? [] : JSON.parse(mess_obj["friends"]);
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
