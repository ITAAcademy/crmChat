'use strict';
springChatControllers.controller('ChatViewItemController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    angular.extend(this, $controller('ChatBotController', { $scope: $scope }));

    $scope.name = "ChatViewItemController";
    var chatControllerScope = Scopes.get('ChatController');

    $scope.parse = function() {

    };


    $scope.getNewItem = function(answer, href)
    {
    	//generation here data
    	///take message from parent scope
    	$scope.sendPostToUrl(href,answer);
    }
    $scope.sendPostToUrl = function(href, linkData) {
        /*$http({
            method: 'POST',
            url: href,
            data: linkData,
            //headers: {'Content-Type': 'application/x-www-form-urlencoded'};
        });*/
        $http.post(serverPrefix +'\\'+ href, linkData). // + $scope.dialogName).
        success(function(data, status, headers, config) {
           // console.log('room with bot created: ' + $scope.dialogName)
        }).
        error(function(data, status, headers, config) {
            //console.log('creating room with bot failed ')
        });
    }
    $scope.sendPostToUrlRoom = function(href, linkData, roomId) {
        $http({
            method: 'POST',
            url: roomId + "/" + href,
            data: linkData,
            //headers: {'Content-Type': 'application/x-www-form-urlencoded'};
        });
    }

}]);
