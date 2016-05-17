'use strict';

springChatControllers.controller('ChatBotViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    $scope.name = "ChatBotViewBuilderController";
    var chatControllerScope = Scopes.get('ChatController');
    var chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');

    $scope.selected = {
        "properties": {
            "vasia": true,
            "petia": "sdfdsgfd",
            "salsa": 12,
            "mass": [1, 2, 5]
        }
    };
    $scope.activeViewTab = 1;

    $scope.viewTabs = [
        { title: 'Dynamic Title 1', content: 'Dynamic content 1' },
        { title: 'Dynamic Title 2', content: 'Dynamic content 2', disabled: false }
    ];

    $scope.models = {
        selected: null,
        lists: { "A": [], "B": [] }
    };

    // Generate initial model
    for (var i = 1; i <= 3; ++i) {
        $scope.models.lists.A.push({ label: "Item A" + i });
        $scope.models.lists.B.push({ label: "Item B" + i });
    }

    // Model to JSON for demo purpose
    $scope.$watch('selected', function(model) {
        $scope.modelAsJson = angular.toJson(model.properties, true);
    }, true);















    function getType(value) {
        if (value == true || value == false)
            return "bool";

        if (Array.isArray(value))
            return "array";

        return "string";
    }

    $scope.compareType = function(value, type) {
        return getType(value) == type;
    }
}]);
