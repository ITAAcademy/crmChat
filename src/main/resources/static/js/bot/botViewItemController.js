'use strict';
springChatControllers.controller('ChatViewItemController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    angular.extend(this, $controller('ChatBotController', { $scope: $scope }));

    $scope.controllerName = "ChatViewItemController";
    $scope.chatControllerScope = Scopes.get('ChatController');
    $scope.chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');

    $scope.parse = function() {

    };
    $scope.$on('$destroy', function() {
        console.log("destroy");
        if ($scope.$parent.mainScope == null || $scope.$parent.mainScope == undefined)
            return null;

        for (var index = 0; index < $scope.$parent.botChildrens.length; index++) {
            if ($scope.$parent.botChildrens[index].$scope == $scope) {
                $scope.$parent.botChildrens.splice(index, 1);
                debugger;
                break;
            }
        }
    });


    $scope.init = function(scope, element, attr) {
        element[0].style.display = "block";
        /* if (scope.preventParent != undefined && scope.preventParent != null) {
             for (var index = 0; index < scope.preventParent.botChildrens.length; index++) {
                 if (scope.preventParent.botChildrens[index].scope == preventScope) {
                     scope.preventParent.botChildrens.splice(index, 1);
                     debugger;
                     break;
                 }
             }
         }
         scope.preventParent = scope.$parent;
         scope.preventScope = scope;*/
        if (scope.$parent.mainScope == null || scope.$parent.mainScope == undefined)
            return null;
        // var t = angular.element(element[0].parentElement).scope();
        if (element[0].parentElement.localName == "bot-list" && scope.$parent.controllerName != "ChatViewItemController")
            return;

        scope.mainScope = scope.$parent.mainScope;
        //  scope.$parent.botChildrens[scope.$parent.botChildrens.length - 1].scope.disabled = true;
        //if(scope.$parent.botChildrens.length > 0)
        scope.$parent.botChildrens.push({ 'element': element, 'scope': scope });
        scope.botChildrens = new Array();

        if (scope.chatRouteInterfaceScope == null || scope.chatRouteInterfaceScope == undefined)
            return null;


        //  scope.enabledListener(scope, element);
        if (element[0].attributes.name != undefined && scope.chatRouteInterfaceScope.botParameters[element[0].attributes.name.value] != undefined) {
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


    $scope.getNewItem = function(answer, href) {
        //generation here data
        ///take message from parent scope
        console.log('getNewItem func called');
        $scope.sendPostToUrl(href, answer);
    }
    $scope.sendPostToUrl = function(href, linkData) {
        /*$http({
            method: 'POST',
            url: href,
            data: linkData,
            //headers: {'Content-Type': 'application/x-www-form-urlencoded'};
        });*/
        $http.post(serverPrefix + '\\' + href, linkData). // + $scope.dialogName).
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
