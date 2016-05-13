'use strict';
'use strict';
springChatControllers.controller('ChatBotController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {
    //angular.extend(this, $controller('ChatRouteInterface', { $scope: $scope }));

    /*
     * 
     */

    $scope.name = "ChatBotController";
    var chatControllerScope = Scopes.get('ChatController');

    var chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');

    $scope.disabled = false;
    $scope.currentRoom = chatControllerScope.currentRoom;
    chatRouteInterfaceScope.$watch('participants', function() {
    	if(chatRouteInterfaceScope.participants.length > 2)
    	{
    		$scope.disabled = true;
    		chatControllerScope.currentRoom.active = true;
    	}
    	else
    		chatControllerScope.currentRoom.active = false;

    });


    $scope.giveTenant = function() {
        $http.post(serverPrefix + "/bot_operations/close/roomId/" + chatControllerScope.currentRoom.roomId).
        success(function(data, status, headers, config) {
            console.log("ADD USER OK " + data);
            chatControllerScope.userAddedToRoom = true;
        }).
        error(function(data, status, headers, config) {
            chatControllerScope.userAddedToRoom = true;
            toaster.pop('error', "Error", "server request timeout", 1000);
        });
    }

    $rootScope.$watch('isInited', function() {

    });

    $scope.enabledListener = function(scope,element)
    {
        console.log("listner");
        scope.$watch('disabled', function() {
                if (scope.disabled) {
                    for (var i = 0; i < element[0].children.length; i++) {
                        element[0].children[i].style.disabled = "0.5";
                        element[0].children[i].style.pointerEvents = "none";

                    }
                     element[0].style.opacity = ".5";
                    element[0].style.pointerEvents = "none";
                }
            });
    }

}]);
