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
            return  fetchUsersInfoWithoutRole(info);
            return $.Deferred().resolve();
        }

        var fetchUsersInfoWithoutRole = function(info) {
        return UserMonitorService.fetchChatUserWithNickNameLike(info);//Promise
        }

      var  fetchUsersInfo = function (roles, info) {
                return $http.get(serverPrefix + "/chat/findUsersWithRoles?info=" + info + "&roles=" + roles, {});   
        }

        return {
            fetchUsers: fetchUsers,
            fetchUsersInfo: fetchUsersInfo

        }
      
    }
})();
