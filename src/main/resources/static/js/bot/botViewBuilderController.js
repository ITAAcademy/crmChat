'use strict';

springChatControllers.controller('ChatBotViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    $scope.name = "ChatBotViewBuilderController";
    var chatControllerScope = Scopes.get('ChatController');
    var chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');

    $scope.viewTabs = [
        { title: 'Dynamic Title 1', content: 'Dynamic content 1' },
        { title: 'Dynamic Title 2', content: 'Dynamic content 2', disabled: false }
    ];

    $scope.alertMe = function() {
        setTimeout(function() {
            $window.alert('You\'ve selected the alert tab!');
        });
    };

    $scope.model = {
        name: 'Tabs'
    };

}]);
