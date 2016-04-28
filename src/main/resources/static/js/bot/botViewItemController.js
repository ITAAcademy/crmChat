'use strict';
springChatControllers.controller('ChatViewItemController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    $scope.name = "ChatViewItemController";
    var chatControllerScope = Scopes.get('ChatController');

    $scope.parse = function() {

    };
     $scope.sendPostToUrl = function(href,linkData){
                    $http({
                        method: 'POST',
                        url: href,
                        data: linkData,
                        //headers: {'Content-Type': 'application/x-www-form-urlencoded'};
                    });
                }

}]);
