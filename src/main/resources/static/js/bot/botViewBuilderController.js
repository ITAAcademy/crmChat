'use strict';

springChatControllers.controller('ChatBotViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', 'ChatSocket', '$cookieStore', 'Scopes', '$q', '$controller', function($routeParams, $rootScope, $scope, $http, $location, $interval, $cookies, $timeout, toaster, chatSocket, $cookieStore, Scopes, $q, $controller) {

    $scope.name = "ChatBotViewBuilderController";
    var chatControllerScope = Scopes.get('ChatController');
    var chatRouteInterfaceScope = Scopes.get('ChatRouteInterface');
    $scope.BUILDER = BOT_ELEMENTS_MODULE;


    /*************************************
     *  Create tools list and JS objetc
     *************************************/

    $scope.toolsList = [];
    for (var type in BOT_ELEMENTS_MODULE.ElementTypes) {
        var element = BOT_ELEMENTS_MODULE.ElementInstance(BOT_ELEMENTS_MODULE.ElementTypes[type]);

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

    function goToProperties() {
        $scope.activeToolsTab = 2;
    }

    function goToTools() {
        $scope.activeToolsTab = 1;
    }

    $scope.$root.models = {
        selected: null,
    };
    $scope.$root.$watch('models.selected', function() {
        goToProperties(); //show properties if new item selected
    });

    $scope.activeToolsTab = 1;
    $scope.activeViewTab = 0;
    $scope.$watch('activeViewTab', function() {
        $scope.updateView();
    });

    $scope.containerTemplate = BOT_ELEMENTS_MODULE.ElementInstance("bot-container"); //'<div dnd-list="toolsList" bot-container = " "  style="margin: 0px; white-space: pre" content="{0}" time="22" ></div>';
    $scope.containerTemplate.parent = $scope.containerTemplate;
    //$scope.containerTemplate.properties["content"] = 
    //$scope.containerTemplate.addedProperty = 'disabled = "true"';


    var temp = BOT_ELEMENTS_MODULE.ElementInstance("botsubmit");
    temp.parent = $scope.containerTemplate;
    $scope.containerTemplate.childrens.push(temp);

    $scope.viewTabs = [];
    $scope.htmlCodeForRender = [];
    $scope.langForRender = [];

    //create NULL dialogItem JS object
    var botDialogItemClean = function() {
        return {
            "body": "",
            "category": {
                "id": 1,
                "name": null
            },
            "discription" : "",
            "testCase": "",
            "idObject": {
                "id": null,
                "lang": null
            }
        }
    }

    $scope.newDialogItem = botDialogItemClean();

    $scope.updateView = function() {
        if ($scope.activeViewTab == 0 || $scope.activeViewTab === null) {
            console.log("ignore activeViewTab == 0");
            return;
        }
        $scope.$root.elementsListForLink = [];
        $scope.$root.models.selected = null;
        $scope.viewTabs[$scope.activeViewTab - 1].content[$scope.langForRender[$scope.activeViewTab - 1]] = null;

        // $scope.$evalAsync(function() {
        $scope.viewTabs[$scope.activeViewTab - 1].content[$scope.langForRender[$scope.activeViewTab - 1]] = $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].getHTML($scope.$root, false);

        $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]].body = "";
        for (var i = 0; i < $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].childrens.length; i++)
            $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]].body += "\n" + $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].childrens[i].getHTML($scope.$root, true);
        //});
    }


    /********************
     * DRAG& DROP CALLBACKS
     ********************/

    $scope.$watch('dropCallback', function() {
        $scope.$root.dropCallback = $scope.dropCallback;
    });

    $scope.$watch('dragoverCallback', function() {
        $scope.$root.dragoverCallback = $scope.dragoverCallback;
    });

    $scope.$watch('deleteCallback', function() {
        $scope.$root.deleteCallback = $scope.deleteCallback;
    });

    $scope.$watch('dragStart', function() {
        $scope.$root.dragStart = $scope.dragStart;
    });

    $scope.dropCallback = function(event, index, item, external, type, parent) {
        if (item.parent != null) {
            for (var intervalIndex = 0; intervalIndex < item.parent.childrens.length; intervalIndex++) {
                if (item.parent.childrens[intervalIndex] == item) {
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

    $scope.deleteCallback = function(event, index, item, external, type, parent) {
        if (item.parent != null) {
            for (var intervalIndex = 0; intervalIndex < item.parent.childrens.length; intervalIndex++) {
                if (item.parent.childrens[intervalIndex] == item) {
                    item.parent.childrens.splice(intervalIndex, 1);
                    break;
                }
            }
            //item.parent.childrens.splice(index, 1);
        }
        //update after apply params
        $scope.$apply(function() {
            $scope.updateView();
        });
        //update after render
        $scope.$$postDigest(function() {
            goToTools();
        });

        //return item;
    };


    $scope.$root.dragoverCallback = "";
    $scope.dragoverCallback = function(event, index, external, type, parent) {
        // console.log(parent.type);
        return true;
    };


    $scope.dragStart = function(item) {
        $scope.$$postDigest(function() {
            goToTools();
        });

    };

    /*****************************
     * TOOLS FOR CONTROLL
     *****************************/
    var loadView = function loadView(data, status, headers, config) {
        console.log("Load view: " + data);
        var tab = { "title": "test1", "content": new Map(), "objects": new Map(), "items": data };
        //tab.objects["ua"] = BOT_ELEMENTS_MODULE.convertTextToElementInstance(tab.items["ua"].body);
        for (var key in tab.items) {
            tab.objects[key] = BOT_ELEMENTS_MODULE.convertTextToElementInstance(tab.items[key].body);
        }
        $scope.viewTabs.push(tab);
        $scope.langForRender[$scope.viewTabs.length - 1] = "ua";
        $scope.$$postDigest(function() {
            $scope.activeViewTab = $scope.viewTabs.length;
        });

    }

    $scope.createBotDialogItem = function() {
        var requestUrl = serverPrefix + "/bot_operations/create_bot_dialog_item"

        $http.post(requestUrl, $scope.newDialogItem).
        success(function(data, status, headers, config) {
            loadView(data, status, headers, config);
            $scope.newDialogItem = botDialogItemClean();
        }).
        error(function(data, status, headers, config) {
          //  $scope.newDialogItem = botDialogItemClean();
        });
    }

    $scope.loadBotDialogItem = function(id) {
        var botDialogItem = {};
        var requestUrl = serverPrefix + "/bot_operations/get_bot_dialog_item/{0}".format(id);

        $http.get(requestUrl, {}).
        success(loadView).
        error(function(data, status, headers, config) {});
    }

    $scope.saveBotDialogItem = function() {
        $scope.updateView();
        var object = $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]];
        var requestUrl = serverPrefix + "/bot_operations/save_dialog_item";

        $http.post(requestUrl, object).
        success(function(data, status, headers, config) {}).
        error(function(data, status, headers, config) {});
    };

    /*****************************
     * TEST
     *****************************/



    $scope.testElementModule = function() {
        var result = BOT_ELEMENTS_MODULE.convertTextToElementInstance("<botsubmit text=\"test\" name=\"kurva\"><botinput text=\"testIn1\" name=\"in1\"></botinput><botinput text=\"testIn2\" name=\"in2\"></botinput></botsubmit>");
        $scope.$evalAsync(function() {
            $scope.$root.elementsListForLink = [];
            $scope.viewTabs[$scope.activeViewTab - 1].content = result.getHTML($scope.$root, false);
        });
        console.log("zigzag test:" + JSON.stringify(result));
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

    $scope.loadBotDialogItem(1);
    $scope.$$postDigest(function() {
        goToTools();
    });

}]);
