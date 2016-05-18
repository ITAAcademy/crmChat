'use strict';

springChatControllers.controller('ChatBotViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    $scope.name = "ChatBotViewBuilderController";
    var chatControllerScope = Scopes.get('ChatController');
    var chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');
    $scope.BUILDER = BOT_ELEMENTS_MODULE;

    /*******
     *  Create tools list of JS objetc
    *******/
    $scope.toolsList = [];
    for (var type in BOT_ELEMENTS_MODULE.ElementTypes) {
        $scope.toolsList.push(BOT_ELEMENTS_MODULE.ElementInstance(BOT_ELEMENTS_MODULE.ElementTypes[type]));
    }

    $scope.selected = {
        "properties": {
            "vasia": true,
            "petia": "sdfdsgfd",
            "salsa": 12,
            "mass": [1, 2, 5]
        }
    };
    $scope.activeViewTab = 1;

    $scope.containerTemplate = BOT_ELEMENTS_MODULE.ElementInstance("bot-container");//'<div dnd-list="toolsList" bot-container = " "  style="margin: 0px; white-space: pre" content="{0}" time="22" ></div>';
//    $scope.containerTemplate.parent = $scope.containerTemplate;
    $scope.containerTemplate.addedProperty = 'dnd-list="containerTemplate.childrens"';


    var temp = BOT_ELEMENTS_MODULE.ElementInstance("botsubmit");
   // temp.parent = $scope.containerTemplate;
    $scope.containerTemplate.childrens.push(temp);

    $scope.viewTabs = [
        { title: 'Dynamic Title 1', content: $scope.containerTemplate.getHTML() },
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
