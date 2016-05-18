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
        var element = BOT_ELEMENTS_MODULE.ElementInstance(BOT_ELEMENTS_MODULE.ElementTypes[type]);
        element.addedProperty = 'dnd-dragstart = "dragStart(this)" dnd-drop="dropCallback(event, index, item, external, type, this)" dnd-list="this.childrens"' +
            'dnd-draggable="this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this" ng-class="{ &#34;selected &#34; : models.selected === tool}"' ;
        $scope.toolsList.push(element);
    }
    $scope.$root.elementsListForLink = [];
    $scope.getElementsListForLink = function(index) {
        return $scope.$root.elementsListForLink[index];
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

    $scope.containerTemplate = BOT_ELEMENTS_MODULE.ElementInstance("bot-container"); //'<div dnd-list="toolsList" bot-container = " "  style="margin: 0px; white-space: pre" content="{0}" time="22" ></div>';
    //$scope.containerTemplate.parent = $scope.containerTemplate;
    $scope.containerTemplate.addedProperty = 'dnd-drop="dropCallback(event, index, item, external, type, containerTemplate)" dnd-list="containerTemplate.childrens"';


    var temp = BOT_ELEMENTS_MODULE.ElementInstance("botsubmit");
    temp.parent = $scope.containerTemplate;
    temp.addedProperty = 'dnd-dragstart = "dragStart(this)" dnd-drop="dropCallback(event, index, item, external, type, this)" dnd-list="this.childrens"' +
            'dnd-draggable="this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this"';
    $scope.containerTemplate.childrens.push(temp);

    var temp2= BOT_ELEMENTS_MODULE.ElementInstance("botsubmit");
    temp2.parent = temp;
    temp.addedProperty = 'dnd-dragstart = "dragStart(this)" dnd-drop="dropCallback(event, index, item, external, type, this)" dnd-list="this.childrens"' +
            'dnd-draggable="this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this"';
    temp.childrens.push(temp2);

    $scope.viewTabs = [
        { title: 'Dynamic Title 1', content: $scope.containerTemplate.getHTML($scope.$root) },
        { title: 'Dynamic Title 2', content: 'Dynamic content 2', disabled: false }
    ];
    $scope.updateView = function() {
        $scope.$root.elementsListForLink = [];
        $scope.viewTabs[$scope.activeViewTab - 1].content = $scope.containerTemplate.getHTML($scope.$root);
    }

    $scope.dropCallback = function(event, index, item, external, type, parent) {
        if (item.parent != null)
            item.parent.childrens.splice(index, 1);
        item.parent = parent;
        parent.childrens.push(item);
        $scope.updateView();
    };

    $scope.dragCallback = function(event, index, external, type, parent) {
        if (parent != null)
            parent.childrens.splice(index, 1);
    };

      $scope.dragStart = function(item) {
        if (item != null && item.parent != null)
            parent.childrens.splice(index, 1);
    };
    


    $scope.models = {
        selected: null,
        lists: { "A": [], "B": [] }
    };
        $scope.$root.models = {
        selected: null,
        lists: { "A": [], "B": [] }
    };

    

    // Generate initial model
    for (var i = 1; i <= 3; ++i) {
        $scope.models.lists.A.push({ label: "Item A" + i });
        $scope.models.lists.B.push({ label: "Item B" + i });
    }

    $scope.addBotDialogItem = function(body,category,testcase) {
        var botDialogItem = {
            "body":body,"category":null,"testCase":testcase,
            "idObject":{
                "id":null,"lang":null
            }
        };
        
       var requestUrl = serverPrefix + "/bot_operations/add_bot_dialog_item/{0}".format(category);
        $http({
        url: requestUrl,
        method: "POST",
        data: JSON.stringify(botDialogItem)
    })
    }
    $scope.testElementModule = function () {
      var result =   BOT_ELEMENTS_MODULE.convertTextToElementInstance("<botinput text=\"test\" name=\"kurva\"><botinput text=\"testIn1\" name=\"in1\"></botinput><botinput text=\"testIn2\" name=\"in2\"></botinput></botinput>");
      console.log("zigzag test:"+JSON.stringify(result));
    }













    function getType(value) {
        if (value === true || value === false)
            return "bool";

        if (Array.isArray(value))
            return "array";

        return "string";
    }

    $scope.compareType = function(value, type) {
        return getType(value) == type;
    }
}]);
