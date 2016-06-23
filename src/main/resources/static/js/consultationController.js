springChatControllers.controller('ConsultationController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {
    angular.extend(this, $controller('ChatRouteInterface', { $scope: $scope }));

    $scope.consultationId = $routeParams.consultationId;
    $scope.ratings = new Map();
    $scope.consultant = false;
    $scope.status = 0;
    $scope.controllerName = "ConsultationController";
    var chatControllerScope = Scopes.get('ChatController');

    $scope.isCanStart = function() {
        var result = true;
        for (participant in $scope.participants) {
            result &= $scope.participants[participant].online;
        }
        return !result;
    }

    $scope.ssFunction = function(SS) { //SS - start/stop
        $http.post(serverPrefix + "/chat/consultation/" + SS + "/" + $routeParams.consultationId, $scope.ratings)
            .success(function(data, status, headers, config) {
                if (SS == "start") {
                    $scope.isMyRoom = true;
                    $scope.status = 2;
                } else {
                    $scope.isMyRoom = false;
                    $scope.status = 0;
                }
            }).error(function errorHandler(data, status, headers, config) {

            });
    }

    $rootScope.$watch('isInited', function() {
        if ($rootScope.isInited == true) {
            $http.post(serverPrefix + "/chat/consultation/info/" + $routeParams.consultationId)
                .success(function(data, status, headers, config) {
                    $scope.$$postDigest(function() {
                        var nice = $(".scroll").niceScroll();
                        var fileInput = $("#myfile").fileinput({ language: "ua", maxFileSize: 100000, showCaption: false, initialPreviewShowDelete: true, browseLabel: "", browseClass: " btn btn-primary load-btn", uploadExtraData: { kvId: '10' } });
                    });
                    var status = data["status"];
                    $scope.status = status;
                    if (status < 0 || status == undefined) {
                        $rootScope.goToAuthorize(); //not found => go out
                        return; //access deny show MSG
                    }

                    /* if (status == 1 || status == 0)
                         $scope.isMyRoom = false;*/


                    var roomId = data["roomId"];
                    $scope.consultant = data["consultant"];

                    //if ($rootScope.socketSupport) {
                    $scope.goToDialog(roomId).then(function() {
                            chatControllerScope.currentRoom.roomId = roomId;
                            if (status == 1 || status == 0)
                                chatControllerScope.currentRoom.active = false;
                            $scope.pageClass = 'scale-fade-in';
                        }),
                        function() {
                            chatControllerScope.changeLocation("/chatrooms");
                        };
                    /*} else {
                        $scope.goToDialog(roomId).then(function() {
                                chatControllerScope.currentRoom.roomId = roomId;
                                $scope.pageClass = 'scale-fade-in';
                            }),
                            function() {
                                chatControllerScope.changeLocation("/chatrooms");
                            };
                    }*/
                }).error(function errorHandler(data, status, headers, config) {

                });
        }
    });
    /*
     * watch end
     */


}]);
