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

        var fetchRoomsOfUser = function(chatUserId,searchStr,earlierDate,lateDate) {
            var data = {
             chatUserId: chatUserId,
             roomNameLike: searchStr,
             earlierDate: earlierDate,
             lateDate: lateDate
            }
            var queryString = serverPrefix + "/chat/user/findRooms?" + encodeQueryData(data);
            return $http.get(queryString,{});
        }

        var fetchUsersInfo= function(info) {
            return $http.get(serverPrefix + "/chat/findUsers?info="+info,{});
        }

        var fetchUsersInfoExceptRole = function(info,role) {
            return $http.get(serverPrefix + "/chat/findUsersExceptRole?info="+info+"&role="+role,{});
        }

       var convertUserObjectToStringByMatchedProperty = function(user,searchValue){
            var ignorableUserProperties=["avatar","id"];
            var matchedProperty = findPropertyNameByContainingSubstring(user,searchValue,ignorableUserProperties);
            if(matchedProperty!=null) {
                return user[matchedProperty];
            }
            return ""; 
        }

         var convertRoomObjectToStringByMatchedProperty = function(user,searchValue){
            var ignorableUserProperties=[];
            var matchedProperty = findPropertyNameByContainingSubstring(user,searchValue,ignorableUserProperties);
            if(matchedProperty!=null) {
                return user[matchedProperty];
            }
            return ""; 
        }

        return {
            fetchUsers: fetchUsers,
            fetchUsersInfoExceptRole: fetchUsersInfoExceptRole,
            convertRoomObjectToStringByMatchedProperty: convertRoomObjectToStringByMatchedProperty,
            convertUserObjectToStringByMatchedProperty: convertUserObjectToStringByMatchedProperty,
            fetchRoomsOfUser:fetchRoomsOfUser
        }
      
    }
})();
