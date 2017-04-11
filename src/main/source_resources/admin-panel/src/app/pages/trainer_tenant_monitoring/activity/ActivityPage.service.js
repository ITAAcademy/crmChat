/**
 * @author nicolas
 */
(function() {
    'use strict';

    angular.module('Intita.monitor.servises')
        .service('ActivityPageService', ActivityPageService);

    /** @ngInject */
    function ActivityPageService($http) {
        this.loadRatings = function() {
            return $http.get(serverPrefix + "/getRatings");
        }
        this.getRatingsByRoom = function(data) {
            return $http.post(serverPrefix + "/chat/admin/ratingByRoom", data);
        }
    }
})();
