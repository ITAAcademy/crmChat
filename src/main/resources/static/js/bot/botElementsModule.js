var BOT_ELEMENTS_MODULE = function() {
    var publicData = {};

    //Help to prevent mistakes
    var BotElementTypes = ["botinput", "botcheckgroup", "botradiogroup", "bottext", "bot-list", "button", "bot-container", "botlink", "botsubmit", "bot-close"];
    var BotGlobalProperties = ["name", "value"];
    var BotElementProperties = {
        "bot-container": { "time": "00:00", "content": "", "callback": "" },
        "bot-list": { "horizontal": false },
        "botlink": { "text": "empty_text", "ispost": true, "linkindex": 0, "href": "", "classes": "" },
        "botinput": { "text": "empty_text", "name": 0 },
        "botsubmit": { "text": "Submit" },
        "botradiogroup": { "itemscount": 2, "labels": [], "values": [], "legend": "", "groupname": "noname" },
        "botcheckgroup": { "itemscount": 2, "labels": [], "values": [], "legend": "", "groupname": "noname" },
        "botClose": {},
        //"inputListBox": {},
        "bottext": { "text": "some_text", "textcolor": "black", "textsize": 18, "textalign": "left" }
    };
    publicData.ElementProperties = BotElementProperties;
    publicData.ElementTypes = BotElementTypes;
    publicData.GlobalProperties = BotGlobalProperties;
    publicData.isContainer = function(type){
        if(type == "bot-container" || 'bot-list' == type)
            return true;
        return false;
    };



    publicData.ElementInstance = function(type) {
        var object = new Object();
        object.childrens = [];
        if ($.inArray(type, BotElementTypes) == -1) {
            console.error('Element type not correct');
        }
        object.parent = null;
        object.type = type;
        object.id = Math.random();

        object.properties = jQuery.extend(true, {}, BotElementProperties[type]);
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
                    var escapedValue = childrensStr.escapeHtml();
                } else
                if (typeof value != 'undefined') {
                    var escapedValue;
                    if (typeof value === "string")
                        escapedValue = value.escapeHtml();
                    else if(getType(value) == "array")
                         escapedValue = JSON.stringify(value).escapeHtml();
                     else
                        escapedValue = value;
                }

                if(typeof value != "object")
                escapedValue = "'" + escapedValue + "'";

                if (ignoreAddedProperties || key == "content") {
                    propertiesStr += key + '="{0}" '.format(escapedValue);
                } else {
                    propertiesStr += key + '= "$root.this.properties.' + key + '" ';
                }
            }
            var addedClassesFinal = ' ';
            var leftTooltip = "top-left";
            /*if((scope.elementsListForLink.length - 1) %2 == 0)
                leftTooltip = "";*/

            var addedPropertyFinal = this.addedProperty;
            if(publicData.isContainer(this.type))
                addedPropertyFinal += ' dnd-placeholder-body = "' + this.type + '" dnd-dragover="$root.dragoverCallback(event, index, external, type, $root.this)" dnd-drop="$root.dropCallback(event, index, item, external, type, $root.this)" dnd-list="$root.this.childrens"';//dnd-horizontal-list="true" dnd-external-sources="true"
            var addedHeaderFinal = '<li  ' + 'tooltip-placement="{1}" tooltip-trigger="mouseenter" uib-tooltip="{0}"'.format(this.type, leftTooltip) + 'class = "render-element" dnd-dragstart = "$root.dragStart($root.this)" dnd-draggable="$root.this" dnd-effect-allowed="move" dnd-selected="$root.models.selected = $root.this" ng-class="{&#39;selected&#39;: $root.models.selected == $root.this}">';

            var addedFooterFinal = '</li>';
            if (ignoreAddedProperties) {
                addedClassesFinal = addedHeaderFinal = addedFooterFinal = addedPropertyFinal = "";
            }
            var template;
            if (!ignoreAddedProperties)
                template = addedHeaderFinal + '<div class="wrap"><ul class="custom-ul" {0} = " " {1} {2}>{3}</ul></div>'.format(this.type, addedClassesFinal, propertiesStr + " " + addedPropertyFinal, childrensStr) + addedFooterFinal;
            else
                template = addedHeaderFinal + '<{0} {1} {2}>{3}</{0}>'.format(this.type, addedClassesFinal, propertiesStr + " " + addedPropertyFinal, childrensStr) + addedFooterFinal;

            var nv = "elementsListForLink[{0}]".format(index);
            template = template.split("this").join(nv);
            //  console.log(scope.elementsListForLink[index]);
            return template;
        };
        return object;



    }

    publicData.jqueryElementToElementInstance = function(jElement) {
        var elmType = jElement.prop('nodeName').toLowerCase();
        var jqueryChildrens = [];
        for (var i = 0; i < jElement.children().length; i++) {
            jqueryChildrens.push($(jElement.children()[i]));
        }
        //var elmProperties = BotElementProperties[nodeName];
        // var attrName = jElement.attr();
        var elementInstance = BOT_ELEMENTS_MODULE.ElementInstance(elmType);
        for (var propertie in elementInstance.properties) {
            var attrValue = jElement.attr(propertie);
            if (typeof(attrValue) != 'undefined') {
                if(typeof elementInstance.properties[propertie] != "object")
                var stringValue = jElement.attr(propertie).slice(1, -1);
            else
                var stringValue = jElement.attr(propertie);

                var type = getType(elementInstance.properties[propertie]);
                if (type == "bool")
                    elementInstance.properties[propertie] = parseBoolean(stringValue);
                else
                if (type == "array")
                    elementInstance.properties[propertie] = JSON.parse(stringValue);
                else
                if (type == "string")
                    elementInstance.properties[propertie] = stringValue;
            }
            //elementInstance.properties[propertie] = jElement.attr(propertie).slice(1, -1)); //set propertie and remove ' symbols on start and end


        }
        for (var i = 0; i < jqueryChildrens.length; i++) {
            var pare = {};
            //  pare[jqueryChildrens[i].prop('nodeName').toLowerCase()] = jqueryElementToElementInstance(jqueryChildrens[i]);
            var child = BOT_ELEMENTS_MODULE.jqueryElementToElementInstance(jqueryChildrens[i]);
            child.parent = elementInstance;
            elementInstance.childrens.push(child);
        }
        return elementInstance;
    }
    publicData.convertTextToElementInstance = function(str) {
            //var jElement = $('<bot-container content = "\'' + str + '\'"></bot-container>');
            var jElement = $('<bot-container>' + str + '</bot-container>');
            //var appContainer = $('#app-container', jElement);        
            var elementInstance = BOT_ELEMENTS_MODULE.jqueryElementToElementInstance(jElement);
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
