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
        /*element.addedProperty = 'dnd-dragstart = "dragStart($root.this)" dnd-drop="dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"' +
            'dnd-draggable="$root.this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this" ng-class="{ &#34;selected &#34; : models.selected === tool}"' ;*/
          //  element.addedProperty = 'dnd-dragstart = "dragStart($root.this)" dnd-drop="dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"';
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
    $scope.containerTemplate.parent = $scope.containerTemplate;
    //$scope.containerTemplate.addedProperty = 'disabled = "true"';


    var temp = BOT_ELEMENTS_MODULE.ElementInstance("botsubmit");
    temp.parent = $scope.containerTemplate;
   /* temp.addedProperty = 'dnd-dragstart = "dragStart(this)" dnd-drop="dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"' +
            'dnd-draggable="$root.this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this"';*/
    $scope.containerTemplate.childrens.push(temp);

    $scope.viewTabs = [
        { title: 'Dynamic Title 1', content: $scope.containerTemplate.getHTML($scope.$root, false) },
        { title: 'Dynamic Title 2', content: 'Dynamic content 2', disabled: false }
    ];
    $scope.htmlCodeForRender = [];
    $scope.langForRender = ['uk', 'en'];
    $scope.descriprionForRender = ['1','2'];

    $scope.updateView = function() {
        $scope.$root.elementsListForLink = [];
        $scope.viewTabs[$scope.activeViewTab - 1].content = $scope.containerTemplate.getHTML($scope.$root, false);

        $scope.htmlCodeForRender[$scope.activeViewTab - 1] = "";
        for( var i = 0; i < $scope.containerTemplate.childrens.length; i++)
            $scope.htmlCodeForRender[$scope.activeViewTab - 1] += "\n" +  $scope.containerTemplate.childrens[i].getHTML($scope.$root, true);;

    }

    
    $scope.dropCallback = function(event, index, item, external, type, parent) {
        if (item.parent != null)
        {
            for(var intervalIndex = 0; intervalIndex < item.parent.childrens.length; intervalIndex++)
            {
                if(item.parent.childrens[intervalIndex] === item)
                {
                    item.parent.childrens.splice(intervalIndex, 1);
                    break;
                }       
            }
            //item.parent.childrens.splice(index, 1);
        }
        item.parent = parent;
      //  parent.childrens.push(item);
        parent.childrens.splice(index, 0, item);
        $scope.$apply(function() {
            $scope.updateView();
        });
        
        //return item;
    };

     
        $scope.$watch('dropCallback', function() {
            $scope.$root.dropCallback = $scope.dropCallback;
    });

        $scope.$watch('dragoverCallback', function() {
            $scope.$root.dragoverCallback = $scope.dragoverCallback;
    });

    $scope.$root.dragoverCallback = "";
    $scope.dragoverCallback = function(event, index, external, type, parent) {
       // console.log(parent.type);
        return true;
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
