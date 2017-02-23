'use strict';
springChatControllers.controller('ChatBotController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore', '$q', '$controller','RoomsFactory', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, $cookieStore, $q, $controller,RoomsFactory) {
    //angular.extend(this, $controller('ChatRouteInterface', { $scope: $scope }));

    /*
     * 
     */

    $scope.controllerName = "ChatBotController";

    $scope.disabled = false;
    if ( RoomsFactory.getCurrentRoom() != null) {
        $scope.currentRoom =RoomsFactory.getCurrentRoom();
    }

    /*if (chatRouteInterfaceScope != null || chatRouteInterfaceScope != undefined)
        chatRouteInterfaceScope.$watch('participants', function() {
            if (chatRouteInterfaceScope.participants.length > 2) {
                $scope.disabled = true;
                RoomsFactory.getCurrentRoom().active = true;

                // if ($scope.toasterWaitFreeTenant != undefined)
                     //   $scope.toasterWaitFreeTenant.close;
                     toaster.clear();
            } else
                RoomsFactory.getCurrentRoom().active = false;

        });*/

    var askIsFreeTenant;

    $scope.giveTenant = function() {
        $http.post(serverPrefix + "/bot_operations/close/{0}".format(RoomsFactory.getCurrentRoom().roomId)).
        success(function(data, status, headers, config) {
        		{      		      
        		
                $rootScope.showToasterWaitFreeTenant();
        		}            		
        }).
        error(function(data, status, headers, config) {
            RoomsFactory.setUserAddedToRoom(true)
            toaster.pop('error', "Error", "server request timeout", 1000);
        });
    }

    $scope.getParamsInJSON = function(formData) {
        //concat(arrayB);
        if ($scope.itemvalue != undefined)
            formData[$scope.name] = JSON.stringify($scope.itemvalue) || "";
        for (var scopeAndElementKey in $scope.botChildrens) {
            $scope.botChildrens[scopeAndElementKey].scope.getParamsInJSON(formData);
        }
    }

    $rootScope.$watch('isInited', function() {

    });

    $scope.enabledListener = function(scope, element) {
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
