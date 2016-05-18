'use strict';
springChatControllers.controller('ChatViewItemController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    angular.extend(this, $controller('ChatBotController', { $scope: $scope }));

    $scope.name = "ChatViewItemController";
    $scope.chatControllerScope = Scopes.get('ChatController');
    $scope.chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');

    $scope.parse = function() {

    };
     $scope.init = function(scope, element, attr)
 {
    if (scope.chatRouteInterfaceScope == null || scope.chatRouteInterfaceScope == undefined) 
        return null;

    scope.mainScope = scope.$parent.mainScope;
      //  scope.$parent.botChildrens[scope.$parent.botChildrens.length - 1].scope.disabled = true;
      //if(scope.$parent.botChildrens.length > 0)
                scope.$parent.botChildrens.push({ 'element': element, 'scope': scope });
        scope.botChildrens = new Array();

      //  scope.enabledListener(scope, element);
        if(element[0].attributes.name != undefined && scope.chatRouteInterfaceScope.botParameters[element[0].attributes.name.value] != undefined)
        {
            scope.itemvalue = scope.chatRouteInterfaceScope.botParameters[element[0].attributes.name.value];
            scope.mainScope.disabled = true;
        }
        
 }

    /*
    $scope.getChildNodes(){
    	var element = $scope.botChildrens.element;
    	var scope = $scope.botChildrens.scope;
    	var Nodes = {'childrens':};
    }*/


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
