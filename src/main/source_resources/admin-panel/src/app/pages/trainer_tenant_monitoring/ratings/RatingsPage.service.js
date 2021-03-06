/**
 * @author nicolas
 */
(function() {
    'use strict';

    angular.module('Intita.monitor.servises')
        .service('RatingsPageService', RatingsPageService);

    /** @ngInject */
    function RatingsPageService($http) {
        this.loadRatings = function() {
            return $http.get(serverPrefix + "/getRatings");
        }
        this.getRatingsByRoom = function(data) {
            return $http.post(serverPrefix + "/chat/admin/ratingByRoom", data);
        }
        this.getRatingsByChatUser = function(chatUserId) {
            return $http.get(serverPrefix + "/who_rate_user_info?chatUserId="+chatUserId);
        }
    }
})();
