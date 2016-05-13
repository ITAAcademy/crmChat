'use strict';
angular.module('springChat.directives').directive('botContainer', function($compile, $parse) {
    return {
        controller: 'ChatViewItemController',
        link: function(scope, element, attr, ctrl) {

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
            scope.$watch(attr.content, function() {
                  var receivedData = $parse(attr.content)(scope);

                var parsedData = JSON.parse(receivedData); 
                 var prefix = "<form action='bot_operations/{0}/submit_dialog_item/{1}'>".format(scope.currentRoom.roomId,parsedData.id);
                 var sufix = "</form>"
                console.log('botContainer content:'+parsedData.body);
               // var elementValue = parsedData.body.replace(/\\"/g, '"');
                element.html(prefix + parsedData.body+ sufix);
                if (typeof attr.callback != 'undefined') {
                    var callBackFunction = new Function("return " + attr.callback)();
                    if (typeof callBackFunction != 'undefined')
                        callBackFunction(element);
                }
                //scope.content = $parse(parsedData.body)(scope);
                scope.mainScope = scope;
                $compile(element.contents())(scope);
            }, true);
            scope.botChildrens = new Array();
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
                //scope.content = $parse(attr.content)(scope);
                $compile(element.contents())(scope);
            }, true);
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
        scope: {

        },
        link: {
            post: function(scope, element, attr, ctrl) {
                scope.mainScope=scope.$parent.mainScope;
                var body = attr.text;
                console.log("body:" + body);
                var usePost = attr.ispost === 'true';

                var ngclickFunction = '';
                if (usePost &&
                    (typeof attr.linkindex !== 'undefined') &&
                    attr.linkindex.length > 0) {
                    var dataObject = { "body": null, "category" : null, "nextNode" : null, "answer": null};
                    dataObject.nextNode = attr.linkindex;

                    var message = JSON.parse(scope.$parent.message.message);
                    if(message.id == null)
                    	message.id = -1;
                    if(message.category != null)
                    	dataObject.category = message.category.id;
					
					var payLoad = JSON.stringify(dataObject);
                    debugger;
                    var link = 'bot_operations/{0}/get_bot_container/{1}'.format(scope.$parent.currentRoom.roomId, attr.linkindex);
                    ngclickFunction = 'mainScope.getNewItem("{0}","{1}")'.format(payLoad.escapeQuotes(),link);
                }
                var linkHref = '';
                if (!usePost) {
                          var linkTemplate ='href="{0}"';
                    linkHref = linkTemplate.format(attr.href);
                }

              var prefix = '<a class="{0}" ng-click=\'{1}\' {2}>'.format(attr.classes,ngclickFunction,linkHref);

                var suffix = '</a>';
                var elementValue = prefix + body + suffix;


                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);
                
                scope.$parent.botChildrens.push({'element':element,'scope':scope});
                scope.botChildrens = new Array();
            }
        }
    }
});

angular.module('springChat.directives').directive('botinput', function($compile, $parse, $http) {
    return {
        scope:{

        },
        link: {
            post: function(scope, element, attr, ctrl) {
                var body = attr.text;

              var prefix = '<input type="text" name="{0}">'.format(attr.itemindex);

                var suffix = '</input>';
                var elementValue = prefix + body + suffix;


                element.html(elementValue);

                scope.content = elementValue;
                $compile(element.contents())(scope);
            }
        }
    }
});

angular.module('springChat.directives').directive('botsubmit', function($compile, $parse, $http) {
    return {
        scope:{

        },
        controller: 'ChatViewItemController',
        link: {
            post: function(scope, element, attr, ctrl) {
                scope.submitBot = function(event){
                         event.preventDefault();
                         var formElm = $(event.currentTarget.form);
                    //$(event.currentTarget.form).submit();
                    var url = formElm.attr("action");
                   // var dataToSend = JSON.stringify(formElm.serializeArray());
                    var formData =  {}; 
                    $.each((formElm).serializeArray(), function (i, field) { formData[field.name] = field.value || ""; });
                    var dataToSend = JSON.stringify(formData);
                    console.log('dataToSend:'+dataToSend);
                    $.ajax({
           type: "POST",
           contentType:"binary/octet-stream",
           url: url,
           data:dataToSend, // serializes the form's elements.
           success: function(data)
           {
               alert(data); // show response from the php script.
           }
         });

                }
                var body = attr.text;

              var prefix = '<button name="{0}" ng-click="submitBot($event)">'.format('submitBtn');

                var suffix = '</button>';
                var elementValue = prefix + body + suffix;


                element.html(elementValue);
                debugger;
                scope.content = elementValue;
                $compile(element.contents())(scope);
            }
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
        scope:{},
        link: {
            post: function(scope, element, attr, ctrl) {
            	scope.answer = [];
                var checkBoxTemplate = '<div><input {2} type="{0}" name="{1}" value="{{3}}"/><span>  {1}</span></div>';
              //var prefix = '<button name="{1}" ng-click="submitBot($event)">'.format(attr.itemIndex);
              var index = 0;
              var prefix="<fieldset><legend>{0}</legend>".format(attr.legend);
              var body = "";
              var item_type = "checkbox";
              var modalT = 'ng-model="answer[{0}]"';
              if (attr.isradio === 'true' || attr.isradio === true)
              {
              	modalT = 'ng-model="answer"';
              	item_type = "radio";
              	scope.answer = false;
              } 
              debugger;
              var labels = eval(attr.labels);//JSON.parse(attr.labels.replace('\'', '\"'));

              for (var i = 0; i < labels.length; i++){
              	var modalTemp = modalT.format(i);
                body += checkBoxTemplate.format(item_type," " + labels[i], modalTemp, i);
                if (attr.isradio == 'false')
                	scope.answer[i] = false;
              }
              

                var suffix = '</fieldset>{{answer}}';
                var elementValue = prefix + body + suffix;



                element.html(elementValue);
                debugger;
                scope.content = elementValue;
                $compile(element.contents())(scope);
                scope.mainScope=scope.$parent.mainScope;
                scope.$parent.botChildrens.push({'element':element,'scope':scope});
                scope.botChildrens = new Array();
            }
        }
    }
});
