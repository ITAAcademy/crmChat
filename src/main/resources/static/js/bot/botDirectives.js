'use strict';
angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
    	controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {
            scope.$watch(attr.content, function() {
                debugger;
                element.html($parse(attr.content)(scope));
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


angular.module('springChat.directives').directive('botList', function($compile, $parse) {
    return {
    	templateUrl : 'bot_conteiner.html',
    	controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {
            scope.$watch(attr.content, function() {
                debugger;
                element.html($parse(attr.content)(scope));
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