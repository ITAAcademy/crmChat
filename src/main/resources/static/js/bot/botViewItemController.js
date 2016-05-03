'use strict';
springChatControllers.controller('ChatViewItemController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

	angular.extend(this, $controller('ChatBotController', { $scope: $scope }));
	
    $scope.name = "ChatViewItemController";
    var chatControllerScope = Scopes.get('ChatController');

    $scope.parse = function() {

    };
     $scope.sendPostToUrl = function(href,linkData){
     		console.log('http request: methd: POST\n url:'+href+"\ndata:"+linkData+'\n');
                    $http({
                        method: 'POST',
                        url: href,
                        data: linkData,
                        //headers: {'Content-Type': 'application/x-www-form-urlencoded'};
                    });
                }

}]);
