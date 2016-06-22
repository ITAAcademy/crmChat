'use strict';
angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
        controller: 'ChatBotController',
        scope: {
            content: '&'
        },
        link: function(scope, element, attr, ctrl) {
            scope.mainScope = scope;
            scope.getCurrentMessageTime = function() {
                var date = $parse(attr.time)(scope.$parent)
                return formatDateToTime(new Date(date));
            }
            var processBotParameters = function(strWithParams) {
                var functionNamesMap = {
                    'time': 'getCurrentMessageTime()'
                };
                var botParametersMap = scope.chatRouteInterfaceScope.botParameters;
                var SIGN_OF_NAME_OR_FUNCTION = "$";
                var processedStr = strWithParams;
                for (var functionNameKey in functionNamesMap) {
                    var functionResult = $parse(functionNamesMap[functionNameKey])(scope);
                    processedStr = processedStr.replace(SIGN_OF_NAME_OR_FUNCTION + functionNameKey, functionResult);
                }
                for (var botParameterKey in botParametersMap) {
                    processedStr = processedStr.replace(SIGN_OF_NAME_OR_FUNCTION + botParameterKey.slice(1, -1), botParametersMap[botParameterKey])
                }
                return processedStr;
            }


            scope.$watch('content', function() {
                var receivedData = $parse(attr.content)(scope.$parent);
                var parsedData;
                try {
                    parsedData = JSON.parse(receivedData);
                } catch (err) {
                    console.info("JSON NOT VALID TRY CREATE");
                    parsedData = { "id": -1, body: receivedData };
                }

                scope.currentMessage = parsedData;
                if (scope.chatRouteInterfaceScope != null || scope.chatRouteInterfaceScope != undefined)
                    scope.nextDialogItemJS = new Function("param", scope.currentMessage.testCase)(scope.chatRouteInterfaceScope.botParameters); //next item calc
                else
                    scope.nextDialogItemJS = -1;

                var prefix = "";
                var sufix = ""
                if (scope.chatRouteInterfaceScope != null || scope.chatRouteInterfaceScope != undefined)
                    var body = processBotParameters(parsedData.body);
                else
                    var body = parsedData.body;
                console.log('botContainer content:' + body);
                // var elementValue = parsedData.body.replace(/\\"/g, '"');
                element.html(prefix + body + sufix);
                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + scope.$eval(attr.callback))();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                //scope.content = $parse(parsedData.body)(scope);

                $compile(element.contents())(scope);
                if (attr.disabled != null || attr.disabled != undefined)
                    scope.disabled = attr.disabled;

                scope.$watch('disabled', function() {
                    if (scope.disabled) {
                        for (var i = 0; i < element[0].children.length; i++) {
                            //       element[0].children[i].style.disabled = "true";
                            element[0].children[i].style.pointerEvents = "none";
                        }
                        // element[0]..style.disabled = "true";
                        element[0].style.pointerEvents = "none";
                    }
                });
            }, true);

            scope.botChildrens = new Array();
            if (scope.chatRouteInterfaceScope != null || scope.chatRouteInterfaceScope != undefined) {
                if (scope.chatRouteInterfaceScope.botContainers.length > 0)
                    scope.chatRouteInterfaceScope.botContainers[scope.chatRouteInterfaceScope.botContainers.length - 1].scope.disabled = true;
                scope.chatRouteInterfaceScope.botContainers.push({ 'element': element, 'scope': scope });
            }


            scope.enabledListener(scope, element);
            //scope.giveTenant();
        }
    }
});

/*

angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {
            scope.$watch(attr.content, function() {

                var html = "<span>fsdfsdfsdfds</span>           <div class=\"btn-group\" role=\"group\" aria-label=\"...\">  <button type=\"button\" class=\"btn btn-default\">Left</button>  <button type=\"button\" class=\"btn btn-default\">Middle</button>  <button type=\"button\" class=\"btn btn-default\">Right</button> </div>                <span>fsdfsdfsdfds3</span>                <span>fsdfsdfsdfds4</span>"
                var parser = new DOMParser();
                // doc = parser.parseFromString(html, "text/xml");
                var div = document.createElement('div');
                div.innerHTML = html;
                var elements = div.children;

                var head = "<ul class='list-group'>";
                var footer = "</ul>";
                var result = head;

                for (var i = 0; i < elements.length; i++) {
                    result += "<li class=\"list-group-item toggle animation\">" + elements[i].outerHTML + "</li>";
                }

                element.html(result);

                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + attr.callback)();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                scope.content = $parse(attr.content)(scope);
                $compile(element.contents())(scope);
            }, true);
        }
    }
});

*/


/*        link: {
            post: function(scope, element, attr, ctrl) {
                var last = "";
                scope.$watch(
                    function() {
                        return element[0].children.length; },
                    function(newValue, oldValue) {
                        debugger;
                        if (element[0].children[element[0].children.length - 1].classList.length > 0) {
                            
                            var elements = element[0].children;

                            var head = "<ul class='list-group'>";
                            var footer = "</ul>";
                            var result = head;

                            for (var i = 0; i < elements.length; i++) {
                                result += "<li class=\"list-group-item toggle animation\">" + elements[i].outerHTML + "</li>";
                            }
                            element.html(result);

                            if (typeof attr.callback != 'undefined') {
                                var callBackFunction = new Function("return " + attr.callback)();
                                if (typeof callBackFunction != 'undefined')
                                    callBackFunction(element);
                            }
                            //scope.content = $parse(attr.content)(scope);
                            $compile(element.contents())(scope);
                            scope.init(scope, element, attr);
                        }
                    }
                );

            }
        }
        */
angular.module('springChat.directives').directive('botList', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            horizontal: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                function updateClasses() {
                    var elements = element[0].children;
                    if (scope.horizontal) {
                        element[0].classList.add("layout-ul-horizontal");
                        element[0].classList.remove("layout-ul-vertical");
                    } else {
                        element[0].classList.remove("layout-ul-horizontal");
                        element[0].classList.add("layout-ul-vertical");
                    }
                    //element[0].classList.toggle("layout-ul-vertical");
                    console.log("render List");
                    for (var i = 0; i < elements.length; i++) {
                        if (scope.horizontal) {
                            elements[i].classList.add("layout-li-horizontal");
                            elements[i].classList.remove("layout-li-vertical");
                        } else {
                            elements[i].classList.remove("layout-li-horizontal");
                            elements[i].classList.add("layout-li-vertical");
                        }

                        // elements[i].classList.toggle("layout-li-vertical");
                    }
                }
                scope.$watch('horizontal', function() {
                    updateClasses();
                });

                scope.listenContent = function() {
                    scope.unlistenContent = scope.$watch(element[0], function() {
                        updateClasses();
                        scope.unlistenContent();
                        $compile(element.contents())(scope);
                        scope.$$postDigest(function() {
                            //    scope.listenContent();    
                        })

                    }, true);
                };
                scope.listenContent();
                scope.init(scope, element, attr);
            }
        }
    }
});
/*
attributes list: 
text,
linkindex, //unique index of link for sending to server
ispost,//if false - redirect to link href, true - make post request
href, //link to other page or address for post request 
classes; // list of classes like: "btn btn-large"
*/
angular.module('springChat.directives').directive('botlink', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            text: '=',
            ispost: '=',
            linkindex: '=',
            href: '<',
            classes: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                // scope.href = "";
                scope.mainScope = scope.$parent.mainScope;
                var body = "{{text}}";
                console.log("body:" + body);
                var usePost = attr.ispost === 'true';
                if (usePost) {
                    scope.href = "";
                }
                scope.$watch('ispost', function() {
                    if (scope.ispost) {
                        scope.href = "";
                    }
                });
                scope.$watch('href', function() {
                    if (typeof scope.href != 'undefined' && scope.href.length > 0)
                        scope.ispost = false;
                });
                scope.itemvalue = body;
                if (usePost &&
                    (typeof attr.linkindex !== 'undefined') && attr.linkindex.length > 0) {
                    var dataObject = { "body": null, "category": null, "nextNode": null, "answer": null };
                    dataObject.nextNode = attr.linkindex;

                    var message = scope.mainScope.currentMessage;
                    if (message.id == null)
                        message.id = -1;
                    if (message.category != null)
                        dataObject.category = message.category.id;

                    var payLoad = JSON.stringify(dataObject);

                    var link = 'bot_operations/{0}/get_bot_container/{1}'.format(scope.$parent.currentRoom.roomId, attr.linkindex);
                    var functionStr = 'getNewItem("{0}","{1}")'.format(payLoad.escapeQuotes(), link);
                    scope.onClick = function() {
                        debugger;
                        scope.$eval(functionStr);
                    }

                    // });
                }



                var prefix = '<a class="{{classes}}" ng-click="onClick()" ng-model="itemvalue" ng-href="{{href}}"> ';

                var suffix = '</a>';
                var elementValue = prefix + body + suffix;


                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);

                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('botinput', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            text: '=',
            name: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                var body = "{{text}}";
                scope.itemvalue = "";
                var prefix = '<input type="text" name="{0}" ng-model="itemvalue">'.format(attr.name);

                var suffix = '</input>';
                var elementValue = prefix + body + suffix;


                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('botsubmit', function($compile, $parse, $http) {
    return {
        scope: {
            text: '='
        },
        controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {
            scope.mainScope = scope.$parent.mainScope;
            scope.submitBot = function(event) {
                event.preventDefault();
                var formElm = $(event.currentTarget.form);
                //$(event.currentTarget.form).submit();
                if (scope.mainScope.nextDialogItemJS == undefined || scope.mainScope.nextDialogItemJS == "") {
                    alert("NO JS FUNCTION");
                    return;
                }
                var urlPrefix = "bot_operations/{0}/submit_dialog_item/{1}";
                if (scope.mainScope.currentRoom != undefined)
                    urlPrefix = urlPrefix.format(scope.mainScope.currentRoom.roomId, scope.mainScope.currentMessage.idObject.id);
                else
                    urlPrefix = urlPrefix.format(0, scope.mainScope.currentMessage.idObject.id); //send from quize
                var url = urlPrefix + "/next_item/" + scope.mainScope.nextDialogItemJS;
                // var dataToSend = JSON.stringify(formElm.serializeArray());
                var formData = {};
                //concat(arrayB);
                scope.mainScope.getParamsInJSON(formData);
                // $.each((formElm).serializeArray(), function (i, field) { formData[field.name] = field.value || ""; });
                var dataToSend = JSON.stringify(formData);
                console.log('dataToSend:' + dataToSend);
                $.ajax({
                    type: "POST",
                    contentType: "binary/octet-stream",
                    url: document.location.origin + globalConfig.baseUrl + url,
                    data: dataToSend, // serializes the form's elements.
                    success: function(data) {
                        if (data == "quize save")
                            alert("Ваша відповідь прийнята. Дякуємо за потрачений час"); // show response from the php script.
                    },
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        alert("Вибачте за незручності, наразі відповідь не може бути прийнята");
                    }
                });

            }
            var body = "{{text}}";

            var prefix = '<button name="{0}" ng-click="submitBot($event)">'.format('submitBtn');

            var suffix = '</button>';
            var elementValue = prefix + body + suffix;


            element.html(elementValue);

            //var result = new Function("param",body)(scope.rootScope.botParam); //next item calc
            scope.content = elementValue;
            scope.init(scope, element, attr);
            $compile(element.contents())(scope);
        }
    }
});
/*
attributes:
attr.labels[] - text of items
attr.values[] - values of items (Not used, because we use index)
attr.cbname - name of group, determining names of children
attr.legend - title of checkgroup
attr.isradio - determine if group is readiogroup
*/
angular.module('springChat.directives').directive('botcheckgroup', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            legend: '=',
            labels: '=',
            name: '=',
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                //scope.itemvalue = [];
                var checkBoxTemplate = '<div><input type="{0}" name="{1}" value="{2}" />{3}</div>';

                //var prefix = '<button name="{1}" ng-click="submitBot($event)">'.format(attr.itemIndex);
                var index = 0;
                var prefix = "<fieldset><legend>{{legend}}</legend>";
                var body = "";
                var item_type = "checkbox";

                // var labels = scope.$root.elementsListForLink[3].properties.labels; //JSON.parse(attr.labels.replace('\'', '\"'));
                /* scope.$watch('labels', function() {
                 if (scope.labels.length > scope.values.length){
                     scope.values.push('false');
                 }
                 else
                scope.values.pop();
                 }, true);*/ // watching properties


                function initElement() {
                    if (typeof scope.labels == 'undefined') return;
                    body = "";
                    for (var i = 0; i < scope.labels.length; i++) {
                        var name = "{{name}}_item";
                        var value = i;
                        var label = "{{labels[{0}]}}".format(i);
                        body += checkBoxTemplate.format(item_type, name, value, label);
                    }

                    var suffix = '</fieldset>';
                    var elementValue = prefix + body + suffix;

                    element.html(elementValue);

                    scope.content = elementValue;
                    $compile(element.contents())(scope);
                    scope.init(scope, element, attr);
                }
                scope.$watch("labels.length", function() {
                    initElement();
                }, true);
            }
        }
    }
});



angular.module('springChat.directives').directive('botradiogroup', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            legend: '=',
            labels: '=',
            name: '=',
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                //scope.itemvalue = [];
                var checkBoxTemplate = '<div><input type="{0}" name="{1}" value="{2}" />{3}</div>';

                //var prefix = '<button name="{1}" ng-click="submitBot($event)">'.format(attr.itemIndex);
                var index = 0;
                var prefix = "<fieldset><legend>{{legend}}</legend>";
                var body = "";
                var item_type = "radio";

                // var labels = scope.$root.elementsListForLink[3].properties.labels; //JSON.parse(attr.labels.replace('\'', '\"'));
                /* scope.$watch('labels', function() {
                 if (scope.labels.length > scope.values.length){
                     scope.values.push('false');
                 }
                 else
                scope.values.pop();
                 }, true);*/ // watching properties


                function initElement() {
                    if (typeof scope.labels == 'undefined') return;
                    body = "";
                    for (var i = 0; i < scope.labels.length; i++) {
                        var name = "{{name}}_item";
                        var value = i;
                        var label = "{{labels[{0}]}}".format(i);
                        body += checkBoxTemplate.format(item_type, name, value, label);
                    }

                    var suffix = '</fieldset>';
                    var elementValue = prefix + body + suffix;

                    element.html(elementValue);

                    scope.content = elementValue;
                    $compile(element.contents())(scope);
                    scope.init(scope, element, attr);
                }
                scope.$watch("labels.length", function() {
                    initElement();
                }, true);
            }
        }
    }
});



/*
attributes:

*/
angular.module('springChat.directives').directive('botClose', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {},
        link: {
            post: function(scope, element, attr, ctrl) {

                var elementValue = '<a class="btn btn-default" ng-click="giveTenant()">Get tenant</a>';

                element.html(elementValue);
                scope.content = elementValue;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('botarray', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            dataarray: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                scope.addNewItemFunction = function() {
                    scope.dataarray.push('');
                }
                scope.removeItem = function(id) {
                    scope.dataarray.splice(id, 1);
                }
                scope.moveDown = function(id) {
                    if (id >= scope.dataarray.length - 1 || id < 0) return;
                    var topElm = scope.dataarray[id + 1];
                    var currentElm = scope.dataarray[id];
                    scope.dataarray[id + 1] = currentElm;
                    scope.dataarray[id] = topElm;
                }
                scope.moveUp = function(id) {
                    if (id >= scope.dataarray.length || id < 1) return;
                    var bottomElm = scope.dataarray[id - 1];
                    var currentElm = scope.dataarray[id];
                    scope.dataarray[id - 1] = currentElm;
                    scope.dataarray[id] = bottomElm;
                }

                var elementValuePrefix = '<div ng-repeat="data in dataarray track by $index">';
                var removeElementButton = '<button class="property_array_button" ng-click="removeItem($index)"><span class="glyphicon glyphicon-remove "></button>';
                var moveDownElementButton = '<button class="property_array_button" ng-click="moveUp($index)"><span class="glyphicon glyphicon-arrow-up "></span></button>';
                var moveUpElementButton = '<button class="property_array_button" ng-click="moveDown($index)"><span class="glyphicon glyphicon-arrow-down "></span></button>';
                // var elementMenu = '<div class="dropdown property_array_edit_menu"><button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown"><span class="glyphicon glyphicon-menu-hamburger"></button><ul class="property_array_dropdown dropdown-menu dropdown-menu-right"><li>{0}</li><li>{1}</li><li>{2}</li></ul></div>'.format(moveDownElementButton,moveUpElementButton,removeElementButton);
                var elementMenu = '<div class="button-group-inline">{0}{1}{2}</div>'.format(moveDownElementButton, moveUpElementButton, removeElementButton);
                var elementValueContent = '<span class="property_array_row_indexer">{{$index}}</span><input class="property_array_edit_input" type="text" ng-model="dataarray[$index]">' + elementMenu;


                var elementSuffix = '</div><div class = "btn btn-default add-item-btn" ng-click="addNewItemFunction()">+</div>';
                var elementHtml = elementValuePrefix + elementValueContent + elementSuffix;
                element.html(elementHtml);
                scope.content = elementHtml;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('botselect', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            size: '=',
            multiple: '=',
            options: '=',
            itemvalue: '<',
            name : '<'
        },
        link: {
            post: function(scope, element, attr, ctrl) {
              //  scope.itemvalue = "QQQQQQQQQQQQ";
                var elementPrefix = '<select data-ng-attr-size="{{size}}" multiple ng-options="k as v for (k,v) in options" ng-model="itemvalue"></select>';
                var elementAlternativePrefix = '<select ng-options="k as v for (k,v) in options" ng-model="itemvalue"></select>';
                var elementHtml =  elementAlternativePrefix;
                element.html(elementHtml);
                scope.content = elementHtml;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});




angular.module('springChat.directives').directive('inputListBox', function($compile, $parse) {
    return {
        restrict: 'E',
        scope: {
            inputValue: '=ngModel',
            listData: '=ngListData'
        },
        link: function(scope, element, attributes) {

            scope.$watch('inputValue', function() {
                console.log('scope.$watch inputValue ' + scope.inputValue)

                if (scope.keyPressProcessed == false) {

                    var result = [];
                    for (var i = 0; i < scope.listData.length; i++) {
                        var listElement = scope.listData[i];

                        var size = scope.inputValue.length;

                        var substr = listElement.substring(0, size);

                        console.log("scope.inputValue = " + scope.inputValue + " listElement = " + listElement + " input_size = " + size + " substr = " + substr);

                        if (size == 1) {
                            if (listElement[0] == scope.inputValue)
                                result.push(listElement);
                        } else
                        if (listElement.substring(0, scope.inputValue.length) == scope.inputValue)
                            result.push(listElement);
                    }
                    scope.listToShow = result; //scope.listData;
                }
            });


            scope.listToShow = [];
            scope.onSelectFromList = function(value) {
                scope.inputValue = value;
            };

        },
        template: ' <input class="modal_input" type="text"  ng-keypress="keyPressProcessed = false;"  ng-keydown="keyDown($event)"     typeahead-wait-ms = "1"          typeahead-on-select = " onSelectFromList($model)"     uib-typeahead="value for value in listToShow"     ></input>'

    }
})

angular.module('springChat.directives').directive('botcheckbox', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            legend: '=',
            labels: '=',
            itemvalue: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                //scope.itemvalue = [];
                var checkBoxTemplate = '<div><input {2} type="{0}" name="{1}" value="{{3}}"/><span>  {1}</span></div>';

                //var prefix = '<button name="{1}" ng-click="submitBot($event)">'.format(attr.itemIndex);
                var index = 0;
                var prefix = "<fieldset><legend>{{legend}}</legend>";
                var body = "";
                var item_type = "checkbox";
                var modalT = 'ng-model="{{itemvalue[{0}]}}"';
                var labels = eval(attr.labels); //JSON.parse(attr.labels.replace('\'', '\"'));

                for (var i = 0; i < labels.length; i++) {
                    var modalTemp = modalT.format(i);
                    body += checkBoxTemplate.format(item_type, " " + "{{labels[" + i + "]}}", modalTemp, i);
                    scope.itemvalue[i] = false;
                }


                var suffix = '</fieldset>{{itemvalue}}';
                var elementValue = prefix + body + suffix;
                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('bottext', function($compile, $parse, $http) {
    return {
        controller: 'ChatViewItemController',
        scope: {
            text: "=",
            textcolor: "=",
            textsize: "=",
            textalign: "="
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                //scope.itemvalue = [];
                var textItemTemplate = '<p style="color:{0};font-size:{1}px;text-align:{2};">{3}</p>';
                var elementBody = "{{text}}";
                var elementValue = textItemTemplate.format("{{textcolor}}", "{{textsize}}", "{{textalign}}", "{{text}}");
                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);
                scope.init(scope, element, attr);
            }
        }
    }
});

angular.module('springChat.directives').directive('botrating', botrating);

function botrating() {
    return {
        controller: 'ChatViewItemController',
        restrict: 'EA',
        template: '<ul class="star-rating" ng-class="{readonly: readonly}">' + '<p>{{text}}</p>' +
            '  <li ng-repeat="star in stars" class="star" ng-class="{filled: star.filled}" ng-style="star.filled && {\'color\' : starcolor}" ng-click="toggle($index)">' +
            '    <i class="fa fa-star"></i>' + // or &#9733
            '  </li>' +
            '</ul>',
        scope: {
            itemvalue: '<',
            max: '=', // optional (default is 5)
            onRatingSelect: '=?',
            readonly: '=',
            name: '=',
            text: '=',
            starcolor: '='
        },
        link: function(scope, element, attributes) {

            if (typeof attributes.onratingselect == 'undefined' || attributes.onratingselect == "angular.noop")
                attributes.onratingselect = function() {};

            if (scope.max == undefined) {
                scope.max = 5;
            }
            scope.fillesStarStyle = {
                "color":scope.starcolor
            }
            function updateStars() {
                scope.stars = [];
                for (var i = 0; i < scope.max; i++) {
                    scope.stars.push({
                        filled: i < scope.itemvalue
                    });
                }
            };
            scope.toggle = function(index) {
                if (scope.readonly == undefined || scope.readonly == false) {
                    scope.itemvalue = index + 1;
                    debugger;
                    attributes.onratingselect({
                        rating: index + 1
                    });
                    updateStars();
                }
            };
            scope.itemvalue = 1;
            scope.$watch('max', function(oldValue, newValue) {
                if (oldValue != newValue) {
                    updateStars();
                }
            });
            scope.$watch('itemvalue', function(oldValue, newValue) {
                if (oldValue != newValue) {
                    updateStars();
                }
            });
            updateStars();
            scope.init(scope, element, attributes);
        }
    };
};

angular.module('springChat.directives').directive('botcalendar', botcalendar);

function isAssignable($parse, attrs, propertyName) {
    var fn = $parse(attrs[propertyName]);
    return angular.isFunction(fn.assign);
}

function botcalendar($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        template: '<span class="input-group">' +
            '<input type="text" class="form-control" uib-datepicker-popup="{{format}}" ng-model="itemvalue" is-open="popup1.opened" datepicker-options="dateOptions" ng-required="true" close-text="Close" />' +
            '<span class="input-group-btn">' +
            '<button type="button" class="btn btn-default" ng-click="open1()"><i class="glyphicon glyphicon-calendar"></i></button>' +
            '</span>' +
            '</span>',
        scope: {
            itemvalue: '<', //date
            name: '=',
        },
        link: {
            pre: function(scope, element, attributes) {
                scope.popup1 = {
                    opened: false
                };
            },
            post: function(scope, element, attributes) {
                scope.popup1 = {};
                scope.open1 = function() {
                    console.log('open1');
                    scope.popup1.opened = true;
                };
                scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
                scope.format = scope.formats[2];
                scope.altInputFormats = ['dd.MM.yyyy'];
                scope.dateOptions = {
                    maxDate: new Date(2020, 5, 22),
                    minDate: new Date(1900, 1, 1),
                    startingDay: 1
                };
                // if (isAssignable($parse, attributes, 'itemvalue')) {
                if (getType(scope.itemvalue) == "date") {
                    scope.itemvalue = new Date(scope.itemvalue);
                }
                scope.init(scope, element, attributes);
                //}

            }
        }
    };
};
