springChatControllers.controller('ChatRouteController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {
    angular.extend(this, $controller('ChatRouteInterface', { $scope: $scope }));
    /*
     * 
     */

    $scope.controllerName = "ChatRouteController";
    var chatControllerScope = Scopes.get('ChatController');

    $rootScope.$watch('isInited', function() {
        console.log("try " + chatControllerScope.currentRoom);
        if ($rootScope.isInited == true) {
   
            var room = getRoomById($scope.rooms, $routeParams.roomId);

            if (room != null && room.type == 2 && $scope.controllerName != "ConsultationController") //redirect to consultation
            {
                $http.post(serverPrefix + "/chat/consultation/fromRoom/" + room.roomId)
                    .success(function(data, status, headers, config) {
                        if (data == "" || data == undefined)
                            $rootScope.goToAuthorize(); //not found => go out
                        else
                            $location.path("consultation_view/" + data);
                    }).error(function errorHandler(data, status, headers, config) {
                        $rootScope.goToAuthorize(); //not found => go out
                    });
                return;
            }

            if ($rootScope.socketSupport) {
                $scope.goToDialog($routeParams.roomId).then(function() {
                    chatControllerScope.currentRoom.roomId = $routeParams.roomId;
                    $scope.pageClass = 'scale-fade-in';
                }, function() {
                    $rootScope.goToAuthorize();
                    toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                   // location.reload();
                });
            } else {
                $scope.goToDialog($routeParams.roomId).then(function() {
                    chatControllerScope.currentRoom.roomId = $routeParams.roomId;
                    $scope.pageClass = 'scale-fade-in';
                }, function() {
                    $rootScope.goToAuthorize(); 
                    toaster.pop('warning', errorMsgTitleNotFound, errorMsgContentNotFound, 5000);
                    //location.reload();
                    //alert("ERR");
                });
            }
        }

    });
}]);
