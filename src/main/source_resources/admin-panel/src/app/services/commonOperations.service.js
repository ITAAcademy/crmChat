/**
 * @author zigzag
 */
(function() {
    'use strict';

    angular.module('Intita.monitor.servises')
        .service('CommonOperationsService', CommonOperationsService);
    /** @ngInject */
    function CommonOperationsService($http,UserMonitorService) {
    	var fetchUsers = function(info) {
            if (info.trim() != '')
            return  fetchUsersInfo(info);
            return $.Deferred().resolve();
        }

        var fetchUsersInfo= function(info) {
            return UserMonitorService.fetchChatUserWithNickNameLike(info);//Promise
        }

        var fetchUsersInfoExceptRole = function(info,role) {
            return $http.get(serverPrefix + "/chat/findUsersExceptRole?info="+info+"&role="+role,{});
        }

        return {
            fetchUsers: fetchUsers,
            fetchUsersInfoExceptRole: fetchUsersInfoExceptRole

        }
      
    }
})();
