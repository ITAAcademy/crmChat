'use strict';
angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {

            scope.$watch('disabled', function() {
            	console.log("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
                if (scope.disabled) {
                    for (var i = 0; i < element[0].children.length; i++) {
                        //       element[0].children[i].style.disabled = "true";
                        element[0].children[i].style.pointerEvents = "none";
                    }
                    // element[0]..style.disabled = "true";
                    element[0].style.pointerEvents = "none";
                }
            });
            scope.$watch(attr.content, function() {

                element.html($parse(attr.content)(scope));
                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + attr.callback)();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                scope.content = $parse(attr.content)(scope);

                $compile(element.contents())(scope);
            }, true);

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

angular.module('springChat.directives').directive('botList', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {
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
                scope.content = $parse(attr.content)(scope);
                $compile(element.contents())(scope);
            }, true);
        }
    }
});
