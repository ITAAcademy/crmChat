var BOT_ELEMENTS_MODULE = function() {
    var publicData = {};

    //Help to prevent mistakes
    var BotElementTypes = ["botinput", "botcheckgroup", "radiogroup", "text", "bot-list", "button", "bot-container", "botlink", "botsubmit", "botClose"];
    var BotGlobalProperties = ["name", "value"];
    var BotElementProperties = {
        "bot-container": { "time": "00:00", "content": "", "callback": "" },
        "bot-list": { "callback": "" },
        "botlink": { "text": "empty_text", "ispost": true, "linkindex": 0, "href": "", "classes": "" },
        "botinput": { "text": "empty_text", "linkindex": 0 },
        "botsubmit": { "text": "" },
        "botcheckgroup": { "labels": [], "values": [], "legend": "", "isradio": false },
        "botClose": {}
    };
    publicData.ElementProperties = BotElementProperties;
    publicData.ElementTypes = BotElementTypes;
    publicData.GlobalProperties = BotGlobalProperties;





    publicData.ElementInstance = function(type) {
        var object = new Object();
        object.childrens = [];
        if ($.inArray(type, BotElementTypes) == -1) {
            console.error('Element type not correct');
        }
        object.parent = null;
        object.type = type;
        object.properties = BotElementProperties[type];
        object.addedProperty = "";
        object.getHTML = function(scope, ignoreAddedProperties) {
            var have_content = false;
            scope.elementsListForLink.push(this);
            var index = scope.elementsListForLink.length - 1;

            var childrensStr = "";
            for (var i = 0; i < this.childrens.length; i++) {
                var currentStr = this.childrens[i].getHTML(scope, ignoreAddedProperties);
                /*var nv = "elementsListForLink[{0}]".format(scope.elementsListForLink.length - 1);//update external file
                childrensStr += currentStr.split("this").join(nv);*/
                childrensStr += currentStr;
            }
            var propertiesStr = "";
            for (var key in this.properties) {
                var value = this.properties[key];
                if (key == "content") {
                    var escapedValue = "'" + childrensStr.escapeHtml() + "'";
                } else
                if (typeof value != 'undefined') {
                    var escapedValue;
                    if (typeof value === "string") escapedValue = value.escapeHtml();
                    else escapedValue = value;
                }
                if (ignoreAddedProperties || key == "content") {
                    propertiesStr += key + '="{0} " '.format(escapedValue);
                } else {
                    propertiesStr += key + '= "$root.this.properties.' + key + '" ';
                }
            }
            var addedClassesFinal = ' ';
            var leftTooltip = "top-left";
            /*if((scope.elementsListForLink.length - 1) %2 == 0)
                leftTooltip = "";*/
            var addedPropertyFinal = this.addedProperty +  ' dnd-placeholder-body = "' + this.type + '" dnd-dragover="$root.dragoverCallback(event, index, external, type, $root.this)" dnd-dragstart = "$root.dragStart($root.this)" dnd-drop="$root.dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"';
            var addedHeaderFinal = '<li  ' + 'tooltip-placement="{1}" tooltip-trigger="mouseenter" uib-tooltip="{0}"'.format(this.type, leftTooltip)  + 'class = "render-element" dnd-draggable="$root.this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this">';
            var addedFooterFinal = '</li>';
            if (ignoreAddedProperties) {
                addedClassesFinal = addedHeaderFinal = addedFooterFinal = addedPropertyFinal = "";
            }
            var template;
            if (!ignoreAddedProperties)
                template = addedHeaderFinal + '<div class="wrap"><ul {0} = " " {1} {2}>{3}</ul></div>'.format(this.type, addedClassesFinal, propertiesStr + " " + addedPropertyFinal, childrensStr) + addedFooterFinal;
            else
                template = addedHeaderFinal + '<{0} {1} {2}>{3}</{0}>'.format(this.type, addedClassesFinal, propertiesStr + " " + addedPropertyFinal, childrensStr) + addedFooterFinal;

            var nv = "elementsListForLink[{0}]".format(index);
            template = template.split("this").join(nv);
            //  console.log(scope.elementsListForLink[index]);
            return template;
        };
        return object;



    }

    function jqueryElementToElementInstance(jElement) {
        var elmType = jElement.prop('nodeName').toLowerCase();
        var jqueryChildrens = [];
        for (var i = 0; i < jElement.children().length; i++) {
            jqueryChildrens.push($(jElement.children()[i]));
        }
        //var elmProperties = BotElementProperties[nodeName];
        // var attrName = jElement.attr();
        var elementInstance = publicData.ElementInstance(elmType);
        for (var propertie in elementInstance.properties) {
            elementInstance.properties[propertie] = jElement.attr(propertie);
        }
        for (var i = 0; i < jqueryChildrens.length; i++) {
            var pare = {};
          //  pare[jqueryChildrens[i].prop('nodeName').toLowerCase()] = jqueryElementToElementInstance(jqueryChildrens[i]);
            elementInstance.childrens.push(jqueryElementToElementInstance(jqueryChildrens[i]));
        }
        return elementInstance;
    }
    publicData.convertTextToElementInstance = function(str) {
            var jElement = $("<bot-container>" + str + "</bot-container>");
            //var appContainer = $('#app-container', jElement);        
            var elementInstance = jqueryElementToElementInstance(jElement);
            return elementInstance;

        }
        /* var testProperties = { "name": "default" };
         var testElementType = "botcheckgroup";
         var testElementProperties = { "name": "group1", "labels": "myarrvar" };
         var getTestElement = function() {
             return new publicData.ElementInstance(testElementType, testElementProperties);
         }
         var testElm = getTestElement();
         var testElm2 = getTestElement();
         testElm.childrens.push(testElm2);
         console.log('test:' + testElm.getHTML());*/

    return publicData;

}();
