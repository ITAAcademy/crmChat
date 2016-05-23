'use strict';
angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
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
                    processedStr = processedStr.replace(SIGN_OF_NAME_OR_FUNCTION + botParameterKey, botParametersMap[botParameterKey])
                }
                return processedStr;
            }


            scope.$watch('content', function() {
                var receivedData = $parse(attr.content)(scope.$parent);
                var parsedData;
                try {
                    parsedData = JSON.parse(receivedData);
                } catch (err) {
                    console.log("JSON NOT VALID TRY CREATE");
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
             content: '&',
             callback: '&'
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                scope.$watch(attr.content, function() {
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
                }, true);
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
        href: '=',
        classes: '=',
        ngclickFunction: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                scope.mainScope = scope.$parent.mainScope;
                var body ="{{text}}";
                console.log("body:" + body);
                var usePost = attr.ispost === 'true';
                  scope.$watch('ispost', function() {
                    if (scope.ispost) {
                    scope.href = "";
                    }
                });
                scope.itemvalue = body;
                if (usePost &&
                    (typeof attr.linkindex !== 'undefined') &&
                    attr.linkindex.length > 0) {
                    var dataObject = { "body": null, "category": null, "nextNode": null, "answer": null };
                    dataObject.nextNode = attr.linkindex;

                    var message = scope.mainScope.currentMessage;
                    if (message.id == null)
                        message.id = -1;
                    if (message.category != null)
                        dataObject.category = message.category.id;

                    var payLoad = JSON.stringify(dataObject);

                    var link = 'bot_operations/{0}/get_bot_container/{{linkindex}}'.format(scope.$parent.currentRoom.roomId);
                    scope.ngclickFunction = 'getNewItem("{0}","{1}")'.format(payLoad.escapeQuotes(), link);
                }

                var prefix = '<a class="{{classes}}" ng-click=\"ngclickFunction\" href="{{href}}" ng-model="itemvalue">';

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
        itemindex: '='
        },
        link: {
            post: function(scope, element, attr, ctrl) {
                var body = "{{text}}";
                scope.itemvalue = "";
                var prefix = '<input type="text" name="{0}" ng-model="itemvalue">'.format(attr.itemindex);

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
            text : '='
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

                var url = formElm.attr("action") + "/next_item/" + scope.mainScope.nextDialogItemJS;
                // var dataToSend = JSON.stringify(formElm.serializeArray());
                var formData = {};
                for (var scopeAndElementKey in scope.$parent.botChildrens) {
                    var scopeAndElement = scope.$parent.botChildrens[scopeAndElementKey];
                    if (typeof scopeAndElement.element != 'undefined' && typeof scopeAndElement.element[0].attributes.name != 'undefined') {

                        formData[scopeAndElement.element[0].attributes.name.value] = JSON.stringify(scopeAndElement.scope.itemvalue) || "";
                        scope.chatRouteInterfaceScope.botParameters[scopeAndElement.element[0].attributes.name.value] = scopeAndElement.scope.itemvalue;
                    }
                }
                // $.each((formElm).serializeArray(), function (i, field) { formData[field.name] = field.value || ""; });
                var dataToSend = JSON.stringify(formData);
                console.log('dataToSend:' + dataToSend);
                $.ajax({
                    type: "POST",
                    contentType: "binary/octet-stream",
                    url: url,
                    data: dataToSend, // serializes the form's elements.
                    success: function(data) {
                        // alert(data); // show response from the php script.
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
angular.module('springChat.directives').directive('botradiogroup', function($compile, $parse, $http) {
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
              
                modalT = 'ng-model="{{itemvalue}}"';
                item_type = "radio";
                scope.itemvalue = false;

                var labels = eval(attr.labels); //JSON.parse(attr.labels.replace('\'', '\"'));

                for (var i = 0; i < labels.length; i++) {
                    var modalTemp = modalT.format(i);
                    body += checkBoxTemplate.format(item_type, " " + "{{labels["+i+"]}}", modalTemp, i);
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

                scope.$parent.botChildrens.push({ 'element': element, 'scope': scope });
                scope.botChildrens = new Array();
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
          
  scope.$watch('inputValue', function(){
       console.log('scope.$watch inputValue ' + scope.inputValue)

       if ( scope.keyPressProcessed == false)
       {

            var result = [];     
            for (var i = 0; i < scope.listData.length; i++)
                   {
                    var listElement = scope.listData[i];

                    var size = scope.inputValue.length;

                    var substr = listElement.substring(0, size);

                    console.log("scope.inputValue = " + scope.inputValue + " listElement = "+ listElement + " input_size = " + size + " substr = " + substr);

                    if (size == 1)
                    {
                     if (listElement[0] == scope.inputValue) 
                                                result.push(listElement);
                                        }
                                        else
                                        if (listElement.substring(0, scope.inputValue.length) == scope.inputValue) 
                                                result.push(listElement);
                                       }                  
                                       scope.listToShow = result ;//scope.listData;
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
                    body += checkBoxTemplate.format(item_type, " " + "{{labels["+i+"]}}", modalTemp, i);                  
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