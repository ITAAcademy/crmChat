'use strict';

forumBuilderControllers.controller('ChatViewBuilderController', ['$routeParams', '$rootScope', '$scope', '$window', '$uibModal', '$http', '$location', '$interval', '$cookies', '$timeout', 'toaster', '$cookieStore', '$q', '$controller', function($routeParams, $rootScope, $scope, $window, $uibModal, $http, $location, $interval, $cookies, $timeout, toaster, $cookieStore, $q, $controller) {

    $scope.controllerName = "ChatViewBuilderController";
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
    $scope.langForRender = [];
    $scope.tabsChanges = [];
    $scope.DialogItemInputMode = "auto";

    //create NULL dialogItem JS object
    var botDialogItemClean = function() {
        return {
            "body": "",
            "category": {
                "id": 1,
                "name": null
            },
            "description": "",
            "testCase": "",
            "idObject": {
                "id": null,
                "lang": null
            }
        }
    }
    $scope.updateLoadButton = function() {
        if ($scope.DialogItemInputMode==="auto" &&
            ($scope.selectedBotDialogItemForModal != null)) {
            $scope.isLoadButtonDisabled = false;
        } else $scope.isLoadButtonDisabled = true;
    if ($scope.DialogItemInputMode==="manual"){
        if ($scope.selectedBotDialogItemForModalIdManually!=null)
        $scope.isLoadButtonDisabled = false
    else  $scope.isLoadButtonDisabled = true;
    }
    }

    $scope.newDialogItem = botDialogItemClean();

    $scope.updateView = function() {
        if ($scope.activeViewTab == 0 || $scope.activeViewTab === null) {
            return;
        }
        $scope.$root.elementsListForLink = [];
        $scope.$root.models.selected = null;
        $scope.viewTabs[$scope.activeViewTab - 1].content[$scope.langForRender[$scope.activeViewTab - 1]] = null;

        $scope.viewTabs[$scope.activeViewTab - 1].content[$scope.langForRender[$scope.activeViewTab - 1]] = $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].getHTML($scope.$root, false);

        $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]].body = "";
        for (var i = 0; i < $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].childrens.length; i++)
            $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]].body += "\n" + $scope.viewTabs[$scope.activeViewTab - 1].objects[$scope.langForRender[$scope.activeViewTab - 1]].childrens[i].getHTML($scope.$root, true);
        $scope.tabsChanges[$scope.viewTabs.length - 1] = true;

        var nice = $(".editor-panel-right-scroll").niceScroll();
        var nice = $("textarea").niceScroll();


    }
     $scope.isColor = function(str){
                if (str=="textcolor") return true;
                else false;
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
        var lang = $scope.langForRender[$scope.activeViewTab - 1] ;
        if (lang==null) lang="ua";
        var tab = { "title": "№" + data[lang].idObject.id, "content": new Map(), "objects": new Map(), "items": data };
        //tab.objects["ua"] = BOT_ELEMENTS_MODULE.convertTextToElementInstance(tab.items["ua"].body);
        for (var key in tab.items) {
            tab.objects[key] = BOT_ELEMENTS_MODULE.convertTextToElementInstance(tab.items[key].body);
        }
        $scope.viewTabs.push(tab);
        $scope.langForRender[$scope.viewTabs.length - 1] = "ua";
        $scope.tabsChanges[$scope.viewTabs.length - 1] = false;
        $scope.$$postDigest(function() {
            $scope.activeViewTab = $scope.viewTabs.length;
        });
    }

    $scope.updateViewByLang = function() {
        var tab = $scope.viewTabs[$scope.activeViewTab - 1];
        var lang = $scope.langForRender[$scope.activeViewTab - 1];

        tab.objects[lang] = BOT_ELEMENTS_MODULE.convertTextToElementInstance(tab.items[lang].body);
        $scope.updateView();
    }

    $scope.createBotDialogItem = function(description, categoryId) {
        var requestUrl = serverPrefix + "/bot_operations/create_bot_dialog_item"
       // 
        $scope.newDialogItem.description = description;
        $scope.newDialogItem.category.id = categoryId;
        $http.post(requestUrl, $scope.newDialogItem).
        success(function(data, status, headers, config) {
            //
            loadView(data, status, headers, config);
            $scope.newDialogItem = botDialogItemClean();
            toaster.pop('note', "Created", "dialogItem created", 5000);
        }).
        error(function(data, status, headers, config) {
            //  $scope.newDialogItem = botDialogItemClean();
        });
    }
    $scope.createNewCategory = function(categoryName) {
        var requestUrl = serverPrefix + "/bot_operations/create_category/" + categoryName

        $http.get(requestUrl, {}).
        success(function(data, status, headers, config) {
            toaster.pop('note', "Created", "category created", 5000);
        }).
        error(function(data, status, headers, config) {
            //  $scope.newDialogItem = botDialogItemClean();
        });
    }

    $scope.loadBotDialogItem = function() {
        var id;
        if ($scope.DialogItemInputMode == 'auto' && $scope.selectedBotDialogItemForModal !=null )id = $scope.selectedBotDialogItemForModal.idObject.id;
        else
        if ($scope.DialogItemInputMode == 'manual') id = $scope.selectedBotDialogItemForModalIdManually;
        var botDialogItem = {};
        var requestUrl = serverPrefix + "/bot_operations/get_bot_dialog_item/{0}".format(id);

        $http.get(requestUrl, {}).
        success(loadView).
        error(function(data, status, headers, config) {});
    }
    $scope.selectedBotCategoryForModalChanged = function(categoryId){
        if (typeof categoryId=="undefined")return;
        var categoryNamesMap = $scope.botCategoriesForModal.reduce(function(map, obj) {
            map[obj.id] = obj.name;
            return map;
        }, {});
        initDialogItems(categoryId);
        $scope.categoryInputModel = categoryNamesMap[categoryId];
    }

    $scope.initDialogItemModalLoader = function() {

        $scope.botDialogItemsForModal = [];
        $scope.botCategoriesForModal = [];
        $scope.selectedBotDialogItemForModal = {};
          $scope.$watch('selectedBotDialogItemForModal', function(newVal,oldVal) {
            if ((newVal != null && typeof newVal != 'undefined' ) &&
                (newVal.idObject != null && typeof newVal.idObject != 'undefined'))
        $scope.selectedBotDialogItemForModalChanged(newVal.idObject.id);
    else  $scope.selectedBotDialogItemForModalChanged("");
    });
        $scope.selectedBotCategoryForModal = {};

        $scope.botDialogItemsIdsForModal = [];
        $scope.botCategoriesIdsForModal = [];
        $scope.selectedBotDialogItemForModal;
        $scope.selectedBotCategoryForModal;
        $scope.allCategories = [];
        $scope.categoryTemp = null;
        $scope.categoryInputModel = [];
        $scope.dialogElementInputModel = [];
    };
    $scope.initDialogItemModalLoader();
    $scope.loadCategoriesIds = function() {

        var requestUrl = serverPrefix + "/bot_operations/get_all_categories_ids";

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botCategoriesIdsForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }
    var loadAllCategories = function() {
        var requestUrl = serverPrefix + "/bot_operations/get_all_category";

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.allCategories = data;
        }).error(function(data, status, headers, config) {});
    }
    var loadCategoriesIdsByName = function(name) {

        var requestUrl = serverPrefix + "/bot_operations/get_categories_ids_where_names_like/" + name;

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botCategoriesIdsForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }
    var loadDialogItemsIdsByDescription = function(category, description) {

        var requestUrl = serverPrefix + "/bot_operations/get_dialog_items_ids_where_description_like/" + category + '/' + description;

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botDialogItemsIdsForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }
    $scope.reloadCategoriesByName = function(name) {
        loadCategoriesIdsByName(name).success(function() {
            if ($scope.botCategoriesIdsForModal.length > 0)
                $scope.selectedBotCategoryForModal = $scope.botCategoriesIdsForModal[0];
        });
    }
    $scope.reloadDialogItemsByDescription = function(category, description) {
        loadDialogItemsIdsByDescription(category, description).success(function() {
            if ($scope.botDialogItemsIdsForModal.length > 0)
                $scope.selectedBotDialogItemForModal = $scope.botDialogItemsIdsForModal[0];
        });
    }

    $scope.loadDialogItemsIds = function(categoryId) {

        var requestUrl = serverPrefix + "/bot_operations/get_dialog_items_ids/{0}".format(categoryId);
        return $http({
                method: 'GET',
                url: requestUrl //,
                    //params: 'limit=10, sort_by=created:desc',
                    //headers: {'Authorization': 'Token token=xxxxYYYYZzzz'}
            }).
            //return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botDialogItemsIdsForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }

    function initDialogItemsIds(categoryId) {
        $scope.loadDialogItemsIds(categoryId).success(function() {
            if ($scope.botDialogItemsIdsForModal.length > 0) {
                var firstDialogItemId = $scope.botDialogItemsIdsForModal[0];
                $scope.selectedBotDialogItemForModal = firstDialogItemId;
            }
        });
    }
    $scope.categoriesNamesList = [];
    $scope.dialogItemsDescriptionsList = [];
    $scope.reloadCategoriesNames = function() {
        $timeout.cancel($scope.reloadingCategoriesNamesPromise);
        $scope.reloadingCategoriesNamesPromise = $timeout(function() {
            loadFirst5CategoriesNames($scope.categoryNameToLoad);
        }, 200);

    }
    $scope.reloadDialogItemsDescriptions = function() {
        $timeout.cancel($scope.reloadDialogItemsDescriptionsPromise);
        $scope.reloadDialogItemsDescriptionsPromise = $timeout(function() {
            loadFirst5DialogItemsDescriptions($scope.selectedBotCategoryForModal, $scope.dialogItemDescriptionToLoad);
        }, 200);

    }

    function loadFirst5CategoriesNames(name) {
        var nameToCheck = name || "_";
        var requestUrl = serverPrefix + "/bot_operations/get_five_categories_names_like/{0}".format(nameToCheck);
        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.categoriesNamesList = data;
        }).
        error(function(data, status, headers, config) {});
    }

    function loadFirst5DialogItemsDescriptions(category, description) {
        var nameToCheck = name || "_";
        var requestUrl = serverPrefix + "/bot_operations/get_five_dialog_items_description_where_description_like/{0}/{1}".format(category, description);
        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.dialogItemsDescriptionsList = data;
        }).
        error(function(data, status, headers, config) {});
    }

    $scope.loadCategoriesByName = function(name) {
        name = name || "_";
        var requestUrl = serverPrefix + "/bot_operations/get_bot_category_names_having_string_first5/" + encodeURIComponent(name);

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botCategoriesForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }
    $scope.loadDialogItemsByDescription = function(categoryId, description) {
        description = description || "";
        categoryId = categoryId || "0";
        var requestUrl = serverPrefix +
            "/bot_operations/get_bot_dialog_items_descriptions_having_string_first5/" + categoryId + "/" + encodeURIComponent(description);

        return $http.get(requestUrl, {}).
        success(function(data) {
            $scope.botDialogItemsForModal = data;
        }).
        error(function(data, status, headers, config) {});
    }

    

    function initDialogItems(categoryId) {
        $scope.loadDialogItemsByDescription(categoryId).success(function() {
            if ($scope.botDialogItemsForModal.length > 0) {
                var firstDialogItem = $scope.botDialogItemsForModal[0];
                $scope.selectedBotDialogItemForModal = firstDialogItem;
                $scope.dialogElementInputModel = firstDialogItem;
            }
        });
    }
    $scope.reloadCategories = function() {
        $timeout.cancel($scope.reloadingCategoriesPromise);
        $scope.reloadingCategoriesPromise = $timeout(function() {
            $scope.loadCategoriesByName($scope.categoryInputModel);
        }, 200);

    }
    $scope.isLoadButtonDisabled = true;
    $scope.selectedBotDialogItemForModalChanged = function(dialogItemId) {

        if (typeof dialogItemId=="undefined")return;
        var dialogItemsDescriptionsMap = $scope.botDialogItemsForModal.reduce(function(map, obj) {
            map[obj.idObject.id] = obj.description;
            return map;
        }, {});
        $scope.dialogElementInputModel = dialogItemsDescriptionsMap[dialogItemId];
        $scope.updateLoadButton();
    }

    $scope.reloadDialogItems = function(categoryId) {
        if (typeof categoryId == 'undefined') return;
        $timeout.cancel($scope.reloadDialogItemsPromise);
        $scope.reloadDialogItemsPromise = $timeout(function() {
            $scope.loadDialogItemsByDescription(categoryId, $scope.dialogElementInputModel);           
        }, 200);

    }
    $scope.onCategoryInputSelect = function(modelValue) {
        $scope.categoryInputModel = modelValue;
        var categoryNamesMap = $scope.botCategoriesForModal.reduce(function(map, obj) {
            map[obj.id] = obj.name;
            return map;
        }, {});
        //$scope.selectedBotCategoryForModalId=getKeyByValue(modelValue,categoryNamesMap);
        for (var i = 0; i < $scope.botCategoriesForModal.length; i++) {
            if ($scope.botCategoriesForModal[i].name == modelValue) {
                $scope.selectedBotCategoryForModal = $scope.botCategoriesForModal[i];
                initDialogItems($scope.botCategoriesForModal[i].id);
                break;
            }
        }
        //$scope.selectedBotCategoryForModal = $scope.botCategoriesForModal[0];
        //$scope.selectedBotCategoryForModal=getKeyByValue(modelValue,categoryNamesMap);
    }
    $scope.onDialogItemInputSelect = function(modelValue) {
        $scope.dialogElementInputModel = modelValue;

        for (var i = 0; i < $scope.botDialogItemsForModal.length; i++) {
            if ($scope.botDialogItemsForModal[i].description == modelValue) {
                $scope.selectedBotDialogItemForModal = $scope.botDialogItemsForModal[i];
                break;
            }
        }
        //$scope.selectedBotDialogItemForModalChanged();
    }


    function initCategoriesAndDialogItems() {
        $scope.loadCategoriesByName().success(function() {
            if ($scope.botCategoriesForModal.length > 0) {
                var firstCategory = $scope.botCategoriesForModal[0];
                $scope.selectedBotCategoryForModal = firstCategory;
                $scope.categoryInputModel = firstCategory;
                initDialogItems(firstCategory.id);
            }
        });
    }

    initCategoriesAndDialogItems();


    $scope.saveBotDialogItem = function() {
        $scope.updateView();
        var object = $scope.viewTabs[$scope.activeViewTab - 1].items[$scope.langForRender[$scope.activeViewTab - 1]];
        var requestUrl = serverPrefix + "/bot_operations/save_dialog_item";
        $http.post(requestUrl, object).
        success(function(data, status, headers, config) {
            $scope.tabsChanges[$scope.viewTabs.length - 1] = false;
            toaster.pop('note', "Save " + $scope.langForRender[$scope.activeViewTab - 1], "save complete", 5000);
        }).
        error(function(data, status, headers, config) {
            toaster.pop('error', "Error", "cant save", 5000);
        });
        $scope.onExit();
    };
    $scope.setBotDialogItemAsDefault = function(object) {
        
        var currentItemId = $scope.viewTabs[$scope.activeViewTab - 1].items["ua"].idObject.id;
        var currentCategoryId = object.id;
        var requestUrl = serverPrefix + "/bot_operations/set_item/{0}/as_default/{1}".format(currentItemId, currentCategoryId);

        $http.get(requestUrl, object).
        success(function(data, status, headers, config) {

        }).
        error(function(data, status, headers, config) {
            //  alert(status " " + headers);
        });
    };

    $scope.loadDlgItemModalVisible = false;

    $scope.toggleLoadDialogItemModal = function() {
        $('#dialog_item_modal_loader').modal('toggle');

        //if ($scope.loadDlgItemModalVisible == true)
        //  $scope.dialogName = $scope.dialogNameBackup;

        $scope.loadDlgItemModalVisible = !$scope.loadDlgItemModalVisible;
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
    }

    $scope.compareType = function(elm,propertyName, type) {
        return BOT_ELEMENTS_MODULE.getBotElementPropertyType(elm,propertyName) == type;
    }

    /* $scope.saveToCoockies = function(){
         if()
     }*/
    $scope.onExit = function() {
        var obj_for_save = [];
        for (var index in $scope.viewTabs) {
            obj_for_save.push($scope.viewTabs[index].items);
        }
        var str = JSON.stringify(obj_for_save);
        $cookies.put('save_object', str);

        if (str.length * 2 > 4000)
            return "All your changes can not be stored in temporary storage. Some changes will be lost !!!?";
    };

    $window.onbeforeunload = $scope.onExit;

    // $scope.loadBotDialogItem(1);
    /******************************
     * LOAD FROM COOKIES
     ******************************/
    function loadFromCookise() {
        var cookies = $cookies.get('save_object');
        var obj;
        if (cookies != null && cookies != undefined)
            obj = JSON.parse($cookies.get('save_object'));

        if (obj == null || obj == undefined)
            return;
        for (var index in obj) {
            loadView(obj[index], null, null, null, null);
        }
    }

    function checkChanges(index) {

    }

    function closeTab(index) {
        $scope.viewTabs.splice(index, 1);
    }
    /*****************************************
     * MODAL DIALOGS
     ******************************************/
    $scope.tryCloseTabDialog = null;
    $scope.tryCloseTabDialogOpen = function(index) {
        var real_index = index - 1;
        if (index == null || index == undefined) {
            real_index = $scope.activeViewTab - 1;
        }

        var needWarning = $scope.tabsChanges[real_index];
        if (needWarning) {
            $scope.tryCloseTabDialog = $uibModal.open({
                animation: true,
                scope: $scope,
                templateUrl: 'close_tab.html',
                size: "md"
            });
            $scope.tryCloseTabDialog.index = real_index;
        } else {
            $scope.$$postDigest(function() {
                closeTab(real_index);
            });
        }
    };

    $scope.showSetDefaultDialog = function() {
        loadAllCategories();
        $scope.setDefaultDialog = $uibModal.open({
            animation: true,
            scope: $scope,
            templateUrl: 'set_default.html',
            size: "md"
        });
    }

    $scope.showCreateItemDialog = function() {
        loadAllCategories();
        $scope.createItemDialog = $uibModal.open({
            animation: true,
            scope: $scope,
            templateUrl: 'create_item_dialog.html',
            size: "md"
        });
    }

    $scope.showCreateCategoryDialog = function() {
        $scope.createCategoryDialog = $uibModal.open({
            animation: true,
            scope: $scope,
            templateUrl: 'create_category.html',
            size: "md"
        });
    }

    $scope.tryCloseTabDialogClose = function(b_closeTab) {
        $scope.tryCloseTabDialog.close();
        if (b_closeTab == true) {
            if ($scope.tryCloseTabDialog.index == null || $scope.tryCloseTabDialog.index == undefined)
                closeTab($scope.activeViewTab - 1);
            else
                closeTab($scope.tryCloseTabDialog.index);
        }
        $scope.tryCloseTabDialog = null;

    };

    $scope.open1 = function() {
        $scope.popup1.opened = true;
    };
    $scope.popup1 = {};
    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
    $scope.format = $scope.formats[2];
    $scope.altInputFormats = ['dd.MM.yyyy'];
    $scope.dateOptions = {
        maxDate: new Date(2020, 5, 22),
        minDate: new Date(1900, 1, 1),
        startingDay: 1
    };

    loadFromCookise();
    $scope.$$postDigest(function() {
        goToTools();
    });

}]);
